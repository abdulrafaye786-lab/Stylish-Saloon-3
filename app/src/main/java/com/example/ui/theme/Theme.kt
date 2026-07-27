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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF78350F),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    outlineVariant = Color(0xFF475569)
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
