package com.thewintershadow.thoughtsmith.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Dark color scheme for the Thought Smith app.
 *
 * Uses muted, calming colors suitable for low-light conditions while maintaining
 * good contrast for readability. The dark theme helps reduce eye strain during
 * evening journaling sessions.
 *
 * Background Colors:
 * - background: Very dark gray (#1A1A1F) for the main canvas
 * - surface: Slightly lighter gray (#252530) for elevated components
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = LightBlue,
        secondary = LightGreen,
        tertiary = LightPurple,
        background = Color(0xFF1A1A1F),
        surface = Color(0xFF252530),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFFE8E8E8),
        onSurface = Color(0xFFE8E8E8),
    )

/**
 * Light color scheme for the Thought Smith app.
 *
 * Features soft, calming colors that create a peaceful environment for
 * journaling and reflection. The cream background provides a warm, paper-like
 * feel that's easy on the eyes.
 *
 * Background Colors:
 * - background: Cream (#F5F1EB) for a warm, paper-like feel
 * - surface: Pure white for elevated components and cards
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
private val LightColorScheme =
    lightColorScheme(
        primary = SoftBlue,
        secondary = SoftGreen,
        tertiary = SoftPurple,
        background = Cream,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF2C2C2C),
        onSurface = Color(0xFF2C2C2C),
    )

/**
 * Main theme composable for the Thought Smith app.
 *
 * This function applies the Material Design 3 theme with customized colors
 * and typography. It supports both light and dark themes, and can use
 * Android 12+ dynamic colors that adapt to the user's system theme.
 *
 * Theme Features:
 * - Dynamic color support on Android 12+ (adapts to wallpaper)
 * - Automatic dark/light theme switching based on system settings
 * - Custom color schemes optimized for mental health and journaling
 * - Consistent typography across the app
 *
 * Dynamic Color:
 * When enabled on Android 12+, the app's colors adapt to match the user's
 * system theme derived from their wallpaper. This creates a more personalized
 * and integrated experience.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system preference
 * @param dynamicColor Whether to use dynamic colors on Android 12+. Defaults to true
 * @param content The composable content to apply the theme to
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
@Composable
fun ThoughtSmithTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
