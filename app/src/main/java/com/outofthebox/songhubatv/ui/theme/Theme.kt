package com.outofthebox.songhubatv.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MicTeal,
    onPrimary = Color(0xFF003731),
    primaryContainer = MicTealDark,
    onPrimaryContainer = Color(0xFFE6FFFA),
    secondary = MicAmber,
    onSecondary = Color(0xFF442900),
    secondaryContainer = Color(0xFF5D4300),
    onSecondaryContainer = Color(0xFFFFE1A2),
    tertiary = MicPink,
    error = Color(0xFFFFB4AB),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE3E8EE),
    surface = Color(0xFF10151C),
    onSurface = Color(0xFFE3E8EE),
    surfaceVariant = Color(0xFF1A2130),
    onSurfaceVariant = Color(0xFFC3C9D3)
)

private val LightColorScheme = lightColorScheme(
    primary = MicTealDark,
    onPrimary = Color.White,
    secondary = Color(0xFF755B00),
    tertiary = MicPink,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF1A1D24),
    surface = Color(0xFFF9FAFB),
    onSurface = Color(0xFF1A1D24)
)

@Composable
fun SongHubATVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

fun Activity.exposeThemeCompat() = Unit