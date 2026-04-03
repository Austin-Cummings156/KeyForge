package com.example.keyforge.crypto

import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.model.CredentialEntity
import com.example.keyforge.data.model.CredentialPayload
import com.example.keyforge.security.CryptoEngine
import com.example.keyforge.security.VaultManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CredentialCrypto(
    private val cryptoEngine: CryptoEngine,
    private val vaultManager: VaultManager
) {
    private val json = Json

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