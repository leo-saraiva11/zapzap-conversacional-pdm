package com.example.zapzap.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Contact
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ContactRepository
import com.example.zapzap.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository
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

    suspend fun createGroup(): Pair<String, String>? {
        val userId = authRepository.currentUserId ?: return null
        val memberIds = _selectedContacts.value.toList() + userId

        _isLoading.value = true
        val result = groupRepository.createGroup(
            name = _groupName.value,
            photoUrl = "",
            memberIds = memberIds,
            createdBy = userId
        )
        _isLoading.value = false

        return result.getOrNull()?.let { conv ->
            Pair(conv.id, conv.name)
        }
    }
}
