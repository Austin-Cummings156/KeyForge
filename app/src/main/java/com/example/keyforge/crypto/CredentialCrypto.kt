package com.example.keyforge.crypto

import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.model.CredentialEntity
import com.example.keyforge.data.model.CredentialPayload
import com.example.keyforge.security.CryptoEngine
import com.example.keyforge.security.VaultManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converts between plaintext credential models and encrypted database entities.
 *
 * The UI and ViewModel work with [Credential], while Room stores only
 * [CredentialEntity] values containing ciphertext and nonce data. This class is
 * the boundary where credential fields are serialized, encrypted, decrypted,
 * and deserialized.
 */

class CredentialCrypto(
    private val cryptoEngine: CryptoEngine,
    private val vaultManager: VaultManager
) {
    private val json = Json

    /**
     * Serializes and encrypts a credential for database storage.
     *
     * Site name, username, password, and notes are packed into [CredentialPayload]
     * before encryption so Room never stores those fields as plaintext columns.
     */

    fun encryptCredential(credential: Credential): CredentialEntity {
        val key = vaultManager.requireActiveVaultKey()

        val payload = CredentialPayload(
            siteName = credential.siteName,
            username = credential.username,
            password = credential.password,
            notes = credential.notes
        )

        val jsonBytes = json.encodeToString(payload).toByteArray(Charsets.UTF_8)

        val encrypted = cryptoEngine.encrypt(jsonBytes, key)

        return CredentialEntity(
            id = credential.id,
            encryptedData = encrypted.ciphertext,
            nonce = encrypted.nonce,
            createdAt = credential.createdAt,
            updatedAt = credential.updatedAt
        )
    }

    /**
     * Decrypts a stored credential entity back into the app's plaintext model.
     *
     * This requires the vault to be unlocked because the active vault key is needed
     * to decrypt the encrypted payload.
     */

    fun decryptCredential(entity: CredentialEntity): Credential {
        val key = vaultManager.requireActiveVaultKey()

        val decryptedBytes = cryptoEngine.decrypt(
            ciphertext = entity.encryptedData,
            key = key,
            nonce = entity.nonce
        )

        val payload = json.decodeFromString<CredentialPayload>(
            String(decryptedBytes, Charsets.UTF_8)
        )

        return Credential(
            id = entity.id,
            siteName = payload.siteName,
            username = payload.username,
            password = payload.password,
            notes = payload.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}