/**
 * Build configuration for the Thought Smith Android application.
 *
 * This build script configures the Android app module including:
 * - Build variants and signing configurations
 * - Android SDK versions and compatibility settings
 * - Jetpack Compose setup
 * - Dependencies and libraries
 * - Kotlin configuration
 *
 * Key Features Configured:
 * - Minimum SDK 33 (Android 13) for modern Android features
 * - Target SDK 36 for latest Android compatibility
 * - Jetpack Compose for modern UI development
 * - Material Design 3 theming
 * - Kotlin Coroutines for asynchronous operations
 * - DataStore for settings persistence
 * - OkHttp/Retrofit for AI API communication
 * - Navigation Compose for screen navigation
 *
 * Release Signing:
 * - Configured for release builds with keystore.properties
 * - Enables app store distribution and production deployment
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */

import java.io.FileInputStream
import java.util.Properties

// Apply required Gradle plugins
plugins {
    alias(libs.plugins.android.application) // Android app support
    alias(libs.plugins.kotlin.android) // Kotlin language support
    alias(libs.plugins.kotlin.compose) // Compose compiler plugin
    alias(libs.plugins.ktlint) // Kotlin linting and formatting
    alias(libs.plugins.dokka) // Dokka documentation generator
}

// Load signing configuration from keystore.properties file
// This keeps sensitive signing information out of version control
val keystorePropertiesFile = rootProject.file("app/keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreProperties = keystorePropertiesFile.exists()
if (hasKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

/**
 * Main Android configuration block
 */
android {
    // Configure release signing with keystore for app store distribution
    signingConfigs {
        if (hasKeystoreProperties) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    // Package namespace following reverse domain notation
    namespace = "com.thewintershadow.thoughtsmith"

    // SDK 36 provides latest Android features and security updates
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        // Unique application identifier for app stores
        applicationId = "com.thewintershadow.thoughtsmith"

        // Minimum SDK 33 (Android 13) for:
        // - Enhanced privacy controls
        // - Improved storage access
        // - Modern Material Design 3 support
        minSdk = 33

        // Target latest SDK for optimal compatibility
        targetSdk = 36

        // Version information for app store releases
        versionCode = 3 // Increment for each release
        versionName = "1.5" // User-visible version string

        // Test runner for instrumented tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Build type configurations
    buildTypes {
        release {
            // Disable code minification for easier debugging in production
            // Can be enabled later for smaller APK size
            isMinifyEnabled = false

            // ProGuard rules for code optimization and obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            // Apply release signing configuration if available
            if (hasKeystoreProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // Java compatibility settings
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Enable Jetpack Compose UI toolkit
    buildFeatures {
        compose = true
    }
}

/**
 * Kotlin compiler configuration
 * Using compilerOptions DSL for Kotlin 2.3.0+ (replaces deprecated kotlinOptions)
 */
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

/**
 * Ktlint configuration for code formatting and linting
 */
ktlint {
    // Use latest version of ktlint
    version.set("1.4.1")

    // Enable additional rule sets
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "120",
            "ij_kotlin_allow_trailing_comma" to "true",
            "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
            "ktlint_standard_function-naming" to "disabled",
            "ktlint_standard_filename" to "disabled",
            "ktlint_standard_no-wildcard-imports" to "disabled",
        ),
    )

    // Configure reporters
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }

    // Include generated sources in linting (Compose compiler, etc)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

/**
 * Application dependencies
 *
 * Dependencies are organized by category:
 * - Core Android libraries
 * - Jetpack Compose UI libraries
 * - Architecture components (ViewModel, Navigation)
 * - Data persistence (DataStore)
 * - Network communication (Retrofit, OkHttp)
 * - JSON parsing (Gson)
 * - Asynchronous programming (Coroutines)
 * - Testing libraries
 */
dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle-aware components
    implementation(libs.androidx.activity.compose) // Compose integration for activities

    // Jetpack Compose UI libraries
    implementation(platform(libs.androidx.compose.bom)) // Compose Bill of Materials for version alignment
    implementation(libs.androidx.compose.ui) // Core Compose UI
    implementation(libs.androidx.compose.ui.graphics) // Graphics and drawing APIs
    implementation(libs.androidx.compose.ui.tooling.preview) // Preview support for Android Studio
    implementation(libs.androidx.compose.material3) // Material Design 3 components
    implementation("androidx.compose.material:material-icons-extended") // Material Icons (managed by BOM)

    // Architecture components
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel integration with Compose
    implementation(libs.androidx.navigation.compose) // Navigation for Compose apps

    // Data persistence
    implementation(libs.androidx.datastore.preferences) // Modern replacement for SharedPreferences

    // Network communication for AI APIs
    implementation(libs.retrofit) // HTTP client library
    implementation(libs.retrofit.gson) // JSON converter for Retrofit
    implementation(libs.okhttp) // HTTP client implementation
    implementation(libs.okhttp.logging) // HTTP request/response logging
    implementation(libs.gson) // JSON parsing library

    // Asynchronous programming
    implementation(libs.kotlinx.coroutines.android) // Coroutines for background operations

    // Testing libraries
    testImplementation(libs.junit) // Unit testing framework
    androidTestImplementation(libs.androidx.junit) // Android testing extensions
    androidTestImplementation(libs.androidx.espresso.core) // UI testing framework
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Compose testing

    // Debug tools (only included in debug builds)
    debugImplementation(libs.androidx.compose.ui.tooling) // Compose layout inspector
    debugImplementation(libs.androidx.compose.ui.test.manifest) // Test manifest for UI testing
}

/**
 * Dokka configuration for generating API documentation
 */
tasks.dokkaHtml {
    outputDirectory.set(file("${layout.buildDirectory.get()}/dokka/html"))

    // Configure Dokka module
    moduleName.set("Thought Smith")

    // Include Android sources
    dokkaSourceSets {
        configureEach {
            // Include all source files
            includeNonPublic.set(false)

            // Display name for the source set
            displayName.set("Thought Smith")

            // Report undocumented code
            reportUndocumented.set(true)

            // Skip empty packages
            skipEmptyPackages.set(true)
        }
    }
}

tasks.dokkaJavadoc {
    outputDirectory.set(file("${layout.buildDirectory.get()}/dokka/javadoc"))

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
        }
    }
}

tasks.dokkaGfm {
    outputDirectory.set(file("${layout.buildDirectory.get()}/dokka/gfm"))

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
        }
    }
}
