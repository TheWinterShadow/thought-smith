/**
 * Color definitions for the Thought Smith app theme.
 *
 * This file defines the color palette used throughout the application, following
 * Material Design 3 principles with a focus on soft, calming colors appropriate
 * for a mental health and journaling application.
 *
 * Color Philosophy:
 * The color scheme is designed to create a peaceful, non-intrusive environment
 * that promotes reflection and mindfulness. All colors are muted and gentle,
 * avoiding harsh contrasts that might cause stress or distraction.
 *
 * Color Families:
 * - Blue: Primary family, representing clarity and calm
 * - Green: Secondary family, representing growth and balance
 * - Beige/Cream: Neutral family, representing warmth and comfort
 * - Purple: Accent family, representing creativity and introspection
 *
 * Each family includes three shades (Soft, Light, Pale) for light theme
 * and darker variants for dark theme support.
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */

package com.thewintershadow.thoughtsmith.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme - Blue Family

/** Soft blue - Primary color for interactive elements */
val SoftBlue = Color(0xFF6B9BD2)

/** Light blue - Secondary variant of primary */
val LightBlue = Color(0xFFA8C8E8)

/** Pale blue - Background tint for primary containers */
val PaleBlue = Color(0xFFE8F0F8)

// Light Theme - Green Family

/** Soft green - Secondary color for success and growth */
val SoftGreen = Color(0xFF7FB3A3)

/** Light green - Secondary variant */
val LightGreen = Color(0xFFB8D4C8)

/** Pale green - Background tint for secondary containers */
val PaleGreen = Color(0xFFE8F4F0)

// Light Theme - Beige/Cream Family

/** Warm beige - Tertiary color for warmth and comfort */
val WarmBeige = Color(0xFFD4C4B0)

/** Light beige - Tertiary variant */
val LightBeige = Color(0xFFE8E0D4)

/** Cream - Background color for light theme */
val Cream = Color(0xFFF5F1EB)

// Light Theme - Purple Family

/** Soft purple - Accent color for special elements */
val SoftPurple = Color(0xFF9B8FB8)

/** Light purple - Accent variant */
val LightPurple = Color(0xFFC4BAD4)

/** Pale purple - Background tint for accent containers */
val PalePurple = Color(0xFFE8E4F0)

// Dark Theme Colors

/** Dark blue - Darker variant for dark theme */
val DarkBlue = Color(0xFF4A7BA7)

/** Dark green - Darker variant for dark theme */
val DarkGreen = Color(0xFF5F8F7A)

/** Dark beige - Darker variant for dark theme */
val DarkBeige = Color(0xFFB8A890)

/** Dark purple - Darker variant for dark theme */
val DarkPurple = Color(0xFF7A6B95)

// Legacy color mappings for backward compatibility
// These maintain compatibility with older Material Design color naming

/** Legacy mapping: Purple80 -> SoftPurple */
val Purple80 = SoftPurple

/** Legacy mapping: PurpleGrey80 -> LightPurple */
val PurpleGrey80 = LightPurple

/** Legacy mapping: Pink80 -> LightGreen */
val Pink80 = LightGreen

/** Legacy mapping: Purple40 -> SoftPurple */
val Purple40 = SoftPurple

/** Legacy mapping: PurpleGrey40 -> SoftGreen */
val PurpleGrey40 = SoftGreen

/** Legacy mapping: Pink40 -> SoftGreen */
val Pink40 = SoftGreen
