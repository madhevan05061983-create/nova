package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = NovaDeepSpace,
    primaryContainer = NovaCyanDark,
    onPrimaryContainer = NovaWhite,
    secondary = NovaCyanLight,
    onSecondary = NovaDeepSpace,
    background = NovaDeepSpace,
    onBackground = NovaWhite,
    surface = NovaSurfaceDark,
    onSurface = NovaWhite,
    surfaceVariant = Color(0xFF0F172A),
    onSurfaceVariant = NovaSlateText,
    outline = NovaCardBorder,
    error = NovaRose,
    onError = NovaWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
