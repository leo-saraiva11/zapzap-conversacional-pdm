package com.example.zapzap.domain.model

/**
 * Modelo de domínio representando uma mensagem no chat.
 */
data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val localMediaPath: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING,
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val isEdited: Boolean = false
)

/**
 * Tipo da mensagem.
 */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LOCATION,
    STICKER;

    companion object {
        fun fromString(value: String): MessageType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                TEXT
            }
        }
    }
}

/**
 * Status de entrega da mensagem.
 * SENDING → SENT → DELIVERED → READ
 */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ;

    companion object {
        fun fromString(value: String): MessageStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                SENDING
            }
        }
    }
}
