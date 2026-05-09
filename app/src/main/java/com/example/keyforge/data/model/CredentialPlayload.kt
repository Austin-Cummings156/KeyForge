package com.example.keyforge.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CredentialPayload(
    val siteName: String, // Name of the website or application
    val username: String, // Username associated with the credential
    val password: String, // Password for the credential
    val notes: String // Additional notes or comments about the credential
)