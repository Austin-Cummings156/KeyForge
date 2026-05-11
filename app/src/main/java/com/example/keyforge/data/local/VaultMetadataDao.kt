package com.example.keyforge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.keyforge.data.model.VaultMetadata

/**
 * Room DAO for the single vault metadata record.
 *
 * The metadata record stores salts, Argon2 parameters, and wrapped vault-key
 * material needed to unlock the encrypted vault. It does not store the user's
 * master password or plaintext recovery key.
 */
@Dao
interface VaultMetadataDao {

    @Query("SELECT * FROM vault_metadata WHERE id = 1 LIMIT 1")
    suspend fun getVaultMetadata(): VaultMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceVaultMetadata(metadata: VaultMetadata)

    @Query("DELETE FROM vault_metadata")
    suspend fun clearVaultMetadata()
}