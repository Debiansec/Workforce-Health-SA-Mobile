package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WfhRedPrimary,
    onPrimary = Color.White,
    primaryContainer = CriticalRedContainer,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = WfhRedVibrant,
    onSecondary = Color.White,
    secondaryContainer = WfhSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = ComplianceGreenLight,
    onTertiary = Color.White,
    tertiaryContainer = ComplianceGreenContainer,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = WfhDarkBackground,
    onBackground = WfhTextPrimary,
    surface = WfhSurfaceDark,
    onSurface = WfhTextPrimary,
    surfaceVariant = WfhSurfaceCard,
    onSurfaceVariant = WfhTextSecondary,
    outline = WfhBorderSubtle,
    error = CriticalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = WfhRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = WfhRedDark,
    onSecondary = Color.White,
    tertiary = ComplianceGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF455A64),
    outline = Color(0xFFCFD8DC),
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun WorkForceHealthTheme(
    darkTheme: Boolean = true, // Default to professional dark theme per prompt guidelines
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
