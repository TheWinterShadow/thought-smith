//
//  AIService.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation

/// Service class for communicating with various AI providers' APIs.
class AIService {
    static let shared = AIService()
    
    private let session: URLSession
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        self.session = URLSession(configuration: config)
    }
    
    /// Get an AI response from the specified provider and model.
    func getAIResponse(
        messages: [Message],
        provider: AIProvider,
        model: String,
        apiKey: String,
        systemContext: String
    ) async throws -> String {
        AppLogger.shared.info("AIService", "Requesting AI response from \(provider.displayName) using model \(model)")
        
        switch provider {
        case .openAI:
            return try await getOpenAIResponse(messages: messages, model: model, apiKey: apiKey, systemContext: systemContext)
        case .gemini:
            return try await getGeminiResponse(messages: messages, model: model, apiKey: apiKey, systemContext: systemContext)
        case .anthropic:
            return try await getAnthropicResponse(messages: messages, model: model, apiKey: apiKey, systemContext: systemContext)
        }
    }
    
    // MARK: - OpenAI
    
    private func getOpenAIResponse(messages: [Message], model: String, apiKey: String, systemContext: String) async throws -> String {
        let url = URL(string: "https://api.openai.com/v1/chat/completions")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let openAIMessages: [[String: Any]] = [
            ["role": "system", "content": systemContext]
        ] + messages.map { message in
            ["role": message.isUser ? "user" : "assistant", "content": message.content]
        }
        
        let body: [String: Any] = [
            "model": model,
            "messages": openAIMessages,
            "temperature": 0.7
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIServiceError.apiError("OpenAI API error: \(String(data: data, encoding: .utf8) ?? "Unknown")")
        }
        
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let choices = json["choices"] as? [[String: Any]],
           let firstChoice = choices.first,
           let message = firstChoice["message"] as? [String: Any],
           let content = message["content"] as? String {
            AppLogger.shared.info("AIService", "Successfully received AI response from OpenAI")
            return content
        }
        
        throw AIServiceError.invalidResponse
    }
    
    // MARK: - Gemini
    
    private func getGeminiResponse(messages: [Message], model: String, apiKey: String, systemContext: String) async throws -> String {
        let url = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/\(model):generateContent?key=\(apiKey)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let contents = messages.map { message in
            [
                "role": message.isUser ? "user" : "model",
                "parts": [["text": message.content]]
            ] as [String: Any]
        }
        
        let body: [String: Any] = [
            "contents": contents,
            "systemInstruction": ["parts": [["text": systemContext]]]
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIServiceError.apiError("Gemini API error: \(String(data: data, encoding: .utf8) ?? "Unknown")")
        }
        
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let candidates = json["candidates"] as? [[String: Any]],
           let firstCandidate = candidates.first,
           let content = firstCandidate["content"] as? [String: Any],
           let parts = content["parts"] as? [[String: Any]],
           let firstPart = parts.first,
           let text = firstPart["text"] as? String {
            AppLogger.shared.info("AIService", "Successfully received AI response from Gemini")
            return text
        }
        
        throw AIServiceError.invalidResponse
    }
    
    // MARK: - Anthropic
    
    private func getAnthropicResponse(messages: [Message], model: String, apiKey: String, systemContext: String) async throws -> String {
        let url = URL(string: "https://api.anthropic.com/v1/messages")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(apiKey, forHTTPHeaderField: "x-api-key")
        request.setValue("2023-06-01", forHTTPHeaderField: "anthropic-version")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let anthropicMessages = messages.map { message in
            [
                "role": message.isUser ? "user" : "assistant",
                "content": message.content
            ] as [String: Any]
        }
        
        let body: [String: Any] = [
            "model": model,
            "max_tokens": 4096,
            "system": systemContext,
            "messages": anthropicMessages
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIServiceError.apiError("Anthropic API error: \(String(data: data, encoding: .utf8) ?? "Unknown")")
        }
        
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let content = json["content"] as? [[String: Any]],
           let firstContent = content.first,
           let text = firstContent["text"] as? String {
            AppLogger.shared.info("AIService", "Successfully received AI response from Anthropic")
            return text
        }
        
        throw AIServiceError.invalidResponse
    }
    
    // MARK: - TTS Methods
    
    func getOpenAITTSAudio(text: String, apiKey: String, voice: String = "nova", model: String = "tts-1") async throws -> Data {
        AppLogger.shared.info("AIService", "Requesting TTS audio from OpenAI")
        
        let url = URL(string: "https://api.openai.com/v1/audio/speech")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "model": model,
            "input": text,
            "voice": voice
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIServiceError.apiError("OpenAI TTS API error")
        }
        
        AppLogger.shared.info("AIService", "Successfully received TTS audio (\(data.count) bytes)")
        return data
    }
    
    func getGeminiTTSAudio(text: String, apiKey: String, model: String = "gemini-2.5-flash-preview-tts", voiceName: String = "Kore") async throws -> Data {
        AppLogger.shared.info("AIService", "Requesting TTS audio from Gemini API (model: \(model), voice: \(voiceName))")
        
        let url = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/\(model):generateContent")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "x-goog-api-key")
        
        let body: [String: Any] = [
            "contents": [
                ["parts": [["text": text]]]
            ],
            "generationConfig": [
                "responseModalities": ["AUDIO"],
                "speechConfig": [
                    "voiceConfig": [
                        "prebuiltVoiceConfig": [
                            "voiceName": voiceName
                        ]
                    ]
                ]
            ],
            "model": model
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw AIServiceError.apiError("Gemini TTS API error: \(String(data: data, encoding: .utf8) ?? "Unknown")")
        }
        
        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let candidates = json["candidates"] as? [[String: Any]],
           let candidate = candidates.first,
           let content = candidate["content"] as? [String: Any],
           let parts = content["parts"] as? [[String: Any]],
           let part = parts.first,
           let inlineData = part["inlineData"] as? [String: Any],
           let audioDataBase64 = inlineData["data"] as? String,
           let audioData = Data(base64Encoded: audioDataBase64) {
            AppLogger.shared.info("AIService", "Successfully generated Gemini TTS audio (\(audioData.count) bytes, PCM format)")
            return audioData
        }
        
        throw AIServiceError.invalidResponse
    }
    
    func getAnthropicTTSAudio(text: String, apiKey: String) async throws -> Data {
        throw AIServiceError.apiError("Anthropic TTS API is not yet available. Please use OpenAI TTS or AWS Polly instead.")
    }
    
    func getAWSPollyTTSAudio(text: String, accessKey: String, secretKey: String, region: String, voiceId: String = "Joanna", engine: String = "neural") async throws -> Data {
        // Note: AWS Polly requires Signature Version 4 signing which is complex
        // For production, consider using AWS SDK for Swift
        throw AIServiceError.apiError("AWS Polly requires AWS SDK for proper authentication. Please use a different TTS provider.")
    }
}

enum AIServiceError: LocalizedError {
    case apiError(String)
    case invalidResponse
    
    var errorDescription: String? {
        switch self {
        case .apiError(let message):
            return message
        case .invalidResponse:
            return "Invalid response format from API"
        }
    }
}

