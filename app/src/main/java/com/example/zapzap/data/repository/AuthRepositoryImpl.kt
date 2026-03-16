package com.example.zapzap.data.repository

import com.example.zapzap.data.local.dao.UserDao
import com.example.zapzap.data.mapper.UserMapper
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de autenticação.
 * Usa Firebase Auth para login/cadastro e Firestore para dados do perfil.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val user = UserMapper.fromFirestore(
                                snapshot.data ?: emptyMap(),
                                firebaseUser.uid
                            )
                            trySend(user)
                        } else {
                            trySend(null)
                        }
                    }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Usuário não encontrado")
            val user = getUserFromFirestore(firebaseUser.uid)
            // Atualizar status para online
            updateUserStatus(firebaseUser.uid, UserStatus.ONLINE)
            // Salvar no cache local
            userDao.insertUser(UserMapper.toEntity(user))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Usuário não encontrado")

            // Verificar se o usuário já existe no Firestore
            val existingUser = try {
                getUserFromFirestore(firebaseUser.uid)
            } catch (e: Exception) {
                null
            }

            val user = existingUser ?: User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                status = UserStatus.ONLINE
            )

            // Salvar/atualizar no Firestore
            saveUserToFirestore(user.copy(status = UserStatus.ONLINE))
            userDao.insertUser(UserMapper.toEntity(user))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPhoneVerification(phoneNumber: String): Result<String> {
        // Nota: verificação por telefone requer Activity para callbacks
        // A implementação completa será feita na ViewModel com PhoneAuthProvider
        return Result.failure(UnsupportedOperationException("Use PhoneAuthProvider na Activity"))
    }

    override suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Usuário não encontrado")

            val user = User(
                uid = firebaseUser.uid,
                phone = firebaseUser.phoneNumber ?: "",
                status = UserStatus.ONLINE
            )

            saveUserToFirestore(user)
            userDao.insertUser(UserMapper.toEntity(user))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Erro ao criar conta")

            // Atualizar perfil do Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val user = User(
                uid = firebaseUser.uid,
                displayName = name,
                email = email,
                status = UserStatus.ONLINE
            )

            // Salvar no Firestore
            saveUserToFirestore(user)
            // Cache local
            userDao.insertUser(UserMapper.toEntity(user))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                updateUserStatus(uid, UserStatus.OFFLINE)
            }
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getUserFromFirestore(uid: String): User {
        val doc = firestore.collection("users").document(uid).get().await()
        if (doc.exists()) {
            return UserMapper.fromFirestore(doc.data ?: emptyMap(), uid)
        }
        throw Exception("Usuário não encontrado no Firestore")
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.uid)
            .set(UserMapper.toFirestore(user))
            .await()
    }

    private suspend fun updateUserStatus(uid: String, status: UserStatus) {
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "status" to status.name,
                    "lastSeen" to System.currentTimeMillis()
                )
            ).await()
    }
}
