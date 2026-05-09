package com.example.keyforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) // Automatically generated primary key
    val id: Int = 0, // Unique identifier for the credential
    val encryptedData: ByteArray, // Encrypted data for the credential
    val nonce: ByteArray, // Nonce for encryption
    val createdAt: Long = System.currentTimeMillis(), // Timestamp for when the credential was created
    val updatedAt: Long = System.currentTimeMillis() // Timestamp for when the credential was last updated
) {
    // Override equals to compare CredentialEntity objects
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CredentialEntity

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    // Override hashCode to ensure consistent hashing
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}