/**
 * Top-level build file for the Thought Smith Android application.
 * 
 * This file configures plugins and settings that apply to all sub-projects/modules
 * in the application. Currently, the app uses a single module structure, but this
 * file enables future modularization if needed.
 * 
 * Configured Plugins:
 * - android.application: Android app module support
 * - kotlin.android: Kotlin language support for Android
 * - kotlin.compose: Kotlin Compiler Plugin for Jetpack Compose
 * 
 * The 'apply false' directive means these plugins are made available to sub-modules
 * but are not applied at the project root level.
 * 
 * @author TheWinterShadow
 * @since 1.0.0
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.versions) // Gradle Versions Plugin for dependency updates
}