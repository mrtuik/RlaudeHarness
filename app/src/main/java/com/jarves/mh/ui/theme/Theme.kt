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
val PocketAccent = Color(0xFF737373)
val PocketBackground = Color(0xFF0A0A0A)
val PocketSurface = Color(0xFF141414)
val PocketSurfaceVariant = Color(0xFF1F1F1F)
val PocketOutline = Color(0xFF333333)

// Strict monochrome. The only chromatic colour is a muted red reserved for errors / destructive actions.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF111111),
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color(0xFFF2F2F2),
    inversePrimary = Color(0xFF111111),
    secondary = Color(0xFFBDBDBD),
    onSecondary = Color(0xFF111111),
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFFBDBDBD),
    onTertiary = Color(0xFF111111),
    tertiaryContainer = Color(0xFF2A2A2A),
    onTertiaryContainer = Color(0xFFF2F2F2),
    background = PocketBackground,
    onBackground = Color(0xFFEDEDED),
    surface = PocketSurface,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = PocketSurfaceVariant,
    onSurfaceVariant = Color(0xFFC9C9C9),
    surfaceTint = Color(0xFFF2F2F2),
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF111111),
    error = Color(0xFFE5A3A3),
    onError = Color(0xFF3A0D0D),
    errorContainer = Color(0xFF3A1D1D),
    onErrorContainer = Color(0xFFF5CFCF),
    outline = PocketOutline,
    outlineVariant = Color(0xFF2A2A2A),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2A2A2A),
    surfaceDim = Color(0xFF0A0A0A),
    surfaceContainerLowest = Color(0xFF050505),
    surfaceContainerLow = Color(0xFF101010),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1B1B1B),
    surfaceContainerHighest = Color(0xFF222222),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDEDED),
    onPrimaryContainer = Color(0xFF111111),
    inversePrimary = Color(0xFFF2F2F2),
    secondary = Color(0xFF555555),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF111111),
    tertiary = Color(0xFF555555),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE8E8E8),
    onTertiaryContainer = Color(0xFF111111),
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Color(0xFF3D3D3D),
    surfaceTint = Color(0xFF111111),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF2F2F2),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF5DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE4E4E4),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE6E6E6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color(0xFFECECEC),
    surfaceContainerHighest = Color(0xFFE6E6E6),
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
