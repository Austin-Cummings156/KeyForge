package com.example.keyforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = KeyForgeAccentBlue,
    onPrimary = KeyForgeTextPrimary,

    secondary = KeyForgeAccentBlue,
    onSecondary = KeyForgeTextPrimary,

    tertiary = KeyForgeAccentBlue,
    onTertiary = KeyForgeTextPrimary,

    background = KeyForgeBackground,
    onBackground = KeyForgeTextPrimary,

    surface = KeyForgeSurface,
    onSurface = KeyForgeTextPrimary,

    surfaceVariant = KeyForgeSurface,
    onSurfaceVariant = KeyForgeTextSecondary,

    error = KeyForgeDestructiveRed,
    onError = KeyForgeTextPrimary,

    outline = KeyForgeBorder,
    outlineVariant = KeyForgeBorder,

    scrim = KeyForgeBackground
)

private val LightColorScheme = lightColorScheme(
    primary = KeyForgeAccentBlue,
    onPrimary = KeyForgeTextPrimary,

    secondary = KeyForgeAccentBlue,
    onSecondary = KeyForgeTextPrimary,

    tertiary = KeyForgeAccentBlue,
    onTertiary = KeyForgeTextPrimary,

    background = KeyForgeBackground,
    onBackground = KeyForgeTextPrimary,

    surface = KeyForgeSurface,
    onSurface = KeyForgeTextPrimary,

    surfaceVariant = KeyForgeSurface,
    onSurfaceVariant = KeyForgeTextSecondary,

    error = KeyForgeDestructiveRed,
    onError = KeyForgeTextPrimary,

    outline = KeyForgeBorder,
    outlineVariant = KeyForgeBorder,

    scrim = KeyForgeBackground
)

@Composable
fun KeyForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}