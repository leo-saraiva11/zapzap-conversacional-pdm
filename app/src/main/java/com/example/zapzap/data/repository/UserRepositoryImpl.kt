package com.example.zapzap.data.repository

import android.net.Uri
import com.example.zapzap.data.local.dao.UserDao
import com.example.zapzap.data.mapper.UserMapper
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus
import com.example.zapzap.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de usuários.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val userDao: UserDao
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = UserMapper.fromFirestore(snapshot.data ?: emptyMap(), userId)
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid)
                .update(UserMapper.toFirestore(user))
                .await()
            userDao.insertUser(UserMapper.toEntity(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePhoto(userId: String, photoUri: Uri): Result<String> {
        return try {
            val fileName = "profile_${userId}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("profile_photos/$fileName")
            ref.putFile(photoUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            firestore.collection("users").document(userId)
                .update("photoUrl", downloadUrl)
                .await()

            userDao.getUserByIdOnce(userId)?.let { entity ->
                userDao.updateUser(entity.copy(photoUrl = downloadUrl))
            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStatus(userId: String, status: UserStatus): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "lastSeen" to now
                    )
                ).await()
            userDao.updateStatus(userId, status.name, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                Result.success(UserMapper.fromFirestore(doc.data ?: emptyMap(), userId))
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
