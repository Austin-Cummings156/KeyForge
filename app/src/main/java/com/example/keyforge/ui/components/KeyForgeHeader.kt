package com.example.keyforge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.keyforge.R

/**
 * Shared KeyForge branding header used across authentication and vault screens.
 *
 * Keeping this header centralized helps the app feel consistent while moving
 * between setup, login, recovery, and credential screens.
 */
@Composable
fun KeyForgeHeader(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.keyforge_header),
        contentDescription = "KeyForge Header",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}