package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ImmersiveBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = ImmersiveBlueDark,
    secondary = ImmersiveEmerald,
    onSecondary = Color.White,
    secondaryContainer = ImmersiveEmeraldBg,
    onSecondaryContainer = ImmersiveEmeraldDark,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = CreamCanvas,
    onBackground = TextPrimary,
    surface = CreamCardBg,
    onSurface = TextPrimary,
    surfaceVariant = CreamInputBg,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White,
    outlineVariant = CreamBorder
)

private val DarkColorScheme = lightColorScheme(
    primary = ImmersiveBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = ImmersiveBlueDark,
    secondary = ImmersiveEmerald,
    onSecondary = Color.White,
    secondaryContainer = ImmersiveEmeraldBg,
    onSecondaryContainer = ImmersiveEmeraldDark,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = CreamCanvas,
    onBackground = TextPrimary,
    surface = CreamCardBg,
    onSurface = TextPrimary,
    surfaceVariant = CreamInputBg,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White,
    outlineVariant = CreamBorder
)

@Composable
fun SalonTheme(
    appTheme: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
