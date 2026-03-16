package com.example.zapzap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para armazenamento local de contatos.
 */
@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val displayName: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val addedAt: Long = 0L
)
