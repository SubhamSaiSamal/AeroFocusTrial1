package com.aerofocus.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AeroFocus night-sky color palette.
 *
 * Designed for maximum contrast and a premium, aviation-inspired feel.
 * All UI surfaces use the deep dark base; accents are warm sunset tones.
 */

// ── Primary Backgrounds ─────────────────────────────────────────────
val DeepNight = Color(0xFF0B0C10)      // Root scaffold / screen background
val SurfaceDark = Color(0xFF1F2833)    // Cards, dialogs, unselected elements
val SegmentActive = Color(0xFF3A4750)  // Active segment in toggle bar

// ── Accent Colors ───────────────────────────────────────────────────
val WarmGlow = Color(0xFFFFB347)       // Primary: timer text, buttons, route lines
val MutedSunset = Color(0xFFE27D60)    // Secondary: emergency landing, warnings
val SkyBlue = Color(0xFF66B2FF)        // Tertiary: map water, info accents

// ── Text ────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFFFFFFF)    // High contrast white
val TextSecondary = Color(0xFF86909C)  // Tags, subtitles, inactive icons
val TextOnAccent = Color(0xFF0B0C10)   // Text on top of WarmGlow buttons

// ── Status Colors ───────────────────────────────────────────────────
val SuccessGreen = Color(0xFF4CAF50)   // Successful landing indicator
val ErrorRed = Color(0xFFEF5350)       // Failed session / emergency

// ── Frosted Glass ───────────────────────────────────────────────────
val FrostedGlass = Color(0x331F2833)   // 20% opacity surface for glass effect
val FrostedGlassBorder = Color(0x55FFFFFF)  // Subtle white border for glass

// ── Map ─────────────────────────────────────────────────────────────
val MapBackground = Color(0xFF0D1117)  // Slightly different from root for depth
val MapLandmass = Color(0xFF1A2332)    // Continents / landmass fill
val MapRouteLine = Color(0x88FFB347)   // Route line with 53% opacity
val NodeGlow = Color(0xFFFFB347)       // Pulsating city node
val NodeLocked = Color(0xFF3A4750)     // Locked city node (dimmed)
