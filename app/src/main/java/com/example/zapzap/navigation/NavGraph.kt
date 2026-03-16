package com.example.zapzap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zapzap.ui.screens.auth.AuthViewModel
import com.example.zapzap.ui.screens.auth.LoginScreen
import com.example.zapzap.ui.screens.auth.RegisterScreen
import com.example.zapzap.ui.screens.auth.ResetPasswordScreen
import com.example.zapzap.ui.screens.chat.ChatScreen
import com.example.zapzap.ui.screens.contacts.ContactsScreen
import com.example.zapzap.ui.screens.conversations.ConversationsScreen
import com.example.zapzap.ui.screens.groups.CreateGroupScreen
import com.example.zapzap.ui.screens.profile.ProfileScreen

/**
 * Grafo de navegação principal do ZapZap.
 * Decide a tela inicial baseado no estado de autenticação.
 */
@Composable
fun ZapZapNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) Screen.Conversations.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToResetPassword = { navController.navigate(Screen.ResetPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Conversations.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Conversations.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        // Conversas
        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onNavigateToChat = { conversationId, name ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, name))
                },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToCreateGroup = { navController.navigate(Screen.CreateGroup.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Chat
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("conversationName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val conversationName = backStackEntry.arguments?.getString("conversationName") ?: ""
            ChatScreen(
                conversationId = conversationId,
                conversationName = conversationName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Perfil
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Contatos
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { conversationId, name ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, name))
                }
            )
        }

        // Criar Grupo
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { conversationId, name ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, name)) {
                        popUpTo(Screen.Conversations.route)
                    }
                }
            )
        }
    }
}
