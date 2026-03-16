package com.example.zapzap.domain.repository

import com.example.zapzap.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de autenticação.
 * Define o contrato para operações de login, cadastro e sessão.
 */
interface AuthRepository {
    /** Usuário atualmente autenticado (null se não logado) */
    val currentUser: Flow<User?>

    /** ID do usuário atual */
    val currentUserId: String?

    /** Verifica se há um usuário logado */
    val isLoggedIn: Boolean

    /** Login com email e senha */
    suspend fun loginWithEmail(email: String, password: String): Result<User>

    /** Login com conta Google */
    suspend fun loginWithGoogle(idToken: String): Result<User>

    /** Login com telefone (enviar código) */
    suspend fun sendPhoneVerification(phoneNumber: String): Result<String>

    /** Verificar código do telefone */
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User>

    /** Cadastro com email e senha */
    suspend fun register(name: String, email: String, password: String): Result<User>

    /** Recuperação de senha */
    suspend fun resetPassword(email: String): Result<Unit>

    /** Logout */
    suspend fun logout(): Result<Unit>
}
