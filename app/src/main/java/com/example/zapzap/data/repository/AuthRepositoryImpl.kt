package com.example.zapzap.data.repository

import android.util.Log
import com.example.zapzap.data.local.dao.UserDao
import com.example.zapzap.data.mapper.UserMapper
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    private val TAG = "AuthRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private fun updateDeviceFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            if (token != null) {
                repositoryScope.launch {
                    try {
                        firestore.collection("users").document(uid).update("fcmToken", token).await()
                    } catch (e: Exception) { Log.e(TAG, "Erro token Firestore: ${e.message}") }
                }
            }
        }
    }

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val basicUser = User(
                    uid = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: ""
                )
                trySend(basicUser)

                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val user = UserMapper.fromFirestore(snapshot.data ?: emptyMap(), firebaseUser.uid)
                            trySend(user)
                        }
                    }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Iniciando signInWithEmailAndPassword para $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = User(uid = firebaseUser.uid, displayName = firebaseUser.displayName ?: "", email = firebaseUser.email ?: "")
                    
                    repositoryScope.launch {
                        try {
                            updateUserStatus(firebaseUser.uid, UserStatus.ONLINE)
                            updateDeviceFcmToken(firebaseUser.uid)
                        } catch (e: Exception) { Log.e(TAG, "Erro status: ${e.message}") }
                    }
                    
                    Log.d(TAG, "Login bem sucedido, retornando resultado")
                    if (continuation.isActive) continuation.resume(Result.success(user))
                } else {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("Usuário nulo")))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Falha login: ${exception.message}")
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Iniciando createUserWithEmailAndPassword para $email")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = User(uid = firebaseUser.uid, displayName = name, email = email, status = UserStatus.ONLINE)
                    
                    repositoryScope.launch {
                        try {
                            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                            firebaseUser.updateProfile(profileUpdates).await()
                            saveUserToFirestore(user)
                            userDao.insertUser(UserMapper.toEntity(user))
                            updateDeviceFcmToken(firebaseUser.uid)
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro background registro: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "Registro bem sucedido, retornando resultado")
                    if (continuation.isActive) continuation.resume(Result.success(user))
                } else {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("Erro ao criar usuário")))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Falha registro: ${exception.message}")
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    override suspend fun logout(): Result<Unit> {
        auth.signOut()
        return Result.success(Unit)
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection("users").document(user.uid).set(UserMapper.toFirestore(user)).await()
        } catch (e: Exception) { Log.e(TAG, "Erro firestore: ${e.message}") }
    }

    private suspend fun updateUserStatus(uid: String, status: UserStatus) {
        try {
            firestore.collection("users").document(uid).update("status", status.name).await()
        } catch (e: Exception) { Log.e(TAG, "Erro status: ${e.message}") }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> = Result.failure(Exception("Pendente"))
    override suspend fun sendPhoneVerification(phoneNumber: String): Result<String> = Result.failure(Exception("Pendente"))
    override suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User> = Result.failure(Exception("Pendente"))
    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)
}
