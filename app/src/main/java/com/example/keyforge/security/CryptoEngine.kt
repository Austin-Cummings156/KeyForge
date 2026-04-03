package com.example.keyforge.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoEngine {

    private val secureRandom = SecureRandom()

    fun generateNonce(size: Int = 12): ByteArray {
        return ByteArray(size).also { secureRandom.nextBytes(it) }
    }

    fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        nonce: ByteArray = generateNonce()
    ): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, nonce)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext)

        return EncryptedData(
            ciphertext = ciphertext,
            nonce = nonce
        )
    }

    fun decrypt(
        ciphertext: ByteArray,
        key: ByteArray,
        nonce: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, nonce)

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
        return cipher.doFinal(ciphertext)
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val nonce: ByteArray
)