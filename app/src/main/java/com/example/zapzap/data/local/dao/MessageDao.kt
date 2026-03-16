package com.example.zapzap.data.local.dao

import androidx.room.*
import com.example.zapzap.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com mensagens.
 * Inclui queries para busca por palavra-chave e mensagens fixadas.
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    /** Busca mensagens por palavra-chave (Requisito Especial 14) */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND text LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchMessages(conversationId: String, query: String): Flow<List<MessageEntity>>

    /** Obtém a mensagem fixada (Requisito Especial 13) */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isPinned = 1 LIMIT 1")
    fun getPinnedMessage(conversationId: String): Flow<MessageEntity?>

    /** Atualiza status de uma mensagem */
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    /** Fixa/desfixa uma mensagem */
    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun updatePinnedStatus(messageId: String, isPinned: Boolean)

    /** Desfixa todas as mensagens da conversa (antes de fixar uma nova) */
    @Query("UPDATE messages SET isPinned = 0 WHERE conversationId = :conversationId")
    suspend fun unpinAllMessages(conversationId: String)

    /** Obtém mensagens não sincronizadas */
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    /** Marca mensagem como sincronizada */
    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun markAsSynced(messageId: String)
}
