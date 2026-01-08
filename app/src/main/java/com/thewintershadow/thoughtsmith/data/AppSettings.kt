package com.thewintershadow.thoughtsmith.data

/**
 * Data class representing all user-configurable settings for the Thought Smith app.
 *
 * This class encapsulates all the configuration options that users can customize to
 * personalize their journaling experience, including AI provider selection, API credentials,
 * and behavioral preferences for how the AI assistant should interact with them.
 *
 * Settings are persisted using SharedPreferences and are restored when the app is restarted.
 *
 * @property aiProvider The selected AI service provider (OpenAI, Gemini, or Anthropic).
 *                      Defaults to OpenAI as it's the most widely supported.
 * @property aiModel The specific AI model to use from the selected provider.
 *                   Defaults to "gpt-4o-mini" for good performance at reasonable cost.
 * @property apiKey The user's API key for the selected AI provider. Must be configured
 *                  before the app can make AI requests. Stored securely on device.
 * @property aiContext The system prompt/context that defines how the AI should behave.
 *                     This shapes the AI's personality and approach to conversations.
 * @property outputFormatInstructions Instructions for how the AI should format the
 *                                    generated journal entries. Controls structure and style.
 * @property ttsProvider The Text-to-Speech provider to use (Local, OpenAI, Gemini, Anthropic, or AWS Polly).
 *                       Defaults to LOCAL for offline support.
 * @property ttsOpenAIApiKey API key for OpenAI TTS (separate from text API key)
 * @property ttsOpenAIModel OpenAI TTS model (e.g., "tts-1", "tts-1-hd")
 * @property ttsGeminiApiKey API key for Gemini TTS (separate from text API key)
 * @property ttsGeminiModel Gemini TTS model (e.g., "gemini-2.5-flash-preview-tts")
 * @property ttsGeminiVoiceName Gemini TTS voice name (e.g., "Kore", "Aoede", "Charon", "Fenrir")
 * @property ttsAnthropicApiKey API key for Anthropic TTS (separate from text API key)
 * @property ttsAnthropicModel Anthropic TTS model (when available)
 * @property awsAccessKey AWS access key for AWS Polly TTS (required if using AWS_POLLY)
 * @property awsSecretKey AWS secret key for AWS Polly TTS (required if using AWS_POLLY)
 * @property awsRegion AWS region for AWS Polly TTS (e.g., "us-east-1")
 *
 * Usage Example:
 * ```kotlin
 * val settings = AppSettings(
 *     aiProvider = AIProvider.ANTHROPIC,
 *     aiModel = "claude-3-5-sonnet-20241022",
 *     apiKey = "sk-ant-...",
 *     aiContext = "You are a mindful counselor helping with reflection.",
 *     ttsProvider = TTSProvider.REMOTE
 * )
 * ```
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
data class AppSettings(
    val aiProvider: AIProvider = AIProvider.OPENAI,
    val aiModel: String = "gpt-4o-mini",
    val apiKey: String = "",
    val aiContext: String =
        "You are a supportive friend helping someone with their daily journaling. " +
            "Ask thoughtful questions, show empathy, and help them explore their thoughts and feelings.",
    val outputFormatInstructions: String =
        "Format the journal entry as a clean markdown document with a " +
            "title, date, and well-organized sections based on our conversation.",
    val ttsProvider: TTSProvider = TTSProvider.LOCAL,
    // OpenAI TTS configuration
    val ttsOpenAIApiKey: String = "",
    val ttsOpenAIModel: String = "tts-1",
    // Gemini TTS configuration
    val ttsGeminiApiKey: String = "",
    val ttsGeminiModel: String = "gemini-2.5-flash-preview-tts",
    val ttsGeminiVoiceName: String = "Kore",
    // Anthropic TTS configuration
    val ttsAnthropicApiKey: String = "",
    val ttsAnthropicModel: String = "",
    // AWS Polly configuration
    val awsAccessKey: String = "",
    val awsSecretKey: String = "",
    val awsRegion: String = "us-east-1",
)
