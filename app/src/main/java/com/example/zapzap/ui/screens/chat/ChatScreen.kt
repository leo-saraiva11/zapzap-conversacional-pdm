package com.example.zapzap.ui.screens.chat

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.ui.theme.*
import com.example.zapzap.ui.util.DateFormatUtils
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de chat principal.
 * Exibe mensagens em tempo real com bolhas, barra de fixados,
 * busca por palavra-chave, e botões para câmera/GPS/microfone.
 */
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

    // Inicializar com ID da conversa
    LaunchedEffect(conversationId) {
        viewModel.setConversationId(conversationId)
    }

    // Auto-scroll ao receber novas mensagens
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Launchers para câmera e galeria
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.sendMediaMessage(photoUri!!, MessageType.IMAGE)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.sendMediaMessage(it, MessageType.IMAGE) }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.sendMediaMessage(it, MessageType.FILE) }
    }

    // Permissões
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            photoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Obter localização
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.sendLocationMessage(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Permissão negada
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearching) {
                // Barra de busca (Requisito Especial 14)
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Buscar mensagens...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fechar busca")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                conversationName.ifBlank { "Chat" },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mensagem fixada (Requisito Especial 13)
            pinnedMessage?.let { pinned ->
                PinnedMessageBar(
                    message = pinned,
                    onUnpin = { viewModel.togglePinMessage(pinned.id, true) }
                )
            }

            // Lista de mensagens
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Agrupar por data
                val groupedMessages = messages.groupBy {
                    DateFormatUtils.formatDate(it.timestamp)
                }

                groupedMessages.forEach { (date, msgs) ->
                    // Header de data
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = date,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(msgs, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == viewModel.currentUserId,
                            searchQuery = searchQuery,
                            onLongPress = {
                                viewModel.togglePinMessage(message.id, message.isPinned)
                            }
                        )
                    }
                }
            }

            // Barra de input
            ChatInputBar(
                messageText = messageText,
                isRecording = isRecording,
                onMessageChange = { viewModel.updateMessageText(it) },
                onSendMessage = { viewModel.sendTextMessage() },
                onAttachClick = { showAttachMenu = !showAttachMenu },
                onCameraClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onLocationClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onVoiceStart = {
                    viewModel.setRecording(true)
                    // TODO: Implementar gravação de áudio com MediaRecorder
                },
                onVoiceStop = {
                    viewModel.setRecording(false)
                    // TODO: Parar gravação e enviar
                }
            )

            // Menu de anexos
            if (showAttachMenu) {
                AttachmentMenu(
                    onImageClick = { galleryLauncher.launch("image/*") },
                    onVideoClick = { galleryLauncher.launch("video/*") },
                    onFileClick = { fileLauncher.launch("*/*") },
                    onDismiss = { showAttachMenu = false }
                )
            }
        }
    }
}

/**
 * Barra de mensagem fixada (Requisito Especial 13).
 */
@Composable
fun PinnedMessageBar(
    message: Message,
    onUnpin: () -> Unit
) {
    Surface(
        color = AccentPin.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PushPin,
                contentDescription = "Mensagem fixada",
                tint = AccentPin,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mensagem fixada",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentPin
                )
                Text(
                    message.text.ifBlank { "📎 Mídia" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onUnpin, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Desafixar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Bolha de mensagem com suporte a highlight de busca.
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    searchQuery: String = "",
    onLongPress: () -> Unit = {}
) {
    val bubbleColor = if (isOwnMessage) BubbleSent else BubbleReceived
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isOwnMessage) 48.dp else 0.dp,
                end = if (isOwnMessage) 0.dp else 48.dp
            ),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isOwnMessage) 12.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 12.dp
            ),
            color = bubbleColor,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            }
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Nome do remetente (em grupos)
                if (!isOwnMessage && message.senderName.isNotBlank()) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Conteúdo da mensagem
                when (message.type) {
                    MessageType.TEXT -> {
                        // Highlight de busca (Requisito Especial 14)
                        if (searchQuery.isNotBlank() && message.text.contains(searchQuery, ignoreCase = true)) {
                            val annotatedString = buildAnnotatedString {
                                val text = message.text
                                var start = 0
                                val lowerText = text.lowercase()
                                val lowerQuery = searchQuery.lowercase()

                                while (start < text.length) {
                                    val index = lowerText.indexOf(lowerQuery, start)
                                    if (index == -1) {
                                        append(text.substring(start))
                                        break
                                    }
                                    append(text.substring(start, index))
                                    withStyle(SpanStyle(background = AccentSearch, fontWeight = FontWeight.Bold)) {
                                        append(text.substring(index, index + searchQuery.length))
                                    }
                                    start = index + searchQuery.length
                                }
                            }
                            Text(text = annotatedString, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    MessageType.IMAGE -> {
                        Text("📷 Imagem", style = MaterialTheme.typography.bodyMedium)
                        // TODO: Coil AsyncImage com message.mediaUrl
                    }
                    MessageType.VIDEO -> {
                        Text("🎥 Vídeo", style = MaterialTheme.typography.bodyMedium)
                    }
                    MessageType.AUDIO -> {
                        Text("🎤 Mensagem de voz", style = MaterialTheme.typography.bodyMedium)
                        // TODO: AudioPlayer composable
                    }
                    MessageType.LOCATION -> {
                        Text("📍 Localização", style = MaterialTheme.typography.bodyMedium)
                        // TODO: Mini-mapa com Google Maps Compose
                        Text(
                            "Lat: ${"%.4f".format(message.latitude)}, Lng: ${"%.4f".format(message.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MessageType.FILE -> {
                        Text("📎 Arquivo", style = MaterialTheme.typography.bodyMedium)
                    }
                    MessageType.STICKER -> {
                        Text(message.text, fontSize = 48.sp)
                    }
                }

                // Ícone de fixado
                if (message.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Fixada",
                        modifier = Modifier.size(12.dp),
                        tint = AccentPin
                    )
                }

                // Timestamp e status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatUtils.formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val statusIcon = when (message.status) {
                            MessageStatus.SENDING -> "🕐"
                            MessageStatus.SENT -> "✓"
                            MessageStatus.DELIVERED -> "✓✓"
                            MessageStatus.READ -> "✓✓"
                        }
                        Text(
                            text = statusIcon,
                            fontSize = 10.sp,
                            color = if (message.status == MessageStatus.READ) TickRead else TickDefault
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de input do chat com botões de anexo, câmera, localização e microfone.
 */
@Composable
fun ChatInputBar(
    messageText: String,
    isRecording: Boolean,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onLocationClick: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Campo de texto
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    // Botão de anexo
                    IconButton(onClick = onAttachClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Anexar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = onMessageChange,
                        placeholder = { Text("Mensagem") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    // Câmera
                    IconButton(onClick = onCameraClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Câmera",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botão de enviar ou gravar voz
            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage()
                    } else {
                        // TODO: Toggle gravação de voz
                        if (isRecording) onVoiceStop() else onVoiceStart()
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    if (messageText.isNotBlank()) Icons.AutoMirrored.Filled.Send
                    else if (isRecording) Icons.Default.Stop
                    else Icons.Default.Mic,
                    contentDescription = if (messageText.isNotBlank()) "Enviar" else "Gravar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Botão de localização flutuante (acessível via menu de anexo)
    }
}

/**
 * Menu de anexos.
 */
@Composable
fun AttachmentMenu(
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onFileClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentButton(icon = Icons.Default.Image, label = "Imagem", onClick = onImageClick)
            AttachmentButton(icon = Icons.Default.Videocam, label = "Vídeo", onClick = onVideoClick)
            AttachmentButton(icon = Icons.Default.InsertDriveFile, label = "Arquivo", onClick = onFileClick)
        }
    }
}

@Composable
fun AttachmentButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Cria um URI temporário para foto da câmera.
 */
fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File.createTempFile("IMG_${timeStamp}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}
