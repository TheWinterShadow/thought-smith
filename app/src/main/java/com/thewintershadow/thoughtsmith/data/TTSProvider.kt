/**
 * Text-to-Speech provider definitions for voice output configuration.
 *
 * This file defines the available TTS options for reading AI responses aloud.
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
package com.thewintershadow.thoughtsmith.data

/**
 * Enum representing available Text-to-Speech providers.
 *
 * This enum defines the different TTS options available to users:
 * - LOCAL: Uses Android's built-in TextToSpeech engine (free, offline)
 * - OPENAI: Uses OpenAI's TTS API for natural voices
 * - ANTHROPIC: Uses Anthropic's TTS API (if available)
 * - AWS_POLLY: Uses Amazon Polly TTS service
 *
 * @property displayName User-friendly name for the provider
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
enum class TTSProvider(
    val displayName: String,
) {
    LOCAL("Local (Device)"),
    OPENAI("OpenAI TTS"),
    ANTHROPIC("Anthropic TTS"),
    AWS_POLLY("AWS Polly"),
}
