package com.example.zapzap.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                                val result = viewModel.createGroup()
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
                .padding(paddingValues)
        ) {
            // Nome do grupo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Group, contentDescription = null,
                            modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { viewModel.updateGroupName(it) },
                    label = { Text("Nome do grupo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    val isSelected = selectedContacts.contains(contact.userId)
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
                                onCheckedChange = { viewModel.toggleContactSelection(contact.userId) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
