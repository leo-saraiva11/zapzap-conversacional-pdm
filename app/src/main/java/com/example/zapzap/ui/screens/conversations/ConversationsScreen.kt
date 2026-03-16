package com.example.zapzap.ui.screens.conversations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.ui.util.DateFormatUtils

/**
 * Tela de lista de conversas (tela principal após login).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel()
) {
    val conversations by viewModel.filteredConversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClose = {
                        showSearch = false
                        viewModel.updateSearchQuery("")
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "ZapZap",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Novo Grupo") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToCreateGroup()
                                },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Sair") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToContacts,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "Nova conversa",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (conversations.isEmpty()) {
            // Lista vazia
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nenhuma conversa ainda",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Toque no botão + para iniciar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(conversations, key = { it.id }) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = {
                            onNavigateToChat(conversation.id, conversation.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar conversa...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)) },
            leadingIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Fechar busca",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (conversation.type == ConversationType.GROUP) "👥"
                        else conversation.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name.ifBlank { "Conversa" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = DateFormatUtils.formatConversationTime(conversation.lastMessageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conversation.unreadCount > 0)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage.ifBlank { "Nenhuma mensagem" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                conversation.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 80.dp))
}
