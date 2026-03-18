package com.example.zapzap.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.ui.theme.StatusBusy
import com.example.zapzap.ui.theme.StatusOffline
import com.example.zapzap.ui.theme.StatusOnline
import com.example.zapzap.ui.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.userProfile.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var editName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var editAbout by remember(user) { mutableStateOf(user?.about ?: "") }
    var editStatus by remember(user) { mutableStateOf(user?.status ?: UserStatus.ONLINE) }

    val context = LocalContext.current
    var showPhotoOptions by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.updateProfilePhoto(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isEditing) {
                            viewModel.updateProfile(editName, editAbout, editStatus)
                        } else {
                            viewModel.toggleEditing()
                        }
                    }) {
                        Icon(
                            if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Salvar" else "Editar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isEditing) {
                            showPhotoOptions = true
                        }
                    },
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (user?.photoUrl.isNullOrEmpty()) {
                        Text(
                            text = user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displayMedium
                        )
                    } else {
                        coil.compose.SubcomposeAsyncImage(
                            model = user?.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            loading = { CircularProgressIndicator(modifier = Modifier.padding(32.dp)) },
                            error = {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                }
                            }
                        )
                    }
                    if (isEditing) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Mudar foto",
                                    tint = Color.White)
                            }
                        }
                    }
                }
            }

            if (showPhotoOptions) {
                AlertDialog(
                    onDismissRequest = { showPhotoOptions = false },
                    title = { Text("Mudar foto de perfil") },
                    text = {
                        Column {
                            ListItem(
                                headlineContent = { Text("Câmera") },
                                leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                                modifier = Modifier.clickable {
                                    val uri = ImageUtils.createTempImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                    showPhotoOptions = false
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Galeria") },
                                leadingContent = { Icon(Icons.Default.Image, null) },
                                modifier = Modifier.clickable {
                                    galleryLauncher.launch("image/*")
                                    showPhotoOptions = false
                                }
                            )
                        }
                    },
                    confirmButton = {}
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Nome
            if (isEditing) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nome") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = user?.displayName ?: "Sem nome",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            ListItem(
                headlineContent = { Text(user?.email ?: "Sem email") },
                leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
                supportingContent = { Text("Email") }
            )

            // Telefone
            if (!user?.phone.isNullOrBlank()) {
                ListItem(
                    headlineContent = { Text(user?.phone ?: "") },
                    leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) },
                    supportingContent = { Text("Telefone") }
                )
            }

            HorizontalDivider()

            // Recado / About
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editAbout,
                    onValueChange = { editAbout = it },
                    label = { Text("Recado") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ListItem(
                    headlineContent = { Text(user?.about ?: "Olá! Estou usando o ZapZap.") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    supportingContent = { Text("Recado") }
                )
            }

            // Status
            ListItem(
                headlineContent = {
                    if (isEditing) {
                        Row {
                            UserStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = editStatus == status,
                                    onClick = { editStatus = status },
                                    label = { Text(status.toDisplayString()) },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (status) {
                                            UserStatus.ONLINE -> StatusOnline
                                            UserStatus.OFFLINE -> StatusOffline
                                            UserStatus.BUSY -> StatusBusy
                                        }
                                    )
                                )
                            }
                        }
                    } else {
                        Text(user?.status?.toDisplayString() ?: "Offline")
                    }
                },
                leadingContent = {
                    val statusColor = when (user?.status) {
                        UserStatus.ONLINE -> StatusOnline
                        UserStatus.BUSY -> StatusBusy
                        else -> StatusOffline
                    }
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = statusColor
                    ) {}
                },
                supportingContent = { Text("Status") }
            )
        }
    }
}
