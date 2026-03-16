package com.example.zapzap.domain.repository

import com.example.zapzap.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de grupos.
 */
interface GroupRepository {
    /** Cria um novo grupo */
    suspend fun createGroup(
        name: String,
        photoUrl: String,
        memberIds: List<String>,
        createdBy: String
    ): Result<Conversation>

    /** Edita informações do grupo */
    suspend fun editGroup(groupId: String, name: String, photoUrl: String): Result<Unit>

    /** Adiciona membros ao grupo */
    suspend fun addMembers(groupId: String, memberIds: List<String>): Result<Unit>

    /** Remove membro do grupo */
    suspend fun removeMember(groupId: String, memberId: String): Result<Unit>

    /** Lista membros do grupo */
    fun getGroupMembers(groupId: String): Flow<List<com.example.zapzap.domain.model.User>>

    /** Sair do grupo */
    suspend fun leaveGroup(groupId: String, userId: String): Result<Unit>
}
