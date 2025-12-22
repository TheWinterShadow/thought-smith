package com.thewintershadow.thoughtsmith.repository

import com.google.gson.Gson
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
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
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
        systemContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            AppLogger.info("AIService", "Requesting AI response from ${provider.displayName} using model $model")
            val result = when (provider) {
                AIProvider.OPENAI -> getOpenAIResponse(messages, model, apiKey, systemContext)
                AIProvider.GEMINI -> getGeminiResponse(messages, model, apiKey, systemContext)
                AIProvider.ANTHROPIC -> getAnthropicResponse(messages, model, apiKey, systemContext)
            }
            if (result.isSuccess) {
                AppLogger.info("AIService", "Successfully received AI response from ${provider.displayName}")
            } else {
                AppLogger.error("AIService", "Failed to get AI response from ${provider.displayName}", result.exceptionOrNull())
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
        systemContext: String
    ): Result<String> {
        val requestBody = gson.toJson(mapOf(
            "model" to model,
            "messages" to (listOf(mapOf("role" to "system", "content" to systemContext)) +
                messages.map { mapOf("role" to (if (it.isUser) "user" else "assistant"), "content" to it.content) }),
            "temperature" to 0.7
        ))
        
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))
        
        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }
        
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
        systemContext: String
    ): Result<String> {
        val contents = messages.map { mapOf(
            "role" to (if (it.isUser) "user" else "model"),
            "parts" to listOf(mapOf("text" to it.content))
        )}
        
        val requestBody = gson.toJson(mapOf(
            "contents" to contents,
            "systemInstruction" to mapOf("parts" to listOf(mapOf("text" to systemContext)))
        ))
        
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))
        
        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }
        
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
        systemContext: String
    ): Result<String> {
        val requestBody = gson.toJson(mapOf(
            "model" to model,
            "max_tokens" to 4096,
            "system" to systemContext,
            "messages" to messages.map { mapOf(
                "role" to (if (it.isUser) "user" else "assistant"),
                "content" to it.content
            ) }
        ))
        
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))
        
        if (!response.isSuccessful) {
            return Result.failure(Exception("API Error: $responseBody"))
        }
        
        val jsonResponse = gson.fromJson(responseBody, Map::class.java)
        val content = jsonResponse["content"] as? List<*>
        val text = (content?.firstOrNull() as? Map<*, *>)?.get("text") as? String
        
        return if (text != null) {
            Result.success(text)
        } else {
            Result.failure(Exception("Invalid response format"))
        }
    }
}

