//
//  TTSProvider.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation

/// Enumeration of supported text-to-speech providers.
enum TTSProvider: String, Codable, CaseIterable, Hashable {
    case local
    case openAI
    case gemini
    case awsPolly
    
    var displayName: String {
        switch self {
        case .local:
            return "Local (iOS)"
        case .openAI:
            return "OpenAI TTS"
        case .gemini:
            return "Google Gemini TTS"
        case .awsPolly:
            return "AWS Polly"
        }
    }
}
