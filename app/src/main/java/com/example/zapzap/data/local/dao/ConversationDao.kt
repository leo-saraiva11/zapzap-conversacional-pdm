package com.example.zapzap.data.local.dao

import androidx.room.*
import com.example.zapzap.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com conversas.
 */
@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationById(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationByIdOnce(conversationId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    /** Busca conversas por nome */
    @Query("SELECT * FROM conversations WHERE name LIKE '%' || :query || '%' ORDER BY lastMessageTime DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    /** Atualiza contagem de não lidas */
    @Query("UPDATE conversations SET unreadCount = :count WHERE id = :conversationId")
    suspend fun updateUnreadCount(conversationId: String, count: Int)

    /** Atualiza última mensagem */
    @Query("UPDATE conversations SET lastMessage = :message, lastMessageTime = :time, lastMessageSenderId = :senderId WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, message: String, time: Long, senderId: String)

    /** Atualiza o ID da mensagem fixada */
    @Query("UPDATE conversations SET pinnedMessageId = :messageId WHERE id = :conversationId")
    suspend fun updatePinnedMessageId(conversationId: String, messageId: String)
}
