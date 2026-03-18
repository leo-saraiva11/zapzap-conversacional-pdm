package com.example.zapzap.data.remote

import android.content.Context
import android.util.Log
import com.example.zapzap.domain.network.FcmService
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject

class FcmServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) : FcmService {

    private val scopes = listOf("https://www.googleapis.com/auth/firebase.messaging")

    private fun getAccessToken(): String? {
        return try {
            // Nota para ambiente Acadêmico/Dev: Colocar a service key em app/src/main/assets/service-account.json
            val inputStream: InputStream = context.assets.open("service-account.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(scopes)
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("FcmService", "Erro ao obter Access Token: ${e.message}")
            null
        }
    }

    override suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
        conversationId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val projectId = context.getString(com.example.zapzap.R.string.firebase_project_id)
                val accessToken = getAccessToken() ?: return@withContext

                val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"

                val jsonPayload = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                        })
                        put("data", JSONObject().apply {
                            put("conversationId", conversationId)
                        })
                        // Som para Android
                        put("android", JSONObject().apply {
                            put("notification", JSONObject().apply {
                                put("channel_id", context.getString(com.example.zapzap.R.string.default_notification_channel_id))
                                put("sound", "default")
                                put("priority", "high")
                            })
                        })
                    })
                }

                val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("FcmService", "Falha FCM: ${response.code} ${response.message}")
                    Log.e("FcmService", "Body: ${response.body?.string()}")
                } else {
                    Log.d("FcmService", "Notificação enviada com sucesso pro token: $token")
                }
            } catch (e: Exception) {
                Log.e("FcmService", "Erro rede FCM: ${e.message}", e)
            }
        }
    }
}
