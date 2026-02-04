//
//  AIProvider.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation

/// Enumeration of supported AI service providers.
/// Enumeration of supported AI providers.
enum AIProvider: String, Codable, CaseIterable, Hashable {
    case openAI
    case gemini
    case anthropic
    
    var displayName: String {
        switch self {
        case .openAI:
            return "OpenAI"
        case .gemini:
            return "Google Gemini"
        case .anthropic:
            return "Anthropic"
        }
    }
}

/// Represents a specific AI model from a provider.
struct AIModel: Identifiable, Hashable {
    var id: String { modelName }
    let provider: AIProvider
    let modelName: String
    let displayName: String
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(provider)
        hasher.combine(modelName)
    }
    
    static func == (lhs: AIModel, rhs: AIModel) -> Bool {
        lhs.provider == rhs.provider && lhs.modelName == rhs.modelName
    }
}

/// Container for all supported AI models organized by provider.
struct AIModels {
    static let openAIModels = [
        AIModel(provider: .openAI, modelName: "gpt-4o", displayName: "GPT-4o"),
        AIModel(provider: .openAI, modelName: "gpt-4o-mini", displayName: "GPT-4o Mini"),
        AIModel(provider: .openAI, modelName: "gpt-4-turbo", displayName: "GPT-4 Turbo"),
        AIModel(provider: .openAI, modelName: "gpt-3.5-turbo", displayName: "GPT-3.5 Turbo")
    ]
    
    static let geminiModels = [
        AIModel(provider: .gemini, modelName: "gemini-3-flash-preview", displayName: "Gemini 3 Flash Preview"),
    ]
    
    static let anthropicModels = [
        AIModel(provider: .anthropic, modelName: "claude-opus-4-5-20251101", displayName: "Claude Opus 4.5"),
        AIModel(provider: .anthropic, modelName: "claude-haiku-4-5-20251001", displayName: "Claude Haiku 4.5"),
        AIModel(provider: .anthropic, modelName: "claude-sonnet-4-5-20250929", displayName: "Claude Sonnet 4.5"),
        AIModel(provider: .anthropic, modelName: "claude-opus-4-1-20250805", displayName: "Claude Opus 4.1"),
        AIModel(provider: .anthropic, modelName: "claude-opus-4-20250514", displayName: "Claude Opus 4"),
        AIModel(provider: .anthropic, modelName: "claude-sonnet-4-20250514", displayName: "Claude Sonnet 4"),
        AIModel(provider: .anthropic, modelName: "claude-3-5-haiku-20241022", displayName: "Claude Haiku 3.5"),
        AIModel(provider: .anthropic, modelName: "claude-3-haiku-20240307", displayName: "Claude Haiku 3")
    ]
    
    static func getModels(for provider: AIProvider) -> [AIModel] {
        switch provider {
        case .openAI: return openAIModels
        case .gemini: return geminiModels
        case .anthropic: return anthropicModels
        }
    }
}

