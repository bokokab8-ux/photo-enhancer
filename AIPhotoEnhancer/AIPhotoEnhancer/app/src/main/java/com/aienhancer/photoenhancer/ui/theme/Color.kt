package com.aienhancer.photoenhancer.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand seed palette - used as the fallback scheme on devices/API levels
// where Material You dynamic color is unavailable (pre-Android 12) or
// disabled by the user.

val Purple10 = Color(0xFF21005D)
val Purple20 = Color(0xFF381E72)
val Purple30 = Color(0xFF4F378B)
val Purple40 = Color(0xFF6750A4)
val Purple80 = Color(0xFFD0BCFF)
val Purple90 = Color(0xFFEADDFF)

val Teal10 = Color(0xFF002020)
val Teal20 = Color(0xFF003737)
val Teal30 = Color(0xFF004F4F)
val Teal40 = Color(0xFF1C6B6B)
val Teal80 = Color(0xFF70F7F6)
val Teal90 = Color(0xFF93FAF9)

val Orange40 = Color(0xFFB3261E)
val Orange80 = Color(0xFFF2B8B5)
val Orange90 = Color(0xFFF9DEDC)
val Orange10 = Color(0xFF410E0B)
val Orange20 = Color(0xFF601410)

val Neutral10 = Color(0xFF1C1B1F)
val Neutral90 = Color(0xFFE6E1E5)
val Neutral99 = Color(0xFFFFFBFE)
val NeutralVariant30 = Color(0xFF49454F)
val NeutralVariant50 = Color(0xFF79747E)
val NeutralVariant80 = Color(0xFFCAC4D0)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Teal30,
    onTertiary = Color.White,
    error = Orange40,
    onError = Color.White,
    errorContainer = Orange90,
    onErrorContainer = Orange10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant80,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = Teal80,
    onTertiary = Teal10,
    error = Orange80,
    onError = Orange20,
    errorContainer = Orange40,
    onErrorContainer = Orange90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant50
)

