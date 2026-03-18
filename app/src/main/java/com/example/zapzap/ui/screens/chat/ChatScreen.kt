package com.example.zapzap.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.zapzap.domain.model.Message
import com.example.zapzap.domain.model.MessageStatus
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.ui.theme.*
import com.example.zapzap.ui.util.DateFormatUtils
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

    LaunchedEffect(conversationId) {
        viewModel.setConversationId(conversationId)
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

    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            viewModel.sendMediaMessage(photoUri!!, MessageType.IMAGE)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMediaMessage(it, MessageType.IMAGE) }
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMediaMessage(it, MessageType.FILE) }
    }

    // Permissões
    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startRecording(context)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            photoUri = uri
            cameraLauncher.launch(uri)
        }
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

    // Dialog: Info do Grupo
    if (showGroupInfo) {
        Dialog(onDismissRequest = { showGroupInfo = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
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
                        Text(conversationName.ifBlank { "Carregando..." }, fontWeight = FontWeight.Bold, color = Color.White)
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
                        IconButton(onClick = { 
                            showGroupInfo = true 
                            viewModel.fetchGroupMembers()
                        }) {
                            Icon(Icons.Default.Group, contentDescription = "Ver Grupo", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            pinnedMessage?.let { PinnedMessageBar(it, onUnpin = { viewModel.togglePinMessage(it.id, true) }) }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val grouped = messages.groupBy { DateFormatUtils.formatDate(it.timestamp) }
                grouped.forEach { (date, msgs) ->
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), Alignment.Center) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color.LightGray.copy(0.3f)) {
                                Text(date, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    items(msgs, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isOwnMessage = msg.senderId == viewModel.currentUserId,
                            searchQuery = searchQuery,
                            onLongPress = { viewModel.togglePinMessage(msg.id, msg.isPinned) },
                            onImageClick = { fullScreenImageUrl = it }
                        )
                    }
                }
            }

            ChatInputBar(
                messageText = messageText,
                isRecording = isRecording,
                onMessageChange = { viewModel.updateMessageText(it) },
                onSendMessage = { viewModel.sendTextMessage() },
                onAttachClick = { showAttachMenu = !showAttachMenu },
                onCameraClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onLocationClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                onVoiceStart = { voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onVoiceStop = { viewModel.stopRecording(context) }
            )

            if (showAttachMenu) {
                AttachmentMenu(onImageClick = { galleryLauncher.launch("image/*") }, onVideoClick = { galleryLauncher.launch("video/*") }, onFileClick = { fileLauncher.launch("*/*") }, onDismiss = { showAttachMenu = false })
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    searchQuery: String = "",
    onLongPress: () -> Unit = {},
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val bubbleColor = if (isOwnMessage) BubbleSent else BubbleReceived
    
    Box(modifier = Modifier.fillMaxWidth().padding(start = if (isOwnMessage) 64.dp else 0.dp, end = if (isOwnMessage) 0.dp else 64.dp), contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(12.dp), color = bubbleColor, modifier = Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }) {
            Column(modifier = Modifier.padding(8.dp)) {
                when (message.type) {
                    MessageType.TEXT -> Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                    MessageType.IMAGE -> AsyncImage(model = message.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(message.mediaUrl) }, contentScale = ContentScale.Crop)
                    MessageType.AUDIO -> AudioPlayerMessage(url = message.mediaUrl)
                    MessageType.LOCATION -> Text("📍 Localização", Modifier.clickable { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${message.latitude},${message.longitude}?q=${message.latitude},${message.longitude}")); context.startActivity(intent) }, color = Color.Blue, fontWeight = FontWeight.Bold)
                    MessageType.FILE -> Row(Modifier.clickable { val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl)); context.startActivity(intent) }, verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.InsertDriveFile, null); Spacer(Modifier.width(8.dp)); Text("Arquivo", style = MaterialTheme.typography.bodyMedium) }
                    else -> Text(message.text)
                }
                Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                    Text(DateFormatUtils.formatTime(message.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    if (isOwnMessage) { 
                        Spacer(Modifier.width(4.dp))
                        val statusIcon = when (message.status) {
                            MessageStatus.SENDING -> Icons.Default.Schedule
                            MessageStatus.SENT -> Icons.Default.Check
                            MessageStatus.DELIVERED -> Icons.Default.DoneAll
                            MessageStatus.READ -> Icons.Default.DoneAll
                        }
                        // Azul se foi lida. Cinza escuro se foi apenas entregue.
                        val statusColor = when (message.status) {
                            MessageStatus.READ -> Color(0xFF34B7F1) // Azul tipo WhatsApp
                            MessageStatus.DELIVERED -> Color.DarkGray
                            else -> Color.Gray
                        }
                        Icon(statusIcon, null, Modifier.size(16.dp), tint = statusColor) 
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
fun ChatInputBar(messageText: String, isRecording: Boolean, onMessageChange: (String) -> Unit, onSendMessage: () -> Unit, onAttachClick: () -> Unit, onCameraClick: () -> Unit, onLocationClick: () -> Unit, onVoiceStart: () -> Unit, onVoiceStop: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAttachClick) { Icon(Icons.Default.Add, null) }
            TextField(value = messageText, onValueChange = onMessageChange, placeholder = { Text("Mensagem") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            if (messageText.isBlank()) {
                IconButton(onClick = onCameraClick) { Icon(Icons.Default.CameraAlt, null) }
                IconButton(onClick = { if (isRecording) onVoiceStop() else onVoiceStart() }, colors = IconButtonDefaults.iconButtonColors(contentColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary)) { Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, null) }
            } else {
                IconButton(onClick = onSendMessage) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
fun AttachmentMenu(onImageClick: () -> Unit, onVideoClick: () -> Unit, onFileClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = {}, title = { Text("Anexar") }, text = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { IconButton(onClick = { onImageClick(); onDismiss() }) { Icon(Icons.Default.Image, null) }; Text("Galeria", style = MaterialTheme.typography.labelSmall) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { IconButton(onClick = { onFileClick(); onDismiss() }) { Icon(Icons.Default.InsertDriveFile, null) }; Text("Documento", style = MaterialTheme.typography.labelSmall) }
        }
    })
}

@Composable
fun PinnedMessageBar(message: Message, onUnpin: () -> Unit) {
    Surface(color = Color.LightGray.copy(0.2f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PushPin, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(message.text.ifBlank { "Mídia" }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis); IconButton(onClick = onUnpin) { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
        }
    }
}

fun createTempImageUri(context: Context): Uri {
    val file = File.createTempFile("IMG_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
