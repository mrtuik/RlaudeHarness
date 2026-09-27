package com.jarves.mh.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Neutral mid-grey used wherever the old palette had a coloured accent (readable on light and dark). */
val PocketAccent = Color(0xFF767467)
/** Warm neutral canvas (Claude-style paper tone) instead of pure black/white — still strict monochrome, just warmer. */
val PocketBackground = Color(0xFF262624)
val PocketSurface = Color(0xFF2D2D2B)
val PocketSurfaceVariant = Color(0xFF373634)
val PocketOutline = Color(0xFF454440)

// Strict monochrome, warmed to a Claude-style paper/ink palette instead of pure black/white greys.
// The only chromatic colour is a muted red reserved for errors / destructive actions.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFEDEBE4),
    onPrimary = Color(0xFF262624),
    primaryContainer = Color(0xFF34332F),
    onPrimaryContainer = Color(0xFFEDEBE4),
    inversePrimary = Color(0xFF262624),
    secondary = Color(0xFFC9C6BC),
    onSecondary = Color(0xFF262624),
    secondaryContainer = Color(0xFF34332F),
    onSecondaryContainer = Color(0xFFEDEBE4),
    tertiary = Color(0xFFC9C6BC),
    onTertiary = Color(0xFF262624),
    tertiaryContainer = Color(0xFF34332F),
    onTertiaryContainer = Color(0xFFEDEBE4),
    background = PocketBackground,
    onBackground = Color(0xFFEDEBE4),
    surface = PocketSurface,
    onSurface = Color(0xFFEDEBE4),
    surfaceVariant = PocketSurfaceVariant,
    onSurfaceVariant = Color(0xFFC9C6BC),
    surfaceTint = Color(0xFFEDEBE4),
    inverseSurface = Color(0xFFEDEBE4),
    inverseOnSurface = Color(0xFF262624),
    error = Color(0xFFE5A3A3),
    onError = Color(0xFF3A0D0D),
    errorContainer = Color(0xFF3A1D1D),
    onErrorContainer = Color(0xFFF5CFCF),
    outline = PocketOutline,
    outlineVariant = Color(0xFF3A3937),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3A3937),
    surfaceDim = Color(0xFF1E1E1C),
    surfaceContainerLowest = Color(0xFF191917),
    surfaceContainerLow = Color(0xFF232321),
    surfaceContainer = Color(0xFF2A2A28),
    surfaceContainerHigh = Color(0xFF34332F),
    surfaceContainerHighest = Color(0xFF3E3D38),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF262624),
    onPrimary = Color(0xFFF5F4EF),
    primaryContainer = Color(0xFFEBE9E1),
    onPrimaryContainer = Color(0xFF262624),
    inversePrimary = Color(0xFFEDEBE4),
    secondary = Color(0xFF57554C),
    onSecondary = Color(0xFFF5F4EF),
    secondaryContainer = Color(0xFFE6E3DA),
    onSecondaryContainer = Color(0xFF262624),
    tertiary = Color(0xFF57554C),
    onTertiary = Color(0xFFF5F4EF),
    tertiaryContainer = Color(0xFFE6E3DA),
    onTertiaryContainer = Color(0xFF262624),
    background = Color(0xFFF5F4EF),
    onBackground = Color(0xFF262624),
    surface = Color(0xFFFAF9F6),
    onSurface = Color(0xFF262624),
    surfaceVariant = Color(0xFFEDEBE4),
    onSurfaceVariant = Color(0xFF57554C),
    surfaceTint = Color(0xFF262624),
    inverseSurface = Color(0xFF262624),
    inverseOnSurface = Color(0xFFF5F4EF),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF5DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFFD9D6CC),
    outlineVariant = Color(0xFFE6E3DA),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE9E7DF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF9F6),
    surfaceContainer = Color(0xFFF2F0E9),
    surfaceContainerHigh = Color(0xFFECE9E1),
    surfaceContainerHighest = Color(0xFFE6E3DA),
)

enum class AppThemeMode { SYSTEM, DARK, LIGHT }

@Composable
fun PocketTheme(themeMode: AppThemeMode = AppThemeMode.LIGHT, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        content = content,
    )
}
