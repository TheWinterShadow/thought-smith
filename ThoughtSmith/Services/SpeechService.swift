//
//  SpeechService.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import AVFoundation
import Speech
import AVFAudio
import Combine

/// Service for handling speech-to-text and text-to-speech functionality.
@MainActor
class SpeechService: NSObject, ObservableObject {
    static let shared = SpeechService()
    
    @Published var isListening = false
    @Published var isSpeaking = false
    
    private var speechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()
    
    private var speechSynthesizer = AVSpeechSynthesizer()
    private var currentTTSProvider: TTSProvider = .local
    private let aiService = AIService.shared
    
    private var recognitionContinuation: CheckedContinuation<String, Error>?
    
    override init() {
        super.init()
        setupSpeechRecognizer()
    }
    
    private func setupSpeechRecognizer() {
        speechRecognizer = SFSpeechRecognizer(locale: Locale.current)
    }
    
    /// Check if speech recognition is available on this device.
    func isSpeechRecognitionAvailable() -> Bool {
        #if targetEnvironment(simulator)
        // In simulator, always return true since we're using test mode
        return true
        #else
        guard let recognizer = speechRecognizer else { return false }
        return recognizer.isAvailable
        #endif
    }
    
    /// Request speech recognition authorization.
    func requestAuthorization() async -> Bool {
        await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { status in
                continuation.resume(returning: status == .authorized)
            }
        }
    }
    
    /// Start listening for speech input and return recognized text.
    func startListening() async throws -> String {
        #if targetEnvironment(simulator)
        // In simulator, show an alert and return test text after a delay
        AppLogger.shared.warning("SpeechService", "🧪 SIMULATOR MODE: Using test speech input (real mic not available)")
        
        isListening = true
        
        // Simulate "listening" for 2 seconds
        AppLogger.shared.info("SpeechService", "Simulating listening for 2 seconds...")
        try? await Task.sleep(nanoseconds: 2_000_000_000)
        
        isListening = false
        
        // Return test text
        let testPhrases = [
            "Today was a great day. I accomplished a lot and feel proud of myself.",
            "I'm feeling stressed about work, but I'm trying to stay positive.",
            "Had a wonderful time with friends today. Grateful for good company.",
            "Feeling overwhelmed with everything going on, need to take a break.",
            "Made progress on my goals today. Small steps count!"
        ]
        
        let selectedPhrase = testPhrases.randomElement() ?? testPhrases[0]
        AppLogger.shared.info("SpeechService", "Returning test phrase: \(selectedPhrase)")
        
        return selectedPhrase
        #else
        // Real device implementation
        AppLogger.shared.info("SpeechService", "🎤 DEVICE MODE: Using real speech recognition")
        
        guard isSpeechRecognitionAvailable() else {
            throw SpeechServiceError.notAvailable
        }
        
        guard await requestAuthorization() else {
            throw SpeechServiceError.authorizationDenied
        }
        
        if isListening {
            throw SpeechServiceError.alreadyListening
        }
        
        return try await withCheckedThrowingContinuation { continuation in
            self.recognitionContinuation = continuation
            
            do {
                // Cancel previous task if any
                recognitionTask?.cancel()
                recognitionTask = nil
                
                // Create recognition request
                recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
                guard let recognitionRequest = recognitionRequest else {
                    continuation.resume(throwing: SpeechServiceError.setupFailed)
                    return
                }
                
                recognitionRequest.shouldReportPartialResults = true
                
                // Start audio session
                let audioSession = AVAudioSession.sharedInstance()
                try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
                try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
                
                // Setup audio engine
                let inputNode = audioEngine.inputNode
                let recordingFormat = inputNode.outputFormat(forBus: 0)
                
                inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
                    recognitionRequest.append(buffer)
                }
                
                audioEngine.prepare()
                try audioEngine.start()
                
                isListening = true
                
                // Start recognition task
                recognitionTask = speechRecognizer?.recognitionTask(with: recognitionRequest) { [weak self] result, error in
                    guard let self = self else { return }
                    
                    if let result = result {
                        if result.isFinal {
                            let recognizedText = result.bestTranscription.formattedString
                            self.stopListening()
                            self.recognitionContinuation?.resume(returning: recognizedText)
                            self.recognitionContinuation = nil
                        }
                    } else if let error = error {
                        self.stopListening()
                        self.recognitionContinuation?.resume(throwing: error)
                        self.recognitionContinuation = nil
                    }
                }
                
            } catch {
                stopListening()
                continuation.resume(throwing: error)
            }
        }
        #endif
    }
    
    /// Stop listening for speech input.
    func stopListening() {
        if isListening {
            audioEngine.stop()
            audioEngine.inputNode.removeTap(onBus: 0)
            recognitionRequest?.endAudio()
            recognitionRequest = nil
            recognitionTask?.cancel()
            recognitionTask = nil
            
            let audioSession = AVAudioSession.sharedInstance()
            try? audioSession.setActive(false)
            
            isListening = false
        }
    }
    
    /// Set the TTS provider to use for speech synthesis.
    func setTTSProvider(_ provider: TTSProvider) {
        currentTTSProvider = provider
        AppLogger.shared.info("SpeechService", "TTS provider set to: \(provider.displayName)")
    }
    
    /// Speak the given text using text-to-speech.
    func speak(_ text: String, settings: AppSettings) async {
        if text.isEmpty {
            AppLogger.shared.warning("SpeechService", "Cannot speak empty text")
            return
        }
        
        isSpeaking = true
        
        switch currentTTSProvider {
        case .local:
            speakLocal(text)
        case .openAI:
            await speakOpenAI(text, apiKey: settings.ttsOpenAIApiKey, model: settings.ttsOpenAIModel)
        case .gemini:
            await speakGemini(text, apiKey: settings.ttsGeminiApiKey, model: settings.ttsGeminiModel, voiceName: settings.ttsGeminiVoiceName)
        case .anthropic:
            await speakAnthropic(text, apiKey: settings.ttsAnthropicApiKey)
        case .awsPolly:
            await speakAWSPolly(text, accessKey: settings.awsAccessKey, secretKey: settings.awsSecretKey, region: settings.awsRegion)
        }
    }
    
    /// Speak using local iOS TTS engine.
    private func speakLocal(_ text: String) {
        let utterance = AVSpeechUtterance(string: text)
        if #available(iOS 16, *) {
            utterance.voice = AVSpeechSynthesisVoice(language: Locale.current.language.languageCode?.identifier ?? "en-US")
        } else {
            utterance.voice = AVSpeechSynthesisVoice(language: Locale.current.languageCode ?? "en-US")
        }
        utterance.rate = 0.5
        
        speechSynthesizer.delegate = self
        speechSynthesizer.speak(utterance)
    }
    
    /// Speak using OpenAI TTS API.
    private func speakOpenAI(_ text: String, apiKey: String, model: String) async {
        if apiKey.isEmpty {
            AppLogger.shared.warning("SpeechService", "API key required for OpenAI TTS, falling back to local TTS")
            speakLocal(text)
            return
        }
        
        do {
            let audioData = try await aiService.getOpenAITTSAudio(text: text, apiKey: apiKey, model: model)
            await playAudioData(audioData, isMP3: true)
        } catch {
            AppLogger.shared.error("SpeechService", "Failed to generate OpenAI TTS, falling back to local TTS", error: error)
            speakLocal(text)
        }
    }
    
    /// Speak using Google Gemini TTS API.
    private func speakGemini(_ text: String, apiKey: String, model: String, voiceName: String) async {
        if apiKey.isEmpty {
            AppLogger.shared.warning("SpeechService", "API key required for Gemini TTS, falling back to local TTS")
            speakLocal(text)
            return
        }
        
        do {
            let audioData = try await aiService.getGeminiTTSAudio(text: text, apiKey: apiKey, model: model, voiceName: voiceName)
            // Gemini returns PCM, need to convert to WAV or use AVAudioPlayer
            await playAudioData(audioData, isMP3: false, isPCM: true)
        } catch {
            AppLogger.shared.error("SpeechService", "Failed to generate Gemini TTS, falling back to local TTS", error: error)
            speakLocal(text)
        }
    }
    
    /// Speak using Anthropic TTS API.
    private func speakAnthropic(_ text: String, apiKey: String) async {
        if apiKey.isEmpty {
            AppLogger.shared.warning("SpeechService", "API key required for Anthropic TTS, falling back to local TTS")
            speakLocal(text)
            return
        }
        
        do {
            let audioData = try await aiService.getAnthropicTTSAudio(text: text, apiKey: apiKey)
            await playAudioData(audioData, isMP3: true)
        } catch {
            AppLogger.shared.error("SpeechService", "Failed to generate Anthropic TTS, falling back to local TTS", error: error)
            speakLocal(text)
        }
    }
    
    /// Speak using AWS Polly TTS API.
    private func speakAWSPolly(_ text: String, accessKey: String, secretKey: String, region: String) async {
        if accessKey.isEmpty || secretKey.isEmpty {
            AppLogger.shared.warning("SpeechService", "AWS credentials required for AWS Polly TTS, falling back to local TTS")
            speakLocal(text)
            return
        }
        
        do {
            let audioData = try await aiService.getAWSPollyTTSAudio(text: text, accessKey: accessKey, secretKey: secretKey, region: region)
            await playAudioData(audioData, isMP3: true)
        } catch {
            AppLogger.shared.error("SpeechService", "Failed to generate AWS Polly TTS, falling back to local TTS", error: error)
            speakLocal(text)
        }
    }
    
    /// Play audio data using AVAudioPlayer.
    private func playAudioData(_ audioData: Data, isMP3: Bool, isPCM: Bool = false) async {
        // For PCM data from Gemini, we'd need to convert to WAV format
        // For simplicity, we'll use AVAudioPlayer which supports MP3
        // PCM conversion would require adding WAV headers
        
        do {
            let player = try AVAudioPlayer(data: audioData)
            player.delegate = self
            player.play()
        } catch {
            AppLogger.shared.error("SpeechService", "Failed to create audio player", error: error)
            isSpeaking = false
        }
    }
    
    /// Stop speaking if currently speaking.
    func stopSpeaking() {
        speechSynthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
    }
    
    /// Check if TTS is currently speaking.
    func isCurrentlySpeaking() -> Bool {
        return speechSynthesizer.isSpeaking || isSpeaking
    }
    
    /// Clean up resources when the service is no longer needed.
    func cleanup() {
        stopListening()
        stopSpeaking()
        AppLogger.shared.info("SpeechService", "SpeechService cleaned up")
    }
}

// MARK: - AVSpeechSynthesizerDelegate
extension SpeechService: AVSpeechSynthesizerDelegate {
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
    
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
}

// MARK: - AVAudioPlayerDelegate
extension SpeechService: AVAudioPlayerDelegate {
    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
}

enum SpeechServiceError: LocalizedError {
    case notAvailable
    case authorizationDenied
    case alreadyListening
    case setupFailed
    
    var errorDescription: String? {
        switch self {
        case .notAvailable:
            return "Speech recognition is not available on this device"
        case .authorizationDenied:
            return "Speech recognition authorization denied"
        case .alreadyListening:
            return "Already listening"
        case .setupFailed:
            return "Failed to setup speech recognition"
        }
    }
}

