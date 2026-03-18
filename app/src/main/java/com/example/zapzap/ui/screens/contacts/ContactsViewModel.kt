package com.example.zapzap.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Contact
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ChatRepository
import com.example.zapzap.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _addContactQuery = MutableStateFlow("")
    val addContactQuery: StateFlow<String> = _addContactQuery.asStateFlow()

    private val _foundContact = MutableStateFlow<Contact?>(null)
    val foundContact: StateFlow<Contact?> = _foundContact.asStateFlow()

    val contacts: StateFlow<List<Contact>> = flow {
        val userId = authRepository.currentUserId ?: return@flow
        emitAll(contactRepository.getContacts(userId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredContacts: StateFlow<List<Contact>> = combine(
        contacts,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.displayName.contains(query, ignoreCase = true) ||
                    it.phone.contains(query) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateAddContactQuery(query: String) {
        _addContactQuery.value = query
    }

    fun searchUserToAdd() {
        viewModelScope.launch {
            val result = contactRepository.findUserByEmailOrPhone(_addContactQuery.value.trim())
            _foundContact.value = result.getOrNull()
        }
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            contactRepository.addContact(userId, contact)
            _foundContact.value = null
            _addContactQuery.value = ""
        }
    }

    fun removeContact(contactId: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            contactRepository.removeContact(userId, contactId)
            
            // Buscar e deletar a conversa individual com esse contato
            try {
                val conversations = chatRepository.getConversations(userId)
                conversations.first().forEach { conv ->
                    if (conv.type == com.example.zapzap.domain.model.ConversationType.INDIVIDUAL &&
                        conv.participantIds.contains(contactId) &&
                        conv.participantIds.contains(userId)) {
                        chatRepository.deleteConversation(conv.id)
                    }
                }
            } catch (_: Exception) { /* Se falhar ao deletar conversa, pelo menos o contato foi removido */ }
        }
    }

    fun importDeviceContacts() {
        viewModelScope.launch {
            val result = contactRepository.importDeviceContacts()
            val deviceContacts = result.getOrNull() ?: emptyList()
            val currentUserId = authRepository.currentUserId ?: return@launch
            
            deviceContacts.forEach { deviceContact ->
                val searchResult = contactRepository.findUserByEmailOrPhone(deviceContact.phone)
                val registeredUser = searchResult.getOrNull()
                
                if (registeredUser != null) {
                    // Update display name map to use the device's saved name
                    contactRepository.addContact(currentUserId, registeredUser.copy(displayName = deviceContact.displayName))
                }
            }
        }
    }

    suspend fun startConversation(contactUserId: String): Pair<String, String>? {
        val userId = authRepository.currentUserId ?: return null
        val result = chatRepository.getOrCreateConversation(userId, contactUserId)
        return result.getOrNull()?.let { conv ->
            Pair(conv.id, conv.name)
        }
    }
}
