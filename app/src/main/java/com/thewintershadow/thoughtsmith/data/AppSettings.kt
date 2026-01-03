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
 * @property ttsProvider The Text-to-Speech provider to use (Local, OpenAI, Anthropic, or AWS Polly).
 *                       Defaults to LOCAL for offline support.
 * @property ttsProviderType The AI provider for TTS (OpenAI, Anthropic) - separate from text API provider.
 * @property ttsModel The TTS model/voice to use (e.g., "tts-1", "tts-1-hd" for OpenAI, voice ID for AWS Polly).
 * @property ttsApiKey The API key for TTS provider (separate from text API key).
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
    val ttsProviderType: AIProvider = AIProvider.OPENAI, // For OpenAI/Anthropic TTS
    val ttsModel: String = "tts-1", // TTS model/voice
    val ttsApiKey: String = "", // Separate API key for TTS
    val awsAccessKey: String = "",
    val awsSecretKey: String = "",
    val awsRegion: String = "us-east-1",
)
