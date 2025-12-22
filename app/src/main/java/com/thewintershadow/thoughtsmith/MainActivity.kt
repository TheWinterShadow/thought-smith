package com.thewintershadow.thoughtsmith

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.thewintershadow.thoughtsmith.navigation.NavGraph
import com.thewintershadow.thoughtsmith.ui.theme.ThoughtSmithTheme

/**
 * MainActivity - The main entry point for the Thought Smith Android application.
 * 
 * Thought Smith is an AI-powered journaling app that helps users explore their thoughts
 * and feelings through interactive conversations with AI assistants. The app generates
 * beautifully formatted journal entries from these conversations.
 * 
 * This activity serves as the host for the entire app using Jetpack Compose for the UI
 * and Navigation Compose for screen transitions.
 * 
 * Key Features:
 * - Sets up edge-to-edge display for modern Android UI
 * - Initializes the app's navigation controller
 * - Applies the Material Design 3 theme
 * - Hosts the main navigation graph
 * 
 * @author TheWinterShadow
 * @since 1.0.0
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Called when the activity is first created.
     * 
     * This method:
     * 1. Enables edge-to-edge display for immersive UI experience
     * 2. Sets up Jetpack Compose as the content view
     * 3. Applies the app's Material Design 3 theme
     * 4. Creates a navigation controller for screen navigation
     * 5. Initializes the navigation graph with all app screens
     * 
     * @param savedInstanceState Bundle containing the activity's previously saved state,
     *                          or null if this is the first time the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern, immersive UI
        enableEdgeToEdge()
        
        // Set up Jetpack Compose content
        setContent {
            // Apply the app's Material Design 3 theme
            ThoughtSmithTheme {
                // Create navigation controller for handling screen transitions
                val navController = rememberNavController()
                
                // Initialize the main navigation graph
                // This sets up all available screens and their navigation routes
                NavGraph(navController = navController)
            }
        }
    }
}