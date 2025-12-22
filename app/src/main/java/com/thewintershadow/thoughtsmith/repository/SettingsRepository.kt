package com.thewintershadow.thoughtsmith.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension property for creating DataStore instance.
 * This follows Android's recommended pattern for DataStore initialization.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing user settings and preferences using DataStore.
 *
 * This repository provides persistent storage for all app configuration including:
 * - AI provider and model selection
 * - API keys for authentication
 * - AI behavior customization (context and formatting)
 *
 * Uses DataStore Preferences for type-safe, asynchronous data storage that replaces
 * SharedPreferences with better performance and Kotlin Coroutines support.
 *
 * Features:
 * - Reactive data flow using Kotlin Flow
 * - Type-safe preference keys
 * - Automatic data persistence
 * - Default value handling
 * - Coroutine-based async operations
 *
 * Data Flow:
 * ```
 * UI ↔ ViewModel ↔ SettingsRepository ↔ DataStore ↔ Storage
 * ```
 *
 * @param context Android context for accessing DataStore
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class SettingsRepository(
    private val context: Context,
) {
    /** DataStore instance for preferences storage */
    private val dataStore = context.dataStore

    // Preference keys for type-safe data access
    private val aiProviderKey = stringPreferencesKey("ai_provider")
    private val aiModelKey = stringPreferencesKey("ai_model")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val aiContextKey = stringPreferencesKey("ai_context")
    private val outputFormatKey = stringPreferencesKey("output_format")

    /**
     * Flow of current app settings.
     *
     * This Flow automatically emits updated AppSettings whenever any setting changes,
     * providing reactive updates to the UI. Default values are provided for all settings
     * to ensure the app works out of the box.
     *
     * The Flow is collected by ViewModels to keep the UI in sync with stored preferences.
     *
     * Default Values:
     * - AI Provider: OpenAI (most common/accessible)
     * - AI Model: gpt-4o-mini (good balance of quality and cost)
     * - API Key: Empty (must be configured by user)
     * - AI Context: Supportive journaling assistant personality
     * - Output Format: Clean markdown with organized sections
     */
    val settings: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                aiProvider = AIProvider.valueOf(preferences[aiProviderKey] ?: AIProvider.OPENAI.name),
                aiModel = preferences[aiModelKey] ?: "gpt-4o-mini",
                apiKey = preferences[apiKeyKey] ?: "",
                aiContext =
                    preferences[aiContextKey]
                        ?: "You are a supportive friend helping someone with their daily journaling. " +
                        "Ask thoughtful questions, show empathy, and help them explore their thoughts and feelings.",
                outputFormatInstructions =
                    preferences[outputFormatKey]
                        ?: "Format the journal entry as a clean markdown document with a title, date, " +
                        "and well-organized sections based on our conversation.",
            )
        }

    /**
     * Update all settings atomically.
     *
     * This function saves all settings in a single transaction, ensuring data consistency.
     * Changes are automatically propagated to the settings Flow, triggering UI updates.
     *
     * @param settings The complete AppSettings object to save
     *
     * @throws Exception if DataStore write operation fails
     */
    suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[aiProviderKey] = settings.aiProvider.name
            preferences[aiModelKey] = settings.aiModel
            preferences[apiKeyKey] = settings.apiKey
            preferences[aiContextKey] = settings.aiContext
            preferences[outputFormatKey] = settings.outputFormatInstructions
        }
    }
}
