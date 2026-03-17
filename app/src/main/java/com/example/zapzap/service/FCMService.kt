package com.example.zapzap.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.zapzap.MainActivity
import com.example.zapzap.R
import com.example.zapzap.ZapZapApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Serviço Firebase Cloud Messaging para notificações push.
 * Recebe notificações de novas mensagens e exibe ao usuário.
 */
class FCMService : FirebaseMessagingService() {

    /**
     * Chamado quando um novo token FCM é gerado.
     * Salva o token no Firestore para o usuário atual.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    /**
     * Chamado quando uma mensagem remota é recebida.
     * Cria uma notificação local para o usuário.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: "Nova mensagem"
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val conversationId = message.data["conversationId"] ?: ""

        if (conversationId.isNotEmpty()) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .whereEqualTo("status", "SENT")
                    .get()
                    .addOnSuccessListener { unreadQuery ->
                        if (!unreadQuery.isEmpty) {
                            val batch = db.batch()
                            var hasUpdates = false
                            for (doc in unreadQuery.documents) {
                                if (doc.getString("senderId") != uid) {
                                    batch.update(doc.reference, "status", "DELIVERED")
                                    hasUpdates = true
                                }
                            }
                            if (hasUpdates) {
                                batch.commit()
                            }
                        }
                    }
            }
        }

        showNotification(title, body, conversationId)
    }

    private fun showNotification(title: String, body: String, conversationId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("conversationId", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ZapZapApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(conversationId.hashCode(), notification)
    }
}
