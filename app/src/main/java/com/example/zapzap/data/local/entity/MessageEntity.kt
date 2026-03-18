package com.example.zapzap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para armazenamento local de mensagens.
 * Serve como cache offline para sincronização.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String = "",
    val text: String = "",
    val type: String = "TEXT",
    val mediaUrl: String = "",
    val localMediaPath: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = "SENDING",
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val isEdited: Boolean = false,
    val isSynced: Boolean = false
)
