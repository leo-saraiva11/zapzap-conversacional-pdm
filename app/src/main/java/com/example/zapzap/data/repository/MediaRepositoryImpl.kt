package com.example.zapzap.data.repository

import android.content.Context
import android.net.Uri
import com.example.zapzap.domain.model.MediaAttachment
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.repository.MediaRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de mídia.
 * Usa Firebase Storage para upload/download de arquivos.
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : MediaRepository {

    override suspend fun uploadMedia(
        uri: Uri,
        type: MessageType,
        conversationId: String
    ): Result<MediaAttachment> {
        return try {
            val fileName = "${UUID.randomUUID()}_${System.currentTimeMillis()}"
            val folder = when (type) {
                MessageType.IMAGE -> "images"
                MessageType.VIDEO -> "videos"
                MessageType.AUDIO -> "audios"
                else -> "files"
            }

            val extension = context.contentResolver.getType(uri)?.let { mimeType ->
                when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                    mimeType.contains("png") -> ".png"
                    mimeType.contains("gif") -> ".gif"
                    mimeType.contains("mp4") -> ".mp4"
                    mimeType.contains("webm") -> ".webm"
                    mimeType.contains("ogg") -> ".ogg"
                    mimeType.contains("mp3") || mimeType.contains("mpeg") -> ".mp3"
                    mimeType.contains("m4a") -> ".m4a"
                    mimeType.contains("wav") -> ".wav"
                    mimeType.contains("pdf") -> ".pdf"
                    else -> ""
                }
            } ?: ""

            val fullFileName = "$fileName$extension"
            val ref = storage.reference
                .child("$folder/$conversationId/$fullFileName")

            // Upload
            ref.putFile(uri).await()

            // Obter URL de download
            val downloadUrl = ref.downloadUrl.await().toString()

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

    override suspend fun downloadMedia(url: String, fileName: String): Result<String> {
        return try {
            val localFile = File(context.cacheDir, fileName)
            val ref = storage.getReferenceFromUrl(url)
            ref.getFile(localFile).await()
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedia(url: String): Result<Unit> {
        return try {
            val ref = storage.getReferenceFromUrl(url)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
