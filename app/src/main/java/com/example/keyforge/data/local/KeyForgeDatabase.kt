package com.example.keyforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.keyforge.data.model.Credential

@Database(
    entities = [Credential::class],
    version = 1,
    exportSchema = false
)

abstract class KeyForgeDatabase : RoomDatabase() {
    abstract fun credentialDao(): CredentialDao
}