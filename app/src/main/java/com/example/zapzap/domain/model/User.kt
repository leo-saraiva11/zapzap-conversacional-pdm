package com.example.zapzap.domain.model

/**
 * Modelo de domínio representando um usuário do ZapZap.
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    val publicKey: String = "",
    val about: String = "Olá! Estou usando o ZapZap."
)

/**
 * Status de conectividade do usuário.
 */
enum class UserStatus {
    ONLINE,
    OFFLINE,
    BUSY;

    fun toDisplayString(): String = when (this) {
        ONLINE -> "Online"
        OFFLINE -> "Offline"
        BUSY -> "Ocupado"
    }
}
