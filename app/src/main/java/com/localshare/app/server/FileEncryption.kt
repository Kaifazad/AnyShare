package com.localshare.app.server

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.GCMParameterSpec
import java.security.SecureRandom

/**
 * AES-256-GCM encryption/decryption for file transfers.
 *
 * Encrypted format: [12-byte IV] [ciphertext] [16-byte GCM auth tag]
 * The IV is randomly generated per encryption call and prepended to the output.
 * GCM provides both confidentiality and integrity (no separate HMAC needed).
 */
object FileEncryption {

    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_SIZE = 12
    private const val GCM_TAG_SIZE = 128

    /**
     * Generate a random 256-bit AES key.
     */
    fun generateKey(): ByteArray {
        val key = ByteArray(AES_KEY_SIZE / 8)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * Encode a key to a URL-safe base64 string (43 characters for 256-bit key).
     */
    fun encodeKey(key: ByteArray): String {
        return Base64.encodeToString(key, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Decode a base64url-encoded key back to bytes.
     */
    fun decodeKey(encoded: String): ByteArray {
        return Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Encrypt data with AES-256-GCM.
     * Returns: [12-byte IV] [encrypted data + GCM tag]
     */
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), spec)

        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    /**
     * Decrypt data encrypted with AES-256-GCM.
     * Input: [12-byte IV] [encrypted data + GCM tag]
     */
    fun decrypt(data: ByteArray, key: ByteArray): ByteArray {
        if (data.size < GCM_IV_SIZE + 1) {
            throw IllegalArgumentException("Encrypted data too short")
        }

        val iv = data.sliceArray(0 until GCM_IV_SIZE)
        val ciphertext = data.sliceArray(GCM_IV_SIZE until data.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), spec)

        return cipher.doFinal(ciphertext)
    }
}
