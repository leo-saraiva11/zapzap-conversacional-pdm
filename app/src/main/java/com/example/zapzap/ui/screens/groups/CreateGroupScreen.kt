package com.example.zapzap.ui.screens.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (String, String) -> Unit,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val groupName by viewModel.groupName.collectAsState()
    val selectedContacts by viewModel.selectedContacts.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> photoUri = uri }

    var coverUri by remember { mutableStateOf<Uri?>(null) }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> coverUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Grupo", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val result = viewModel.createGroup(photoUri, coverUri)
                                result?.let { (id, name) -> onGroupCreated(id, name) }
                            }
                        },
                        enabled = groupName.isNotBlank() && selectedContacts.isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Criar",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
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
        ) {
            // Header: Capa e Foto de Perfil
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                // Background da Capa
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { coverPickerLauncher.launch("image/*") },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (coverUri != null) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Capa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, null)
                                Text("Adicionar Capa", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Foto de Perfil sobreposta
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 0.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null,
                                modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nome do grupo
            OutlinedTextField(
                value = groupName,
                onValueChange = { viewModel.updateGroupName(it) },
                label = { Text("Nome do grupo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            // Header
            Text(
                text = "Selecionar participantes (${selectedContacts.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Lista de contatos
            LazyColumn {
                items(contacts, key = { it.id }) { contact ->
                    val isSelected = selectedContacts.contains(contact.id)
                    ListItem(
                        headlineContent = { Text(contact.displayName) },
                        supportingContent = { Text(contact.phone.ifBlank { contact.email }) },
                        leadingContent = {
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        contact.displayName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleContactSelection(contact.id) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
