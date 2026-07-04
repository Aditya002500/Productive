package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * CaptureFlow design tokens — single source of truth.
 *
 * Palette: forest green accent, near-black neutrals, white surfaces (light)
 * / charcoal surfaces (dark). No screen should hardcode a hex value; every
 * color used in UI must come from MaterialTheme.colorScheme or the semantic
 * accents exposed at the bottom of this file.
 */

// ---------------------------------------------------------------------------
// Brand green ramp
// ---------------------------------------------------------------------------
val Green50 = Color(0xFFF0FDF4)
val Green100 = Color(0xFFDCFCE7)
val Green200 = Color(0xFFBBF7D0)
val Green300 = Color(0xFF86EFAC)
val Green400 = Color(0xFF4ADE80)
val Green500 = Color(0xFF22C55E)
val Green600 = Color(0xFF16A34A)
val Green700 = Color(0xFF15803D) // Primary — deep forest green
val Green800 = Color(0xFF166534)
val Green900 = Color(0xFF14532D)

// ---------------------------------------------------------------------------
// Neutral ramp (blacks / whites)
// ---------------------------------------------------------------------------
val Neutral0 = Color(0xFFFFFFFF)
val Neutral50 = Color(0xFFF8FAF9)
val Neutral100 = Color(0xFFF1F5F3)
val Neutral200 = Color(0xFFE4E9E6)
val Neutral300 = Color(0xFFCBD5D0)
val Neutral400 = Color(0xFF9AA6A0)
val Neutral500 = Color(0xFF6B7770)
val Neutral600 = Color(0xFF4B5651)
val Neutral700 = Color(0xFF333B37)
val Neutral800 = Color(0xFF1E2422)
val Neutral850 = Color(0xFF171B19)
val Neutral900 = Color(0xFF111413)
val Neutral950 = Color(0xFF0B0E0D)

// ---------------------------------------------------------------------------
// Light color scheme tokens
// ---------------------------------------------------------------------------
val LightPrimary = Green700
val LightOnPrimary = Neutral0
val LightPrimaryContainer = Green100
val LightOnPrimaryContainer = Green900

val LightSecondary = Green600
val LightOnSecondary = Neutral0
val LightSecondaryContainer = Green50
val LightOnSecondaryContainer = Green800

val LightTertiary = Green800
val LightOnTertiary = Neutral0
val LightTertiaryContainer = Green200
val LightOnTertiaryContainer = Green900

val LightBackground = Neutral50
val LightOnBackground = Neutral900
val LightSurface = Neutral0
val LightOnSurface = Neutral900
val LightSurfaceVariant = Neutral100
val LightOnSurfaceVariant = Neutral500

val LightSurfaceContainerLowest = Neutral0
val LightSurfaceContainerLow = Neutral50
val LightSurfaceContainer = Neutral100
val LightSurfaceContainerHigh = Neutral200
val LightSurfaceContainerHighest = Neutral300

val LightOutline = Neutral300
val LightOutlineVariant = Neutral200

val LightInverseSurface = Neutral900
val LightInverseOnSurface = Neutral50
val LightInversePrimary = Green400

// ---------------------------------------------------------------------------
// Dark color scheme tokens
// ---------------------------------------------------------------------------
val DarkPrimary = Green400
val DarkOnPrimary = Green900
val DarkPrimaryContainer = Green800
val DarkOnPrimaryContainer = Green100

val DarkSecondary = Green300
val DarkOnSecondary = Green900
val DarkSecondaryContainer = Green700
val DarkOnSecondaryContainer = Green100

val DarkTertiary = Green300
val DarkOnTertiary = Green900
val DarkTertiaryContainer = Green800
val DarkOnTertiaryContainer = Green100

val DarkBackground = Neutral950
val DarkOnBackground = Neutral100
val DarkSurface = Neutral900
val DarkOnSurface = Neutral100
val DarkSurfaceVariant = Neutral800
val DarkOnSurfaceVariant = Neutral400

val DarkSurfaceContainerLowest = Neutral950
val DarkSurfaceContainerLow = Neutral900
val DarkSurfaceContainer = Neutral850
val DarkSurfaceContainerHigh = Neutral800
val DarkSurfaceContainerHighest = Neutral700

val DarkOutline = Neutral600
val DarkOutlineVariant = Neutral700

val DarkInverseSurface = Neutral100
val DarkInverseOnSurface = Neutral900
val DarkInversePrimary = Green700

// ---------------------------------------------------------------------------
// Shared status colors (both schemes)
// ---------------------------------------------------------------------------
val Error = Color(0xFFDC2626)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFEE2E2)
val OnErrorContainer = Color(0xFF7F1D1D)

val DarkError = Color(0xFFFCA5A5)
val DarkOnError = Color(0xFF450A0A)
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFEE2E2)

/**
 * Semantic accent colors for status that isn't part of the M3 scheme.
 * Exposed through [com.example.ui.theme.AppAccents] via LocalAppAccents so
 * they flip correctly between light and dark. Screens must read these from
 * the theme rather than hardcoding.
 */
val SuccessLight = Green600
val SuccessDark = Green400
val WarningLight = Color(0xFFD97706)
val WarningDark = Color(0xFFFBBF24)
val InfoLight = Color(0xFF0E7490)
val InfoDark = Color(0xFF67E8F9)
