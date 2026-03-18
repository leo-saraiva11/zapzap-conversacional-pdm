package com.example.zapzap.domain.model

/**
 * Modelo de domínio representando uma conversa (individual ou grupo).
 */
data class Conversation(
    val id: String = "",
    val name: String = "",
    val type: ConversationType = ConversationType.INDIVIDUAL,
    val photoUrl: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val lastMessageStatus: MessageStatus = MessageStatus.SENT,
    val unreadCount: Int = 0,
    val pinnedMessageId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)

/**
 * Tipo da conversa.
 */
enum class ConversationType {
    INDIVIDUAL,
    GROUP;

    companion object {
        fun fromString(value: String): ConversationType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                INDIVIDUAL
            }
        }
    }
}
