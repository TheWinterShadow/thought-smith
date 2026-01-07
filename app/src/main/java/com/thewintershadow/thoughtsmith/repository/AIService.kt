package com.thewintershadow.thoughtsmith.repository

import com.google.gson.GsonBuilder
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.Message
import com.thewintershadow.thoughtsmith.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Service class for communicating with various AI providers' APIs.
 *
 * This service handles HTTP communication with multiple AI providers including:
 * - OpenAI (GPT models)
 * - Google Gemini (Generative AI)
 * - Anthropic (Claude models)
 *
 * Each provider has different API formats and authentication methods, which are
 * handled transparently by this service. The service provides a unified interface
 * for sending conversation messages and receiving AI responses.
 *
 * Features:
 * - Unified API for multiple AI providers
 * - Proper error handling and logging
 * - Configurable timeouts for network requests
 * - JSON request/response handling
 * - System context/prompt support
 * - Asynchronous operations using Coroutines
 *
 * Network Configuration:
 * - 30-second connection and read timeouts
 * - HTTP request/response logging for debugging
 * - Automatic retry handling through OkHttp
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class AIService {
    /**
     * HTTP client configured for AI API communication.
     * Includes timeouts and logging for debugging.
     */
    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            ).build()

    /**
     * JSON serializer/deserializer for API communication.
     */
    private val gson = GsonBuilder().create()

    /**
     * Get an AI response from the specified provider and model.
     *
     * This is the main entry point for AI communication. It routes requests to the
     * appropriate provider-specific method and handles common error scenarios.
     *
     * The conversation history is maintained by passing all previous messages, allowing
     * the AI to maintain context throughout the conversation.
     *
     * @param messages List of conversation messages between user and AI
     * @param provider The AI service provider to use (OpenAI, Gemini, or Anthropic)
     * @param model The specific AI model to use (e.g., "gpt-4o", "gemini-1.5-pro")
     * @param apiKey The API key for authentication with the provider
     * @param systemContext The system prompt that defines AI behavior and personality
     *
     * @return Result containing either the AI response text or an exception
     *
     * @throws Exception Various network, authentication, or API-specific errors
     */
    suspend fun getAIResponse(
        messages: List<Message>,
        provider: AIProvider,
        model: String,
        apiKey: String,
        systemContext: String,
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                AppLogger.info("AIService", "Requesting AI response from ${provider.displayName} using model $model")
                val result =
                    when (provider) {
                        AIProvider.OPENAI -> getOpenAIResponse(messages, model, apiKey, systemContext)
                        AIProvider.GEMINI -> getGeminiResponse(messages, model, apiKey, systemContext)
                        AIProvider.ANTHROPIC -> getAnthropicResponse(messages, model, apiKey, systemContext)
                    }
                if (result.isSuccess) {
                    AppLogger.info("AIService", "Successfully received AI response from ${provider.displayName}")
                } else {
                    AppLogger.error(
                        "AIService",
                        "Failed to get AI response from ${provider.displayName}",
                        result.exceptionOrNull(),
                    )
                }
                result
            } catch (e: Exception) {
                AppLogger.error("AIService", "Exception while getting AI response", e)
                Result.failure(e)
            }
        }

    /**
     * Handle API communication with OpenAI's ChatGPT models.
     *
     * OpenAI uses a messages array format with roles (system, user, assistant).
     * The API endpoint is `/v1/chat/completions` with Bearer token authentication.
     *
     * Message Format:
     * - System message: Defines AI behavior and personality
     * - User messages: Questions and statements from the user
     * - Assistant messages: Previous AI responses (for context)
     *
     * The temperature parameter (0.7) controls response creativity:
     * - Lower values (0.0-0.3): More focused and deterministic
     * - Medium values (0.4-0.7): Balanced creativity and consistency
     * - Higher values (0.8-1.0): More creative and varied responses
     *
     * @param messages Conversation history
     * @param model OpenAI model name (e.g., "gpt-4o", "gpt-3.5-turbo")
     * @param apiKey OpenAI API key
     * @param systemContext System prompt for AI behavior
     *
     * @return Result containing the AI response or error
     */
    private fun getOpenAIResponse(
        messages: List<Message>,
        model: String,
        apiKey: String,
        systemContext: String,
    ): Result<String> {
        // Build request body with system context first, then conversation messages
        val requestBody =
            gson.toJson(
                mapOf(
                    "model" to model,
                    "messages" to (
                        // System message defines AI behavior
                        listOf(mapOf("role" to "system", "content" to systemContext)) +
                            // Convert app messages to OpenAI format
                            messages.map {
                                mapOf(
                                    "role" to (if (it.isUser) "user" else "assistant"),
                                    "content" to it.content,
                                )
                            }
                    ),
                    // Temperature controls creativity (0.0-1.0)
                    "temperature" to 0.7,
                ),
            )

        // Build HTTP request with Bearer token authentication
        val request =
            Request
                .Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

        // Execute request synchronously (called from IO dispatcher)
        val response = client.newCall(request).execute()
        val responseBody = response.body.string()

        // Check for HTTP errors
        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }

        // Parse JSON response to extract AI's message
        val jsonResponse = gson.fromJson(responseBody, Map::class.java)
        val choices = jsonResponse["choices"] as? List<*>
        val message = (choices?.firstOrNull() as? Map<*, *>)?.get("message") as? Map<*, *>
        val content = message?.get("content") as? String

        return if (content != null) {
            Result.success(content)
        } else {
            Result.failure(Exception("Invalid response format"))
        }
    }

    /**
     * Handle API communication with Google's Gemini models.
     *
     * Gemini uses a contents array format with roles (user, model) and parts structure.
     * The API endpoint includes the model name and requires an API key as a query parameter.
     * System instructions are provided separately from the conversation content.
     *
     * Message Structure:
     * - Each message has a "role" (user or model) and "parts" array
     * - Parts contain the actual text content
     * - System instructions are passed separately via systemInstruction field
     *
     * Authentication:
     * Unlike OpenAI's Bearer token, Gemini uses API key as URL query parameter
     *
     * @param messages Conversation history
     * @param model Gemini model name (e.g., "gemini-1.5-pro", "gemini-1.5-flash")
     * @param apiKey Google API key
     * @param systemContext System instructions for AI behavior
     *
     * @return Result containing the AI response or error
     */
    private fun getGeminiResponse(
        messages: List<Message>,
        model: String,
        apiKey: String,
        systemContext: String,
    ): Result<String> {
        // Convert messages to Gemini's parts-based format
        val contents =
            messages.map {
                mapOf(
                    "role" to (if (it.isUser) "user" else "model"),
                    "parts" to listOf(mapOf("text" to it.content)),
                )
            }

        // Build request with separate system instruction
        val requestBody =
            gson.toJson(
                mapOf(
                    "contents" to contents,
                    // System instructions are separate from message content
                    "systemInstruction" to mapOf("parts" to listOf(mapOf("text" to systemContext))),
                ),
            )

        // API key is passed as query parameter, not header
        val request =
            Request
                .Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body.string()

        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }

        // Parse nested response structure: candidates -> content -> parts -> text
        val jsonResponse = gson.fromJson(responseBody, Map::class.java)
        val candidates = jsonResponse["candidates"] as? List<*>
        val content = (candidates?.firstOrNull() as? Map<*, *>)?.get("content") as? Map<*, *>
        val parts = content?.get("parts") as? List<*>
        val text = (parts?.firstOrNull() as? Map<*, *>)?.get("text") as? String

        return if (text != null) {
            Result.success(text)
        } else {
            Result.failure(Exception("Invalid response format"))
        }
    }

    /**
     * Handle API communication with Anthropic's Claude models.
     *
     * Claude uses a messages array format with roles (user, assistant) and supports
     * a separate system parameter for context. Authentication uses a custom x-api-key header
     * and requires an API version header.
     *
     * Key Differences from Other Providers:
     * - Custom authentication header: x-api-key (not Authorization)
     * - API version header required: anthropic-version
     * - max_tokens parameter required (not optional like OpenAI)
     * - System context as top-level "system" field
     *
     * Claude Models are known for:
     * - Thoughtful, nuanced responses
     * - Strong reasoning capabilities
     * - Long context windows
     * - Constitutional AI training for safety
     *
     * @param messages Conversation history
     * @param model Claude model name (e.g., "claude-3-5-sonnet", "claude-3-haiku")
     * @param apiKey Anthropic API key
     * @param systemContext System prompt for AI behavior
     *
     * @return Result containing the AI response or error
     */
    private fun getAnthropicResponse(
        messages: List<Message>,
        model: String,
        apiKey: String,
        systemContext: String,
    ): Result<String> {
        // Build request with required max_tokens parameter
        val requestBody =
            gson.toJson(
                mapOf(
                    "model" to model,
                    // Claude requires explicit max_tokens (OpenAI has defaults)
                    "max_tokens" to 4096,
                    // System context is a top-level field, not a message
                    "system" to systemContext,
                    // Messages use "user" and "assistant" roles
                    "messages" to
                        messages.map {
                            mapOf(
                                "role" to (if (it.isUser) "user" else "assistant"),
                                "content" to it.content,
                            )
                        },
                ),
            )

        // Use custom x-api-key header for authentication
        val request =
            Request
                .Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                // API version header is required for Anthropic
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body.string()

        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }

        // Parse Claude's simpler response format: content array -> text
        val jsonResponse = gson.fromJson(responseBody, Map::class.java)
        val content = jsonResponse["content"] as? List<*>
        val text = (content?.firstOrNull() as? Map<*, *>)?.get("text") as? String

        return if (text != null) {
            Result.success(text)
        } else {
            Result.failure(Exception("Invalid response format"))
        }
    }

    /**
     * Generate speech from text using OpenAI's TTS API.
     *
     * OpenAI TTS provides high-quality, natural-sounding voices.
     * The API returns audio data in MP3 format.
     *
     * @param text The text to convert to speech
     * @param apiKey OpenAI API key
     * @param voice The voice to use (alloy, echo, fable, onyx, nova, shimmer). Defaults to "nova"
     * @param model The TTS model to use (tts-1 or tts-1-hd). Defaults to "tts-1"
     *
     * @return Result containing the audio data as ByteArray or an error
     */
    suspend fun getOpenAITTSAudio(
        text: String,
        apiKey: String,
        voice: String = "nova",
        model: String = "tts-1",
    ): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                AppLogger.info("AIService", "Requesting TTS audio from OpenAI")

                val requestBody =
                    gson.toJson(
                        mapOf(
                            "model" to model,
                            "input" to text,
                            "voice" to voice,
                        ),
                    )

                val request =
                    Request
                        .Builder()
                        .url("https://api.openai.com/v1/audio/speech")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody.toRequestBody("application/json".toMediaType()))
                        .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body.bytes()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API Error: ${responseBody.toString(Charsets.UTF_8)}"))
                }

                AppLogger.info("AIService", "Successfully received TTS audio (${responseBody.size} bytes)")
                Result.success(responseBody)
            } catch (e: Exception) {
                AppLogger.error("AIService", "Exception while getting TTS audio", e)
                Result.failure(e)
            }
        }

    /**
     * Generate speech from text using Anthropic's TTS API.
     *
     * Note: Anthropic may not have a public TTS API yet. This is a placeholder
     * for future implementation when the API becomes available.
     *
     * @param text The text to convert to speech
     * @param apiKey Anthropic API key
     *
     * @return Result containing the audio data as ByteArray or an error
     */
    suspend fun getAnthropicTTSAudio(
        text: String,
        apiKey: String,
    ): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                AppLogger.info("AIService", "Requesting TTS audio from Anthropic")

                // Note: Anthropic TTS API endpoint may not be available yet
                // This is a placeholder implementation
                // When available, update with actual API endpoint and request format

                // For now, return an error indicating the API is not yet available
                Result.failure(
                    Exception(
                        "Anthropic TTS API is not yet available. Please use OpenAI TTS or AWS Polly instead.",
                    ),
                )
            } catch (e: Exception) {
                AppLogger.error("AIService", "Exception while getting Anthropic TTS audio", e)
                Result.failure(e)
            }
        }

    /**
     * Generate speech from text using AWS Polly TTS API.
     *
     * AWS Polly provides a wide variety of natural-sounding voices in multiple languages.
     * The API returns audio data in MP3 format.
     *
     * AWS Signature Version 4 authentication is required.
     *
     * @param text The text to convert to speech
     * @param accessKey AWS access key ID
     * @param secretKey AWS secret access key
     * @param region AWS region (e.g., "us-east-1")
     * @param voiceId The voice ID to use (e.g., "Joanna", "Matthew", "Amy"). Defaults to "Joanna"
     * @param engine The engine to use ("standard" or "neural"). Defaults to "neural"
     *
     * @return Result containing the audio data as ByteArray or an error
     */
    suspend fun getAWSPollyTTSAudio(
        text: String,
        accessKey: String,
        secretKey: String,
        region: String,
        voiceId: String = "Joanna",
        engine: String = "neural",
    ): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                AppLogger.info("AIService", "Requesting TTS audio from AWS Polly")

                // AWS Polly uses REST API with Signature Version 4 authentication
                // For simplicity, we'll use a basic implementation
                // In production, you should use AWS SDK for proper signature handling

                val requestBody =
                    gson.toJson(
                        mapOf(
                            "Text" to text,
                            "OutputFormat" to "mp3",
                            "VoiceId" to voiceId,
                            "Engine" to engine,
                        ),
                    )

                // AWS Polly endpoint
                val endpoint = "https://polly.$region.amazonaws.com/v1/speech"

                // Note: AWS requires Signature Version 4 signing which is complex
                // For a production app, consider using AWS SDK for Android
                // This is a simplified implementation that may need AWS SDK integration

                val request =
                    Request
                        .Builder()
                        .url(endpoint)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Amz-Target", "AWSPollyService.SynthesizeSpeech")
                        // TODO: Add proper AWS Signature Version 4 authentication
                        // For now, this will fail authentication - consider using AWS SDK
                        .post(requestBody.toRequestBody("application/json".toMediaType()))
                        .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body.bytes()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(
                            "AWS Polly API Error: ${responseBody.toString(Charsets.UTF_8)}",
                        ),
                    )
                }

                AppLogger.info(
                    "AIService",
                    "Successfully received AWS Polly TTS audio (${responseBody.size} bytes)",
                )
                Result.success(responseBody)
            } catch (e: Exception) {
                AppLogger.error("AIService", "Exception while getting AWS Polly TTS audio", e)
                Result.failure(e)
            }
        }
}
