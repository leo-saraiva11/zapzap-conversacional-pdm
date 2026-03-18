package com.example.zapzap.ui.screens.chat

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.repository.AuthRepository
import com.example.zapzap.domain.repository.ChatRepository
import com.example.zapzap.domain.repository.MediaRepository
import com.example.zapzap.domain.repository.UserRepository
import com.example.zapzap.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _conversationId = MutableStateFlow("")
    val conversationId: StateFlow<String> = _conversationId.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    val currentUserId: String? get() = authRepository.currentUserId

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    val pinnedMessage: StateFlow<Message?> = _conversationId
        .filter { it.isNotBlank() }
        .flatMapLatest { chatRepository.getPinnedMessage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _groupMembers = MutableStateFlow<List<com.example.zapzap.domain.model.User>>(emptyList())
    
    val groupMembers: StateFlow<List<com.example.zapzap.domain.model.User>> = _conversationId
        .filter { it.isNotBlank() }
        .flatMapLatest { groupRepository.getGroupMembers(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversation = MutableStateFlow<com.example.zapzap.domain.model.Conversation?>(null)
    val currentConversation: StateFlow<com.example.zapzap.domain.model.Conversation?> = _currentConversation.asStateFlow()

    fun fetchMessages(conversationId: String) {
        _conversationId.value = conversationId
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: ""
            chatRepository.getMessages(conversationId, userId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun setConversationId(id: String) {
        _conversationId.value = id
        viewModelScope.launch {
            val conv = chatRepository.getConversation(id).getOrNull()
            _currentConversation.value = conv
            authRepository.currentUserId?.let { userId ->
                chatRepository.markAllAsRead(id, userId)
            }
        }
    }

    fun renameGroup(newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                groupRepository.editGroup(_conversationId.value, newName, "")
                // Refresh local info
                val updatedConv = chatRepository.getConversation(_conversationId.value).getOrNull()
                _currentConversation.value = updatedConv
            }
        }
    }

    fun removeParticipant(participantId: String) {
        viewModelScope.launch {
            groupRepository.removeMember(_conversationId.value, participantId)
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage(text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return

        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val user = userRepository.getUserById(userId).getOrNull()

            val message = Message(
                conversationId = _conversationId.value,
                senderId = userId,
                senderName = user?.displayName ?: "",
                text = trimmedText,
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

    fun startRecording(context: Context) {
        try {
            audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao iniciar gravação", e)
        }
    }

    fun stopRecording(context: Context) {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false

            audioFile?.let { file ->
                val uri = Uri.fromFile(file)
                sendMediaMessage(uri, MessageType.AUDIO)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao parar gravação", e)
            _isRecording.value = false
            mediaRecorder = null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            audioFile?.delete()
            audioFile = null
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao cancelar gravação", e)
            _isRecording.value = false
            mediaRecorder = null
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
                text = "Localização",
                status = MessageStatus.SENDING
            )
            chatRepository.sendMessage(message)
        }
    }

    fun togglePinMessage(messageId: String, currentlyPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePinMessage(_conversationId.value, messageId, !currentlyPinned)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(_conversationId.value, messageId)
        }
    }

    fun editMessage(messageId: String, newText: String) {
        viewModelScope.launch {
            chatRepository.editMessage(_conversationId.value, messageId, newText)
        }
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }
}
