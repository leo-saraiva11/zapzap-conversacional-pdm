package com.example.zapzap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val userProfile: StateFlow<User?> = flow {
        val userId = authRepository.currentUserId ?: return@flow
        emitAll(userRepository.getUserProfile(userId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    fun updateProfile(name: String, about: String, status: UserStatus) {
        viewModelScope.launch {
            val currentUser = userProfile.value ?: return@launch
            userRepository.updateProfile(
                currentUser.copy(
                    displayName = name,
                    about = about,
                    status = status
                )
            )
            _isEditing.value = false
        }
    }

    fun updateProfilePhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            userRepository.updateProfilePhoto(userId, uri)
        }
    }
}
