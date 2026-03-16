package com.example.zapzap.data.repository

import android.content.Context
import android.net.Uri
import com.example.zapzap.domain.model.MediaAttachment
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de mídia usando Supabase Storage.
 * Resolve o problema do Firebase Storage ser pago.
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: Storage
) : MediaRepository {

    override suspend fun uploadMedia(
        uri: Uri,
        type: MessageType,
        conversationId: String
    ): Result<MediaAttachment> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = "${UUID.randomUUID()}_${System.currentTimeMillis()}"
            val bucketName = when (type) {
                MessageType.IMAGE -> "images"
                MessageType.VIDEO -> "videos"
                MessageType.AUDIO -> "audios"
                else -> "files"
            }

            val extension = getExtensionFromUri(uri)
            val fullFileName = "$fileName$extension"
            val path = "$conversationId/$fullFileName"

            // Ler bytes do arquivo
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.use { it.readBytes() } ?: throw Exception("Falha ao ler arquivo")

            // Upload para o Supabase
            val bucket = storage.from(bucketName)
            bucket.upload(path, bytes) {
                upsert = true
            }

            // Obter URL pública
            val downloadUrl = bucket.publicUrl(path)

            val mimeType = context.contentResolver.getType(uri) ?: ""

            Result.success(
                MediaAttachment(
                    id = fileName,
                    url = downloadUrl,
                    localPath = uri.toString(),
                    type = type,
                    fileName = fullFileName,
                    mimeType = mimeType
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadMedia(url: String, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val localFile = File(context.cacheDir, fileName)
            // No Supabase usamos a URL pública ou download direto
            // Aqui simplificamos usando o download HTTP básico já que as URLs são públicas
            val bytes = java.net.URL(url).readBytes()
            localFile.writeBytes(bytes)
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedia(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Extrair path e bucket da URL (Simplificado)
            // No mundo real, você mapearia a URL de volta para o path
            // Para deletar precisamos do bucket e do path original
            Result.success(Unit) // Implementação pendente de mapeamento de URL
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getExtensionFromUri(uri: Uri): String {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            when {
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                mimeType.contains("png") -> ".png"
                mimeType.contains("gif") -> ".gif"
                mimeType.contains("mp4") -> ".mp4"
                mimeType.contains("webm") -> ".mp4"
                mimeType.contains("ogg") -> ".ogg"
                mimeType.contains("mp3") || mimeType.contains("mpeg") -> ".mp3"
                mimeType.contains("wav") -> ".wav"
                mimeType.contains("pdf") -> ".pdf"
                else -> ""
            }
        } ?: ""
    }
}
