package com.example.keyforge.crypto

import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.model.CredentialEntity
import com.example.keyforge.data.model.CredentialPayload
import com.example.keyforge.security.CryptoEngine
import com.example.keyforge.security.VaultManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CredentialCrypto(
    private val cryptoEngine: CryptoEngine, // Handles AES encryption/decryption
    private val vaultManager: VaultManager // Provides the active vault key
) {
    private val json = Json // Used for serialization

    fun encryptCredential(credential: Credential): CredentialEntity {
        val key = vaultManager.requireActiveVaultKey()

        val payload = CredentialPayload(
            siteName = credential.siteName,
            username = credential.username,
            password = credential.password,
            notes = credential.notes
        )

        // Convert CredentialPayload to JSON and then JSON into ByteArray
        val jsonBytes = json.encodeToString(payload).toByteArray(Charsets.UTF_8)

        // Encrypt JSON bytes using AES-GCM
        val encrypted = cryptoEngine.encrypt(jsonBytes, key)

        return CredentialEntity(
            id = credential.id,
            encryptedData = encrypted.ciphertext,
            nonce = encrypted.nonce,
            createdAt = credential.createdAt,
            updatedAt = credential.updatedAt
        )
    }

    fun decryptCredential(entity: CredentialEntity): Credential {
        val key = vaultManager.requireActiveVaultKey()

        // Decrypt the entity using AES-GCM to raw JSON bytes
        val decryptedBytes = cryptoEngine.decrypt(
            ciphertext = entity.encryptedData,
            key = key,
            nonce = entity.nonce
        )

        // Convert JSON bytes to CredentialPayload
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