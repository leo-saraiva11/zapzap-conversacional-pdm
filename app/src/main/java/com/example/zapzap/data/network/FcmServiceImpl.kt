package com.example.zapzap.data.network

import android.util.Log
import com.example.zapzap.domain.network.FcmService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do serviço de notificações push (FCM).
 * Esta implementação atual serve como um stub para permitir o build do projeto,
 * logando as tentativas de envio no Logcat.
 */
@Singleton
class FcmServiceImpl @Inject constructor() : FcmService {
    
    override suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
        conversationId: String
    ): Result<Unit> {
        Log.d("FCM_SERVICE", "Simulando envio de notificação: $title - $body para o token: $token")
        // TODO: Implementar envio real via FCM HTTP v1 se necessário para o TP5
        return Result.success(Unit)
    }
}
