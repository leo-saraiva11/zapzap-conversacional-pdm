package com.example.zapzap.data.mapper

import com.example.zapzap.data.local.entity.MessageEntity
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.google.firebase.Timestamp

object MessageMapper {
    fun toDomain(entity: MessageEntity): Message = Message(
        id = entity.id,
        conversationId = entity.conversationId,
        senderId = entity.senderId,
        senderName = entity.senderName,
        text = entity.text,
        type = MessageType.fromString(entity.type),
        mediaUrl = entity.mediaUrl,
        localMediaPath = entity.localMediaPath,
        latitude = entity.latitude,
        longitude = entity.longitude,
        timestamp = entity.timestamp,
        status = MessageStatus.fromString(entity.status),
        isPinned = entity.isPinned,
        isEncrypted = entity.isEncrypted,
        isEdited = entity.isEdited,
        repliedMessageId = entity.repliedMessageId,
        repliedMessageText = entity.repliedMessageText,
        repliedMessageSender = entity.repliedMessageSender
    )

    fun toEntity(message: Message, isSynced: Boolean = true): MessageEntity = MessageEntity(
        id = message.id,
        conversationId = message.conversationId,
        senderId = message.senderId,
        senderName = message.senderName,
        text = message.text,
        type = message.type.name,
        mediaUrl = message.mediaUrl,
        localMediaPath = message.localMediaPath,
        latitude = message.latitude,
        longitude = message.longitude,
        timestamp = message.timestamp,
        status = message.status.name,
        isPinned = message.isPinned,
        isEncrypted = message.isEncrypted,
        isEdited = message.isEdited,
        isSynced = isSynced,
        repliedMessageId = message.repliedMessageId,
        repliedMessageText = message.repliedMessageText,
        repliedMessageSender = message.repliedMessageSender
    )

    fun fromFirestore(map: Map<String, Any?>, messageId: String): Message {
        // CORREÇÃO CRÍTICA: Tratar Timestamp do Firebase e números diversos
        val ts = when (val t = map["timestamp"]) {
            is Timestamp -> t.toDate().time
            is Long -> t
            is Number -> t.toLong()
            else -> System.currentTimeMillis()
        }

        return Message(
            id = messageId,
            conversationId = map["conversationId"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            senderName = map["senderName"] as? String ?: "",
            text = map["text"] as? String ?: "",
            type = MessageType.fromString(map["type"] as? String ?: "TEXT"),
            mediaUrl = map["mediaUrl"] as? String ?: "",
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
            timestamp = if (ts <= 0) System.currentTimeMillis() else ts,
            status = MessageStatus.fromString(map["status"] as? String ?: "SENT"),
            isPinned = map["isPinned"] as? Boolean ?: false,
            isEncrypted = map["isEncrypted"] as? Boolean ?: false,
            isEdited = map["isEdited"] as? Boolean ?: false,
            repliedMessageId = map["repliedMessageId"] as? String ?: "",
            repliedMessageText = map["repliedMessageText"] as? String ?: "",
            repliedMessageSender = map["repliedMessageSender"] as? String ?: ""
        )
    }

    fun toFirestore(message: Message): Map<String, Any?> = mapOf(
        "conversationId" to message.conversationId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "text" to message.text,
        "type" to message.type.name,
        "mediaUrl" to message.mediaUrl,
        "latitude" to message.latitude,
        "longitude" to message.longitude,
        "timestamp" to (if (message.timestamp <= 0) System.currentTimeMillis() else message.timestamp),
        "status" to message.status.name,
        "isPinned" to message.isPinned,
        "isEncrypted" to message.isEncrypted,
        "isEdited" to message.isEdited,
        "repliedMessageId" to message.repliedMessageId,
        "repliedMessageText" to message.repliedMessageText,
        "repliedMessageSender" to message.repliedMessageSender
    )
}
