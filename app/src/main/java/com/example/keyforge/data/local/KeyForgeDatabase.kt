package com.example.keyforge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.keyforge.data.model.CredentialEntity
import com.example.keyforge.data.model.VaultMetadata

/**
 * Local Room database for KeyForge.
 *
 * The database stores encrypted credentials and vault metadata only. Plaintext
 * credential contents and master passwords should never be persisted here.
 */
@Database(
    entities = [CredentialEntity::class, VaultMetadata::class],
    version = 4,
    exportSchema = false
)
abstract class KeyForgeDatabase : RoomDatabase() {

    abstract fun credentialDao(): CredentialDao
    abstract fun vaultMetadataDao(): VaultMetadataDao

    /**
     * Provides a process-wide singleton database instance.
     *
     * Room database creation is synchronized to prevent multiple instances from
     * being created at the same time.
     */
    companion object {
        @Volatile
        private var INSTANCE: KeyForgeDatabase? = null

        fun getDatabase(context: Context): KeyForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyForgeDatabase::class.java,
                    "keyforge_database"
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}