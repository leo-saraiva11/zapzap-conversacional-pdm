package com.example.zapzap.ui.screens.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a lista de conversas.
 */
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val conversations: StateFlow<List<Conversation>> = flow {
        val userId = authRepository.currentUserId ?: return@flow
        emitAll(chatRepository.getConversations(userId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredConversations: StateFlow<List<Conversation>> = combine(
        conversations,
        _searchQuery
    ) { convs, query ->
        if (query.isBlank()) convs
        else convs.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.lastMessage.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
