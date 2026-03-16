package com.example.zapzap.domain.repository

import android.net.Uri
import com.example.zapzap.domain.model.MediaAttachment
import com.example.zapzap.domain.model.MessageType

/**
 * Interface do repositório de mídia.
 * Gerencia upload/download de arquivos via Supabase Storage.
 */
interface MediaRepository {
    /** Upload de arquivo para o storage */
    suspend fun uploadMedia(
        uri: Uri,
        type: MessageType,
        conversationId: String
    ): Result<MediaAttachment>

    /** Download de arquivo para cache local */
    suspend fun downloadMedia(url: String, fileName: String): Result<String>

    /** Deleta arquivo do storage */
    suspend fun deleteMedia(url: String): Result<Unit>
}
