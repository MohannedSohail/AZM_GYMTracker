package com.example.azm_gymtracker

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.view.drawToBitmap
import com.example.azm_gymtracker.ui.animation.FluidThemeRevealCanvas
import com.example.azm_gymtracker.ui.animation.ThemeRevealStyle
import com.example.azm_gymtracker.ui.navigation.AppNavHost
import com.example.azm_gymtracker.ui.theme.AZM_GYMTrackerTheme
import com.example.azm_gymtracker.ui.theme.ThemeToggleController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val themePrefs by lazy {
        getSharedPreferences("azm_theme_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as AzmApplication).container
        val initialDarkTheme = themePrefs.getBoolean("is_dark_theme", true)

        setContent {
            var isDarkTheme by remember { mutableStateOf(initialDarkTheme) }
            val view = LocalView.current
            val coroutineScope = rememberCoroutineScope()

            var oldBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var revealOrigin by remember { mutableStateOf<Offset?>(null) }
            val animProgress = remember { Animatable(0f) }

            // Switch between CIRCULAR_MASK and FLUID_DROP anytime to compare!
            val activeRevealStyle = ThemeRevealStyle.CIRCULAR_MASK

            val toggleController = remember {
                ThemeToggleController { origin ->
                    if (animProgress.isRunning) return@ThemeToggleController

                    if (view.width > 0 && view.height > 0) {
                        try {
                            oldBitmap = view.drawToBitmap()
                        } catch (_: Throwable) {
                            oldBitmap = null
                        }
                    }

                    revealOrigin = origin
                    val newTheme = !isDarkTheme
                    isDarkTheme = newTheme
                    themePrefs.edit { putBoolean("is_dark_theme", newTheme) }

                    val durationMs = if (activeRevealStyle == ThemeRevealStyle.CIRCULAR_MASK) 780 else 850
                    val easing = CubicBezierEasing(0.20f, 0.0f, 0.10f, 1.0f) // Ultra-smooth emphasized deceleration curve

                    coroutineScope.launch {
                        animProgress.snapTo(0f)
                        animProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = durationMs,
                                easing = easing,
                            ),
                        )
                        oldBitmap?.recycle()
                        oldBitmap = null
                    }
                }
            }

            AZM_GYMTrackerTheme(
                darkTheme = isDarkTheme,
                onToggleTheme = toggleController,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppNavHost(container = appContainer)
                    }

                    oldBitmap?.let { bitmap ->
                        FluidThemeRevealCanvas(
                            bitmap = bitmap,
                            progress = animProgress.value,
                            origin = revealOrigin,
                            isDark = isDarkTheme,
                            style = activeRevealStyle,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}