package com.example.keyforge.ui.state

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data object SetupRequired : VaultUiState
    data object Locked : VaultUiState
    data object Unlocked : VaultUiState
    data class Error(val message: String) : VaultUiState
}