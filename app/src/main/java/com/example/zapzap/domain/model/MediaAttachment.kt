package com.example.zapzap.domain.model

/**
 * Modelo de domínio para anexo de mídia.
 */
data class MediaAttachment(
    val id: String = "",
    val url: String = "",
    val localPath: String = "",
    val type: MessageType = MessageType.IMAGE,
    val fileName: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val thumbnailUrl: String = ""
)
