package com.example.keyforge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.repository.CredentialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CredentialViewModel(
    private val repository: CredentialRepository
) : ViewModel() {

    val credentials: StateFlow<List<Credential>> = repository
        .getAllCredentials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun insertCredential(credential: Credential) {
        viewModelScope.launch {
            repository.insertCredential(credential)
        }
    }

    fun updateCredential(credential: Credential) {
        viewModelScope.launch {
            repository.updateCredential(credential)
        }
    }

    fun deleteCredential(credential: Credential) {
        viewModelScope.launch {
            repository.deleteCredential(credential)
        }
    }
}