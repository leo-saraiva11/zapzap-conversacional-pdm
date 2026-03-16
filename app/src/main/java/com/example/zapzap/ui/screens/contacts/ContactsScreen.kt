package com.example.zapzap.ui.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapzap.domain.model.Contact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.filteredContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contatos", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.importDeviceContacts() }) {
                        Icon(Icons.Default.ContactPhone, contentDescription = "Importar contatos",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Adicionar contato",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Busca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Buscar contato...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum contato", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Adicione contatos para conversar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItem(
                            contact = contact,
                            onClick = {
                                scope.launch {
                                    val result = viewModel.startConversation(contact.userId)
                                    result?.let { (convId, name) ->
                                        onNavigateToChat(convId, name.ifBlank { contact.displayName })
                                    }
                                }
                            },
                            onDelete = { viewModel.removeContact(contact.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog para adicionar contato
    if (showAddDialog) {
        AddContactDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(contact.displayName, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(
                contact.phone.ifBlank { contact.email },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
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
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Remover") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
}

@Composable
private fun AddContactDialog(
    viewModel: ContactsViewModel,
    onDismiss: () -> Unit
) {
    val query by viewModel.addContactQuery.collectAsState()
    val foundContact by viewModel.foundContact.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Contato") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateAddContactQuery(it) },
                    label = { Text("Email ou telefone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.searchUserToAdd() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank()
                ) {
                    Text("Buscar")
                }
                foundContact?.let { contact ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(contact.displayName) },
                            supportingContent = { Text(contact.email.ifBlank { contact.phone }) },
                            trailingContent = {
                                IconButton(onClick = {
                                    viewModel.addContact(contact)
                                    onDismiss()
                                }) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Adicionar")
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
