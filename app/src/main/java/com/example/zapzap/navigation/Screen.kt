package com.example.zapzap.navigation

/**
 * Sealed class definindo todas as rotas de navegação do app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("reset_password")
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{conversationId}/{conversationName}") {
        fun createRoute(conversationId: String, conversationName: String) =
            "chat/$conversationId/$conversationName"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Contacts : Screen("contacts")
    object CreateGroup : Screen("create_group")
    object GroupInfo : Screen("group_info/{groupId}") {
        fun createRoute(groupId: String) = "group_info/$groupId"
    }
}
