package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colors strictly from DESIGN.md and DESIGN_Dark.md
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF37e1e6),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFF006063),
    onPrimaryContainer = Color(0xFF37e1e6),
    background = Color(0xFF191c1e), // on-surface from light is background for dark
    onBackground = Color(0xFFeff1f3),
    surface = Color(0xFF2d3133), // inverse-surface
    onSurface = Color(0xFFeff1f3),
    surfaceVariant = Color(0xFF3b494a),
    onSurfaceVariant = Color(0xFFbac9c9),
    error = Color(0xFFffdad6),
    onError = Color(0xFF93000a)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00696c),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFF37e1e6),
    onPrimaryContainer = Color(0xFF006063),
    background = Color(0xFFf7f9fb),
    onBackground = Color(0xFF191c1e),
    surface = Color(0xFFffffff), // surface-container-lowest
    onSurface = Color(0xFF191c1e),
    surfaceVariant = Color(0xFFe0e3e5), // surface-variant
    onSurfaceVariant = Color(0xFF3b494a),
    error = Color(0xFFba1a1a),
    onError = Color(0xFFffffff)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
