package com.example.zapzap.domain.network

interface FcmService {
    suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
        conversationId: String
    )
}
