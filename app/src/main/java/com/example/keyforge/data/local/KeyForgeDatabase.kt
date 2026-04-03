package com.example.keyforge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.keyforge.data.model.CredentialEntity
import com.example.keyforge.data.model.VaultMetadata

@Database(
    entities = [CredentialEntity::class, VaultMetadata::class],
    version = 3,
    exportSchema = false
)
abstract class KeyForgeDatabase : RoomDatabase() {

    abstract fun credentialDao(): CredentialDao
    abstract fun vaultMetadataDao(): VaultMetadataDao

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
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}