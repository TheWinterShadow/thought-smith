package com.thewintershadow.thoughtsmith.data

/**
 * Enumeration of supported AI service providers.
 *
 * This enum defines the different AI companies and services that the app can integrate with
 * for generating conversational responses and journal entries. Each provider offers different
 * models with varying capabilities, costs, and response styles.
 *
 * @property displayName Human-readable name of the provider shown in the UI
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
enum class AIProvider(
    val displayName: String,
) {
    /** OpenAI's GPT models (GPT-4o, GPT-4, GPT-3.5, etc.) */
    OPENAI("OpenAI"),

    /** Google's Gemini models (Gemini 1.5 Pro, Flash, etc.) */
    GEMINI("Google Gemini"),

    /** Anthropic's Claude models (Claude 4.5, Claude 3.5, etc.) */
    ANTHROPIC("Anthropic Claude"),
}

/**
 * Represents a specific AI model from a provider.
 *
 * This data class encapsulates information about a particular AI model, including which
 * provider it belongs to, its API identifier, and a user-friendly display name.
 *
 * @property provider The AI service provider that offers this model
 * @property modelName The technical/API identifier for the model (e.g., "gpt-4o", "claude-3-opus")
 * @property displayName User-friendly name shown in the UI (e.g., "GPT-4o", "Claude 3 Opus")
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
data class AIModel(
    val provider: AIProvider,
    val modelName: String,
    val displayName: String,
)

/**
 * Container object for all supported AI models organized by provider.
 *
 * This object provides static collections of available AI models from each supported provider.
 * It includes the latest models from OpenAI, Google Gemini, and Anthropic Claude, with their
 * correct API identifiers and user-friendly display names.
 *
 * The models are organized by provider and include various capabilities:
 * - Different context windows and token limits
 * - Various pricing tiers (from economy to premium)
 * - Different response speeds and quality levels
 * - Specialized capabilities (reasoning, coding, creative writing, etc.)
 *
 * Usage:
 * ```kotlin
 * // Get all OpenAI models
 * val openAIModels = AIModels.getModelsForProvider(AIProvider.OPENAI)
 *
 * // Get a specific model list
 * val anthropicModels = AIModels.ANTHROPIC_MODELS
 * ```
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
object AIModels {
    /**
     * Available OpenAI models.
     * Includes GPT-4o (latest), GPT-4o Mini (cost-effective), GPT-4 Turbo, and GPT-3.5 Turbo.
     */
    val OPENAI_MODELS =
        listOf(
            AIModel(AIProvider.OPENAI, "gpt-4o", "GPT-4o"),
            AIModel(AIProvider.OPENAI, "gpt-4o-mini", "GPT-4o Mini"),
            AIModel(AIProvider.OPENAI, "gpt-4-turbo", "GPT-4 Turbo"),
            AIModel(AIProvider.OPENAI, "gpt-3.5-turbo", "GPT-3.5 Turbo"),
        )

    /**
     * Available Google Gemini models.
     * Includes Gemini 1.5 Pro (most capable), Gemini 1.5 Flash (fast), and Gemini Pro.
     */
    val GEMINI_MODELS =
        listOf(
            AIModel(AIProvider.GEMINI, "gemini-1.5-pro", "Gemini 1.5 Pro"),
            AIModel(AIProvider.GEMINI, "gemini-1.5-flash", "Gemini 1.5 Flash"),
            AIModel(AIProvider.GEMINI, "gemini-pro", "Gemini Pro"),
        )

    /**
     * Available Anthropic Claude models.
     * Includes the latest Claude 4.5 series (Opus, Sonnet, Haiku) and earlier Claude versions.
     * Claude models are known for their thoughtful, nuanced responses and strong reasoning.
     */
    val ANTHROPIC_MODELS =
        listOf(
            AIModel(AIProvider.ANTHROPIC, "claude-opus-4-5-20251101", "Claude Opus 4.5"),
            AIModel(AIProvider.ANTHROPIC, "claude-haiku-4-5-20251001", "Claude Haiku 4.5"),
            AIModel(AIProvider.ANTHROPIC, "claude-sonnet-4-5-20250929", "Claude Sonnet 4.5"),
            AIModel(AIProvider.ANTHROPIC, "claude-opus-4-1-20250805", "Claude Opus 4.1"),
            AIModel(AIProvider.ANTHROPIC, "claude-opus-4-20250514", "Claude Opus 4"),
            AIModel(AIProvider.ANTHROPIC, "claude-sonnet-4-20250514", "Claude Sonnet 4"),
            AIModel(AIProvider.ANTHROPIC, "claude-3-5-haiku-20241022", "Claude Haiku 3.5"),
            AIModel(AIProvider.ANTHROPIC, "claude-3-haiku-20240307", "Claude Haiku 3"),
        )

    /**
     * Retrieves all available models for a specific AI provider.
     *
     * This function returns the appropriate model list based on the provider type.
     * It's useful for populating UI dropdowns and validating model selections.
     *
     * @param provider The AI provider to get models for
     * @return List of AIModel objects for the specified provider
     *
     * @throws IllegalArgumentException if an unsupported provider is passed
     *
     * Usage:
     * ```kotlin
     * val claudeModels = AIModels.getModelsForProvider(AIProvider.ANTHROPIC)
     * val openaiModels = AIModels.getModelsForProvider(AIProvider.OPENAI)
     * ```
     */
    fun getModelsForProvider(provider: AIProvider): List<AIModel> =
        when (provider) {
            AIProvider.OPENAI -> OPENAI_MODELS
            AIProvider.GEMINI -> GEMINI_MODELS
            AIProvider.ANTHROPIC -> ANTHROPIC_MODELS
        }
}
