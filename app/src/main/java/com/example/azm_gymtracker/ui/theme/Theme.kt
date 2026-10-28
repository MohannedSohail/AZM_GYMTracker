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
    primary = AzmLightPrimary,
    onPrimary = Color.White,
    primaryContainer = AzmLightSurfaceSelected,
    onPrimaryContainer = AzmLightPrimary,
    secondary = AzmDeepGreen,
    onSecondary = Color.White,
    tertiary = MemberColorGold,
    background = AzmLightBackground,
    onBackground = AzmLightOnSurface,
    surface = AzmLightSurface,
    onSurface = AzmLightOnSurface,
    surfaceVariant = AzmLightSurfaceVariant,
    onSurfaceVariant = AzmLightOnSurfaceMuted,
    outline = AzmLightBorder,
    error = AzmLightError,
    onError = Color.White,
)

fun interface ThemeToggleController {
    operator fun invoke(origin: androidx.compose.ui.geometry.Offset?)
}

operator fun ThemeToggleController.invoke() = invoke(null)

val LocalThemeIsDark = androidx.compose.runtime.compositionLocalOf { true }
val LocalThemeToggle = androidx.compose.runtime.compositionLocalOf { ThemeToggleController { _ -> } }

@Composable
fun AZM_GYMTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: ThemeToggleController = ThemeToggleController { _ -> },
    content: @Composable () -> Unit,
) {
    val targetColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = targetColorScheme.background.toArgb()
                window.navigationBarColor = targetColorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeIsDark provides darkTheme,
        LocalThemeToggle provides onToggleTheme,
    ) {
        MaterialTheme(
            colorScheme = targetColorScheme,
            typography = Typography,
            content = content,
        )
    }
}

@Composable
fun AZM_GYMTrackerTheme(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    content: @Composable () -> Unit,
) = AZM_GYMTrackerTheme(
    darkTheme = darkTheme,
    onToggleTheme = ThemeToggleController { onToggleTheme() },
    content = content,
)