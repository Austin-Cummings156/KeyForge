package com.example.keyforge.ui.state

/**
 * Top-level authentication/navigation state for the vault.
 *
 * The activity observes this state to decide whether the user should see setup,
 * login, recovery unlock, password reset, or the unlocked credential vault.
 */
sealed interface VaultUiState {
    data object Loading : VaultUiState
    data object SetupRequired : VaultUiState
    data object Locked : VaultUiState
    data object Unlocked : VaultUiState
    data object RecoveryUnlocked : VaultUiState
    data class Error(val message: String) : VaultUiState
}