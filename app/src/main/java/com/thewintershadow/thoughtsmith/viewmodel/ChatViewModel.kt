package com.thewintershadow.thoughtsmith.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.Message
import com.thewintershadow.thoughtsmith.repository.AIService
import com.thewintershadow.thoughtsmith.repository.FileStorageService
import com.thewintershadow.thoughtsmith.repository.SettingsRepository
import com.thewintershadow.thoughtsmith.util.AppLogger
import com.thewintershadow.thoughtsmith.util.SpeechService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI state data class for the Chat screen.
 *
 * This data class represents the complete UI state of the chat interface,
 * including messages, loading states, error conditions, and journal generation status.
 *
 * @property messages List of all chat messages between user and AI
 * @property isLoading True when waiting for AI response to user message
 * @property error Error message to display to user, or null if no error
 * @property isSaving True when saving journal entry to file
 * @property saveSuccess Success message after saving, or null if not saved
 * @property formattedSummary Generated journal entry content, or null if not generated
 * @property isGeneratingSummary True when AI is generating journal entry format
 * @property inputMode True for speech input, false for text input
 * @property outputMode True for speech output, false for text output
 * @property isListening True when actively listening for speech input
 * @property isSpeaking True when text-to-speech is currently speaking
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: String? = null,
    val formattedSummary: String? = null,
    val isGeneratingSummary: Boolean = false,
    val inputMode: Boolean = false, // false = text, true = speech
    val outputMode: Boolean = false, // false = text, true = speech
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
)

/**
 * ViewModel for the Chat screen - manages chat state and AI interactions.
 *
 * This ViewModel handles all the business logic for the main chat interface including:
 * - Managing the conversation state between user and AI
 * - Sending messages to AI services and handling responses
 * - Generating formatted journal entries from conversations
 * - Managing error states and loading indicators
 * - Coordinating with repository layer for settings and file operations
 *
 * Architecture:
 * ```
 * ChatScreen ↔ ChatViewModel ↔ [AIService, SettingsRepository, FileStorageService]
 * ```
 *
 * The ViewModel follows MVVM architecture pattern and uses Kotlin Coroutines for
 * asynchronous operations. State is exposed through StateFlow for reactive UI updates.
 *
 * @param application Application context for accessing Android services
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class ChatViewModel(
    application: Application,
) : AndroidViewModel(application) {
    // Repository dependencies for data operations
    private val aiService = AIService()
    private val fileStorageService = FileStorageService(application)
    private val settingsRepository = SettingsRepository(application)
    private val speechService = SpeechService(application)

    // Private mutable state flow for internal state management
    private val _uiState = MutableStateFlow(ChatUiState())

    // Public read-only state flow for UI consumption
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Initialize the ViewModel and set up the initial welcome message.
     */
    init {
        AppLogger.info("ChatViewModel", "ChatViewModel initialized")

        // Start with a friendly welcome message from the AI
        viewModelScope.launch {
            val welcomeMessage = "Hi! I'm here to help you with your journaling today. What's on your mind?"
            _uiState.value =
                _uiState.value.copy(
                    messages =
                        listOf(
                            Message(
                                content = welcomeMessage,
                                isUser = false,
                            ),
                        ),
                )
        }

        // Observe settings changes to update TTS provider
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                speechService.setTTSProvider(settings.ttsProvider)
                AppLogger.info("ChatViewModel", "TTS provider updated to: ${settings.ttsProvider.displayName}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.cleanup()
    }

    /**
     * Send a user message to the AI and handle the response.
     *
     * This function:
     * 1. Validates the input message and current state
     * 2. Adds the user message to the conversation
     * 3. Retrieves current AI settings
     * 4. Sends the conversation to the AI service
     * 5. Handles the AI response or error
     * 6. Updates the UI state accordingly
     *
     * @param userMessage The message text from the user
     */
    fun sendMessage(userMessage: String) {
        // Prevent sending blank messages or while already loading
        if (userMessage.isBlank() || _uiState.value.isLoading) return

        AppLogger.info("ChatViewModel", "User sending message: ${userMessage.take(50)}...")

        // Create user message object and update conversation
        val userMsg = Message(content = userMessage.trim(), isUser = true)
        val updatedMessages = _uiState.value.messages + userMsg

        // Update UI to show loading state
        _uiState.value =
            _uiState.value.copy(
                messages = updatedMessages,
                isLoading = true,
                error = null,
            )

        // Handle AI communication asynchronously
        viewModelScope.launch {
            try {
                // Get current user settings
                val settings = settingsRepository.settings.first()

                // Validate API key is configured
                if (settings.apiKey.isBlank()) {
                    AppLogger.warning("ChatViewModel", "API key not configured")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = "Please configure your API key in Settings",
                        )
                    return@launch
                }

                // Send conversation to AI service
                val result =
                    aiService.getAIResponse(
                        messages = updatedMessages,
                        provider = settings.aiProvider,
                        model = settings.aiModel,
                        apiKey = settings.apiKey,
                        systemContext = settings.aiContext,
                    )

                // Handle AI response
                if (result.isSuccess) {
                    val aiResponse = result.getOrNull() ?: ""
                    val aiMsg = Message(content = aiResponse, isUser = false)
                    AppLogger.info("ChatViewModel", "AI response received successfully")

                    _uiState.value =
                        _uiState.value.copy(
                            messages = updatedMessages + aiMsg,
                            isLoading = false,
                            error = null,
                        )

                    // Speak the response if speech output is enabled
                    if (_uiState.value.outputMode) {
                        val currentSettings = settingsRepository.settings.first()
                        speakText(
                            aiResponse,
                            currentSettings.ttsApiKey, // Use TTS-specific API key
                            currentSettings.ttsProviderType, // TTS provider type
                            currentSettings.ttsModel, // TTS model
                            currentSettings.awsAccessKey,
                            currentSettings.awsSecretKey,
                            currentSettings.awsRegion,
                        )
                    }
                } else {
                    // Handle AI service error
                    val error = result.exceptionOrNull()
                    AppLogger.error("ChatViewModel", "Failed to get AI response", error)
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = error?.message ?: "Failed to get AI response",
                        )
                }
            } catch (e: Exception) {
                // Handle unexpected exceptions
                AppLogger.error("ChatViewModel", "Exception while sending message", e)
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred",
                    )
            }
        }
    }

    /**
     * Generate a formatted journal entry from the current conversation.
     *
     * This function takes the entire conversation history and asks the AI to format
     * it as a structured journal entry according to user-configured formatting instructions.
     * The result can then be saved to a file.
     */
    fun saveJournalEntry() {
        // Prevent duplicate operations
        if (_uiState.value.messages.isEmpty() ||
            _uiState.value.isSaving ||
            _uiState.value.isGeneratingSummary
        ) {
            return
        }

        AppLogger.info("ChatViewModel", "Generating formatted summary")
        _uiState.value = _uiState.value.copy(isGeneratingSummary = true, error = null)

        viewModelScope.launch {
            try {
                val settings = settingsRepository.settings.first()

                // Validate API key
                if (settings.apiKey.isBlank()) {
                    AppLogger.warning("ChatViewModel", "API key not configured")
                    _uiState.value =
                        _uiState.value.copy(
                            isGeneratingSummary = false,
                            error = "Please configure your API key in Settings",
                        )
                    return@launch
                }

                // Create a formatting request message with conversation history
                val conversationText =
                    _uiState.value.messages.joinToString("\n\n") {
                        "${if (it.isUser) "You" else "AI"}: ${it.content}"
                    }

                val formatMessage =
                    Message(
                        content =
                            "Please format the following conversation as a journal entry according to " +
                                "these instructions:\\n\\n${settings.outputFormatInstructions}\\n\\n---\\n\\n" +
                                "Conversation:\\n\\n$conversationText",
                        isUser = true,
                    )

                val messagesWithFormat = _uiState.value.messages + formatMessage

                // Request formatted journal entry from AI
                val result =
                    aiService.getAIResponse(
                        messages = messagesWithFormat,
                        provider = settings.aiProvider,
                        model = settings.aiModel,
                        apiKey = settings.apiKey,
                        systemContext = settings.aiContext,
                    )

                if (result.isSuccess) {
                    val formattedContent = result.getOrNull() ?: ""
                    AppLogger.info("ChatViewModel", "Formatted summary generated successfully")
                    _uiState.value =
                        _uiState.value.copy(
                            isGeneratingSummary = false,
                            formattedSummary = formattedContent,
                        )
                } else {
                    val error = result.exceptionOrNull()
                    AppLogger.error("ChatViewModel", "Failed to generate formatted summary", error)
                    _uiState.value =
                        _uiState.value.copy(
                            isGeneratingSummary = false,
                            error = "Failed to generate summary: ${error?.message ?: "Unknown error"}",
                        )
                }
            } catch (e: Exception) {
                AppLogger.error("ChatViewModel", "Exception while generating summary", e)
                _uiState.value =
                    _uiState.value.copy(
                        isGeneratingSummary = false,
                        error = e.message ?: "Failed to generate summary",
                    )
            }
        }
    }

    /**
     * Accept the generated journal summary and prepare for file save.
     *
     * @param formattedContent The AI-generated formatted journal entry
     */
    fun acceptSummaryAndSave(formattedContent: String) {
        _uiState.value =
            _uiState.value.copy(
                formattedSummary = formattedContent,
                isSaving = true,
            )
    }

    /**
     * Reject the generated journal summary and return to normal state.
     */
    fun rejectSummary() {
        _uiState.value = _uiState.value.copy(formattedSummary = null)
    }

    /**
     * Handle the result of a file save operation.
     *
     * @param success True if the file was saved successfully, false if failed or cancelled
     * @param filePath Optional path where the file was saved
     */
    fun onFileSaved(
        success: Boolean,
        filePath: String? = null,
    ) {
        if (success) {
            AppLogger.info("ChatViewModel", "Journal entry saved successfully")
            _uiState.value =
                _uiState.value.copy(
                    isSaving = false,
                    formattedSummary = null,
                    saveSuccess = filePath?.let { "Journal entry saved to: $it" } ?: "Journal entry saved successfully",
                )
        } else {
            AppLogger.error("ChatViewModel", "Failed to save journal entry or cancelled", null)
            _uiState.value =
                _uiState.value.copy(
                    isSaving = false,
                    formattedSummary = null,
                    error = null, // Don't show error if user cancelled
                )
        }
    }

    /**
     * Clear the entire chat conversation and start fresh.
     */
    fun clearChat() {
        AppLogger.info("ChatViewModel", "Clearing chat")
        _uiState.value =
            _uiState.value.copy(
                messages =
                    listOf(
                        Message(
                            content = "Hi! I'm here to help you with your journaling today. What's on your mind?",
                            isUser = false,
                        ),
                    ),
                error = null,
                saveSuccess = null,
            )
    }

    /**
     * Clear any current error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear any current save success message.
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = null)
    }

    /**
     * Toggle between text and speech input mode.
     */
    fun toggleInputMode() {
        val newMode = !_uiState.value.inputMode
        _uiState.value = _uiState.value.copy(inputMode = newMode)
        AppLogger.info("ChatViewModel", "Input mode changed to: ${if (newMode) "speech" else "text"}")
        
        // Stop listening if switching to text mode
        if (!newMode) {
            stopListening()
        }
    }

    /**
     * Toggle between text and speech output mode.
     */
    fun toggleOutputMode() {
        val newMode = !_uiState.value.outputMode
        _uiState.value = _uiState.value.copy(outputMode = newMode)
        AppLogger.info("ChatViewModel", "Output mode changed to: ${if (newMode) "speech" else "text"}")
        
        // Stop speaking if switching to text mode
        if (!newMode) {
            stopSpeaking()
        }
    }

    /**
     * Start listening for speech input.
     */
    fun startListening() {
        if (_uiState.value.isListening || _uiState.value.isLoading) {
            return
        }

        if (!speechService.isSpeechRecognitionAvailable()) {
            _uiState.value = _uiState.value.copy(
                error = "Speech recognition is not available on this device"
            )
            return
        }

        AppLogger.info("ChatViewModel", "Starting speech recognition")
        _uiState.value = _uiState.value.copy(isListening = true, error = null)

        viewModelScope.launch {
            speechService.startListening()
                .catch { e ->
                    AppLogger.error("ChatViewModel", "Speech recognition error", e)
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        error = e.message ?: "Speech recognition failed"
                    )
                }
                .collect { result ->
                    // Keep listening active - don't set isListening = false here
                    // Only stop when the flow completes (which happens when stopListening is called)
                    if (result.isSuccess) {
                        val recognizedText = result.getOrNull() ?: ""
                        if (recognizedText.isNotBlank()) {
                            // Automatically send the recognized text as a message
                            sendMessage(recognizedText)
                        }
                    } else {
                        // Only show error for fatal errors (non-fatal ones are handled internally)
                        val error = result.exceptionOrNull()
                        if (error != null && error.message?.contains("Fatal", ignoreCase = true) == true) {
                            AppLogger.error("ChatViewModel", "Fatal speech recognition error", error)
                            _uiState.value = _uiState.value.copy(
                                isListening = false,
                                error = error.message ?: "Speech recognition failed"
                            )
                        }
                    }
                }
            // Flow completed - listening has stopped
            _uiState.value = _uiState.value.copy(isListening = false)
        }
    }

    /**
     * Stop listening for speech input.
     */
    fun stopListening() {
        if (_uiState.value.isListening) {
            speechService.stopListening()
            _uiState.value = _uiState.value.copy(isListening = false)
            AppLogger.info("ChatViewModel", "Stopped listening")
        }
    }

    /**
     * Speak the given text using text-to-speech.
     *
     * @param text The text to speak
     * @param ttsApiKey API key for OpenAI/Anthropic TTS (required if using OPENAI or ANTHROPIC provider)
     * @param ttsProviderType The AI provider for TTS (OpenAI or Anthropic)
     * @param ttsModel The TTS model/voice to use
     * @param awsAccessKey AWS access key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsSecretKey AWS secret key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsRegion AWS region for AWS Polly (required if using AWS_POLLY provider)
     */
    private fun speakText(
        text: String,
        ttsApiKey: String = "",
        ttsProviderType: AIProvider = AIProvider.OPENAI,
        ttsModel: String = "tts-1",
        awsAccessKey: String = "",
        awsSecretKey: String = "",
        awsRegion: String = "us-east-1",
    ) {
        if (text.isBlank()) return
        
        _uiState.value = _uiState.value.copy(isSpeaking = true)
        
        viewModelScope.launch {
            speechService.speak(text, ttsApiKey, ttsProviderType, ttsModel, awsAccessKey, awsSecretKey, awsRegion)
            
            // Check if speaking is done (polling approach)
            // Wait a bit and check if still speaking
            kotlinx.coroutines.delay(100)
            while (speechService.isSpeaking()) {
                kotlinx.coroutines.delay(100)
            }
            _uiState.value = _uiState.value.copy(isSpeaking = false)
        }
    }

    /**
     * Stop speaking if currently speaking.
     */
    fun stopSpeaking() {
        speechService.stopSpeaking()
        _uiState.value = _uiState.value.copy(isSpeaking = false)
    }
}
