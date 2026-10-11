package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Sunlit Agriculture Color Scheme (v2)
private val AgriSunlitColorScheme = lightColorScheme(
    primary = AgriPrimaryFresh,
    onPrimary = Color.White,
    primaryContainer = AgriPrimaryContainer,
    onPrimaryContainer = AgriPrimaryText,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SkyBlueContainer,
    onSecondaryContainer = SkyBlueText,
    tertiary = SunGold,
    onTertiary = Color.White,
    tertiaryContainer = SunGoldContainer,
    onTertiaryContainer = SunGoldText,
    background = SunlitBackground,
    onBackground = TextSlatePrimary,
    surface = SunlitSurface,
    onSurface = TextSlatePrimary,
    surfaceVariant = SunlitSurfaceSubtle,
    onSurfaceVariant = TextSlateSecondary,
    outline = SunlitBorder,
    outlineVariant = SunlitBorderStrong,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedBg,
    onErrorContainer = AlertRed
)

@Composable
fun AgriTwinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgriSunlitColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AgriTwinTheme(content = content)
}
