package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = ElegantSecondary,
    onSecondary = ElegantOnSecondary,
    secondaryContainer = ElegantSecondaryContainer,
    onSecondaryContainer = ElegantOnSecondaryContainer,
    background = ElegantBackground,
    onBackground = ElegantOnBackground,
    surface = ElegantSurface,
    onSurface = ElegantOnSurface,
    surfaceVariant = ElegantCardBg,
    onSurfaceVariant = ElegantOnSurfaceSecondary,
    outline = ElegantBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the elegant dark visual style
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve perfect fidelity
    content: @Composable () -> Unit,
) {
    // For visual design fidelity as requested by the Elegant Dark specification
    val colorScheme = ElegantDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
