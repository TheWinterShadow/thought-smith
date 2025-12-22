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
