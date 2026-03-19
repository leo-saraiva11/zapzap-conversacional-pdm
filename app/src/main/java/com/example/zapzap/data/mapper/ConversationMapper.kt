package com.example.zapzap.data.mapper

import com.example.zapzap.data.local.entity.ConversationEntity
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.MessageStatus
import org.json.JSONArray

object ConversationMapper {
    fun toDomain(entity: ConversationEntity): Conversation = Conversation(
        id = entity.id,
        name = entity.name,
        type = ConversationType.fromString(entity.type),
        photoUrl = entity.photoUrl,
        participantIds = deserializeList(entity.participantIds),
        lastMessage = entity.lastMessage,
        lastMessageTime = entity.lastMessageTime,
        lastMessageSenderId = entity.lastMessageSenderId,
        lastMessageStatus = MessageStatus.fromString(entity.lastMessageStatus),
        unreadCount = entity.unreadCount,
        pinnedMessageId = entity.pinnedMessageId,
        createdAt = entity.createdAt,
        createdBy = entity.createdBy
    )

    fun toEntity(conversation: Conversation): ConversationEntity = ConversationEntity(
        id = conversation.id,
        name = conversation.name,
        type = conversation.type.name,
        photoUrl = conversation.photoUrl,
        participantIds = serializeList(conversation.participantIds),
        lastMessage = conversation.lastMessage,
        lastMessageTime = conversation.lastMessageTime,
        lastMessageSenderId = conversation.lastMessageSenderId,
        lastMessageStatus = conversation.lastMessageStatus.name,
        unreadCount = conversation.unreadCount,
        pinnedMessageId = conversation.pinnedMessageId,
        createdAt = conversation.createdAt,
        createdBy = conversation.createdBy
    )

    fun fromFirestore(map: Map<String, Any?>, conversationId: String, currentUserId: String? = null): Conversation {
        val rawParticipants = map["participantIds"] as? List<*>
        val participants = rawParticipants?.mapNotNull { it?.toString() } ?: emptyList()

        val lastMsg = map["lastMessage"] as? String ?: ""
        val decryptedLastMsg = if (lastMsg.isNotEmpty() && lastMsg != "📷 Mídia" && !lastMsg.startsWith("📍") && !lastMsg.contains("http")) {
            com.example.zapzap.util.EncryptionHelper.decrypt(lastMsg)
        } else {
            lastMsg
        }

        val unreadCountsMap = map["unreadCounts"] as? Map<String, Number>
        var computedUnread = (map["unreadCount"] as? Number)?.toInt() ?: 0
        if (currentUserId != null && unreadCountsMap != null) {
            computedUnread = unreadCountsMap[currentUserId]?.toInt() ?: 0
        }

        return Conversation(
            id = conversationId,
            name = map["name"] as? String ?: "",
            type = ConversationType.fromString(map["type"] as? String ?: "INDIVIDUAL"),
            photoUrl = map["photoUrl"] as? String ?: "",
            participantIds = participants,
            lastMessage = decryptedLastMsg,
            lastMessageTime = (map["lastMessageTime"] as? Number)?.toLong() ?: 0L,
            lastMessageSenderId = map["lastMessageSenderId"] as? String ?: "",
            lastMessageStatus = MessageStatus.fromString(map["lastMessageStatus"] as? String ?: "SENT"),
            unreadCount = computedUnread,
            pinnedMessageId = map["pinnedMessageId"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
            createdBy = map["createdBy"] as? String ?: ""
        )
    }

    fun toFirestore(conversation: Conversation): Map<String, Any?> = mapOf(
        "name" to conversation.name,
        "type" to conversation.type.name,
        "photoUrl" to conversation.photoUrl,
        "participantIds" to conversation.participantIds,
        "lastMessage" to conversation.lastMessage,
        "lastMessageTime" to conversation.lastMessageTime,
        "lastMessageSenderId" to conversation.lastMessageSenderId,
        "lastMessageStatus" to conversation.lastMessageStatus.name,
        "unreadCount" to conversation.unreadCount,
        "pinnedMessageId" to conversation.pinnedMessageId,
        "createdAt" to conversation.createdAt,
        "createdBy" to conversation.createdBy
    )

    private fun serializeList(list: List<String>): String = JSONArray(list).toString()
    private fun deserializeList(json: String): List<String> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) { emptyList() }
}
