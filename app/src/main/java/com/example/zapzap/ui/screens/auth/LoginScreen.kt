package com.example.zapzap.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.zapzap.ui.theme.PrimaryLight

/**
 * Tela de login do ZapZap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    // Reagir ao sucesso do login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo e título
        Text(
            text = "💬",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ZapZap",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Mensagens instantâneas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Login Switch
        var isPhoneLogin by remember { mutableStateOf(false) }
        var phoneNumber by remember { mutableStateOf("") }
        var verificationCode by remember { mutableStateOf("") }
        var showVerificationCodeField by remember { mutableStateOf(false) }
        var currentVerificationId by remember { mutableStateOf("") }

        LaunchedEffect(authState) {
            if (authState is AuthState.VerificationCodeSent) {
                currentVerificationId = (authState as AuthState.VerificationCodeSent).verificationId
                showVerificationCodeField = true
            }
        }

        TabRow(selectedTabIndex = if (isPhoneLogin) 1 else 0, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Tab(selected = !isPhoneLogin, onClick = { isPhoneLogin = false; viewModel.clearError() }) {
                paddingValues -> Text("Email", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = isPhoneLogin, onClick = { isPhoneLogin = true; viewModel.clearError() }) {
                paddingValues -> Text("Telefone", modifier = Modifier.padding(16.dp))
            }
        }

        if (!isPhoneLogin) {
            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de senha
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Esconder senha" else "Mostrar senha"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Esqueceu a senha
            TextButton(
                onClick = onNavigateToResetPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Esqueceu a senha?")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de login Email
            Button(
                onClick = { viewModel.loginWithEmail(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Entrar", fontSize = 16.sp)
                }
            }
        } else {
            // Login por Telefone
            if (!showVerificationCodeField) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefone (com +55)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+55 11 99999-9999") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.sendPhoneVerification(phoneNumber) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = phoneNumber.length >= 10 && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Enviar Código SMS", fontSize = 16.sp)
                    }
                }
            } else {
                Text("Enviamos um código para $phoneNumber", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Código de 6 dígitos") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.verifyPhoneCode(currentVerificationId, verificationCode) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = verificationCode.length == 6 && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verificar Código", fontSize = 16.sp)
                    }
                }

                TextButton(onClick = { showVerificationCodeField = false }) {
                    Text("Voltar / Tentar outro número")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  ou  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login com Google
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val credentialManager = androidx.credentials.CredentialManager.create(context)
                        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(com.example.zapzap.R.string.default_web_client_id))
                            .setAutoSelectEnabled(true)
                            .build()
                            
                        val request = androidx.credentials.GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                            
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        
                        val credential = result.credential
                        if (credential is androidx.credentials.CustomCredential &&
                            credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                
                            val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                            viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Entrar com Google", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Link para cadastro
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Não tem conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "Criar conta",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Mensagem de erro
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = (authState as AuthState.Error).message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
