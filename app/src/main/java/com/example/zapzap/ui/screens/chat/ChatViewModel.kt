package com.example.zapzap.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ChatRepository
import com.example.zapzap.domain.repository.MediaRepository
import com.example.zapzap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel do chat.
 * Gerencia mensagens em tempo real, envio, mídia, fixar e busca.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _conversationId = MutableStateFlow("")
    val conversationId: StateFlow<String> = _conversationId.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    val currentUserId: String? get() = authRepository.currentUserId

    // Mensagens em tempo real
    val messages: StateFlow<List<Message>> = _conversationId
        .filter { it.isNotBlank() }
        .flatMapLatest { chatRepository.getMessages(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensagem fixada
    val pinnedMessage: StateFlow<Message?> = _conversationId
        .filter { it.isNotBlank() }
        .flatMapLatest { chatRepository.getPinnedMessage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Resultados da busca
    val searchResults: StateFlow<List<Message>> = combine(
        _conversationId,
        _searchQuery
    ) { convId, query ->
        Pair(convId, query)
    }.filter { it.first.isNotBlank() && it.second.isNotBlank() }
        .flatMapLatest { (convId, query) ->
            chatRepository.searchMessages(convId, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setConversationId(id: String) {
        _conversationId.value = id
        // Marcar todas como lidas
        viewModelScope.launch {
            authRepository.currentUserId?.let { userId ->
                chatRepository.markAllAsRead(id, userId)
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendTextMessage() {
        val text = _messageText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val user = userRepository.getUserById(userId).getOrNull()

            val message = Message(
                conversationId = _conversationId.value,
                senderId = userId,
                senderName = user?.displayName ?: "",
                text = text,
                type = MessageType.TEXT,
                status = MessageStatus.SENDING
            )
            chatRepository.sendMessage(message)
            _messageText.value = ""
        }
    }

    fun sendMediaMessage(uri: Uri, type: MessageType) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val user = userRepository.getUserById(userId).getOrNull()
            val convId = _conversationId.value

            val uploadResult = mediaRepository.uploadMedia(uri, type, convId)
            uploadResult.onSuccess { attachment ->
                val message = Message(
                    conversationId = convId,
                    senderId = userId,
                    senderName = user?.displayName ?: "",
                    type = type,
                    mediaUrl = attachment.url,
                    status = MessageStatus.SENDING
                )
                chatRepository.sendMessage(message)
            }
        }
    }

    fun sendLocationMessage(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val user = userRepository.getUserById(userId).getOrNull()

            val message = Message(
                conversationId = _conversationId.value,
                senderId = userId,
                senderName = user?.displayName ?: "",
                type = MessageType.LOCATION,
                latitude = latitude,
                longitude = longitude,
                text = "📍 Localização",
                status = MessageStatus.SENDING
            )
            chatRepository.sendMessage(message)
        }
    }

    fun togglePinMessage(messageId: String, currentlyPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePinMessage(
                _conversationId.value,
                messageId,
                !currentlyPinned
            )
        }
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }
}
