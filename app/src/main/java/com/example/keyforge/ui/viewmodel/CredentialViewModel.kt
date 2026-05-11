package com.example.keyforge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.repository.CredentialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the unlocked credential vault.
 *
 * Exposes decrypted credentials as UI state and delegates create, update, and
 * delete operations to [CredentialRepository].
 */
class CredentialViewModel(
    private val repository: CredentialRepository
) : ViewModel() {

    val credentials = repository.allCredentials
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addCredential(credential: Credential) {
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

/**
 * Factory used to create [CredentialViewModel] with its required
 * [CredentialRepository] dependency.
 */
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