package com.example.keyforge.data.model

data class Credential(
    val id: Int = 0,
    val siteName: String,
    val username: String,
    val password: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)