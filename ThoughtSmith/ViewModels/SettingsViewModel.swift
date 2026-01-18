//
//  SettingsViewModel.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import SwiftUI
import Combine

/// ViewModel for the Settings screen - manages user preferences and AI configuration.
@MainActor
class SettingsViewModel: ObservableObject {
    @Published var settings: AppSettings
    @Published var availableModels: [AIModel] = []
    @Published var isLoading = false
    @Published var saveSuccess = false
    
    private let settingsRepository = SettingsRepository.shared
    
    init() {
        AppLogger.shared.info("SettingsViewModel", "SettingsViewModel initialized")
        self.settings = settingsRepository.settings
        updateAvailableModels()
        
        // Observe settings changes
        settingsRepository.$settings
            .sink { [weak self] newSettings in
                self?.settings = newSettings
                self?.updateAvailableModels()
            }
            .store(in: &cancellables)
    }
    
    private var cancellables = Set<AnyCancellable>()
    
    private func updateAvailableModels() {
        availableModels = AIModels.getModels(for: settings.aiProvider)
    }
    
    /// Update the selected AI provider.
    func updateProvider(_ provider: AIProvider) {
        AppLogger.shared.info("SettingsViewModel", "Updating AI provider to \(provider.displayName)")
        
        let models = AIModels.getModels(for: provider)
        let defaultModel = models.first?.modelName ?? ""
        
        settings.aiProvider = provider
        settings.aiModel = defaultModel
        settingsRepository.updateSettings(settings)
    }
    
    /// Update the selected AI model for the current provider.
    func updateModel(_ modelName: String) {
        AppLogger.shared.info("SettingsViewModel", "Updating AI model to \(modelName)")
        settings.aiModel = modelName
        settingsRepository.updateSettings(settings)
    }
    
    /// Update the API key for the selected AI provider.
    func updateApiKey(_ apiKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "API key updated")
        settings.apiKey = apiKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update the AI context/system prompt.
    func updateAiContext(_ context: String) {
        AppLogger.shared.info("SettingsViewModel", "AI context updated")
        settings.aiContext = context
        settingsRepository.updateSettings(settings)
    }
    
    /// Update the output formatting instructions.
    func updateOutputFormat(_ format: String) {
        AppLogger.shared.info("SettingsViewModel", "Output format instructions updated")
        settings.outputFormatInstructions = format
        settingsRepository.updateSettings(settings)
    }
    
    /// Explicitly save current settings.
    func saveSettings() {
        AppLogger.shared.info("SettingsViewModel", "Saving settings")
        isLoading = true
        saveSuccess = false
        
        settingsRepository.updateSettings(settings)
        
        AppLogger.shared.info("SettingsViewModel", "Settings saved successfully")
        isLoading = false
        saveSuccess = true
    }
    
    /// Update the TTS provider.
    func updateTTSProvider(_ provider: TTSProvider) {
        AppLogger.shared.info("SettingsViewModel", "Updating TTS provider to \(provider.displayName)")
        settings.ttsProvider = provider
        settingsRepository.updateSettings(settings)
    }
    
    /// Update OpenAI TTS API key.
    func updateTTSOpenAIApiKey(_ apiKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "OpenAI TTS API key updated")
        settings.ttsOpenAIApiKey = apiKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update OpenAI TTS model/voice.
    func updateTTSOpenAIModel(_ model: String) {
        AppLogger.shared.info("SettingsViewModel", "Updating OpenAI TTS model to \(model)")
        settings.ttsOpenAIModel = model
        settingsRepository.updateSettings(settings)
    }
    
    /// Update Gemini TTS API key.
    func updateTTSGeminiApiKey(_ apiKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "Gemini TTS API key updated")
        settings.ttsGeminiApiKey = apiKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update Gemini TTS model.
    func updateTTSGeminiModel(_ model: String) {
        AppLogger.shared.info("SettingsViewModel", "Updating Gemini TTS model to \(model)")
        settings.ttsGeminiModel = model
        settingsRepository.updateSettings(settings)
    }
    
    /// Update Gemini TTS voice name.
    func updateTTSGeminiVoiceName(_ voiceName: String) {
        AppLogger.shared.info("SettingsViewModel", "Updating Gemini TTS voice name to \(voiceName)")
        settings.ttsGeminiVoiceName = voiceName
        settingsRepository.updateSettings(settings)
    }
    
    /// Update Anthropic TTS API key.
    func updateTTSAnthropicApiKey(_ apiKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "Anthropic TTS API key updated")
        settings.ttsAnthropicApiKey = apiKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update Anthropic TTS model/voice.
    func updateTTSAnthropicModel(_ model: String) {
        AppLogger.shared.info("SettingsViewModel", "Updating Anthropic TTS model to \(model)")
        settings.ttsAnthropicModel = model
        settingsRepository.updateSettings(settings)
    }
    
    /// Update AWS access key for AWS Polly TTS.
    func updateAWSAccessKey(_ accessKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "AWS access key updated")
        settings.awsAccessKey = accessKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update AWS secret key for AWS Polly TTS.
    func updateAWSSecretKey(_ secretKey: String) {
        AppLogger.shared.debug("SettingsViewModel", "AWS secret key updated")
        settings.awsSecretKey = secretKey
        settingsRepository.updateSettings(settings)
    }
    
    /// Update AWS region for AWS Polly TTS.
    func updateAWSRegion(_ region: String) {
        AppLogger.shared.debug("SettingsViewModel", "AWS region updated to \(region)")
        settings.awsRegion = region
        settingsRepository.updateSettings(settings)
    }
    
    /// Clear the save success state.
    func clearSaveSuccess() {
        saveSuccess = false
    }
}

