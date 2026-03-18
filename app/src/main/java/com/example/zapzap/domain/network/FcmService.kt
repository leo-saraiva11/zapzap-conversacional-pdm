package com.example.zapzap.domain.network

/**
 * Interface para serviço de notificações push (FCM).
 * Usado para enviar notificações entre dispositivos.
 */
interface FcmService {
    /**
     * Envia uma notificação push para um token específico.
     */
    suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
        conversationId: String
    )
}
