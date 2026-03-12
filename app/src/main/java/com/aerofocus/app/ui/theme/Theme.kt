package com.aerofocus.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AeroFocus is dark-mode only — the aviation night-sky aesthetic demands it.
 *
 * This theme maps our custom palette to Material 3's color scheme slots
 * so standard M3 components (buttons, cards, dialogs) automatically use
 * the correct colors without per-component overrides.
 */

private val AeroFocusDarkColorScheme = darkColorScheme(
    // ── Primary (Warm Glow — for FABs, active elements) ─────
    primary = WarmGlow,
    onPrimary = TextOnAccent,
    primaryContainer = WarmGlow.copy(alpha = 0.15f),
    onPrimaryContainer = WarmGlow,

    // ── Secondary (Muted Sunset — for warnings, toggles) ────
    secondary = MutedSunset,
    onSecondary = TextPrimary,
    secondaryContainer = MutedSunset.copy(alpha = 0.15f),
    onSecondaryContainer = MutedSunset,

    // ── Tertiary (Sky Blue — for info accents) ──────────────
    tertiary = SkyBlue,
    onTertiary = TextOnAccent,
    tertiaryContainer = SkyBlue.copy(alpha = 0.15f),
    onTertiaryContainer = SkyBlue,

    // ── Background & Surface ────────────────────────────────
    background = DeepNight,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SegmentActive,
    onSurfaceVariant = TextSecondary,

    // ── Outline ─────────────────────────────────────────────
    outline = TextSecondary,
    outlineVariant = SurfaceDark,

    // ── Error ───────────────────────────────────────────────
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed
)

@Composable
fun AeroFocusTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AeroFocusDarkColorScheme

    // Make the system bars match the theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNight.toArgb()
            window.navigationBarColor = DeepNight.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AeroFocusTypography,
        shapes = AeroFocusShapes,
        content = content
    )
}
