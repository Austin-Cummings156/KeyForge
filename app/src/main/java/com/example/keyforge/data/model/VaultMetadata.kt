package com.example.keyforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_metadata")
data class VaultMetadata(
    @PrimaryKey
    val id: Int = 1,
    val salt: ByteArray,
    val verifierCiphertext: ByteArray,
    val verifierNonce: ByteArray,
    val argon2MemoryCostKb: Int,
    val argon2Iterations: Int,
    val argon2Parallelism: Int,
    val createdAt: Long = System.currentTimeMillis()
)