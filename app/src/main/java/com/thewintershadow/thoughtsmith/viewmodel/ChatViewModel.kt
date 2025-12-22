package com.thewintershadow.thoughtsmith.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thewintershadow.thoughtsmith.data.Message
import com.thewintershadow.thoughtsmith.repository.AIService
import com.thewintershadow.thoughtsmith.repository.FileStorageService
import com.thewintershadow.thoughtsmith.repository.SettingsRepository
import com.thewintershadow.thoughtsmith.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: String? = null,
    val formattedSummary: String? = null,
    val isGeneratingSummary: Boolean = false,
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
            _uiState.value =
                _uiState.value.copy(
                    messages =
                        listOf(
                            Message(
                                content = "Hi! I'm here to help you with your journaling today. What's on your mind?",
                                isUser = false,
                            ),
                        ),
                )
        }
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
}
