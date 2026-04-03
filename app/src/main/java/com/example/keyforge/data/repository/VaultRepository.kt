package com.example.keyforge.data.repository

import com.example.keyforge.data.local.VaultMetadataDao
import com.example.keyforge.data.model.VaultMetadata

class VaultRepository(
    private val vaultMetadataDao: VaultMetadataDao
) {
    suspend fun getVaultMetadata(): VaultMetadata? {
        return vaultMetadataDao.getVaultMetadata()
    }

    suspend fun saveVaultMetadata(metadata: VaultMetadata) {
        vaultMetadataDao.insertOrReplaceVaultMetadata(metadata)
    }

    suspend fun clearVaultMetadata() {
        vaultMetadataDao.clearVaultMetadata()
    }

    suspend fun doesVaultExist(): Boolean {
        return vaultMetadataDao.getVaultMetadata() != null
    }
}