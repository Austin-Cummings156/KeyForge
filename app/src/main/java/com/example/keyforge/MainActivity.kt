package com.example.keyforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.keyforge.data.local.KeyForgeDatabase
import com.example.keyforge.data.model.Credential
import com.example.keyforge.data.repository.CredentialRepository
import com.example.keyforge.ui.CredentialViewModel
import com.example.keyforge.ui.CredentialViewModelFactory
import com.example.keyforge.ui.screens.CredentialDetailScreen
import com.example.keyforge.ui.screens.CredentialFormScreen
import com.example.keyforge.ui.screens.CredentialListScreen
import com.example.keyforge.ui.theme.KeyForgeTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CredentialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            KeyForgeDatabase::class.java,
            "keyforge_database"
        ).build()

        val repository = CredentialRepository(database.credentialDao())

        val factory = CredentialViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CredentialViewModel::class.java]

        setContent {
            val credentials = viewModel.credentials.collectAsStateWithLifecycle()

            var showFormScreen by remember { mutableStateOf(false) }
            var selectedCredential by remember { mutableStateOf<Credential?>(null) }
            var editingCredential by remember {mutableStateOf<Credential?>(null)}

            KeyForgeTheme {
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
                                    viewModel.insertCredential(it)
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
    }
}