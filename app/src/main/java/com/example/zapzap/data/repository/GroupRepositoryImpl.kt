package com.example.zapzap.data.repository

import com.example.zapzap.data.mapper.ConversationMapper
import com.example.zapzap.data.mapper.UserMapper
import com.example.zapzap.domain.model.Conversation
import com.example.zapzap.domain.model.ConversationType
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.repository.GroupRepository
import com.google.firebase.firestore.FieldValue
import com.example.zapzap.domain.network.FcmService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de grupos.
 */
@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fcmService: FcmService
) : GroupRepository {

    private val conversationsCollection = firestore.collection("conversations")

    override suspend fun createGroup(
        name: String,
        photoUrl: String,
        memberIds: List<String>,
        createdBy: String
    ): Result<Conversation> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val conversation = Conversation(
                id = groupId,
                name = name,
                type = ConversationType.GROUP,
                photoUrl = photoUrl,
                participantIds = memberIds,
                createdAt = System.currentTimeMillis(),
                createdBy = createdBy
            )

            conversationsCollection.document(groupId)
                .set(ConversationMapper.toFirestore(conversation))
                .await()

            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editGroup(groupId: String, name: String, photoUrl: String): Result<Unit> {
        return try {
            conversationsCollection.document(groupId)
                .update(
                    mapOf(
                        "name" to name,
                        "photoUrl" to photoUrl
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMembers(groupId: String, memberIds: List<String>): Result<Unit> {
        return try {
            conversationsCollection.document(groupId)
                .update("participantIds", FieldValue.arrayUnion(*memberIds.toTypedArray()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMember(groupId: String, memberId: String): Result<Unit> {
        return try {
            conversationsCollection.document(groupId)
                .update("participantIds", FieldValue.arrayRemove(memberId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGroupMembers(groupId: String): Flow<List<User>> = callbackFlow {
        val listener = conversationsCollection.document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val participantIds = snapshot?.get("participantIds") as? List<String> ?: emptyList()

                if (participantIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Buscar dados de cada membro
                firestore.collection("users")
                    .whereIn("__name__", participantIds.take(30)) // Firestore limita whereIn a 30
                    .get()
                    .addOnSuccessListener { userDocs ->
                        val users = userDocs.documents.mapNotNull { doc ->
                            doc.data?.let { UserMapper.fromFirestore(it, doc.id) }
                        }
                        trySend(users)
                    }
                    .addOnFailureListener {
                        trySend(emptyList())
                    }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        return removeMember(groupId, userId)
    }
}
