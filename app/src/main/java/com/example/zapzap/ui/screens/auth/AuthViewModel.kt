package com.example.zapzap.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de autenticação.
 * Gerencia login, cadastro e recuperação de senha.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Tornar isLoggedIn reativo ao currentUser
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.isLoggedIn)

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginWithEmail(email, password)
            _authState.value = result.fold(
                onSuccess = { 
                    AuthState.Success(it) 
                },
                onFailure = { AuthState.Error(it.message ?: "Erro ao fazer login") }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginWithGoogle(idToken)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Erro ao fazer login com Google") }
            )
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(name, email, password)
            _authState.value = result.fold(
                onSuccess = { 
                    _authState.value = AuthState.Success(it)
                    AuthState.Success(it) 
                },
                onFailure = { 
                    _authState.value = AuthState.Error(it.message ?: "Erro ao criar conta")
                    AuthState.Error(it.message ?: "Erro ao criar conta")
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            _authState.value = result.fold(
                onSuccess = { AuthState.PasswordResetSent },
                onFailure = { AuthState.Error(it.message ?: "Erro ao enviar email de recuperação") }
            )
        }
    }

    fun sendPhoneVerification(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendPhoneVerification(phoneNumber, activity)
            _authState.value = result.fold(
                onSuccess = { AuthState.VerificationCodeSent(it) },
                onFailure = { AuthState.Error(it.message ?: "Erro ao enviar SMS") }
            )
        }
    }

    fun verifyPhoneCode(verificationId: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.verifyPhoneCode(verificationId, code)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Código inválido") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}

/**
 * Estados possíveis da autenticação.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
    data class VerificationCodeSent(val verificationId: String) : AuthState()
}
