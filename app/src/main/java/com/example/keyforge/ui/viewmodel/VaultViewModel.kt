package com.example.keyforge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keyforge.security.VaultManager
import com.example.keyforge.ui.state.VaultUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaultViewModel(
    private val vaultManager: VaultManager
) : ViewModel() {

    private val _vaultUiState = MutableStateFlow<VaultUiState>(VaultUiState.Loading)
    val vaultUiState: StateFlow<VaultUiState> = _vaultUiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkVaultStatus()
    }

    fun checkVaultStatus() {
        viewModelScope.launch {
            try {
                val vaultExists = vaultManager.doesVaultExist()
                _vaultUiState.value = if (vaultExists) {
                    VaultUiState.Locked
                } else {
                    VaultUiState.SetupRequired
                }
            } catch (e: Exception) {
                _vaultUiState.value = VaultUiState.Error(
                    e.message ?: "Failed to load vault state."
                )
            }
        }
    }

    fun createVault(masterPassword: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val passwordChars = masterPassword.toCharArray()

            val result = vaultManager.createVault(passwordChars)
            result.onSuccess {
                _vaultUiState.value = VaultUiState.Unlocked
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to create vault."
            }
        }
    }

    fun unlockVault(masterPassword: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val passwordChars = masterPassword.toCharArray()

            val result = vaultManager.unlockVault(passwordChars)
            result.onSuccess {
                _vaultUiState.value = VaultUiState.Unlocked
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to unlock vault."
            }
        }
    }

    fun lockVault() {
        vaultManager.lockVault()
        _vaultUiState.value = VaultUiState.Locked
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        vaultManager.lockVault()
        super.onCleared()
    }
}

class VaultViewModelFactory(
    private val vaultManager: VaultManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            return VaultViewModel(vaultManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}