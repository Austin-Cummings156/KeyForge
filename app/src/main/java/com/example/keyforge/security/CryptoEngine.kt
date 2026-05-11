package com.example.keyforge.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Small AES-GCM encryption helper used throughout KeyForge.
 *
 * AES-GCM provides authenticated encryption, meaning decryption fails if the
 * ciphertext, nonce, or authentication tag has been tampered with. Each encrypt
 * call should use a unique nonce for the same key.
 */
class CryptoEngine {

    private val secureRandom = SecureRandom()

    /**
     * Generates a random nonce for AES-GCM encryption.
     *
     * The default size is 12 bytes, which is the recommended nonce length for GCM.
     */
    fun generateNonce(size: Int = 12): ByteArray {
        return ByteArray(size).also { secureRandom.nextBytes(it) }
    }

    /**
     * Encrypts plaintext with AES-GCM.
     *
     * @param plaintext Raw bytes to encrypt.
     * @param key AES key bytes. Expected length is 32 bytes for AES-256.
     * @param nonce Unique nonce for this encryption operation.
     *
     * @return Ciphertext and nonce bundled together for storage.
     */
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

    /**
     * Decrypts AES-GCM ciphertext.
     *
     * Decryption throws if the key, nonce, ciphertext, or authentication tag is
     * invalid. Callers use that failure to reject incorrect passwords or recovery keys.
     */
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

/**
 * Result of an AES-GCM encryption operation.
 *
 * Both values must be stored together. The nonce is not secret, but it must be
 * the same nonce used during encryption in order to decrypt successfully.
 */
data class EncryptedData(
    val ciphertext: ByteArray,
    val nonce: ByteArray
)