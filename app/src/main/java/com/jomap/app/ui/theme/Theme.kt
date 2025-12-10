package com.jomap.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8), // Lighter Indigo for dark mode
    onPrimary = Color.White,
    secondary = Color(0xFFFB7185), // Lighter Rose
    onSecondary = Color.White,
    tertiary = Color(0xFF2DD4BF), // Lighter Teal
    background = Color(0xFF0F172A), // Dark Slate Background
    surface = Color(0xFF1E293B), // Dark Slate Surface
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF), // Very light Indigo for backgrounds
    onPrimaryContainer = PrimaryColor,

    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4E6), // Very light Rose
    onSecondaryContainer = Color(0xFF9F1239),

    tertiary = TertiaryColor,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),

    background = BackgroundColor,
    surface = SurfaceColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFCBD5E1) // Light grey for borders
)

@Composable
fun JoMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color to force your new creative brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure Type.kt exists
        content = content
    )
}