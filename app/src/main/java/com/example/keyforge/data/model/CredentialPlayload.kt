package com.example.keyforge.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CredentialPayload(
    val siteName: String,
    val username: String,
    val password: String,
    val notes: String
)