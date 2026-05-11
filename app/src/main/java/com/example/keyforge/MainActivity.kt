package com.example.keyforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keyforge.crypto.CredentialCrypto
import com.example.keyforge.data.local.KeyForgeDatabase
import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.repository.CredentialRepository
import com.example.keyforge.data.repository.VaultRepository
import com.example.keyforge.security.CryptoEngine
import com.example.keyforge.security.VaultManager
import com.example.keyforge.ui.screens.CredentialDetailScreen
import com.example.keyforge.ui.screens.CredentialFormScreen
import com.example.keyforge.ui.screens.CredentialListScreen
import com.example.keyforge.ui.screens.RecoveryKeyDisplayScreen
import com.example.keyforge.ui.screens.RecoveryUnlockScreen
import com.example.keyforge.ui.screens.VaultLoginScreen
import com.example.keyforge.ui.screens.VaultSetupScreen
import com.example.keyforge.ui.state.VaultUiState
import com.example.keyforge.ui.theme.KeyForgeTheme
import com.example.keyforge.ui.viewmodel.CredentialViewModel
import com.example.keyforge.ui.viewmodel.CredentialViewModelFactory
import com.example.keyforge.ui.viewmodel.VaultViewModel
import com.example.keyforge.ui.viewmodel.VaultViewModelFactory

/**
 * Main entry point for KeyForge.
 *
 * This activity wires together the local database, repositories, crypto helpers,
 * vault manager, and ViewModels, then selects the correct Compose screen based
 * on the current vault state.
 *
 * KeyForge intentionally keeps its navigation simple for v1. The activity acts
 * as the top-level coordinator between vault authentication states and the
 * credential management screens.
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CredentialViewModel
    private lateinit var vaultViewModel: VaultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = KeyForgeDatabase.getDatabase(applicationContext)

        val vaultRepository = VaultRepository(database.vaultMetadataDao())
        val vaultManager = VaultManager(vaultRepository)
        val vaultFactory = VaultViewModelFactory(vaultManager)
        vaultViewModel = ViewModelProvider(this, vaultFactory)[VaultViewModel::class.java]

        val credentialCrypto = CredentialCrypto(
            cryptoEngine = CryptoEngine(),
            vaultManager = vaultManager
        )

        val credentialRepository = CredentialRepository(
            credentialDao = database.credentialDao(),
            credentialCrypto = credentialCrypto
        )

        val credentialFactory = CredentialViewModelFactory(credentialRepository)
        viewModel = ViewModelProvider(this, credentialFactory)[CredentialViewModel::class.java]

        setContent {
            val vaultState by vaultViewModel.vaultUiState.collectAsStateWithLifecycle()
            val errorMessage by vaultViewModel.errorMessage.collectAsStateWithLifecycle()
            val generatedRecoveryKey by vaultViewModel.generatedRecoveryKey.collectAsStateWithLifecycle()

            var showRecoveryUnlockScreen by remember { mutableStateOf(false) }

            KeyForgeTheme {
                when {
                    vaultState is VaultUiState.Loading -> {
                        Text("Loading vault...")
                    }

                    vaultState is VaultUiState.SetupRequired -> {
                        VaultSetupScreen(
                            errorMessage = errorMessage,
                            onCreateVault = { password ->
                                vaultViewModel.createVault(password)
                            }
                        )
                    }

                    generatedRecoveryKey != null -> {
                        RecoveryKeyDisplayScreen(
                            recoveryKey = generatedRecoveryKey!!,
                            onContinue = {
                                vaultViewModel.consumeGeneratedRecoveryKey()
                            }
                        )
                    }

                    vaultState is VaultUiState.Locked && showRecoveryUnlockScreen -> {
                        RecoveryUnlockScreen(
                            errorMessage = errorMessage,
                            onUnlockWithRecoveryKey = { recoveryKey ->
                                vaultViewModel.unlockWithRecoveryKey(recoveryKey)
                            },
                            onBackToLogin = {
                                showRecoveryUnlockScreen = false
                                vaultViewModel.clearError()
                            }
                        )
                    }

                    vaultState is VaultUiState.Locked -> {
                        VaultLoginScreen(
                            errorMessage = errorMessage,
                            onUnlockVault = { password ->
                                vaultViewModel.unlockVault(password)
                            },
                            onUseRecoveryKey = {
                                showRecoveryUnlockScreen = true
                                vaultViewModel.clearError()
                            }
                        )
                    }

                    vaultState is VaultUiState.RecoveryUnlocked -> {
                        VaultSetupScreen(
                            errorMessage = errorMessage,
                            onCreateVault = { newPassword ->
                                vaultViewModel.resetMasterPassword(newPassword)
                            },
                            title = "Reset Master Password",
                            subtitle = "Create a new master password for your vault.",
                            buttonText = "Reset Password"
                        )
                    }

                    vaultState is VaultUiState.Unlocked -> {
                        KeyForgeApp(viewModel = viewModel)
                    }

                    vaultState is VaultUiState.Error -> {
                        Text("Error loading vault")
                    }
                }
            }
        }
    }
}

/**
 * Displays the unlocked credential vault experience.
 *
 * This composable owns the simple v1 screen flow for listing credentials,
 * viewing credential details, and opening the add/edit form. Sensitive data
 * should only reach this flow after the vault has been unlocked.
 */
@Composable
fun KeyForgeApp(
    viewModel: CredentialViewModel
) {
    val credentials = viewModel.credentials.collectAsStateWithLifecycle()

    var showFormScreen by remember { mutableStateOf(false) }
    var selectedCredential by remember { mutableStateOf<Credential?>(null) }
    var editingCredential by remember { mutableStateOf<Credential?>(null) }

    when {
        showFormScreen -> {
            val credential = editingCredential

            CredentialFormScreen(
                existingCredential = credential,
                onSave = {
                    if (credential != null) {
                        viewModel.updateCredential(it)
                        editingCredential = null
                        selectedCredential = null
                    } else {
                        viewModel.addCredential(it)
                    }
                    showFormScreen = false
                },
                onCancel = {
                    editingCredential = null
                    showFormScreen = false
                }
            )
        }

        selectedCredential != null -> {
            val credential = selectedCredential

            if (credential != null) {
                CredentialDetailScreen(
                    credential = credential,
                    onBackClick = {
                        selectedCredential = null
                    },
                    onEditClick = {
                        showFormScreen = true
                        editingCredential = credential
                    },
                    onDeleteClick = {
                        viewModel.deleteCredential(credential)
                        selectedCredential = null
                    }
                )
            }
        }

        else -> {
            CredentialListScreen(
                credentials = credentials.value,
                onAddClick = { showFormScreen = true },
                onCredentialClick = { credential ->
                    selectedCredential = credential
                }
            )
        }
    }

    BackHandler(enabled = showFormScreen || selectedCredential != null) {
        when {
            showFormScreen -> {
                editingCredential = null
                showFormScreen = false
            }
            selectedCredential != null -> {
                selectedCredential = null
            }
        }
    }
}