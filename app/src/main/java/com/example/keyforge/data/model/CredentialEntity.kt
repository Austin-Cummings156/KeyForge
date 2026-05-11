package com.example.keyforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Encrypted credential record stored by Room.
 *
 * The actual credential fields are serialized into a payload and encrypted into
 * [encryptedData]. The nonce is stored beside the ciphertext because AES-GCM
 * requires the same nonce for decryption.
 */
@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val encryptedData: ByteArray,
    val nonce: ByteArray,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * ByteArray uses referential equality by default, so content-based equality is
     * implemented manually for reliable comparisons.
     */
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

    /**
     * Matches [equals] by hashing ByteArray contents instead of array references.
     */
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}