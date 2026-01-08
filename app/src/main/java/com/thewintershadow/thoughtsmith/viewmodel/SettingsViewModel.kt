package com.thewintershadow.thoughtsmith.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thewintershadow.thoughtsmith.data.AIModel
import com.thewintershadow.thoughtsmith.data.AIModels
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.AppSettings
import com.thewintershadow.thoughtsmith.data.TTSProvider
import com.thewintershadow.thoughtsmith.repository.SettingsRepository
import com.thewintershadow.thoughtsmith.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state data class for the Settings screen.
 *
 * @property settings Current app settings configuration
 * @property availableModels List of AI models available for the selected provider
 * @property isLoading True when saving settings
 * @property saveSuccess True when settings have been saved successfully
 */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val availableModels: List<AIModel> = emptyList(),
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
)

/**
 * ViewModel for the Settings screen - manages user preferences and AI configuration.
 *
 * This ViewModel handles all settings-related operations including:
 * - Managing AI provider and model selection
 * - API key configuration and storage
 * - AI behavior customization (context and formatting)
 * - Settings persistence through SettingsRepository
 * - Real-time validation and UI state updates
 *
 * The ViewModel automatically updates available models when the AI provider changes
 * and ensures settings are immediately persisted when modified.
 *
 * Architecture:
 * ```
 * SettingsScreen ↔ SettingsViewModel ↔ SettingsRepository ↔ SharedPreferences
 * ```
 *
 * @param application Application context for accessing Android services
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    // Repository for settings persistence
    private val settingsRepository = SettingsRepository(application)

    // Private mutable state for internal management
    private val _uiState = MutableStateFlow(SettingsUiState())

    // Public read-only state for UI consumption
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Initialize the ViewModel and observe settings changes.
     *
     * Sets up a flow collector that automatically updates the UI state
     * whenever settings change, including updating the available models
     * for the selected AI provider.
     */
    init {
        AppLogger.info("SettingsViewModel", "SettingsViewModel initialized")

        viewModelScope.launch {
            // Observe settings changes and update UI state accordingly
            settingsRepository.settings.collect { settings ->
                val models = AIModels.getModelsForProvider(settings.aiProvider)
                _uiState.value =
                    SettingsUiState(
                        settings = settings,
                        availableModels = models,
                    )
            }
        }
    }

    /**
     * Update the selected AI provider.
     *
     * When the provider changes, this function:
     * 1. Updates the provider in settings
     * 2. Automatically selects the first available model from the new provider
     * 3. Persists the changes immediately
     *
     * @param provider The new AI provider to use
     */
    fun updateProvider(provider: AIProvider) {
        AppLogger.info("SettingsViewModel", "Updating AI provider to ${provider.displayName}")

        // Get available models for the new provider
        val models = AIModels.getModelsForProvider(provider)
        val defaultModel = models.firstOrNull()?.modelName ?: ""

        viewModelScope.launch {
            val updatedSettings =
                _uiState.value.settings.copy(
                    aiProvider = provider,
                    aiModel = defaultModel,
                )
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update the selected AI model for the current provider.
     *
     * @param modelName The technical name of the model (e.g., "gpt-4o", "claude-3-5-sonnet")
     */
    fun updateModel(modelName: String) {
        AppLogger.info("SettingsViewModel", "Updating AI model to $modelName")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(aiModel = modelName)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update the API key for the selected AI provider.
     *
     * API keys are stored securely on the device and never logged in full.
     *
     * @param apiKey The API key for the selected provider
     */
    fun updateApiKey(apiKey: String) {
        AppLogger.debug("SettingsViewModel", "API key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(apiKey = apiKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update the AI context/system prompt.
     *
     * This controls how the AI behaves in conversations, setting its personality
     * and approach to interacting with the user.
     *
     * @param context The system prompt or context for the AI
     */
    fun updateAiContext(context: String) {
        AppLogger.info("SettingsViewModel", "AI context updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(aiContext = context)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update the output formatting instructions.
     *
     * These instructions tell the AI how to format the generated journal entries,
     * controlling structure, style, and content organization.
     *
     * @param format The formatting instructions for journal entry generation
     */
    fun updateOutputFormat(format: String) {
        AppLogger.info("SettingsViewModel", "Output format instructions updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(outputFormatInstructions = format)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Explicitly save current settings.
     *
     * Note: Settings are automatically saved when modified, but this function
     * can be used to trigger an explicit save with UI feedback.
     */
    fun saveSettings() {
        AppLogger.info("SettingsViewModel", "Saving settings")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, saveSuccess = false)
            settingsRepository.updateSettings(_uiState.value.settings)
            AppLogger.info("SettingsViewModel", "Settings saved successfully")
            _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
        }
    }

    /**
     * Update the TTS provider.
     *
     * @param provider The TTS provider to use
     */
    fun updateTTSProvider(provider: TTSProvider) {
        AppLogger.info("SettingsViewModel", "Updating TTS provider to ${provider.displayName}")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsProvider = provider)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update OpenAI TTS API key.
     *
     * @param apiKey The OpenAI API key for TTS
     */
    fun updateTTSOpenAIApiKey(apiKey: String) {
        AppLogger.debug("SettingsViewModel", "OpenAI TTS API key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsOpenAIApiKey = apiKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update OpenAI TTS model/voice.
     *
     * @param model The OpenAI TTS model (e.g., "tts-1", "tts-1-hd")
     */
    fun updateTTSOpenAIModel(model: String) {
        AppLogger.info("SettingsViewModel", "Updating OpenAI TTS model to $model")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsOpenAIModel = model)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update Gemini TTS API key.
     *
     * @param apiKey The Gemini API key for TTS
     */
    fun updateTTSGeminiApiKey(apiKey: String) {
        AppLogger.debug("SettingsViewModel", "Gemini TTS API key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsGeminiApiKey = apiKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update Gemini TTS model.
     *
     * @param model The Gemini TTS model (e.g., "gemini-2.5-flash-preview-tts")
     */
    fun updateTTSGeminiModel(model: String) {
        AppLogger.info("SettingsViewModel", "Updating Gemini TTS model to $model")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsGeminiModel = model)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update Gemini TTS voice name.
     *
     * @param voiceName The Gemini TTS voice name (e.g., "Kore", "Aoede", "Charon", "Fenrir")
     */
    fun updateTTSGeminiVoiceName(voiceName: String) {
        AppLogger.info("SettingsViewModel", "Updating Gemini TTS voice name to $voiceName")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsGeminiVoiceName = voiceName)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update Anthropic TTS API key.
     *
     * @param apiKey The Anthropic API key for TTS
     */
    fun updateTTSAnthropicApiKey(apiKey: String) {
        AppLogger.debug("SettingsViewModel", "Anthropic TTS API key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsAnthropicApiKey = apiKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update Anthropic TTS model/voice.
     *
     * @param model The Anthropic TTS model
     */
    fun updateTTSAnthropicModel(model: String) {
        AppLogger.info("SettingsViewModel", "Updating Anthropic TTS model to $model")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(ttsAnthropicModel = model)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update AWS access key for AWS Polly TTS.
     *
     * @param accessKey The AWS access key ID
     */
    fun updateAWSAccessKey(accessKey: String) {
        AppLogger.debug("SettingsViewModel", "AWS access key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(awsAccessKey = accessKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update AWS secret key for AWS Polly TTS.
     *
     * @param secretKey The AWS secret access key
     */
    fun updateAWSSecretKey(secretKey: String) {
        AppLogger.debug("SettingsViewModel", "AWS secret key updated")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(awsSecretKey = secretKey)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Update AWS region for AWS Polly TTS.
     *
     * @param region The AWS region (e.g., "us-east-1")
     */
    fun updateAWSRegion(region: String) {
        AppLogger.debug("SettingsViewModel", "AWS region updated to $region")

        viewModelScope.launch {
            val updatedSettings = _uiState.value.settings.copy(awsRegion = region)
            settingsRepository.updateSettings(updatedSettings)
        }
    }

    /**
     * Clear the save success state.
     *
     * Called after the success message has been shown to the user.
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
