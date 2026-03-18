package com.example.zapzap.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.example.zapzap.data.local.dao.ContactDao
import com.example.zapzap.data.local.entity.ContactEntity
import com.example.zapzap.domain.model.Contact
import com.example.zapzap.domain.repository.ContactRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de contatos.
 * Gerencia contatos com Firestore e importação do dispositivo.
 */
@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val contactDao: ContactDao
) : ContactRepository {

    override fun getContacts(userId: String): Flow<List<Contact>> {
        return contactDao.getContacts(userId).map { entities ->
            entities.map { entity ->
                Contact(
                    id = entity.id,
                    userId = entity.userId,
                    displayName = entity.displayName,
                    phone = entity.phone,
                    email = entity.email,
                    photoUrl = entity.photoUrl,
                    addedAt = entity.addedAt
                )
            }
        }
    }

    override suspend fun addContact(userId: String, contact: Contact): Result<Unit> {
        return try {
            val contactId = if (contact.id.isBlank()) UUID.randomUUID().toString() else contact.id

            // Salvar no Firestore
            firestore.collection("contacts")
                .document(userId)
                .collection("contactList")
                .document(contactId)
                .set(
                    mapOf(
                        "userId" to contact.userId,
                        "displayName" to contact.displayName,
                        "phone" to contact.phone,
                        "email" to contact.email,
                        "photoUrl" to contact.photoUrl,
                        "addedAt" to System.currentTimeMillis()
                    )
                ).await()

            // Cache local
            contactDao.insertContact(
                ContactEntity(
                    id = contactId,
                    userId = userId,
                    displayName = contact.displayName,
                    phone = contact.phone,
                    email = contact.email,
                    photoUrl = contact.photoUrl,
                    addedAt = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeContact(userId: String, contactId: String): Result<Unit> {
        return try {
            firestore.collection("contacts")
                .document(userId)
                .collection("contactList")
                .document(contactId)
                .delete()
                .await()

            contactDao.deleteContactById(userId, contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importDeviceContacts(): Result<List<Contact>> {
        return try {
            val contacts = mutableListOf<Contact>()
            val contentResolver: ContentResolver = context.contentResolver

            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(0) ?: continue
                    val phone = it.getString(1) ?: continue

                    contacts.add(
                        Contact(
                            id = UUID.randomUUID().toString(),
                            displayName = name,
                            phone = phone.replace(Regex("[^+\\d]"), "")
                        )
                    )
                }
            }

            Result.success(contacts.distinctBy { it.phone })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchContacts(userId: String, query: String): Flow<List<Contact>> {
        return contactDao.searchContacts(userId, query).map { entities ->
            entities.map { entity ->
                Contact(
                    id = entity.id,
                    userId = entity.userId,
                    displayName = entity.displayName,
                    phone = entity.phone,
                    email = entity.email,
                    photoUrl = entity.photoUrl,
                    addedAt = entity.addedAt
                )
            }
        }
    }

    override suspend fun findUserByEmailOrPhone(query: String): Result<Contact?> {
        return try {
            // Buscar por email
            var userDoc = firestore.collection("users")
                .whereEqualTo("email", query)
                .limit(1)
                .get().await()

            if (userDoc.isEmpty) {
                // Buscar por telefone — tentar variações de formato
                val phoneVariations = generatePhoneVariations(query)
                for (phone in phoneVariations) {
                    userDoc = firestore.collection("users")
                        .whereEqualTo("phone", phone)
                        .limit(1)
                        .get().await()
                    if (!userDoc.isEmpty) break
                }
            }

            if (userDoc.isEmpty) {
                Result.success(null)
            } else {
                val doc = userDoc.documents.first()
                val data = doc.data ?: emptyMap()
                Result.success(
                    Contact(
                        id = doc.id,
                        userId = doc.id,
                        displayName = data["displayName"] as? String ?: "",
                        phone = data["phone"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        photoUrl = data["photoUrl"] as? String ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gera variações do número de telefone para busca flexível.
     * Ex: "(11) 99999-9999" → ["11999999999", "+5511999999999", "5511999999999"]
     */
    private fun generatePhoneVariations(phone: String): List<String> {
        val digitsOnly = phone.replace(Regex("[^\\d]"), "")
        val variations = mutableListOf<String>()
        
        variations.add(digitsOnly)
        variations.add("+$digitsOnly")
        
        // Se começa com 55 (Brasil), adicionar sem o 55 e com +55
        if (digitsOnly.startsWith("55") && digitsOnly.length > 10) {
            val withoutCountry = digitsOnly.removePrefix("55")
            variations.add(withoutCountry)
            variations.add("+55$withoutCountry")
        } else if (digitsOnly.length in 10..11) {
            // Número local brasileiro — adicionar com código do país
            variations.add("55$digitsOnly")
            variations.add("+55$digitsOnly")
        }
        
        // Adicionar o original
        variations.add(phone)
        
        return variations.distinct()
    }
}
