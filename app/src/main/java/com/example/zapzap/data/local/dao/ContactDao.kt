package com.example.zapzap.data.local.dao

import androidx.room.*
import com.example.zapzap.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com contatos.
 */
@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY displayName ASC")
    fun getContacts(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :contactId AND userId = :userId")
    suspend fun deleteContactById(userId: String, contactId: String)

    @Query("SELECT * FROM contacts WHERE userId = :userId AND (displayName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchContacts(userId: String, query: String): Flow<List<ContactEntity>>
}
