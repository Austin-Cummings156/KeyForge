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

/**
 * ViewModel for vault setup, login, recovery unlock, and password reset flows.
 *
 * UI screens pass user-entered secrets as strings. The ViewModel immediately
 * converts them to CharArray before handing them to [VaultManager], where they
 * are cleared after use.
 */
class VaultViewModel(
    private val vaultManager: VaultManager
) : ViewModel() {

    private val _vaultUiState = MutableStateFlow<VaultUiState>(VaultUiState.Loading)
    val vaultUiState: StateFlow<VaultUiState> = _vaultUiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _generatedRecoveryKey = MutableStateFlow<String?>(null)
    val generatedRecoveryKey: StateFlow<String?> = _generatedRecoveryKey.asStateFlow()

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

    /**
     * Creates the first vault and exposes the generated recovery key exactly once
     * so the setup flow can show it to the user.
     */
    fun createVault(masterPassword: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val passwordChars = masterPassword.toCharArray()

            val result = vaultManager.createVault(passwordChars)
            result.onSuccess { recoveryKey ->
                _generatedRecoveryKey.value = recoveryKey
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

    /**
     * Unlocks using the recovery key and moves the UI into the password reset flow.
     */
    fun unlockWithRecoveryKey(recoveryKey: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val recoveryChars = recoveryKey.toCharArray()

            val result = vaultManager.unlockWithRecoveryKey(recoveryChars)
            result.onSuccess {
                _vaultUiState.value = VaultUiState.RecoveryUnlocked
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to unlock with recovery key."
            }
        }
    }

    fun resetMasterPassword(newMasterPassword: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val passwordChars = newMasterPassword.toCharArray()

            val result = vaultManager.resetMasterPassword(passwordChars)
            result.onSuccess {
                _vaultUiState.value = VaultUiState.Unlocked
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to reset master password."
            }
        }
    }

    fun consumeGeneratedRecoveryKey() {
        _generatedRecoveryKey.value = null
    }

    fun lockVault() {
        vaultManager.lockVault()
        _generatedRecoveryKey.value = null
        _vaultUiState.value = VaultUiState.Locked
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Locks the vault when this ViewModel is destroyed so the active vault key is
     * not intentionally retained longer than the UI lifecycle.
     */
    override fun onCleared() {
        vaultManager.lockVault()
        _generatedRecoveryKey.value = null
        super.onCleared()
    }
}

/**
 * Factory used to create [VaultViewModel] with its required [VaultManager]
 * dependency.
 */
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