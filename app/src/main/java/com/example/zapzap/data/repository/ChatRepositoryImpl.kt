package com.example.zapzap.data.repository

import com.example.zapzap.data.local.dao.ConversationDao
import com.example.zapzap.data.local.dao.MessageDao
import com.example.zapzap.data.mapper.ConversationMapper
import com.example.zapzap.data.mapper.MessageMapper
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
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
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val fcmService: FcmService
) : ChatRepository {

    override fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        var isInitialLoad = true
        val listener = firestore.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (!isInitialLoad && snapshot != null) {
                    val changedDocs = snapshot.documentChanges.filter { 
                        it.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED || it.type == com.google.firebase.firestore.DocumentChange.Type.ADDED
                    }
                    
                    changedDocs.forEach { change ->
                        val map = change.document.data
                        val senderId = map["lastMessageSenderId"] as? String ?: ""
                        val msg = map["lastMessage"] as? String ?: "Nova mensagem"
                        
                        if (senderId.isNotEmpty() && senderId != userId) {
                            val text = if (msg.isNotEmpty() && msg != "📷 Mídia" && !msg.startsWith("📍") && !msg.contains("http")) {
                                try { com.example.zapzap.util.EncryptionHelper.decrypt(msg) } catch(e:Exception) { msg }
                            } else msg
                                                        
                            showLocalNotification("Nova mensagem recebida!", text, change.document.id)
                        }
                    }
                }
                isInitialLoad = false

                launch {
                    val deferredConversations = snapshot?.documents?.mapNotNull { doc ->
                        val base = ConversationMapper.fromFirestore(doc.data ?: emptyMap(), doc.id, userId)
                        
                        if (base.type == ConversationType.GROUP) {
                            // Grupos usam o nome e foto do próprio documento
                            return@mapNotNull async { base }
                        }
                        
                        // Conversas individuais: Identificar o outro participante
                        val otherId = base.participantIds.find { it != userId } ?: userId
                        
                        if (otherId == userId) {
                            return@mapNotNull null // Ignorar conversas corrompidas consigo mesmo
                        }
                        
                        async {
                            try {
                                val uDoc = firestore.collection("users").document(otherId).get().await()
                                val name = uDoc.getString("displayName") ?: uDoc.getString("email") ?: "Usuário"
                                val photoUrl = uDoc.getString("photoUrl") ?: ""
                                
                                base.copy(
                                    name = name,
                                    photoUrl = photoUrl
                                )
                            } catch (_: Exception) {
                                base.copy(name = "Usuário")
                            }
                        }
                    } ?: emptyList()

                    val conversations = deferredConversations.awaitAll()
                    trySend(conversations)
                    
                    // Cache local para offline e inicialização rápida
                    conversationDao.insertConversations(conversations.map { ConversationMapper.toEntity(it) })
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMessages(conversationId: String, userId: String): Flow<List<Message>> = callbackFlow {
        // Observa mudanças locais também! (Optimistic UI e Offline Cache)
        val roomFlow = messageDao.getMessagesByConversation(conversationId)
        
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val msgs = snapshot?.documents?.mapNotNull { doc ->
                    try { 
                        val m = MessageMapper.fromFirestore(doc.data ?: emptyMap(), doc.id)
                        
                        // Decrypt if text
                        val finalMsg = if (m.type == MessageType.TEXT && m.isEncrypted) {
                            m.copy(text = com.example.zapzap.util.EncryptionHelper.decrypt(m.text))
                        } else {
                            m
                        }

                        // Se somos o destinatário e a msg está em SENT, marcar como DELIVERED
                        // (fallback para quando o push FCM não chega)
                        if (finalMsg.senderId != userId && finalMsg.status == MessageStatus.SENT) {
                            launch {
                                updateMessageStatus(conversationId, finalMsg.id, MessageStatus.DELIVERED)
                            }
                        }
                        // Depois marca como READ quando o usuário abriu a conversa
                        if (finalMsg.senderId != userId && (finalMsg.status == MessageStatus.SENT || finalMsg.status == MessageStatus.DELIVERED)) {
                            launch {
                                updateMessageStatus(conversationId, finalMsg.id, MessageStatus.READ)
                            }
                        }
                        
                        finalMsg
                    } catch (_: Exception) { null }
                } ?: emptyList()
                
                launch { 
                    messageDao.insertMessages(msgs.map { MessageMapper.toEntity(it, true) })
                }
            }
            
        // Emite o fluxo do Room que vai ser atualizado pelo Firestore listener e pelas mensagens locais enviadas    
        val job = launch {
            roomFlow.collect { entities ->
                trySend(entities.map { MessageMapper.toDomain(it) })
            }
        }
        
        awaitClose { 
            listener.remove() 
            job.cancel()
        }
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
                return Result.success(ConversationMapper.fromFirestore(existing.data!!, existing.id, currentUserId))
            }

            val id = UUID.randomUUID().toString()
            val conv = Conversation(
                id = id,
                participantIds = if (currentUserId == otherUserId) listOf(currentUserId) else listOf(currentUserId, otherUserId),
                type = ConversationType.INDIVIDUAL,
                createdAt = System.currentTimeMillis()
            )
            
            // Buscar nome e foto para a UI imediatamente
            var displayName = "Usuário"
            var photoUrl = ""
            try {
                val uDoc = firestore.collection("users").document(otherUserId).get().await()
                displayName = uDoc.getString("displayName") ?: uDoc.getString("email") ?: "Usuário"
                photoUrl = uDoc.getString("photoUrl") ?: ""
            } catch (_: Exception) {}
            
            val convForUi = conv.copy(name = displayName, photoUrl = photoUrl)
            
            firestore.collection("conversations").document(id).set(ConversationMapper.toFirestore(conv)).await()
            conversationDao.insertConversations(listOf(ConversationMapper.toEntity(convForUi)))
            Result.success(convForUi)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun sendMessage(message: Message): Result<Message> {
        val id = UUID.randomUUID().toString()
        val ts = System.currentTimeMillis()
        val finalMsg = message.copy(id = id, timestamp = ts, status = MessageStatus.SENDING)
        
        return try {
            // Optimistic UI: Salva localmente em PLAIN TEXT primeiro para exibir na hora
            messageDao.insertMessage(MessageMapper.toEntity(finalMsg).copy(isSynced = false))

            val batch = firestore.batch()
            val msgRef = firestore.collection("conversations").document(finalMsg.conversationId).collection("messages").document(id)
            val convRef = firestore.collection("conversations").document(finalMsg.conversationId)
            
            val textToSend = if (finalMsg.type == MessageType.TEXT) com.example.zapzap.util.EncryptionHelper.encrypt(finalMsg.text) else finalMsg.text
            val sentMsg = finalMsg.copy(status = MessageStatus.SENT, text = textToSend, isEncrypted = finalMsg.type == MessageType.TEXT)
            val textForConv = if (finalMsg.type == MessageType.TEXT) textToSend else "📷 Mídia"
            
            batch.set(msgRef, MessageMapper.toFirestore(sentMsg))
            batch.update(convRef, mapOf(
                "lastMessage" to textForConv,
                "lastMessageTime" to ts,
                "lastMessageSenderId" to sentMsg.senderId,
                "lastMessageStatus" to sentMsg.status.name
            ))
            
            // Increment unread counts for all receivers
            val conv = getConversation(finalMsg.conversationId).getOrNull()
            conv?.participantIds?.filter { it != sentMsg.senderId }?.forEach { receiverId ->
                batch.update(convRef, "unreadCounts.$receiverId", com.google.firebase.firestore.FieldValue.increment(1))
            }
            
            batch.commit().await()
            messageDao.updateMessageStatus(id, MessageStatus.SENT.name)
            messageDao.markAsSynced(id)
            
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
            
            Result.success(sentMsg)
        } catch (e: Exception) { 
            // Falhou ao enviar (ex: sem net), o listener offline worker vai pegar depois 
            Result.failure(e) 
        }
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
                
                // Reset unread count for current user
                val convRef = firestore.collection("conversations").document(conversationId)
                batch.update(convRef, "unreadCounts.$userId", 0)
                hasUpdates = true

                if (hasUpdates) {
                    batch.commit().await()
                }
            } else {
                // Mesmo sem novas mensagens, garante que zera no server (ex: abriu o chat)
                firestore.collection("conversations").document(conversationId)
                    .update("unreadCounts.$userId", 0).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun togglePinMessage(conversationId: String, messageId: String, isPinned: Boolean): Result<Unit> {
        return try {
            val pinnedId = if (isPinned) messageId else ""
            val batch = firestore.batch()
            
            val convRef = firestore.collection("conversations").document(conversationId)
            batch.update(convRef, "pinnedMessageId", pinnedId)
            
            // Tenta atualizar o campo isPinned na mensagem para que outros dispositivos vejam via snapshot listener
            val msgRef = convRef.collection("messages").document(messageId)
            batch.update(msgRef, "isPinned", isPinned)
            
            batch.commit().await()
            
            if (isPinned) {
                messageDao.unpinAllMessages(conversationId)
            }
            messageDao.updatePinnedStatus(messageId, isPinned)
            conversationDao.updatePinnedMessageId(conversationId, pinnedId)
            Result.success(Unit)
        } catch (e: Exception) { 
            // Fallback: se a mensagem não existir mais ou algo falhar, tenta atualizar apenas a conversa
            try {
                val pinnedId = if (isPinned) messageId else ""
                firestore.collection("conversations").document(conversationId)
                    .update("pinnedMessageId", pinnedId).await()
                
                if (isPinned) messageDao.unpinAllMessages(conversationId)
                messageDao.updatePinnedStatus(messageId, isPinned)
                conversationDao.updatePinnedMessageId(conversationId, pinnedId)
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    override fun searchMessages(conversationId: String, query: String): Flow<List<Message>> =
        messageDao.searchMessages(conversationId, query).map { entities -> 
            entities.map { MessageMapper.toDomain(it) } 
        }

    override fun getPinnedMessage(conversationId: String): Flow<Message?> =
        messageDao.getPinnedMessage(conversationId).map { entity -> 
            entity?.let { MessageMapper.toDomain(it) } 
        }

    override suspend fun deleteMessage(conversationId: String, messageId: String): Result<Unit> {
        return try {
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
            
            messageDao.deleteMessageById(messageId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            // Deletar todas as mensagens da sub-coleção
            val messagesQuery = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .get().await()
            
            if (!messagesQuery.isEmpty) {
                val batch = firestore.batch()
                for (doc in messagesQuery.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }

            // Deletar a conversa
            firestore.collection("conversations")
                .document(conversationId)
                .delete()
                .await()

            // Limpar cache local
            messageDao.deleteMessagesByConversation(conversationId)
            val localConv = conversationDao.getConversationByIdOnce(conversationId)
            if (localConv != null) {
                conversationDao.deleteConversation(localConv)
            }

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun editMessage(conversationId: String, messageId: String, newText: String): Result<Unit> {
        return try {
            val encryptedText = com.example.zapzap.util.EncryptionHelper.encrypt(newText)
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .update(mapOf(
                    "text" to encryptedText,
                    "isEdited" to true
                ))
                .await()
            
            messageDao.updateMessageText(messageId, newText)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun searchConversations(userId: String, query: String): Flow<List<Conversation>> =
        conversationDao.searchConversations(query).map { entities -> 
            entities.map { ConversationMapper.toDomain(it) } 
        }

    private fun showLocalNotification(title: String, body: String, conversationId: String) {
        try {
            val intent = android.content.Intent(context, Class.forName("com.example.zapzap.MainActivity")).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("conversationId", conversationId)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, System.currentTimeMillis().toInt(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val notification = androidx.core.app.NotificationCompat.Builder(context, com.example.zapzap.ZapZapApplication.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(conversationId.hashCode(), notification)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Falha ao mostrar notificação local", e)
        }
    }
}
