package com.example.zapzap.domain.repository

import com.example.zapzap.domain.model.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de contatos.
 */
interface ContactRepository {
    /** Observa a lista de contatos do usuário */
    fun getContacts(userId: String): Flow<List<Contact>>

    /** Adiciona um contato */
    suspend fun addContact(userId: String, contact: Contact): Result<Unit>

    /** Remove um contato */
    suspend fun removeContact(userId: String, contactId: String): Result<Unit>

    /** Importa contatos do dispositivo */
    suspend fun importDeviceContacts(): Result<List<Contact>>

    /** Busca contatos por nome ou telefone */
    fun searchContacts(userId: String, query: String): Flow<List<Contact>>

    /** Busca usuário por email ou telefone */
    suspend fun findUserByEmailOrPhone(query: String): Result<Contact?>
}
