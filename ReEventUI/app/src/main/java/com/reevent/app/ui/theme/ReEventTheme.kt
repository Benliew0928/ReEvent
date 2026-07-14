package com.reevent.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReEventLightScheme = lightColorScheme(
    primary = ReEventGreen,
    onPrimary = Color.White,
    primaryContainer = ReEventMint,
    onPrimaryContainer = ReEventGreenDeep,
    secondary = ReEventBlue,
    onSecondary = Color.White,
    secondaryContainer = ReEventBlueSoft,
    onSecondaryContainer = Color(0xFF173B46),
    tertiary = ReEventCoral,
    onTertiary = Color.White,
    tertiaryContainer = ReEventCoralSoft,
    onTertiaryContainer = Color(0xFF552012),
    background = ReEventBackground,
    onBackground = ReEventInk,
    surface = ReEventSurface,
    onSurface = ReEventInk,
    surfaceVariant = ReEventSurfaceAlt,
    onSurfaceVariant = ReEventTextSecondary,
    outline = ReEventLine,
    outlineVariant = ReEventLine
)

@Composable
fun ReEventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReEventLightScheme,
        typography = ReEventTypography,
        shapes = ReEventShapes,
        content = content
    )
}
