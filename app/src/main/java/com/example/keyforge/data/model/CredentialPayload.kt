package com.example.keyforge.data.model

import kotlinx.serialization.Serializable

/**
 * Serializable plaintext payload that is encrypted into [CredentialEntity].
 *
 * This model exists so multiple credential fields can be encrypted as one
 * authenticated blob instead of being stored as separate plaintext columns.
 */
@Serializable
data class CredentialPayload(
    val siteName: String,
    val username: String,
    val password: String,
    val notes: String
)