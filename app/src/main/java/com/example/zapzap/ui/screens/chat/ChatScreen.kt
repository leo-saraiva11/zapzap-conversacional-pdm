package com.example.zapzap.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.ui.theme.*
import com.example.zapzap.ui.util.DateFormatUtils
import com.example.zapzap.ui.util.ImageUtils
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    conversationName: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val pinnedMessage by viewModel.pinnedMessage.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val listState = rememberLazyListState()

    var showAttachMenu by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var previewMediaUri by remember { mutableStateOf<Uri?>(null) }
    var previewMediaType by remember { mutableStateOf<MessageType?>(null) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var forwardingMessage by remember { mutableStateOf<Message?>(null) }
    val allConversations by viewModel.allConversations.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.setConversationId(conversationId)
        viewModel.fetchMessages(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Full screen image viewer
    fullScreenImageUrl?.let { url ->
        Dialog(onDismissRequest = { fullScreenImageUrl = null }) {
            Surface(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp), shape = RoundedCornerShape(12.dp)) {
                AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Fit)
            }
        }
    }

    // Media Preview Dialog (Item 7 Requirement)
    previewMediaUri?.let { uri ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { previewMediaUri = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                color = Color.Black
            ) {

                Box(modifier = Modifier.fillMaxSize()) {
                    if (previewMediaType == MessageType.IMAGE) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Pré-visualização",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (previewMediaType == MessageType.VIDEO) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    setVideoURI(uri)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            }
                        )
                    }

                    // Botões de overlay
                    IconButton(
                        onClick = { previewMediaUri = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .statusBarsPadding()
                            .background(Color.Black.copy(0.4f), CircleShape)
                    ) {

                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = Color.Black // Solid black bar
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .padding(bottom = 64.dp) // Margem de segurança manual para botões do sistema
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {




                            FloatingActionButton(
                                onClick = {
                                    viewModel.sendMediaMessage(uri, previewMediaType!!, replyingToMessage)
                                    previewMediaUri = null
                                    replyingToMessage = null
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Forward Dialog
    if (forwardingMessage != null) {
        Dialog(onDismissRequest = { forwardingMessage = null }) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Text(
                        text = "Encaminhar para...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(allConversations, key = { it.id }) { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.forwardMessage(forwardingMessage!!, conv.id)
                                        forwardingMessage = null
                                        android.widget.Toast.makeText(context, "Mensagem encaminhada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (conv.photoUrl.isEmpty()) {
                                            Text(conv.name.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold)
                                        } else {
                                            AsyncImage(model = conv.photoUrl, contentDescription = null, contentScale = ContentScale.Crop)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(conv.name.ifBlank { "Conversa" }, style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            previewMediaUri = photoUri
            previewMediaType = MessageType.IMAGE
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && photoUri != null) {
            previewMediaUri = photoUri
            previewMediaType = MessageType.VIDEO
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri)
            val type = if (mimeType?.startsWith("video") == true) MessageType.VIDEO else MessageType.IMAGE
            previewMediaUri = uri
            previewMediaType = type
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMediaMessage(it, MessageType.FILE) }
    }

    // Permissões
    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startRecording(context)
    }

    var showCameraOptions by remember { mutableStateOf(false) }
    var isCapturingVideo by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (isCapturingVideo) {
                val uri = ImageUtils.createTempVideoUri(context)
                photoUri = uri
                videoLauncher.launch(uri)
            } else {
                val uri = ImageUtils.createTempImageUri(context)
                photoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    if (showCameraOptions) {
        AlertDialog(
            onDismissRequest = { showCameraOptions = false },
            title = { Text("Câmera") },
            text = { Text("O que você deseja capturar?") },
            confirmButton = {
                TextButton(onClick = {
                    showCameraOptions = false
                    isCapturingVideo = false
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) { Text("Foto") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCameraOptions = false
                    isCapturingVideo = true
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) { Text("Vídeo") }
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { viewModel.sendLocationMessage(it.latitude, it.longitude) }
                }
            } catch (e: SecurityException) {}
        }
    }

    val groupMembers by viewModel.groupMembers.collectAsState()
    var showGroupInfo by remember { mutableStateOf(false) }

    val currentConversation by viewModel.currentConversation.collectAsState()
    val otherUserProfile by viewModel.otherUserProfile.collectAsState()
    val isGroupAdmin = currentConversation?.createdBy == viewModel.currentUserId
    var showContactInfo by remember { mutableStateOf(false) }

    // Dialog: Info do Contato (conversa individual)
    if (showContactInfo && currentConversation?.type == ConversationType.INDIVIDUAL && otherUserProfile != null) {
        ContactInfoDialog(
            user = otherUserProfile!!,
            onDismiss = { showContactInfo = false }
        )
    }

    // Dialog: Info do Grupo
    if (showGroupInfo) {
        Dialog(onDismissRequest = { showGroupInfo = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isGroupAdmin) {
                        var editName by remember { mutableStateOf(currentConversation?.name ?: conversationName) }
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nome do Grupo") },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.renameGroup(editName) }) {
                                    Icon(Icons.Default.Check, contentDescription = "Salvar")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Text(currentConversation?.name ?: conversationName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text("Participantes do Grupo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (groupMembers.isEmpty()) {
                        Text("Carregando...", modifier = Modifier.padding(8.dp))
                    } else {
                        LazyColumn {
                            items(groupMembers) { member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (member.photoUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = member.photoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(member.displayName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(member.displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    
                                    if (isGroupAdmin && member.uid != viewModel.currentUserId) {
                                        IconButton(onClick = { viewModel.removeParticipant(member.uid) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remover participante", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showGroupInfo = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Fechar")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearching) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Buscar...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                        )
                    },
                    navigationIcon = { IconButton(onClick = { viewModel.toggleSearch() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (currentConversation?.type == ConversationType.INDIVIDUAL) {
                                    showContactInfo = true
                                } else {
                                    showGroupInfo = true
                                }
                            }
                        ) {
                            // Avatar do contato/grupo
                            if (otherUserProfile != null && currentConversation?.type == ConversationType.INDIVIDUAL) {
                                val photo = otherUserProfile?.photoUrl ?: ""
                                if (photo.isNotEmpty()) {
                                    AsyncImage(
                                        model = photo,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                conversationName.firstOrNull()?.uppercase() ?: "?",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column {
                                Text(conversationName.ifBlank { "Carregando..." }, fontWeight = FontWeight.Bold, color = Color.White)
                                // Status do contato
                                if (currentConversation?.type == ConversationType.INDIVIDUAL && otherUserProfile != null) {
                                    val statusText = otherUserProfile?.status?.toDisplayString() ?: "Offline"
                                    val statusColor = when (otherUserProfile?.status) {
                                        UserStatus.ONLINE -> StatusOnline
                                        UserStatus.BUSY -> StatusBusy
                                        else -> StatusOffline
                                    }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        }
                        if (currentConversation?.type == ConversationType.GROUP) {
                            IconButton(onClick = { showGroupInfo = true }) {
                                Icon(Icons.Default.Group, contentDescription = "Ver Grupo", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
            pinnedMessage?.let { PinnedMessageBar(it, onUnpin = { viewModel.togglePinMessage(it.id, true) }) }
            Box(modifier = Modifier.weight(1f)) {
                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val bgPatternColor = if (isDark) Color(0xFF0b141a) else Color(0xFFefeae2)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgPatternColor)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val filteredMessages = if (isSearching && searchQuery.isNotBlank()) {
                        messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
                    } else {
                        messages
                    }
                    
                    val groupedByDate = filteredMessages.groupBy { DateFormatUtils.formatDate(it.timestamp) }
                    
                    for ((date, msgs) in groupedByDate) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), Alignment.Center) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1E4B3)) { // Verde claro estilo Zap
                                    Text(date, Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        itemsIndexed(msgs, key = { _, m -> m.id }) { index, msg ->
                            val prevMsg = msgs.getOrNull(index - 1)
                            val nextMsg = msgs.getOrNull(index + 1)
                            val isFirstInGroup = prevMsg?.senderId != msg.senderId
                            val isLastInGroup = nextMsg?.senderId != msg.senderId
                            
                            MessageBubble(
                                message = msg,
                                isOwnMessage = msg.senderId == viewModel.currentUserId,
                                searchQuery = searchQuery,
                                isFirstInGroup = isFirstInGroup,
                                isLastInGroup = isLastInGroup,
                                onPinClick = { viewModel.togglePinMessage(msg.id, msg.isPinned) },
                                onDeleteClick = { viewModel.deleteMessage(msg.id) },
                                onEditClick = { 
                                    editingMessage = msg
                                    viewModel.updateMessageText(msg.text)
                                },
                                onCopyClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.text))
                                    android.widget.Toast.makeText(context, "Mensagem copiada", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onReplyClick = { replyingToMessage = msg },
                                onForwardClick = { forwardingMessage = msg },
                                onImageClick = { fullScreenImageUrl = it }
                            )
                        }
                    }
                }
            }

            ChatInputBar(
                messageText = messageText,
                isRecording = isRecording,
                isEditing = editingMessage != null,
                replyingToMessage = replyingToMessage,
                onMessageChange = { viewModel.updateMessageText(it) },
                onSendMessage = { 
                    if (editingMessage != null) {
                        viewModel.editMessage(editingMessage!!.id, messageText)
                        editingMessage = null
                        viewModel.updateMessageText("")
                    } else {
                        viewModel.sendMessage(messageText, replyingToMessage) 
                        replyingToMessage = null
                    }
                },
                onCancelEdit = {
                    editingMessage = null
                    viewModel.updateMessageText("")
                },
                onCancelReply = { replyingToMessage = null },
                onAttachClick = { showAttachMenu = true },
                onCameraClick = {
                    showCameraOptions = true
                },
                onLocationClick = {
                    locationPermissionLauncher.launch(arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                },
                onVoiceStart = { 
                    voicePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                },
                onVoiceStop = { viewModel.stopRecording(context) },
                onVoiceCancel = { viewModel.cancelRecording() }
            )

            if (showAttachMenu) {
                AttachmentMenu(
                    onImageClick = { galleryLauncher.launch("image/*") },
                    onLocationClick = {
                        showAttachMenu = false
                        locationPermissionLauncher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    onFileClick = { fileLauncher.launch("*/*") },
                    onDismiss = { showAttachMenu = false }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    searchQuery: String = "",
    isFirstInGroup: Boolean = true,
    isLastInGroup: Boolean = true,
    onPinClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    onForwardClick: () -> Unit = {},
    onImageClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bubbleColor = if (isOwnMessage) Color(0xFFE7FFDB) else Color.White
    
    // Configura o arredondamento conforme a posição no grupo
    val shape = RoundedCornerShape(
        topStart = if (!isOwnMessage && isFirstInGroup) 0.dp else 12.dp,
        topEnd = if (isOwnMessage && isFirstInGroup) 0.dp else 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (isFirstInGroup) 4.dp else 1.dp,
                bottom = if (isLastInGroup) 4.dp else 1.dp,
                start = if (isOwnMessage) 64.dp else 0.dp,
                end = if (isOwnMessage) 0.dp else 64.dp
            ),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.pointerInput(message.id) {
                detectTapGestures(
                    onLongPress = { showMenu = true },
                    onTap = { /* Se quiser clique simples pra abrir mídia etc */ }
                )
            }
        ) {
            Box {
                // Menu suspenso de cada mensagem (setinha ou clique longo)
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (message.isPinned) "Desafixar" else "Fixar") },
                        onClick = { onPinClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Responder") },
                        onClick = { onReplyClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Reply, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Copiar") },
                        onClick = { onCopyClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Encaminhar") },
                        onClick = { onForwardClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp)) } // Forward alternative since Material icons might not have it easily available without automirrored
                    )
                    if (isOwnMessage) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { onEditClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Apagar para todos", color = Color.Red) },
                            onClick = { onDeleteClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red) }
                        )
                    }
                }
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                if (!isOwnMessage && isFirstInGroup && message.senderId.isNotEmpty()) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF075E54),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                if (message.repliedMessageId.isNotEmpty()) {
                    Surface(
                        color = bubbleColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).background(Color.Black.copy(0.05f))
                    ) {
                        Column(Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)) {
                            Text(message.repliedMessageSender.ifBlank { "Você" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            Text(message.repliedMessageText.ifBlank { "📷 Mídia" }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        }
                        Box(Modifier.width(4.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    }
                }

                when (message.type) {
                    MessageType.TEXT -> {
                        if (searchQuery.isNotBlank() && message.text.contains(searchQuery, ignoreCase = true)) {
                            val annotatedString = buildAnnotatedString {
                                val textStr = message.text
                                var currentPos = 0
                                var index = textStr.indexOf(searchQuery, ignoreCase = true)
                                while (index >= 0) {
                                    append(textStr.substring(currentPos, index))
                                    withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
                                        append(textStr.substring(index, index + searchQuery.length))
                                    }
                                    currentPos = index + searchQuery.length
                                    index = textStr.indexOf(searchQuery, currentPos, ignoreCase = true)
                                }
                                append(textStr.substring(currentPos))
                            }
                            Text(text = annotatedString, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    MessageType.IMAGE -> {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(message.mediaUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 250.dp, min = 150.dp)
                                .heightIn(max = 250.dp, min = 150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(message.mediaUrl), "video/*")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    try { context.startActivity(intent) } catch (e: Exception) { }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Play Video", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        }
                    }
                    MessageType.AUDIO -> AudioPlayerMessage(url = message.mediaUrl)
                    MessageType.LOCATION -> {
                        Column(Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${message.latitude},${message.longitude}?q=${message.latitude},${message.longitude}"))
                            context.startActivity(intent)
                        }) {
                            Text("📍 Localização", color = Color.Blue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            // Poderia carregar um mapa estático aqui se tivesse chave
                            Surface(Modifier.size(200.dp, 100.dp), color = Color.LightGray, shape = RoundedCornerShape(4.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null) }
                            }
                        }
                    }
                    MessageType.FILE -> {
                        Row(
                            Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl))
                                context.startActivity(intent)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.InsertDriveFile, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Arquivo", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> Text(message.text)
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            "Editada",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    if (message.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp).padding(end = 4.dp),
                            tint = Color.Gray
                        )
                    }
                    Text(
                        DateFormatUtils.formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    if (isOwnMessage) {
                        Spacer(Modifier.width(4.dp))
                        val statusIcon = when (message.status) {
                            MessageStatus.SENDING -> Icons.Default.Schedule
                            MessageStatus.SENT -> Icons.Default.DoneAll // 2 checks cinzas
                            MessageStatus.DELIVERED -> Icons.Default.DoneAll
                            MessageStatus.READ -> Icons.Default.DoneAll
                        }
                        val statusColor = when (message.status) {
                            MessageStatus.READ -> Color(0xFF34B7F1)
                            MessageStatus.DELIVERED -> Color.Gray
                            else -> Color.LightGray
                        }
                        Icon(statusIcon, null, Modifier.size(16.dp), tint = statusColor)
                    }
                }
            }
        }
    }
}
}

@Composable
fun AudioPlayerMessage(url: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
                        setDataSource(url)
                        prepareAsync()
                        setOnPreparedListener { start(); isPlaying = true }
                        setOnCompletionListener { isPlaying = false }
                    }
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                }
            }
        }) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null) }
        Spacer(Modifier.width(8.dp))
        Text("Mensagem de voz", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    isRecording: Boolean,
    isEditing: Boolean = false,
    replyingToMessage: Message? = null,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCancelEdit: () -> Unit = {},
    onCancelReply: () -> Unit = {},
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onLocationClick: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    onVoiceCancel: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Column {
            if (isEditing) {
                Row(
                    Modifier.fillMaxWidth().background(Color.LightGray.copy(0.3f)).padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Editando mensagem", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    IconButton(onClick = onCancelEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                    }
                }
            }
            if (replyingToMessage != null) {
                Row(
                    Modifier.fillMaxWidth().background(Color.LightGray.copy(0.3f)).padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(replyingToMessage.senderName.ifBlank { "Você" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        Text(replyingToMessage.text.ifBlank { "📷 Mídia" }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    Icon(Icons.Default.Mic, null, tint = Color.Red, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("Gravando...", modifier = Modifier.weight(1f), color = Color.Red)
                    TextButton(onClick = onVoiceCancel) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                    IconButton(onClick = onVoiceStop) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = onAttachClick) { Icon(Icons.Default.Add, null) }
                    TextField(
                        value = messageText,
                        onValueChange = onMessageChange,
                        placeholder = { Text("Mensagem") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (messageText.isNotBlank()) {
                                    onSendMessage()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    if (messageText.isBlank()) {
                        IconButton(onClick = onCameraClick) { Icon(Icons.Default.CameraAlt, null) }
                        IconButton(onClick = onVoiceStart) { Icon(Icons.Default.Mic, null) }
                    } else {
                        IconButton(onClick = onSendMessage) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentMenu(onImageClick: () -> Unit, onLocationClick: () -> Unit, onFileClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = {}, title = { Text("Anexar") }, text = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { IconButton(onClick = { onImageClick(); onDismiss() }) { Icon(Icons.Default.Image, null) }; Text("Galeria", style = MaterialTheme.typography.labelSmall) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { IconButton(onClick = { onLocationClick(); onDismiss() }) { Icon(Icons.Default.LocationOn, null) }; Text("Localização", style = MaterialTheme.typography.labelSmall) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { IconButton(onClick = { onFileClick(); onDismiss() }) { Icon(Icons.Default.InsertDriveFile, null) }; Text("Documento", style = MaterialTheme.typography.labelSmall) }
        }
    })
}

@Composable
fun ContactInfoDialog(
    user: User,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto
                Surface(
                    modifier = Modifier.size(100.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (user.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                user.displayName.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome
                Text(
                    user.displayName.ifBlank { "Sem nome" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Status
                val statusColor = when (user.status) {
                    UserStatus.ONLINE -> StatusOnline
                    UserStatus.BUSY -> StatusBusy
                    else -> StatusOffline
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = statusColor
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        user.status.toDisplayString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Email
                if (user.email.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(user.email, style = MaterialTheme.typography.bodyMedium)
                            Text("Email", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Telefone
                if (user.phone.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(user.phone, style = MaterialTheme.typography.bodyMedium)
                            Text("Telefone", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Recado
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(user.about.ifBlank { "Olá! Estou usando o ZapZap." }, style = MaterialTheme.typography.bodyMedium)
                        Text("Recado", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Indicador de criptografia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp), tint = Color(0xFF34B7F1))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Criptografia ativa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Mensagens protegidas com AES-128", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Fechar")
                }
            }
        }
    }
}

@Composable
fun PinnedMessageBar(message: Message, onUnpin: () -> Unit) {
    Surface(color = Color.LightGray.copy(0.2f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PushPin, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(message.text.ifBlank { "Mídia" }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis); IconButton(onClick = onUnpin) { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
        }
    }
}

