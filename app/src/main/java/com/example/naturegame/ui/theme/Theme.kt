package com.example.naturegame.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = GreenGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = LightGreen40,
    background = Color(0xFFFBFDF8),
    surface = Color(0xFFFBFDF8),
    surfaceVariant = Color(0xFFE1E5DC),
    onSurfaceVariant = Color(0xFF43493E),
    outline = Color(0xFF74796D),

    surfaceContainer = Color(0xFFE8F5E9),
    surfaceContainerLow = Color(0xFFF1F8F1),
    surfaceContainerHigh = Color(0xFFDFF0DF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFD7E6D7)
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = GreenGrey80,
    secondaryContainer = Color(0xFF388E3C),
    onSecondaryContainer = Color(0xFFE8F5E9),
    tertiary = LightGreen80,
    background = Color(0xFF1A1C19),
    surface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFF43493E),
    onSurfaceVariant = Color(0xFFC3C8BB),
    outline = Color(0xFF8D9286),

    // Tumman teeman pinnat
    surfaceContainer = Color(0xFF1D201D),
    surfaceContainerLow = Color(0xFF1A1C19),
    surfaceContainerHigh = Color(0xFF242B24),
    surfaceContainerLowest = Color(0xFF0C0E0C),
    surfaceContainerHighest = Color(0xFF2F362F)
)

@Composable
fun LuontopeliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
