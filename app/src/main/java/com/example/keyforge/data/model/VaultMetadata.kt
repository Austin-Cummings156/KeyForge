package com.example.keyforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row metadata record required to unlock the vault.
 *
 * This stores salts, Argon2 parameters, and encrypted copies of the vault key.
 * The vault key is wrapped once by the master-password-derived key and once by
 * the recovery-key-derived key. Neither the master password nor recovery key is
 * stored.
 */
@Entity(tableName = "vault_metadata")
data class VaultMetadata(
    @PrimaryKey
    val id: Int = 1,

    val passwordSalt: ByteArray,
    val passwordWrappedVaultKey: ByteArray,
    val passwordWrappedVaultKeyNonce: ByteArray,

    val recoverySalt: ByteArray,
    val recoveryWrappedVaultKey: ByteArray,
    val recoveryWrappedVaultKeyNonce: ByteArray,

    val argon2MemoryCostKb: Int,
    val argon2Iterations: Int,
    val argon2Parallelism: Int,

    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Provides content-based equality for ByteArray fields.
     *
     * Kotlin arrays compare by reference by default, which is not useful for Room
     * entities containing cryptographic byte arrays.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultMetadata

        if (id != other.id) return false
        if (argon2MemoryCostKb != other.argon2MemoryCostKb) return false
        if (argon2Iterations != other.argon2Iterations) return false
        if (argon2Parallelism != other.argon2Parallelism) return false
        if (createdAt != other.createdAt) return false
        if (!passwordSalt.contentEquals(other.passwordSalt)) return false
        if (!passwordWrappedVaultKey.contentEquals(other.passwordWrappedVaultKey)) return false
        if (!passwordWrappedVaultKeyNonce.contentEquals(other.passwordWrappedVaultKeyNonce)) return false
        if (!recoverySalt.contentEquals(other.recoverySalt)) return false
        if (!recoveryWrappedVaultKey.contentEquals(other.recoveryWrappedVaultKey)) return false
        if (!recoveryWrappedVaultKeyNonce.contentEquals(other.recoveryWrappedVaultKeyNonce)) return false

        return true
    }
    /**
     * Hashes ByteArray contents so hashCode stays consistent with [equals].
     */
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + argon2MemoryCostKb
        result = 31 * result + argon2Iterations
        result = 31 * result + argon2Parallelism
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + passwordSalt.contentHashCode()
        result = 31 * result + passwordWrappedVaultKey.contentHashCode()
        result = 31 * result + passwordWrappedVaultKeyNonce.contentHashCode()
        result = 31 * result + recoverySalt.contentHashCode()
        result = 31 * result + recoveryWrappedVaultKey.contentHashCode()
        result = 31 * result + recoveryWrappedVaultKeyNonce.contentHashCode()
        return result
    }
}