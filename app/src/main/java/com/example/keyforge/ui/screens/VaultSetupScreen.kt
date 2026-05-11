package com.example.keyforge.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.keyforge.ui.components.KeyForgePasswordField
import com.example.keyforge.ui.theme.keyForgeOutlinedTextFieldColors

/**
 * Screen used for both first-time vault creation and master password reset.
 *
 * The displayed title, subtitle, and button text can be customized so the same
 * form supports setup and recovery-driven password reset.
 */
@Composable
fun VaultSetupScreen(
    errorMessage: String?,
    onCreateVault: (String) -> Unit,
    title: String = "Create Your Vault",
    subtitle: String = "Set a master password to protect your credentials. This password is not stored and cannot be recovered.",
    buttonText: String = "Create Vault"
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val confirmPasswordFocusRequester = remember { FocusRequester() }

    fun submit() {
        localError = when {
            password.isBlank() -> "Master password cannot be blank."
            password.length < 8 -> "Master password must be at least 8 characters."
            password != confirmPassword -> "Passwords do not match."
            else -> null
        }

        if (localError == null) {
            onCreateVault(password)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            localError = null
        }
    }

    VaultAuthLayout(
        title = title,
        subtitle = subtitle
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
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { confirmPasswordFocusRequester.requestFocus() }
            )
        )

        KeyForgePasswordField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
            },
            label = { Text("Confirm Master Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .focusRequester(confirmPasswordFocusRequester),
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
            Text(buttonText)
        }
    }
}