//
//  AppSettings.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation

/// Data class representing all user-configurable settings for the Thought Smith app.
struct AppSettings: Codable {
    var aiProvider: AIProvider = .openAI
    var aiModel: String = "gpt-4o-mini"
    var apiKey: String = ""
    var aiContext: String = "You are a supportive friend helping someone with their daily journaling. Ask thoughtful questions, show empathy, and help them explore their thoughts and feelings."
    var outputFormatInstructions: String = "Format the journal entry as a clean markdown document with a title, date, and well-organized sections based on our conversation."
    
    // TTS Configuration
    var ttsProvider: TTSProvider = .local
    var ttsOpenAIApiKey: String = ""
    var ttsOpenAIModel: String = "tts-1"
    var ttsGeminiApiKey: String = ""
    var ttsGeminiModel: String = "gemini-2.5-flash-preview-tts"
    var ttsGeminiVoiceName: String = "Kore"
    var ttsAnthropicApiKey: String = ""
    var ttsAnthropicModel: String = ""
    var awsAccessKey: String = ""
    var awsSecretKey: String = ""
    var awsRegion: String = "us-east-1"
}

