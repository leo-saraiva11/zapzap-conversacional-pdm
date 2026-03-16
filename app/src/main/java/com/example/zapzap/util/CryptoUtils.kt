package com.example.zapzap.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Utilitários de criptografia AES-GCM para mensagens.
 * Usa o Android Keystore para armazenamento seguro de chaves.
 */
object CryptoUtils {
    private const val KEY_ALIAS = "zapzap_message_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    /**
     * Gera ou recupera a chave AES do Android Keystore.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Verifica se a chave já existe
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        // Gerar nova chave
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Criptografa uma mensagem usando AES-GCM.
     * Retorna o texto criptografado em Base64 (IV + ciphertext concatenados).
     */
    fun encrypt(plainText: String): String {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Concatenar IV + ciphertext
        val combined = iv + cipherText
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Descriptografa uma mensagem usando AES-GCM.
     */
    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

        // Extrair IV (12 bytes para GCM) e ciphertext
        val iv = combined.copyOfRange(0, 12)
        val cipherText = combined.copyOfRange(12, combined.size)

        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plainText = cipher.doFinal(cipherText)
        return String(plainText, Charsets.UTF_8)
    }
}
