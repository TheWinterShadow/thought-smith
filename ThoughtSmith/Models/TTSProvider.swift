//
//  TTSProvider.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation

/// Enum representing available Text-to-Speech providers.
enum TTSProvider: String, Codable, CaseIterable {
    case local = "LOCAL"
    case openAI = "OPENAI"
    case gemini = "GEMINI"
    case anthropic = "ANTHROPIC"
    case awsPolly = "AWS_POLLY"
    
    var displayName: String {
        switch self {
        case .local: return "Local (Device)"
        case .openAI: return "OpenAI TTS"
        case .gemini: return "Gemini TTS"
        case .anthropic: return "Anthropic TTS"
        case .awsPolly: return "AWS Polly"
        }
    }
}

