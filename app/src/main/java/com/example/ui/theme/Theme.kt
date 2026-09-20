package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WorkbenchPrimary,
    onPrimary = WorkbenchOnPrimary,
    primaryContainer = WorkbenchPrimaryContainer,
    onPrimaryContainer = WorkbenchOnPrimaryContainer,
    secondary = WorkbenchSecondary,
    onSecondary = WorkbenchOnSecondary,
    secondaryContainer = WorkbenchSecondaryContainer,
    onSecondaryContainer = WorkbenchOnSecondaryContainer,
    tertiary = WorkbenchTertiary,
    onTertiary = WorkbenchOnTertiary,
    tertiaryContainer = WorkbenchTertiaryContainer,
    onTertiaryContainer = WorkbenchOnTertiaryContainer,
    background = WorkbenchBg,
    onBackground = WorkbenchTextPrimary,
    surface = WorkbenchSurface,
    onSurface = WorkbenchTextPrimary,
    surfaceVariant = WorkbenchSurfaceVariant,
    onSurfaceVariant = WorkbenchTextSecondary,
    outline = WorkbenchBorder,
    outlineVariant = WorkbenchBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark by default as requested in spec
    dynamicColor: Boolean = false, // Purposeful dev workbench palette overridden
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
