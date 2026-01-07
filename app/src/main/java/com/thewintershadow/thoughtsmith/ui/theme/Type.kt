/**
 * Typography definitions for the Thought Smith app.
 *
 * This file defines the text styles used throughout the application,
 * following Material Design 3 typography guidelines. The typography is
 * designed to be clear, readable, and comfortable for extended reading
 * during journaling sessions.
 *
 * Type Scale:
 * Material Design 3 provides a comprehensive type scale with 15 different
 * styles ranging from displayLarge to labelSmall. This app primarily uses:
 * - bodyLarge: Main content text (16sp)
 * - titleLarge: Screen titles and headings
 * - labelMedium: UI labels and captions
 *
 * Font Family:
 * Currently uses the system default font family for maximum compatibility
 * and readability across different devices. This can be customized with
 * custom fonts if needed.
 *
 * Line Height and Letter Spacing:
 * Carefully tuned for optimal readability:
 * - Line height: 24sp (1.5x font size) for comfortable reading
 * - Letter spacing: 0.5sp for good character separation
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */

package com.thewintershadow.thoughtsmith.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material Design 3 typography scale for the app.
 *
 * The Typography object defines all text styles used in the app.
 * Only bodyLarge is explicitly customized here, but other styles can
 * be overridden as needed:
 *
 * Display styles (largest):
 * - displayLarge, displayMedium, displaySmall
 *
 * Headline styles:
 * - headlineLarge, headlineMedium, headlineSmall
 *
 * Title styles:
 * - titleLarge, titleMedium, titleSmall
 *
 * Body styles (main content):
 * - bodyLarge (customized), bodyMedium, bodySmall
 *
 * Label styles (smallest):
 * - labelLarge, labelMedium, labelSmall
 */
val Typography =
    Typography(
        /**
         * Main body text style used for chat messages and content.
         *
         * Properties:
         * - Font: System default
         * - Weight: Normal (400)
         * - Size: 16sp (comfortable for reading)
         * - Line Height: 24sp (good spacing for multi-line text)
         * - Letter Spacing: 0.5sp (subtle character separation)
         */
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
    )
