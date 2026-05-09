package com.example.keyforge.data.model

data class Credential(
    val id: Int = 0, // Unique identifier for the credential
    val siteName: String, // Name of the website or application
    val username: String, // Username associated with the credential
    val password: String, // Password for the credential
    val notes: String, // Additional notes or comments about the credential
    val createdAt: Long = System.currentTimeMillis(), // Timestamp for when the credential was created
    val updatedAt: Long = System.currentTimeMillis() // Timestamp for when the credential was last updated
)