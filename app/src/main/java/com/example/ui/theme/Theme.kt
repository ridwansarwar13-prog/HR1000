package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraCyberDarkColorScheme = darkColorScheme(
    primary = AuraPurpleLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E1065),
    onPrimaryContainer = AuraPurpleGlow,
    secondary = AuraCyanNeon,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = AuraCyan,
    tertiary = AuraGold,
    onTertiary = Color.Black,
    background = CyberBgDark,
    onBackground = Color.White,
    surface = CyberSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = CyberCardDark,
    onSurfaceVariant = Color.LightGray
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AuraCyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
