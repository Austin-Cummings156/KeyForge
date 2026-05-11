package com.example.keyforge.data.repository

import com.example.keyforge.crypto.CredentialCrypto
import com.example.keyforge.data.local.CredentialDao
import com.example.keyforge.data.model.Credential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for credential data.
 *
 * This class hides the encryption boundary from the ViewModel. Callers work
 * with plaintext [Credential] objects while the repository encrypts before
 * writing to Room and decrypts after reading from Room.
 */
class CredentialRepository(
    private val credentialDao: CredentialDao,
    private val credentialCrypto: CredentialCrypto
) {
    /**
     * Stream of decrypted credentials for the unlocked vault UI.
     *
     * The database emits encrypted entities; this flow maps them into plaintext
     * credentials using the active vault key.
     */
    val allCredentials: Flow<List<Credential>> =
        credentialDao.getAllCredentials().map { entities ->
            entities.map { credentialCrypto.decryptCredential(it) }
        }

    suspend fun getCredentialById(id: Int): Credential? {
        return credentialDao.getCredentialById(id)?.let {
            credentialCrypto.decryptCredential(it)
        }
    }

    suspend fun insertCredential(credential: Credential) {
        val entity = credentialCrypto.encryptCredential(credential)
        credentialDao.insertCredential(entity)
    }

    suspend fun updateCredential(credential: Credential) {
        val entity = credentialCrypto.encryptCredential(
            credential.copy(updatedAt = System.currentTimeMillis())
        )
        credentialDao.updateCredential(entity)
    }

    suspend fun deleteCredential(credential: Credential) {
        credentialDao.deleteCredentialById(credential.id)
    }
}