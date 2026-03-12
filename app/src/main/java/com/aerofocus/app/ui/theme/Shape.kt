package com.aerofocus.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * AeroFocus shape system.
 *
 * - Cards and surfaces use generous 24dp rounding for a soft, premium feel.
 * - Buttons are fully pill-shaped (50dp) per the design spec.
 * - Small elements (chips, tags) use moderate rounding.
 */
val AeroFocusShapes = Shapes(

    // Chips, tags, small toggles
    extraSmall = RoundedCornerShape(8.dp),

    // Small cards, text fields
    small = RoundedCornerShape(12.dp),

    // Standard cards, dialogs, surfaces (24dp per spec)
    medium = RoundedCornerShape(24.dp),

    // Large containers, bottom sheets
    large = RoundedCornerShape(28.dp),

    // Pill-shaped buttons (per spec: RoundedCornerShape(50))
    extraLarge = RoundedCornerShape(50)
)
