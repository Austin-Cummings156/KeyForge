package com.example.keyforge.data.repository

import com.example.keyforge.data.local.CredentialDao
import com.example.keyforge.data.model.Credential
import kotlinx.coroutines.flow.Flow

class CredentialRepository(
    private val credentialDao: CredentialDao
) {
    fun getAllCredentials(): Flow<List<Credential>> {
        return credentialDao.getAllCredentials()
    }

    suspend fun getCredentialById(id: Int): Credential? {
        return credentialDao.getCredentialById(id)
    }

    suspend fun insertCredential(credential: Credential) {
        credentialDao.insertCredential(credential)
    }

    suspend fun updateCredential(credential: Credential) {
        credentialDao.updateCredential(credential)
    }

    suspend fun deleteCredential(credential: Credential) {
        credentialDao.deleteCredential(credential)
    }
}