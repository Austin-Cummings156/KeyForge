package com.example.keyforge.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.keyforge.ui.theme.keyForgeOutlinedTextFieldColors

/**
 * Recovery-key unlock screen.
 *
 * A valid recovery key unlocks the vault key and sends the user into the master
 * password reset flow instead of directly replacing stored credentials.
 */
@Composable
fun RecoveryUnlockScreen(
    errorMessage: String?,
    onUnlockWithRecoveryKey: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var recoveryKey by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        localError = if (recoveryKey.isBlank()) {
            "Enter your recovery key."
        } else {
            null
        }

        if (localError == null) {
            onUnlockWithRecoveryKey(recoveryKey)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            localError = null
        }
    }

    VaultAuthLayout(
        title = "Recover Your Vault",
        subtitle = "Enter your recovery key to regain access and set a new master password."
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

        OutlinedTextField(
            value = recoveryKey,
            onValueChange = {
                recoveryKey = it.uppercase()
                localError = null
            },
            label = { Text("Recovery Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = keyForgeOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
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
            Text("Recover Vault")
        }

        Button(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Back to Login")
        }
    }
}