package com.example.keyforge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keyforge.data.repository.CredentialRepository

class CredentialViewModelFactory(
    private val repository: CredentialRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CredentialViewModel::class.java)) {
            return CredentialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}