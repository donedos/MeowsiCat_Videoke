package com.outofthebox.meowsicattv.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val DefaultDarkColorScheme = darkColorScheme(
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

private fun buildDarkColorScheme(appTheme: Int): ColorScheme {
    return when (appTheme) {
        1 -> darkColorScheme( // iOS Blue
            primary = iOSBlue,
            onPrimary = Color.White,
            primaryContainer = iOSBlueDark,
            onPrimaryContainer = Color(0xFFE6F0FF),
            secondary = iOSCyan,
            onSecondary = Color(0xFF00324A),
            secondaryContainer = Color(0xFF004A6E),
            onSecondaryContainer = Color(0xFFCBEBFF),
            tertiary = iOSPurple,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        2 -> darkColorScheme( // iOS Purple
            primary = iOSPurple,
            onPrimary = Color.White,
            primaryContainer = iOSPurpleDark,
            onPrimaryContainer = Color(0xFFF6E6FF),
            secondary = iOSSystemPink,
            onSecondary = Color(0xFF4A000E),
            secondaryContainer = Color(0xFF6E0018),
            onSecondaryContainer = Color(0xFFFFD9DF),
            tertiary = iOSBlue,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        3 -> darkColorScheme( // iOS Indigo
            primary = iOSIndigo,
            onPrimary = Color.White,
            primaryContainer = iOSIndigoDark,
            onPrimaryContainer = Color(0xFFEAEBFF),
            secondary = iOSBlue,
            onSecondary = Color(0xFF002252),
            secondaryContainer = Color(0xFF003882),
            onSecondaryContainer = Color(0xFFD6E4FF),
            tertiary = iOSSystemPink,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        4 -> darkColorScheme( // iOS Pink
            primary = iOSSystemPink,
            onPrimary = Color.White,
            primaryContainer = iOSPinkDark,
            onPrimaryContainer = Color(0xFFFFE5E9),
            secondary = iOSOrange,
            onSecondary = Color(0xFF421E00),
            secondaryContainer = Color(0xFF633000),
            onSecondaryContainer = Color(0xFFFFDCC2),
            tertiary = iOSPurple,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        5 -> darkColorScheme( // iOS Orange
            primary = iOSOrange,
            onPrimary = Color.White,
            primaryContainer = iOSOrangeDark,
            onPrimaryContainer = Color(0xFFFFEEDD),
            secondary = iOSYellow,
            onSecondary = Color(0xFF3E3000),
            secondaryContainer = Color(0xFF5A4700),
            onSecondaryContainer = Color(0xFFFFE27A),
            tertiary = iOSSystemPink,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        6 -> darkColorScheme( // iOS Green
            primary = iOSGreen,
            onPrimary = Color.White,
            primaryContainer = iOSGreenDark,
            onPrimaryContainer = Color(0xFFE2F9E7),
            secondary = iOSBlue,
            onSecondary = Color(0xFF00324A),
            secondaryContainer = Color(0xFF004A6E),
            onSecondaryContainer = Color(0xFFCBEBFF),
            tertiary = iOSOrange,
            error = Color(0xFFFFB4AB),
            background = Color(0xFF0B0F14),
            onBackground = Color(0xFFE3E8EE),
            surface = Color(0xFF10151C),
            onSurface = Color(0xFFE3E8EE),
            surfaceVariant = Color(0xFF1A2130),
            onSurfaceVariant = Color(0xFFC3C9D3)
        )
        else -> DefaultDarkColorScheme
    }
}

@Composable
fun MeowsiCatTheme(
    appTheme: Int = 0,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(appTheme) {
        buildDarkColorScheme(appTheme)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun Activity.exposeThemeCompat() = Unit
