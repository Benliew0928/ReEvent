package com.reevent.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ReEventGreen = Color(0xFF005A45)
val ReEventGreenDeep = Color(0xFF003E33)
val ReEventMint = Color(0xFF8ACB88)
val ReEventMintSoft = Color(0xFFE3F3DF)
val ReEventCanvas = Color(0xFFF7EFE3)
val ReEventPaper = Color(0xFFFFFBF4)
val ReEventWarm = Color(0xFFFFC35B)
val ReEventCoral = Color(0xFFE86D4C)
val ReEventBlue = Color(0xFF4E8EA8)
val ReEventInk = Color(0xFF20312D)
val ReEventMuted = Color(0xFF63736E)
val ReEventLine = Color(0xFFE2D7C7)

private val ReEventLightScheme = lightColorScheme(
    primary = ReEventGreen,
    onPrimary = Color.White,
    primaryContainer = ReEventMintSoft,
    onPrimaryContainer = ReEventGreenDeep,
    secondary = ReEventBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEEF4),
    onSecondaryContainer = Color(0xFF173B46),
    tertiary = ReEventCoral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0D6),
    onTertiaryContainer = Color(0xFF552012),
    background = ReEventCanvas,
    onBackground = ReEventInk,
    surface = ReEventPaper,
    onSurface = ReEventInk,
    surfaceVariant = Color(0xFFF0E5D4),
    onSurfaceVariant = ReEventMuted,
    outline = Color(0xFFCDBFAB),
    outlineVariant = ReEventLine
)

private val ReEventTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 39.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun ReEventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReEventLightScheme,
        typography = ReEventTypography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
