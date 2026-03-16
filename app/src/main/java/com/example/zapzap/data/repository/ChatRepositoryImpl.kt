package com.example.zapzap.data.repository

import com.example.zapzap.data.local.dao.ConversationDao
import com.example.zapzap.data.local.dao.MessageDao
import com.example.zapzap.data.mapper.ConversationMapper
import com.example.zapzap.data.mapper.MessageMapper
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de chat.
 * Usa Firestore para tempo real e Room para cache offline.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ChatRepository {

    private val conversationsCollection = firestore.collection("conversations")

    override fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener: ListenerRegistration = conversationsCollection
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val conversation = ConversationMapper.fromFirestore(
                            doc.data ?: emptyMap(),
                            doc.id
                        )
                        // Cache local
                        val entity = ConversationMapper.toEntity(conversation)
                        // Não podemos usar suspend aqui, então fazemos async
                        conversation
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        MessageMapper.fromFirestore(doc.data ?: emptyMap(), doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getConversation(conversationId: String): Result<Conversation> {
        return try {
            val doc = conversationsCollection.document(conversationId).get().await()
            if (doc.exists()) {
                val conversation = ConversationMapper.fromFirestore(
                    doc.data ?: emptyMap(), doc.id
                )
                Result.success(conversation)
            } else {
                Result.failure(Exception("Conversa não encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrCreateConversation(
        currentUserId: String,
        otherUserId: String
    ): Result<Conversation> {
        return try {
            // Procurar conversa individual existente
            val existing = conversationsCollection
                .whereEqualTo("type", ConversationType.INDIVIDUAL.name)
                .whereArrayContains("participantIds", currentUserId)
                .get().await()

            val existingConversation = existing.documents.firstOrNull { doc ->
                val participants = doc.get("participantIds") as? List<*>
                participants?.contains(otherUserId) == true && participants.size == 2
            }

            if (existingConversation != null) {
                return Result.success(
                    ConversationMapper.fromFirestore(
                        existingConversation.data ?: emptyMap(),
                        existingConversation.id
                    )
                )
            }

            // Criar nova conversa
            val conversationId = UUID.randomUUID().toString()
            val conversation = Conversation(
                id = conversationId,
                type = ConversationType.INDIVIDUAL,
                participantIds = listOf(currentUserId, otherUserId),
                createdAt = System.currentTimeMillis(),
                createdBy = currentUserId
            )

            conversationsCollection.document(conversationId)
                .set(ConversationMapper.toFirestore(conversation))
                .await()

            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val messageId = if (message.id.isBlank()) UUID.randomUUID().toString() else message.id
            val finalMessage = message.copy(
                id = messageId,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )

            // Salvar no Firestore
            conversationsCollection
                .document(finalMessage.conversationId)
                .collection("messages")
                .document(messageId)
                .set(MessageMapper.toFirestore(finalMessage))
                .await()

            // Atualizar última mensagem da conversa
            val displayText = when (finalMessage.type) {
                com.example.zapzap.domain.model.MessageType.IMAGE -> "📷 Imagem"
                com.example.zapzap.domain.model.MessageType.VIDEO -> "🎥 Vídeo"
                com.example.zapzap.domain.model.MessageType.AUDIO -> "🎤 Áudio"
                com.example.zapzap.domain.model.MessageType.FILE -> "📎 Arquivo"
                com.example.zapzap.domain.model.MessageType.LOCATION -> "📍 Localização"
                com.example.zapzap.domain.model.MessageType.STICKER -> "🏷️ Sticker"
                else -> finalMessage.text
            }

            conversationsCollection.document(finalMessage.conversationId)
                .update(
                    mapOf(
                        "lastMessage" to displayText,
                        "lastMessageTime" to finalMessage.timestamp,
                        "lastMessageSenderId" to finalMessage.senderId
                    )
                ).await()

            // Cache local
            messageDao.insertMessage(MessageMapper.toEntity(finalMessage, isSynced = true))

            Result.success(finalMessage)
        } catch (e: Exception) {
            // Salvar localmente como não sincronizado se falhou
            val offlineMessage = message.copy(
                id = if (message.id.isBlank()) UUID.randomUUID().toString() else message.id,
                status = MessageStatus.SENDING
            )
            messageDao.insertMessage(MessageMapper.toEntity(offlineMessage, isSynced = false))
            Result.failure(e)
        }
    }

    override suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: MessageStatus
    ): Result<Unit> {
        return try {
            conversationsCollection
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .update("status", status.name)
                .await()

            messageDao.updateMessageStatus(messageId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            val messages = conversationsCollection
                .document(conversationId)
                .collection("messages")
                .whereNotEqualTo("senderId", userId)
                .whereIn("status", listOf(MessageStatus.SENT.name, MessageStatus.DELIVERED.name))
                .get().await()

            val batch = firestore.batch()
            messages.documents.forEach { doc ->
                batch.update(doc.reference, "status", MessageStatus.READ.name)
            }
            batch.commit().await()

            // Zerar contagem de não lidas
            conversationsCollection.document(conversationId)
                .update("unreadCount", 0)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePinMessage(
        conversationId: String,
        messageId: String,
        isPinned: Boolean
    ): Result<Unit> {
        return try {
            // Primeiro desfixa todas as mensagens da conversa
            if (isPinned) {
                val pinned = conversationsCollection
                    .document(conversationId)
                    .collection("messages")
                    .whereEqualTo("isPinned", true)
                    .get().await()

                val batch = firestore.batch()
                pinned.documents.forEach { doc ->
                    batch.update(doc.reference, "isPinned", false)
                }
                batch.commit().await()
                messageDao.unpinAllMessages(conversationId)
            }

            // Fixar/desafixar a mensagem
            conversationsCollection
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .update("isPinned", isPinned)
                .await()

            // Atualizar a conversa com o ID da mensagem fixada
            conversationsCollection.document(conversationId)
                .update("pinnedMessageId", if (isPinned) messageId else "")
                .await()

            messageDao.updatePinnedStatus(messageId, isPinned)
            conversationDao.updatePinnedMessageId(conversationId, if (isPinned) messageId else "")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchMessages(conversationId: String, query: String): Flow<List<Message>> {
        // Busca local via Room (mais eficiente para busca por texto)
        return messageDao.searchMessages(conversationId, query).map { entities ->
            entities.map { MessageMapper.toDomain(it) }
        }
    }

    override fun getPinnedMessage(conversationId: String): Flow<Message?> {
        return messageDao.getPinnedMessage(conversationId).map { entity ->
            entity?.let { MessageMapper.toDomain(it) }
        }
    }

    override fun searchConversations(userId: String, query: String): Flow<List<Conversation>> {
        return conversationDao.searchConversations(query).map { entities ->
            entities.map { ConversationMapper.toDomain(it) }
        }
    }
}
