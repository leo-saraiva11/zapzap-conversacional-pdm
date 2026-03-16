package com.example.zapzap.domain.repository

import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de chat.
 * Define o contrato para operações com mensagens e conversas.
 */
interface ChatRepository {
    /** Observa a lista de conversas do usuário em tempo real */
    fun getConversations(userId: String): Flow<List<Conversation>>

    /** Observa as mensagens de uma conversa em tempo real */
    fun getMessages(conversationId: String): Flow<List<Message>>

    /** Busca uma conversa por ID */
    suspend fun getConversation(conversationId: String): Result<Conversation>

    /** Cria ou busca uma conversa individual entre dois usuários */
    suspend fun getOrCreateConversation(currentUserId: String, otherUserId: String): Result<Conversation>

    /** Envia uma mensagem */
    suspend fun sendMessage(message: Message): Result<Message>

    /** Atualiza o status de uma mensagem */
    suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: com.example.zapzap.domain.model.MessageStatus
    ): Result<Unit>

    /** Marca todas as mensagens como lidas */
    suspend fun markAllAsRead(conversationId: String, userId: String): Result<Unit>

    /** Fixa/desfixa uma mensagem */
    suspend fun togglePinMessage(conversationId: String, messageId: String, isPinned: Boolean): Result<Unit>

    /** Busca mensagens por palavra-chave */
    fun searchMessages(conversationId: String, query: String): Flow<List<Message>>

    /** Obtém a mensagem fixada de uma conversa */
    fun getPinnedMessage(conversationId: String): Flow<Message?>

    /** Busca conversas por nome */
    fun searchConversations(userId: String, query: String): Flow<List<Conversation>>
}
