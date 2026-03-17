package com.example.zapzap.data.repository

import android.util.Log
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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val conversationDao: ConversationDao
) : ChatRepository {

    private val TAG = "ChatRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                repositoryScope.launch {
                    val conversations = snapshot?.documents?.mapNotNull { doc ->
                        val base = ConversationMapper.fromFirestore(doc.data ?: emptyMap(), doc.id)
                        
                        // Melhoria na Identificação: Quem é o outro?
                        val otherId = base.participantIds.find { it != userId } ?: userId
                        
                        try {
                            val uDoc = firestore.collection("users").document(otherId).get().await()
                            val name = uDoc.getString("displayName") ?: uDoc.getString("email") ?: "Usuário"
                            base.copy(
                                name = if (otherId == userId) "$name (Você)" else name,
                                photoUrl = uDoc.getString("photoUrl") ?: ""
                            )
                        } catch (e: Exception) {
                            base.copy(name = "Usuário")
                        }
                    } ?: emptyList()

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
                    try { MessageMapper.fromFirestore(doc.data ?: emptyMap(), doc.id) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(msgs)
                repositoryScope.launch { msgs.forEach { messageDao.insertMessage(MessageMapper.toEntity(it, true)) } }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getOrCreateConversation(currentUserId: String, otherUserId: String): Result<Conversation> {
        return try {
            // Busca conversa onde os dois IDs existem
            val query = firestore.collection("conversations")
                .whereEqualTo("type", ConversationType.INDIVIDUAL.name)
                .whereArrayContains("participantIds", currentUserId)
                .get().await()

            // Filtro manual rigoroso para evitar duplicados
            val existing = query.documents.find { doc ->
                val ids = doc.get("participantIds") as? List<*>
                ids != null && ids.contains(otherUserId) && (ids.size == 2 || (currentUserId == otherUserId && ids.size == 1))
            }

            if (existing != null) {
                return Result.success(ConversationMapper.fromFirestore(existing.data!!, existing.id))
            }

            // Criar nova
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
                "lastMessage" to (if (finalMsg.text.isBlank()) "Mídia" else finalMsg.text),
                "lastMessageTime" to ts,
                "lastMessageSenderId" to finalMsg.senderId
            ))
            
            batch.commit().await()
            Result.success(finalMsg)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getConversation(id: String): Result<Conversation> {
        return try {
            val doc = firestore.collection("conversations").document(id).get().await()
            Result.success(ConversationMapper.fromFirestore(doc.data ?: emptyMap(), id))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateMessageStatus(c: String, m: String, s: MessageStatus) = Result.success(Unit)
    override suspend fun markAllAsRead(c: String, u: String) = Result.success(Unit)
    override suspend fun togglePinMessage(c: String, m: String, p: Boolean) = Result.success(Unit)
    override fun searchMessages(c: String, q: String) = messageDao.searchMessages(c, q).map { it.map { m -> MessageMapper.toDomain(m) } }
    override fun getPinnedMessage(c: String) = messageDao.getPinnedMessage(c).map { it?.let { m -> MessageMapper.toDomain(it) } }
    override fun searchConversations(u: String, q: String) = conversationDao.searchConversations(q).map { it.map { c -> ConversationMapper.toDomain(c) } }
}
