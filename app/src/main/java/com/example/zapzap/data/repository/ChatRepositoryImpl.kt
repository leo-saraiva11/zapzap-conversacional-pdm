package com.example.zapzap.data.repository

import com.example.zapzap.data.local.dao.ConversationDao
import com.example.zapzap.data.local.dao.MessageDao
import com.example.zapzap.data.mapper.ConversationMapper
import com.example.zapzap.data.mapper.MessageMapper
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.network.FcmService
import com.example.zapzap.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val fcmService: FcmService
) : ChatRepository {

    override fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                launch {
                    val deferredConversations = snapshot?.documents?.mapNotNull { doc ->
                        val base = ConversationMapper.fromFirestore(doc.data ?: emptyMap(), doc.id)
                        
                        // Identificação: Quem é o outro participante?
                        val otherId = base.participantIds.find { it != userId } ?: userId
                        
                        // FIX: Ocultar conversas individuais corrompidas do banco (erro anterior de participantIds)
                        if (base.type == ConversationType.INDIVIDUAL && otherId == userId) {
                            return@mapNotNull null
                        }
                        
                        async {
                            try {
                                val uDoc = firestore.collection("users").document(otherId).get().await()
                                val name = uDoc.getString("displayName") ?: uDoc.getString("email") ?: "Usuário"
                                base.copy(
                                    name = name,
                                    photoUrl = uDoc.getString("photoUrl") ?: ""
                                )
                            } catch (_: Exception) {
                                base.copy(name = "Usuário")
                            }
                        }
                    } ?: emptyList()

                    val conversations = deferredConversations.awaitAll()
                    trySend(conversations)
                    conversationDao.insertConversations(conversations.map { ConversationMapper.toEntity(it) })
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val msgs = snapshot?.documents?.mapNotNull { doc ->
                    try { MessageMapper.fromFirestore(doc.data ?: emptyMap(), doc.id) } catch (_: Exception) { null }
                } ?: emptyList()
                trySend(msgs)
                launch { 
                    messageDao.insertMessages(msgs.map { MessageMapper.toEntity(it, true) })
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getOrCreateConversation(currentUserId: String, otherUserId: String): Result<Conversation> {
        return try {
            val query = firestore.collection("conversations")
                .whereEqualTo("type", ConversationType.INDIVIDUAL.name)
                .whereArrayContains("participantIds", currentUserId)
                .get().await()

            val existing = query.documents.find { doc ->
                val ids = doc.get("participantIds") as? List<*>
                ids != null && ids.contains(otherUserId) && (ids.size == 2 || (currentUserId == otherUserId && ids.size == 1))
            }

            if (existing != null) {
                return Result.success(ConversationMapper.fromFirestore(existing.data!!, existing.id))
            }

            val id = UUID.randomUUID().toString()
            val conv = Conversation(
                id = id,
                participantIds = if (currentUserId == otherUserId) listOf(currentUserId) else listOf(currentUserId, otherUserId),
                type = ConversationType.INDIVIDUAL,
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("conversations").document(id).set(ConversationMapper.toFirestore(conv)).await()
            Result.success(conv)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val id = UUID.randomUUID().toString()
            val ts = System.currentTimeMillis()
            val finalMsg = message.copy(id = id, timestamp = ts, status = MessageStatus.SENT)
            
            val batch = firestore.batch()
            val msgRef = firestore.collection("conversations").document(finalMsg.conversationId).collection("messages").document(id)
            val convRef = firestore.collection("conversations").document(finalMsg.conversationId)
            
            batch.set(msgRef, MessageMapper.toFirestore(finalMsg))
            batch.update(convRef, mapOf(
                "lastMessage" to finalMsg.text.ifBlank { "Mídia" },
                "lastMessageTime" to ts,
                "lastMessageSenderId" to finalMsg.senderId
            ))
            
            batch.commit().await()
            
            // Tenta enviar notificação push via FCM Http v1
            try {
                // Encontra quem é o destinatário
                val conv = getConversation(finalMsg.conversationId).getOrNull()
                val receiverId = conv?.participantIds?.find { it != finalMsg.senderId }
                if (receiverId != null) {
                    val userDoc = firestore.collection("users").document(receiverId).get().await()
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        fcmService.sendNotification(
                            token = fcmToken,
                            title = finalMsg.senderName,
                            body = finalMsg.text.ifBlank { "📷 Mídia" },
                            conversationId = finalMsg.conversationId
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Erro ao tentar enviar notificação push: ${e.message}", e)
            }
            
            Result.success(finalMsg)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getConversation(conversationId: String): Result<Conversation> {
        return try {
            val doc = firestore.collection("conversations").document(conversationId).get().await()
            Result.success(ConversationMapper.fromFirestore(doc.data ?: emptyMap(), conversationId))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: MessageStatus
    ): Result<Unit> {
        return try {
            firestore.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update("status", status.name).await()
            messageDao.updateMessageStatus(messageId, status.name)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markAllAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            // Zerar contador de mensagens não lidas localmente
            conversationDao.updateUnreadCount(conversationId, 0)
            
            // Atualizar status para LIDO (READ) no Firestore para mensagens recebidas
            val unreadQuery = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereIn("status", listOf(MessageStatus.SENT.name, MessageStatus.DELIVERED.name))
                .get()
                .await()

            if (!unreadQuery.isEmpty) {
                val batch = firestore.batch()
                var hasUpdates = false
                for (doc in unreadQuery.documents) {
                    if (doc.getString("senderId") != userId) {
                        batch.update(doc.reference, "status", MessageStatus.READ.name)
                        hasUpdates = true
                    }
                }
                if (hasUpdates) {
                    batch.commit().await()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun togglePinMessage(conversationId: String, messageId: String, isPinned: Boolean): Result<Unit> {
        return try {
            if (isPinned) {
                messageDao.unpinAllMessages(conversationId)
            }
            messageDao.updatePinnedStatus(messageId, isPinned)
            conversationDao.updatePinnedMessageId(conversationId, if (isPinned) messageId else "")
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun searchMessages(conversationId: String, query: String): Flow<List<Message>> =
        messageDao.searchMessages(conversationId, query).map { entities -> 
            entities.map { MessageMapper.toDomain(it) } 
        }

    override fun getPinnedMessage(conversationId: String): Flow<Message?> =
        messageDao.getPinnedMessage(conversationId).map { entity -> 
            entity?.let { MessageMapper.toDomain(it) } 
        }

    override fun searchConversations(userId: String, query: String): Flow<List<Conversation>> =
        conversationDao.searchConversations(query).map { entities -> 
            entities.map { ConversationMapper.toDomain(it) } 
        }
}
