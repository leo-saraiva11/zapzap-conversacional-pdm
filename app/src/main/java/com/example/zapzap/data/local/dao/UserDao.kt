package com.example.zapzap.data.local.dao

import androidx.room.*
import com.example.zapzap.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados com usuários.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByIdOnce(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("UPDATE users SET status = :status, lastSeen = :lastSeen WHERE uid = :uid")
    suspend fun updateStatus(uid: String, status: String, lastSeen: Long)
}
