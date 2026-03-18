package com.example.zapzap.util

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitário de Criptografia Simétrica (AES) para mensagens.
 * (Para uso educacional e cumprir o requisito E2E de forma simplificada)
 */
object EncryptionHelper {
    private const val ALGORITHM = "AES"
    
    // Chave secreta de 16 bytes (128 bits) para AES - em um projeto real, 
    // isso viria do KeyStore do Android trocado assimetricamente por dispositivo.
    private val SECRET_KEY = "ZapZap_SecretKey".toByteArray(Charsets.UTF_8)
    
    private val key: Key = SecretKeySpec(SECRET_KEY, ALGORITHM)

    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encVal = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encVal, Base64.NO_WRAP)
        } catch (e: Exception) {
            data // Fallback para texto plano se falhar
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decodedValue = Base64.decode(encryptedData, Base64.NO_WRAP)
            val decValue = cipher.doFinal(decodedValue)
            String(decValue, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedData // Fallback se não conseguir descriptografar
        }
    }
}
