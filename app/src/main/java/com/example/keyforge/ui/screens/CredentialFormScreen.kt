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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.keyforge.data.model.Credential

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
    var passwordVisible by remember { mutableStateOf(false) }

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
    }

    val isEditing = existingCredential != null
    val screenTitle = if (isEditing) "Edit Credential" else "Add Credential"
    val subtitle = if (isEditing) {
        "Update your saved login details"
    } else {
        "Add a new credential to your vault"
    }
    val saveButtonText = if (isEditing) "Update Credential" else "Save Credential"

    val backgroundColor = Color(0xFF121212)
    val fieldBackgroundColor = Color(0xFF1E1E1E)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color.White
    val textSecondary = Color(0xFFB3B3B3)
    val borderColor = Color(0xFF3A3A3A)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
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
                color = accentBlue
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = textPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = siteName,
            onValueChange = { siteName = it },
            label = { Text("Site Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedLabelColor = accentBlue,
                unfocusedLabelColor = textSecondary,
                focusedBorderColor = accentBlue,
                unfocusedBorderColor = borderColor,
                cursorColor = accentBlue,
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor
            )
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedLabelColor = accentBlue,
                unfocusedLabelColor = textSecondary,
                focusedBorderColor = accentBlue,
                unfocusedBorderColor = borderColor,
                cursorColor = accentBlue,
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        },
                        tint = Color.LightGray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedLabelColor = accentBlue,
                unfocusedLabelColor = textSecondary,
                focusedBorderColor = accentBlue,
                unfocusedBorderColor = borderColor,
                cursorColor = accentBlue,
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedLabelColor = accentBlue,
                unfocusedLabelColor = textSecondary,
                focusedBorderColor = accentBlue,
                unfocusedBorderColor = borderColor,
                cursorColor = accentBlue,
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor
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
                    contentColor = textPrimary
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (siteName.isNotBlank() && username.isNotBlank()) {
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
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = Color.White
                )
            ) {
                Text(saveButtonText)
            }
        }
    }
}