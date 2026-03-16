package com.example.zapzap.domain.repository

import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de usuários.
 */
interface UserRepository {
    /** Observa os dados do perfil do usuário */
    fun getUserProfile(userId: String): Flow<User?>

    /** Atualiza o perfil do usuário */
    suspend fun updateProfile(user: User): Result<Unit>

    /** Atualiza foto do perfil */
    suspend fun updateProfilePhoto(userId: String, photoUri: android.net.Uri): Result<String>

    /** Atualiza o status do usuário (online/offline/ocupado) */
    suspend fun updateStatus(userId: String, status: UserStatus): Result<Unit>

    /** Atualiza o token FCM */
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>

    /** Busca usuário por UID */
    suspend fun getUserById(userId: String): Result<User>
}
