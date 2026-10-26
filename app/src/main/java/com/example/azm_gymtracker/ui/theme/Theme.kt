package com.example.azm_gymtracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzmEmeraldGreen,
    onPrimary = AzmDarkBackground,
    primaryContainer = AzmDarkSurfaceSelected,
    onPrimaryContainer = AzmEmeraldGreen,
    secondary = AzmNeonGreen,
    onSecondary = AzmDarkBackground,
    tertiary = MemberColorGold,
    background = AzmDarkBackground,
    onBackground = AzmDarkOnSurface,
    surface = AzmDarkSurface,
    onSurface = AzmDarkOnSurface,
    surfaceVariant = AzmDarkSurfaceVariant,
    onSurfaceVariant = AzmDarkOnSurfaceMuted,
    outline = AzmBorderSubtle,
    error = AzmError,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AzmDeepGreen,
    onPrimary = Color.White,
    primaryContainer = AzmLightSurfaceSelected,
    onPrimaryContainer = AzmDeepGreen,
    secondary = AzmEmeraldGreen,
    onSecondary = AzmLightOnSurface,
    tertiary = MemberColorGold,
    background = AzmLightBackground,
    onBackground = AzmLightOnSurface,
    surface = AzmLightSurface,
    onSurface = AzmLightOnSurface,
    surfaceVariant = AzmLightSurfaceVariant,
    onSurfaceVariant = AzmLightOnSurfaceMuted,
    outline = AzmLightBorder,
    error = AzmError,
    onError = Color.White,
)

@Composable
fun AZM_GYMTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}