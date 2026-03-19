package com.example.zapzap.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Contact
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ContactRepository
import com.example.zapzap.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import android.net.Uri
import com.example.zapzap.domain.model.MessageType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository,
    private val mediaRepository: com.example.zapzap.domain.repository.MediaRepository
) : ViewModel() {

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val contacts: StateFlow<List<Contact>> = flow {
        val userId = authRepository.currentUserId ?: return@flow
        emitAll(contactRepository.getContacts(userId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun toggleContactSelection(contactId: String) {
        _selectedContacts.value = _selectedContacts.value.toMutableSet().apply {
            if (contains(contactId)) remove(contactId) else add(contactId)
        }
    }

    suspend fun createGroup(photoUri: Uri? = null, coverUri: Uri? = null): Pair<String, String>? {
        val userId = authRepository.currentUserId ?: return null
        val memberIds = _selectedContacts.value.toList() + userId

        _isLoading.value = true
        
        var photoUrl = ""
        if (photoUri != null) {
            val uploadResult = mediaRepository.uploadMedia(photoUri, MessageType.IMAGE, "group_profiles")
            photoUrl = uploadResult.getOrNull()?.url ?: ""
        }

        var coverUrl = ""
        if (coverUri != null) {
            val uploadResult = mediaRepository.uploadMedia(coverUri, MessageType.IMAGE, "group_covers")
            coverUrl = uploadResult.getOrNull()?.url ?: ""
        }

        val result = groupRepository.createGroup(
            name = _groupName.value,
            photoUrl = photoUrl,
            coverUrl = coverUrl,
            memberIds = memberIds,
            createdBy = userId
        )
        _isLoading.value = false

        return result.getOrNull()?.let { conv ->
            Pair(conv.id, conv.name)
        }
    }
}
