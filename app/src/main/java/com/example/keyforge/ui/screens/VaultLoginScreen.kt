package com.example.keyforge.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.keyforge.ui.components.KeyForgePasswordField
import com.example.keyforge.ui.theme.keyForgeOutlinedTextFieldColors

/**
 * Login screen for unlocking an existing vault with the master password.
 *
 * Also exposes the recovery-key path when the user cannot unlock with their
 * master password.
 */
@Composable
fun VaultLoginScreen(
    errorMessage: String?,
    onUnlockVault: (String) -> Unit,
    onUseRecoveryKey: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        localError = if (password.isBlank()) {
            "Enter your master password."
        } else {
            null
        }

        if (localError == null) {
            onUnlockVault(password)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            localError = null
        }
    }

    VaultAuthLayout(
        title = "Unlock KeyForge",
        subtitle = "Enter your master password to unlock your vault."
    ) {
        val displayedError = localError ?: errorMessage
        if (displayedError != null) {
            Text(
                text = displayedError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        KeyForgePasswordField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = { Text("Master Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = keyForgeOutlinedTextFieldColors(),
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            )
        )

        Button(
            onClick = { submit() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Unlock Vault")
        }

        TextButton(
            onClick = onUseRecoveryKey,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Use Recovery Key")
        }
    }
}