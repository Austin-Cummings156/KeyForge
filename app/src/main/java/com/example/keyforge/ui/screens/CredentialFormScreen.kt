package com.example.keyforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.keyforge.data.model.Credential
import com.example.keyforge.ui.components.KeyForgePasswordField
import com.example.keyforge.ui.theme.keyForgeOutlinedTextFieldColors

/**
 * Add/edit form for credentials.
 *
 * The same form supports creating new credentials and updating existing ones.
 * Encryption happens after save in the repository layer, not inside the UI.
 */
@Composable
fun CredentialFormScreen(
    onSave: (Credential) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    existingCredential: Credential? = null
) {
    var siteName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    LaunchedEffect(existingCredential) {
        if (existingCredential != null) {
            siteName = existingCredential.siteName
            username = existingCredential.username
            password = existingCredential.password
            notes = existingCredential.notes
        } else {
            siteName = ""
            username = ""
            password = ""
            notes = ""
        }
        localError = null
    }

    val isEditing = existingCredential != null
    val screenTitle = if (isEditing) "Edit Credential" else "Add Credential"
    val subtitle = if (isEditing) {
        "Update your saved login details"
    } else {
        "Add a new credential to your vault"
    }
    val saveButtonText = if (isEditing) "Update Credential" else "Save Credential"

    fun submit() {
        localError = when {
            siteName.isBlank() && username.isBlank() -> "Site name and username are required."
            siteName.isBlank() -> "Site name is required."
            username.isBlank() -> "Username is required."
            password.isBlank() -> "Password is required."
            else -> null
        }

        if (localError == null) {
            val credential = existingCredential?.copy(
                siteName = siteName,
                username = username,
                password = password,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            ) ?: Credential(
                siteName = siteName,
                username = username,
                password = password,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )

            onSave(credential)

            if (!isEditing) {
                siteName = ""
                username = ""
                password = ""
                notes = ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Back",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (localError != null) {
            Text(
                text = localError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = siteName,
            onValueChange = {
                siteName = it
                localError = null
            },
            label = { Text("Site Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = keyForgeOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { usernameFocusRequester.requestFocus() }
            )
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                localError = null
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(usernameFocusRequester),
            colors = keyForgeOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            )
        )

        KeyForgePasswordField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            colors = keyForgeOutlinedTextFieldColors(),
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { notesFocusRequester.requestFocus() }
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
                localError = null
            },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 200.dp)
                .focusRequester(notesFocusRequester),
            colors = keyForgeOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { submit() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(saveButtonText)
            }
        }
    }
}