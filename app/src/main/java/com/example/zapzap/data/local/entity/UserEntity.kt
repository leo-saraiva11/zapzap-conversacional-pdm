package com.example.zapzap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para armazenamento local de usuários.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val status: String = "OFFLINE",
    val lastSeen: Long = 0L,
    val fcmToken: String = "",
    val publicKey: String = "",
    val about: String = "Olá! Estou usando o ZapZap."
)
