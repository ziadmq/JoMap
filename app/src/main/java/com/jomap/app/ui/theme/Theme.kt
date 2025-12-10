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
    primary =  PrimaryColor,
    secondary =  SecondaryColor,
    tertiary = TertiaryColor,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariant, // لون خلفية العناصر الأقل أهمية
    onPrimaryContainer = PrimaryColor,

    secondary = SecondaryColor,
    onSecondary = Color.White,

    background = BackgroundLight,
    surface = SurfaceWhite,
    onSurface = TextPrimary,

    tertiary = TertiaryColor
)
// ... (DarkColorScheme يمكن تركه كما هو أو تعديله لاحقاً)

@Composable
fun JoMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // عطلنا الألوان الديناميكية لنثبت ألوان هويتنا
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
        typography = Typography,
        content = content
    )
}
