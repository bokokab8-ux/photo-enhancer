package com.aienhancer.photoenhancer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Root Material 3 theme for the app.
 *
 * Supports:
 *  - Dynamic color (Material You) on Android 12+ when [dynamicColor] is true,
 *    deriving the entire color scheme from the user's wallpaper.
 *  - A static, hand-tuned brand color scheme as the fallback on older API
 *    levels or when dynamic color is disabled.
 *  - Automatic dark/light switching driven by [isSystemInDarkTheme], while
 *    still allowing an explicit override via [darkTheme] (e.g. for an
 *    in-app theme toggle, wired up by passing a state value from a settings
 *    screen if one is added later).
 *
 * @param darkTheme whether dark mode should be used; defaults to the system setting.
 * @param dynamicColor whether to prefer Android 12+ dynamic color over the static brand scheme.
 */
@Composable
fun AIPhotoEnhancerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
