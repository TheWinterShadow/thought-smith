package com.thewintershadow.thoughtsmith.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class for creating ViewModels with Application dependencies.
 *
 * This factory is required because our ViewModels extend AndroidViewModel and need
 * an Application context for accessing Android services like SharedPreferences
 * and file operations.
 *
 * The factory pattern ensures that ViewModels are created with the proper dependencies
 * and follows the recommended Android architecture guidelines for dependency injection.
 *
 * Supported ViewModels:
 * - ChatViewModel: For managing chat interactions and AI communication
 * - SettingsViewModel: For managing user preferences and configuration
 *
 * Usage:
 * ```kotlin
 * val chatViewModel: ChatViewModel = viewModel(
 *     factory = ViewModelFactory(applicationContext)
 * )
 * ```
 *
 * @param application Application context passed to ViewModels that need it
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class ViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * This method checks the requested ViewModel type and creates an instance
     * with the appropriate dependencies. Currently supports ChatViewModel
     * and SettingsViewModel.
     *
     * @param T The type of ViewModel to create
     * @param modelClass The class of the ViewModel to instantiate
     * @return A new instance of the requested ViewModel
     * @throws IllegalArgumentException if the ViewModel class is not supported
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Create ChatViewModel with Application dependency
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application) as T
        }

        // Create SettingsViewModel with Application dependency
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }

        // Unsupported ViewModel type
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
