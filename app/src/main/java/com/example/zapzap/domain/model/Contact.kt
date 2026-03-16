package com.example.zapzap.domain.model

/**
 * Modelo de domínio representando um contato do usuário.
 */
data class Contact(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
