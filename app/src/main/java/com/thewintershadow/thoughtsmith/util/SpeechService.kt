package com.thewintershadow.thoughtsmith.util

import android.content.Context
import android.media.MediaPlayer
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import androidx.activity.result.ActivityResultLauncher
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.TTSProvider
import com.thewintershadow.thoughtsmith.repository.AIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
class SpeechService(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaPlayer: MediaPlayer? = null
    private val aiService = AIService()
    private var isTtsInitialized = false
    private var isListening = false
    private var shouldContinueListening = false
    private var currentTtsProvider: TTSProvider = TTSProvider.LOCAL

    init {
        initializeTextToSpeech()
    }

    /**
     * Initialize the Text-to-Speech engine.
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
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
    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for speech input and return a Flow of recognized text.
     * Continuously listens until stopListening() is called.
     *
     * @return Flow that emits recognized text or errors
     */
    fun startListening(): Flow<Result<String>> = callbackFlow {
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
        shouldContinueListening = true

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        // Create a coroutine scope for restarting recognition
        val recognitionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        fun startRecognition() {
            if (!shouldContinueListening || speechRecognizer == null) {
                return
            }

            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    AppLogger.info("SpeechService", "Ready for speech input")
                }

                override fun onBeginningOfSpeech() {
                    AppLogger.info("SpeechService", "Speech input started")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changes - can be used for visual feedback
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Partial recognition results
                }

                override fun onEndOfSpeech() {
                    AppLogger.info("SpeechService", "Speech input ended")
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
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
                    
                    // Only log non-fatal errors (timeout, no match, etc. are expected during pauses)
                    val isFatalError = error in listOf(
                        SpeechRecognizer.ERROR_AUDIO,
                        SpeechRecognizer.ERROR_CLIENT,
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_SERVER
                    )
                    
                    if (isFatalError) {
                        AppLogger.error("SpeechService", "Fatal speech recognition error: $errorMessage", null)
                        isListening = false
                        shouldContinueListening = false
                        trySend(Result.failure(Exception(errorMessage)))
                        close()
                    } else {
                        // Recoverable errors (timeout, no match) - just restart listening
                        AppLogger.debug("SpeechService", "Recoverable error, restarting: $errorMessage")
                        if (shouldContinueListening) {
                            // Small delay before restarting to avoid rapid restarts
                            recognitionScope.launch {
                                delay(100)
                                if (shouldContinueListening) {
                                    startRecognition()
                                }
                            }
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    
                    if (text.isNotBlank()) {
                        AppLogger.info("SpeechService", "Speech recognized: ${text.take(50)}...")
                        trySend(Result.success(text))
                    }
                    
                    // Continue listening after getting results
                    if (shouldContinueListening) {
                        startRecognition()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results can be used for real-time feedback
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        AppLogger.debug("SpeechService", "Partial result: $text")
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Additional events
                }
            }

            speechRecognizer?.setRecognitionListener(listener)
            speechRecognizer?.startListening(intent)
        }

        // Start the first recognition session
        startRecognition()

        awaitClose {
            shouldContinueListening = false
            stopListening()
        }
    }

    /**
     * Stop listening for speech input.
     */
    fun stopListening() {
        shouldContinueListening = false
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
     * Uses the currently selected TTS provider.
     *
     * @param text The text to speak
     * @param ttsApiKey API key for OpenAI/Anthropic TTS (required if using OPENAI or ANTHROPIC provider)
     * @param ttsProviderType The AI provider for TTS (OpenAI or Anthropic)
     * @param ttsModel The TTS model/voice to use
     * @param awsAccessKey AWS access key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsSecretKey AWS secret key for AWS Polly (required if using AWS_POLLY provider)
     * @param awsRegion AWS region for AWS Polly (required if using AWS_POLLY provider)
     * @param queueMode Whether to queue this utterance or replace current one (local only)
     */
    suspend fun speak(
        text: String,
        ttsApiKey: String = "",
        ttsProviderType: com.thewintershadow.thoughtsmith.data.AIProvider = com.thewintershadow.thoughtsmith.data.AIProvider.OPENAI,
        ttsModel: String = "tts-1",
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
            TTSProvider.OPENAI -> {
                if (ttsProviderType == AIProvider.OPENAI) {
                    speakOpenAI(text, ttsApiKey, ttsModel)
                } else {
                    AppLogger.warning("SpeechService", "TTS provider type mismatch for OpenAI TTS")
                }
            }
            TTSProvider.ANTHROPIC -> {
                if (ttsProviderType == AIProvider.ANTHROPIC) {
                    speakAnthropic(text, ttsApiKey)
                } else {
                    AppLogger.warning("SpeechService", "TTS provider type mismatch for Anthropic TTS")
                }
            }
            TTSProvider.AWS_POLLY -> speakAWSPolly(text, awsAccessKey, awsSecretKey, awsRegion)
        }
    }

    /**
     * Speak using local Android TTS engine.
     */
    private fun speakLocal(text: String, queueMode: Int) {
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
    private suspend fun speakOpenAI(text: String, apiKey: String, model: String = "tts-1") {
        if (apiKey.isBlank()) {
            AppLogger.warning("SpeechService", "API key required for OpenAI TTS")
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
                AppLogger.error("SpeechService", "Failed to generate OpenAI TTS", error)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating OpenAI TTS", e)
        }
    }

    /**
     * Speak using Anthropic TTS API.
     */
    private suspend fun speakAnthropic(text: String, apiKey: String) {
        if (apiKey.isBlank()) {
            AppLogger.warning("SpeechService", "API key required for Anthropic TTS")
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
                AppLogger.error("SpeechService", "Failed to generate Anthropic TTS", error)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating Anthropic TTS", e)
        }
    }

    /**
     * Speak using AWS Polly TTS API.
     */
    private suspend fun speakAWSPolly(text: String, accessKey: String, secretKey: String, region: String) {
        if (accessKey.isBlank() || secretKey.isBlank()) {
            AppLogger.warning("SpeechService", "AWS credentials required for AWS Polly TTS")
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
                AppLogger.error("SpeechService", "Failed to generate AWS Polly TTS", error)
            }
        } catch (e: Exception) {
            AppLogger.error("SpeechService", "Exception while generating AWS Polly TTS", e)
        }
    }

    /**
     * Save audio data to temporary file and play it.
     */
    private suspend fun playAudioData(audioData: ByteArray) {
        withContext(Dispatchers.IO) {
            val tempFile = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            
            withContext(Dispatchers.Main) {
                playAudioFile(tempFile.absolutePath)
            }
        }
    }

    /**
     * Play an audio file using MediaPlayer.
     */
    private fun playAudioFile(filePath: String) {
        try {
            // Stop any currently playing audio
            stopSpeaking()
            
            mediaPlayer = MediaPlayer().apply {
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
        AppLogger.info("SpeechService", "Stopped speaking")
    }

    /**
     * Check if TTS is currently speaking.
     */
    fun isSpeaking(): Boolean {
        return (textToSpeech?.isSpeaking ?: false) || (mediaPlayer?.isPlaying ?: false)
    }

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
        AppLogger.info("SpeechService", "SpeechService cleaned up")
    }
}

