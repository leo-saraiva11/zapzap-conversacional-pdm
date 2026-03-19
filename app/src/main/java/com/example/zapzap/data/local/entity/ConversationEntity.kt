package com.example.zapzap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para armazenamento local de conversas.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val name: String = "",
    val type: String = "INDIVIDUAL",
    val photoUrl: String = "",
    val participantIds: String = "", // JSON array serializado
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val lastMessageStatus: String = "SENT",
    val unreadCount: Int = 0,
    val pinnedMessageId: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val coverUrl: String = ""
)
