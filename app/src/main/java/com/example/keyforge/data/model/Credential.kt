package com.example.keyforge.data.model

/**
 * Plaintext credential model used by the unlocked UI layer.
 *
 * Instances of this class should only be created after the vault is unlocked.
 * Before persistence, credentials are converted into encrypted [CredentialEntity]
 * records by [CredentialCrypto].
 */
data class Credential(
    val id: Int = 0,
    val siteName: String,
    val username: String,
    val password: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)