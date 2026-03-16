package com.example.zapzap

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application principal do ZapZap.
 * Inicializa o Hilt para injeção de dependência e
 * configura o canal de notificações.
 */
@HiltAndroidApp
class ZapZapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Cria o canal de notificação para mensagens.
     * Necessário para Android 8.0 (API 26) e superior.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de novas mensagens"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "zapzap_messages"
    }
}
