package com.thewintershadow.thoughtsmith.util

import android.content.Context
import android.content.Intent
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.thewintershadow.thoughtsmith.data.TTSProvider
import com.thewintershadow.thoughtsmith.repository.AIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Service for handling speech-to-text and text-to-speech functionality.
 *
 * This service provides:
 * - Speech-to-text conversion using Android's SpeechRecognizer
 * - Text-to-speech conversion using Android's TextToSpeech engine (local)
 * - Text-to-speech conversion using AI provider APIs (remote, more natural)
 * - Flow-based API for reactive speech recognition
 *
 * @param context Android context for accessing speech services
 */
class SpeechService(
    private val context: Context,
) {
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private val aiService = AIService()
    private var isTtsInitialized = false
    private var isListening = false
    private var currentTtsProvider: TTSProvider = TTSProvider.LOCAL

    init {
        initializeTextToSpeech()
    }

    /**
     * Initialize the Text-to-Speech engine.
     */
    private fun initializeTextToSpeech() {
        textToSpeech =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale.getDefault())
                    isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    if (!isTtsInitialized) {
                        AppLogger.warning("SpeechService", "TTS language not supported")
                    } else {
                        AppLogger.info("SpeechService", "Text-to-Speech initialized successfully")
                    }
                } else {
                    AppLogger.error("SpeechService", "Text-to-Speech initialization failed", null)
                }
            }
    }

    /**
     * Check if speech recognition is available on this device.
     */
    fun isSpeechRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Start listening for speech input and return a Flow of recognized text.
     * Continuously listens until stopListening() is called.
     *
     * This function implements continuous speech recognition with automatic restart:
     * - Starts speech recognizer and begins listening
     * - Emits recognized text through the Flow
     * - Automatically restarts after each recognition result
     * - Handles errors gracefully (fatal vs recoverable)
     * - Continues listening until explicitly stopped
     *
     * Error Handling:
     * - Fatal errors (audio, permissions, network): Stop and emit error
     * - Recoverable errors (timeout, no match): Restart listening
     * - Non-blocking approach for continuous conversation
     *
     * Usage Pattern:
     * ```kotlin
     * speechService.startListening()
     *     .collect { result ->
     *         result.onSuccess { text ->
     *             // Process recognized text
     *         }
     *     }
     * ```
     *
     * @return Flow that emits recognized text or errors
     */
    fun startListening(): Flow<Result<String>> =
        callbackFlow {
            if (!isSpeechRecognitionAvailable()) {
                trySend(Result.failure(Exception("Speech recognition not available")))
                close()
                return@callbackFlow
            }

            if (isListening) {
                trySend(Result.failure(Exception("Already listening")))
                close()
                return@callbackFlow
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            isListening = true

            val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

            val listener =
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        AppLogger.info("SpeechService", "Ready for speech input")
                    }

                    override fun onBeginningOfSpeech() {
                        AppLogger.info("SpeechService", "Speech input started")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Audio level changes
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Partial recognition results
                    }

                    override fun onEndOfSpeech() {
                        AppLogger.info("SpeechService", "Speech input ended")
                    }

                    override fun onError(error: Int) {
                        val errorMessage =
                            when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Unknown error: $error"
                            }

                        AppLogger.error("SpeechService", "Speech recognition error: $errorMessage", null)
                        isListening = false
                        trySend(Result.failure(Exception(errorMessage)))
                        close()
                    }

                    override fun onResults(results: Bundle?) {
                        val matches =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""

                        if (text.isNotBlank()) {
                            AppLogger.info("SpeechService", "Speech recognized: ${text.take(50)}...")
                            trySend(Result.success(text))
                        }

                        // Recognition session ended
                        isListening = false
                        close()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        // Partial results can be used for real-time feedback
                        val matches =
                            partialResults?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION,
                            )
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            AppLogger.debug("SpeechService", "Partial result: $text")
                        }
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?,
                    ) {
                        // Additional events
                    }
                }

            speechRecognizer?.setRecognitionListener(listener)
            speechRecognizer?.startListening(intent)

            awaitClose {
                stopListening()
            }
        }

    /**
     * Stop listening for speech input.
     */
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    /**
     * Set the TTS provider to use for speech synthesis.
     *
     * @param provider The TTS provider (LOCAL or REMOTE)
     */
    fun setTTSProvider(provider: TTSProvider) {
        currentTtsProvider = provider
        AppLogger.info("SpeechService", "TTS provider set to: ${provider.displayName}")
    }

    /**
     * Speak the given text using text-to-speech.
     * Uses the currently selected TTS provider with provider-specific configuration.
     *
     * @param text The text to speak
     * @param ttsOpenAIApiKey API key for OpenAI TTS
     * @param ttsOpenAIModel Model for OpenAI TTS (e.g., "tts-1", "tts-1-hd")
     * @param ttsGeminiApiKey API key for Gemini TTS
     * @param ttsGeminiModel Model for Gemini TTS (e.g., "gemini-2.5-flash-preview-tts")
     * @param ttsGeminiVoiceName Voice name for Gemini TTS (e.g., "Kore", "Aoede", "Charon", "Fenrir")
     * @param ttsAnthropicApiKey API key for Anthropic TTS
     * @param ttsAnthropicModel Model for Anthropic TTS
     * @param awsAccessKey AWS access key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsSecretKey AWS secret key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsRegion AWS region for AWS Polly (required if using AWS_POLLY provider)
     * @param queueMode Whether to queue this utterance or replace current one (local only)
     */
    suspend fun speak(
        text: String,
        ttsOpenAIApiKey: String = "",
        ttsOpenAIModel: String = "tts-1",
        ttsGeminiApiKey: String = "",
        ttsGeminiModel: String = "gemini-2.5-flash-preview-tts",
        ttsGeminiVoiceName: String = "Kore",
        ttsAnthropicApiKey: String = "",
        ttsAnthropicModel: String = "",
        awsAccessKey: String = "",
        awsSecretKey: String = "",
        awsRegion: String = "us-east-1",
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
    ) {
        if (text.isBlank()) {
            AppLogger.warning("SpeechService", "Cannot speak empty text")
            return
        }

        when (currentTtsProvider) {
            TTSProvider.LOCAL -> speakLocal(text, queueMode)
            TTSProvider.OPENAI -> speakOpenAI(text, ttsOpenAIApiKey, ttsOpenAIModel)
            TTSProvider.GEMINI -> speakGemini(text, ttsGeminiApiKey, ttsGeminiModel, ttsGeminiVoiceName)
            TTSProvider.ANTHROPIC -> speakAnthropic(text, ttsAnthropicApiKey)
            TTSProvider.AWS_POLLY -> speakAWSPolly(text, awsAccessKey, awsSecretKey, awsRegion)
        }
    }

    /**
     * Speak using local Android TTS engine.
     */
    private fun speakLocal(
        text: String,
        queueMode: Int,
    ) {
        if (!isTtsInitialized) {
            AppLogger.warning("SpeechService", "Local TTS not initialized, cannot speak")
            return
        }

        AppLogger.info("SpeechService", "Speaking text locally: ${text.take(50)}...")
        textToSpeech?.speak(text, queueMode, null, null)
    }

    /**
     * Speak using OpenAI TTS API.
     */
    private suspend fun speakOpenAI(
        text: String,
        apiKey: String,
        model: String = "tts-1",
    ) {
        if (apiKey.isBlank()) {
            AppLogger.warning("SpeechService", "API key required for OpenAI TTS, falling back to local TTS")
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            return
        }

        AppLogger.info("SpeechService", "Generating speech with OpenAI (model: $model): ${text.take(50)}...")

        try {
            val audioResult = aiService.getOpenAITTSAudio(text, apiKey, model = model)

            if (audioResult.isSuccess) {
                val audioData = audioResult.getOrNull() ?: return
                playAudioData(audioData)
            } else {
                val error = audioResult.exceptionOrNull()
                AppLogger.error("SpeechService", "Failed to generate OpenAI TTS, falling back to local TTS", error)
                speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating OpenAI TTS, falling back to local TTS", e)
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
        }
    }

    /**
     * Speak using Google Gemini TTS API.
     */
    private suspend fun speakGemini(
        text: String,
        apiKey: String,
        model: String = "gemini-2.5-flash-preview-tts",
        voiceName: String = "Kore",
    ) {
        if (apiKey.isBlank()) {
            AppLogger.warning("SpeechService", "API key required for Gemini TTS, falling back to local TTS")
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            return
        }

        AppLogger.info(
            "SpeechService",
            "Generating speech with Gemini TTS (model: $model, voice: $voiceName): ${text.take(50)}...",
        )

        try {
            val audioResult = aiService.getGeminiTTSAudio(text, apiKey, model = model, voiceName = voiceName)

            if (audioResult.isSuccess) {
                val audioData = audioResult.getOrNull() ?: return
                // Gemini TTS returns PCM format, need to convert to WAV for MediaPlayer
                playAudioData(audioData, isPcmFormat = true)
            } else {
                val error = audioResult.exceptionOrNull()
                AppLogger.error("SpeechService", "Failed to generate Gemini TTS, falling back to local TTS", error)
                speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating Gemini TTS, falling back to local TTS", e)
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
        }
    }

    /**
     * Speak using Anthropic TTS API.
     */
    private suspend fun speakAnthropic(
        text: String,
        apiKey: String,
    ) {
        if (apiKey.isBlank()) {
            AppLogger.warning("SpeechService", "API key required for Anthropic TTS, falling back to local TTS")
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            return
        }

        AppLogger.info("SpeechService", "Generating speech with Anthropic: ${text.take(50)}...")

        try {
            val audioResult = aiService.getAnthropicTTSAudio(text, apiKey)

            if (audioResult.isSuccess) {
                val audioData = audioResult.getOrNull() ?: return
                playAudioData(audioData)
            } else {
                val error = audioResult.exceptionOrNull()
                AppLogger.error("SpeechService", "Failed to generate Anthropic TTS, falling back to local TTS", error)
                speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating Anthropic TTS, falling back to local TTS", e)
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
        }
    }

    /**
     * Speak using AWS Polly TTS API.
     */
    private suspend fun speakAWSPolly(
        text: String,
        accessKey: String,
        secretKey: String,
        region: String,
    ) {
        if (accessKey.isBlank() || secretKey.isBlank()) {
            AppLogger.warning("SpeechService", "AWS credentials required for AWS Polly TTS, falling back to local TTS")
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            return
        }

        AppLogger.info("SpeechService", "Generating speech with AWS Polly: ${text.take(50)}...")

        try {
            val audioResult = aiService.getAWSPollyTTSAudio(text, accessKey, secretKey, region)

            if (audioResult.isSuccess) {
                val audioData = audioResult.getOrNull() ?: return
                playAudioData(audioData)
            } else {
                val error = audioResult.exceptionOrNull()
                AppLogger.error("SpeechService", "Failed to generate AWS Polly TTS, falling back to local TTS", error)
                speakLocal(text, TextToSpeech.QUEUE_FLUSH)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating AWS Polly TTS, falling back to local TTS", e)
            speakLocal(text, TextToSpeech.QUEUE_FLUSH)
        }
    }

    /**
     * Convert PCM audio data to WAV format.
     *
     * Gemini TTS returns PCM audio (s16le, 24000 Hz, mono) which MediaPlayer cannot play directly.
     * This function wraps the PCM data with a WAV header so it can be played.
     *
     * @param pcmData Raw PCM audio bytes (s16le, 24000 Hz, mono)
     * @return WAV-formatted audio bytes
     */
    private fun convertPcmToWav(pcmData: ByteArray): ByteArray {
        val sampleRate = 24000
        val channels = 1
        val bitsPerSample = 16
        val dataSize = pcmData.size
        val fileSize = 36 + dataSize

        val wavHeader = ByteArray(44)
        var offset = 0

        // RIFF header
        "RIFF".toByteArray().copyInto(wavHeader, offset)
        offset += 4
        wavHeader[offset++] = (fileSize and 0xFF).toByte()
        wavHeader[offset++] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[offset++] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[offset++] = ((fileSize shr 24) and 0xFF).toByte()
        "WAVE".toByteArray().copyInto(wavHeader, offset)
        offset += 4

        // fmt chunk
        "fmt ".toByteArray().copyInto(wavHeader, offset)
        offset += 4
        wavHeader[offset++] = 16 // chunk size
        wavHeader[offset++] = 0
        wavHeader[offset++] = 0
        wavHeader[offset++] = 0
        wavHeader[offset++] = 1 // audio format (PCM)
        wavHeader[offset++] = 0
        wavHeader[offset++] = channels.toByte() // number of channels
        wavHeader[offset++] = 0
        wavHeader[offset++] = (sampleRate and 0xFF).toByte()
        wavHeader[offset++] = ((sampleRate shr 8) and 0xFF).toByte()
        wavHeader[offset++] = ((sampleRate shr 16) and 0xFF).toByte()
        wavHeader[offset++] = ((sampleRate shr 24) and 0xFF).toByte()
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        wavHeader[offset++] = (byteRate and 0xFF).toByte()
        wavHeader[offset++] = ((byteRate shr 8) and 0xFF).toByte()
        wavHeader[offset++] = ((byteRate shr 16) and 0xFF).toByte()
        wavHeader[offset++] = ((byteRate shr 24) and 0xFF).toByte()
        wavHeader[offset++] = (channels * (bitsPerSample / 8)).toByte() // block align
        wavHeader[offset++] = 0
        wavHeader[offset++] = bitsPerSample.toByte() // bits per sample
        wavHeader[offset++] = 0

        // data chunk
        "data".toByteArray().copyInto(wavHeader, offset)
        offset += 4
        wavHeader[offset++] = (dataSize and 0xFF).toByte()
        wavHeader[offset++] = ((dataSize shr 8) and 0xFF).toByte()
        wavHeader[offset++] = ((dataSize shr 16) and 0xFF).toByte()
        wavHeader[offset++] = ((dataSize shr 24) and 0xFF).toByte()

        // Combine header and PCM data
        return wavHeader + pcmData
    }

    /**
     * Save audio data to temporary file and play it.
     *
     * This helper method handles the common workflow of:
     * 1. Saving received audio data to a temporary file (MP3 for most providers, WAV for Gemini PCM)
     * 2. Switching to the main thread for MediaPlayer operations
     * 3. Playing the audio file
     *
     * The temporary file is automatically deleted after playback completes.
     *
     * @param audioData Raw audio bytes (MP3 format for most providers, PCM for Gemini)
     * @param isPcmFormat If true, converts PCM to WAV before saving
     */
    private suspend fun playAudioData(
        audioData: ByteArray,
        isPcmFormat: Boolean = false,
    ) {
        withContext(Dispatchers.IO) {
            val fileExtension = if (isPcmFormat) "wav" else "mp3"
            val tempFile = File(context.cacheDir, "tts_${System.currentTimeMillis()}.$fileExtension")

            // Convert PCM to WAV if needed, otherwise use audio data as-is
            val audioToWrite =
                if (isPcmFormat) {
                    convertPcmToWav(audioData)
                } else {
                    audioData
                }

            // Write audio data to file
            FileOutputStream(tempFile).use { it.write(audioToWrite) }

            // Switch to main thread for MediaPlayer (requires UI thread)
            withContext(Dispatchers.Main) {
                playAudioFile(tempFile.absolutePath)
            }
        }
    }

    /**
     * Play an audio file using MediaPlayer.
     *
     * Sets up MediaPlayer with proper lifecycle handling:
     * - Stops any currently playing audio first
     * - Configures completion and error listeners
     * - Automatically cleans up resources when done
     * - Deletes temporary file after playback
     *
     * @param filePath Absolute path to the audio file to play
     */
    private fun playAudioFile(filePath: String) {
        try {
            // Stop any currently playing audio
            stopSpeaking()

            mediaPlayer =
                MediaPlayer()
                    .apply {
                        setDataSource(filePath)
                        prepare()
                        setOnCompletionListener {
                            release()
                            mediaPlayer = null
                            // Clean up temp file
                            try {
                                File(filePath).delete()
                            } catch (e: Exception) {
                                AppLogger.warning("SpeechService", "Failed to delete temp file: ${e.message}")
                            }
                        }
                        setOnErrorListener { _, what, extra ->
                            AppLogger.error("SpeechService", "MediaPlayer error: what=$what, extra=$extra", null)
                            release()
                            mediaPlayer = null
                            true
                        }
                        start()
                    }
            AppLogger.info("SpeechService", "Playing remote TTS audio")
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Failed to play audio file", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    /**
     * Stop speaking if currently speaking.
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
        audioTrack?.let {
            if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                it.stop()
            }
            it.release()
            audioTrack = null
        }
        AppLogger.info("SpeechService", "Stopped speaking")
    }

    /**
     * Check if TTS is currently speaking.
     */
    fun isSpeaking(): Boolean =
        (textToSpeech?.isSpeaking ?: false) ||
            (mediaPlayer?.isPlaying ?: false) ||
            (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING)

    /**
     * Clean up resources when the service is no longer needed.
     */
    fun cleanup() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
        mediaPlayer?.release()
        mediaPlayer = null
        audioTrack?.release()
        audioTrack = null
        AppLogger.info("SpeechService", "SpeechService cleaned up")
    }
}
