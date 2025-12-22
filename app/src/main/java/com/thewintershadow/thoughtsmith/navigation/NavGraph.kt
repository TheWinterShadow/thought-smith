package com.thewintershadow.thoughtsmith.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thewintershadow.thoughtsmith.ui.screens.ChatScreen
import com.thewintershadow.thoughtsmith.ui.screens.LogsScreen
import com.thewintershadow.thoughtsmith.ui.screens.SettingsScreen

/**
 * Sealed class defining all available navigation destinations in the app.
 * 
 * This sealed class provides type-safe navigation routes for the Thought Smith app.
 * Each route corresponds to a specific screen and defines the navigation path used
 * by Jetpack Navigation Compose.
 * 
 * @property route The string identifier used for navigation routing
 * 
 * Navigation Flow:
 * - Chat (main screen) → Settings → Logs
 * - All screens can navigate back to previous screens
 * 
 * @author TheWinterShadow
 * @since 1.0.0
 */
sealed class Screen(val route: String) {
    /** Main chat interface where users converse with AI */
    object Chat : Screen("chat")
    
    /** Configuration screen for AI settings and preferences */
    object Settings : Screen("settings")
    
    /** Debug and monitoring screen showing app logs */
    object Logs : Screen("logs")
}

/**
 * Main navigation graph for the Thought Smith app.
 * 
 * This composable function sets up the navigation structure using Jetpack Navigation Compose.
 * It defines all available screens, their routes, and the navigation relationships between them.
 * 
 * Navigation Structure:
 * ```
 * ChatScreen (start destination)
 *    ├─→ SettingsScreen
 *    │      └─→ LogsScreen
 *    └─→ [Back navigation supported on all non-root screens]
 * ```
 * 
 * Features:
 * - Type-safe navigation using sealed class routes
 * - Proper back stack management
 * - Lambda-based navigation callbacks for loose coupling
 * - Clear separation of navigation logic from UI components
 * 
 * @param navController The NavHostController that manages navigation state and transitions
 * 
 * @author TheWinterShadow
 * @since 1.0.0
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route // Start with the main chat screen
    ) {
        // Main chat screen - the primary interface for AI conversations
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToSettings = {
                    // Navigate to settings screen when user taps settings button
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Settings screen - configure AI provider, models, and preferences
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    // Return to previous screen (usually Chat)
                    navController.popBackStack()
                },
                onNavigateToLogs = {
                    // Navigate to logs screen for debugging
                    navController.navigate(Screen.Logs.route)
                }
            )
        }
        
        // Logs screen - view app logs and debug information
        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateBack = {
                    // Return to previous screen (usually Settings)
                    navController.popBackStack()
                }
            )
        }
    }
}

