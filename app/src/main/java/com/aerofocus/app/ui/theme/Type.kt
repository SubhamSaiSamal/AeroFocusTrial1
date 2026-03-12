package com.aerofocus.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * AeroFocus typography system.
 *
 * - **Timer Display**: Light-weight serif at 82sp for the premium analog feel.
 * - **Headers**: Bold sans-serif at 28sp.
 * - **Body**: Medium sans-serif at 16sp.
 *
 * Uses system default serif and sans-serif families to avoid bundling
 * custom fonts in the MVP. Replace with Google Fonts (e.g., Noto Serif,
 * Inter) post-MVP by updating the FontFamily references.
 */

val TimerFontFamily = FontFamily.Serif
val UiFontFamily = FontFamily.SansSerif

val AeroFocusTypography = Typography(

    // ── Timer Display ───────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 82.sp,
        letterSpacing = 2.sp,
        color = WarmGlow
    ),

    // ── Large header for screen titles ──────────────────────
    displayMedium = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        letterSpacing = 1.sp
    ),

    // ── Section headers ─────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),

    // ── Card titles / prominent labels ──────────────────────
    titleLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body Text ───────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.25.sp,
        lineHeight = 24.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp
    ),

    bodySmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp
    ),

    // ── Labels / Chips / Tags ───────────────────────────────
    labelLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),

    labelMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),

    labelSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp
    )
)
