package com.example.zapzap.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.zapzap.domain.model.MediaAttachment
import com.example.zapzap.domain.model.MessageType
import com.example.zapzap.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) : MediaRepository {

    private val TAG = "SupabaseStorage"

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

            // Obter bytes
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: if (uri.scheme == "file") File(uri.path ?: "").readBytes() else null
                ?: throw Exception("Arquivo vazio ou inacessível")

            Log.d(TAG, "Fazendo upload para $bucketName/$path (${bytes.size} bytes)")

            // Acesso ao bucket conforme o tutorial oficial
            val bucket = supabaseClient.storage.from(bucketName)
            
            // Upload enviando os bytes
            bucket.upload(path, bytes) {
                upsert = true
            }

            val downloadUrl = bucket.publicUrl(path)
            Log.d(TAG, "Sucesso: $downloadUrl")

            Result.success(
                MediaAttachment(
                    id = fileName,
                    url = downloadUrl,
                    localPath = uri.toString(),
                    type = type,
                    fileName = fullFileName,
                    mimeType = context.contentResolver.getType(uri) ?: ""
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro Supabase: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun downloadMedia(url: String, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val localFile = File(context.cacheDir, fileName)
            val bytes = java.net.URL(url).readBytes()
            localFile.writeBytes(bytes)
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedia(url: String): Result<Unit> = Result.success(Unit)

    private fun getExtensionFromUri(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
            mimeType.contains("png") -> ".png"
            mimeType.contains("mp4") || mimeType.contains("video") -> ".mp4"
            mimeType.contains("mp3") || mimeType.contains("mpeg") -> ".mp3"
            mimeType.contains("aac") || mimeType.contains("m4a") -> ".m4a"
            else -> {
                val path = uri.path ?: ""
                when {
                    path.endsWith(".jpg", true) -> ".jpg"
                    path.endsWith(".png", true) -> ".png"
                    path.endsWith(".mp4", true) -> ".mp4"
                    else -> ".bin"
                }
            }
        }
    }
}
