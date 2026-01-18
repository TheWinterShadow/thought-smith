//
//  ChatViewModel.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import SwiftUI
import Combine

/// ViewModel for the Chat screen - manages chat state and AI interactions.
@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [Message] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var isSaving = false
    @Published var saveSuccess: String?
    @Published var formattedSummary: String?
    @Published var isGeneratingSummary = false
    @Published var inputMode = false // false = text, true = speech
    @Published var outputMode = false // false = text, true = speech
    @Published var isListening = false
    @Published var isSpeaking = false
    @Published var isSavingTranscript = false
    @Published var chatTranscript: String?
    
    private let aiService = AIService.shared
    private let settingsRepository = SettingsRepository.shared
    private let speechService = SpeechService.shared
    
    init() {
        AppLogger.shared.info("ChatViewModel", "ChatViewModel initialized")
        
        // Start with a friendly welcome message
        let welcomeMessage = Message(
            content: "Hi! I'm here to help you with your journaling today. What's on your mind?",
            isUser: false
        )
        messages = [welcomeMessage]
        
        // Observe settings changes to update TTS provider
        settingsRepository.$settings
            .sink { [weak self] settings in
                self?.speechService.setTTSProvider(settings.ttsProvider)
                AppLogger.shared.info("ChatViewModel", "TTS provider updated to: \(settings.ttsProvider.displayName)")
            }
            .store(in: &cancellables)
    }
    
    private var cancellables = Set<AnyCancellable>()
    
    /// Send a user message to the AI and handle the response.
    func sendMessage(_ userMessage: String) {
        guard !userMessage.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              !isLoading else { return }
        
        AppLogger.shared.info("ChatViewModel", "User sending message: \(String(userMessage.prefix(50)))...")
        
        let userMsg = Message(content: userMessage.trimmingCharacters(in: .whitespacesAndNewlines), isUser: true)
        let updatedMessages = messages + [userMsg]
        messages = updatedMessages
        isLoading = true
        error = nil
        
        Task {
            do {
                let settings = settingsRepository.settings
                
                guard !settings.apiKey.isEmpty else {
                    error = "Please configure your API key in Settings"
                    isLoading = false
                    return
                }
                
                let aiResponse = try await aiService.getAIResponse(
                    messages: updatedMessages,
                    provider: settings.aiProvider,
                    model: settings.aiModel,
                    apiKey: settings.apiKey,
                    systemContext: settings.aiContext
                )
                
                let aiMsg = Message(content: aiResponse, isUser: false)
                messages.append(aiMsg)
                isLoading = false
                error = nil
                
                AppLogger.shared.info("ChatViewModel", "AI response received successfully")
                
                // Speak the response if speech output is enabled
                if outputMode {
                    await speechService.speak(aiResponse, settings: settings)
                }
                
            } catch {
                AppLogger.shared.error("ChatViewModel", "Failed to get AI response", error: error)
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }
    
    /// Generate a formatted journal entry from the current conversation.
    func saveJournalEntry() {
        guard !messages.isEmpty, !isSaving, !isGeneratingSummary else { return }
        
        AppLogger.shared.info("ChatViewModel", "Generating formatted summary")
        isGeneratingSummary = true
        error = nil
        
        Task {
            do {
                let settings = settingsRepository.settings
                
                guard !settings.apiKey.isEmpty else {
                    error = "Please configure your API key in Settings"
                    isGeneratingSummary = false
                    return
                }
                
                // Combine all messages into a readable conversation format
                let conversationText = messages.map { message in
                    "\(message.isUser ? "You" : "AI"): \(message.content)"
                }.joined(separator: "\n\n")
                
                // Create formatting request
                let formatMessage = Message(
                    content: "Please format the following conversation as a journal entry according to these instructions:\n\n\(settings.outputFormatInstructions)\n\n---\n\nConversation:\n\n\(conversationText)",
                    isUser: true
                )
                
                let messagesWithFormat = messages + [formatMessage]
                
                let formattedContent = try await aiService.getAIResponse(
                    messages: messagesWithFormat,
                    provider: settings.aiProvider,
                    model: settings.aiModel,
                    apiKey: settings.apiKey,
                    systemContext: settings.aiContext
                )
                
                AppLogger.shared.info("ChatViewModel", "Formatted summary generated successfully")
                formattedSummary = formattedContent
                isGeneratingSummary = false
                
            } catch {
                AppLogger.shared.error("ChatViewModel", "Failed to generate formatted summary", error: error)
                self.error = "Failed to generate summary: \(error.localizedDescription)"
                isGeneratingSummary = false
            }
        }
    }
    
    /// Accept the generated journal summary and prepare for file save.
    func acceptSummaryAndSave(_ formattedContent: String) {
        formattedSummary = formattedContent
        isSaving = true
    }
    
    /// Reject the generated journal summary and return to normal state.
    func rejectSummary() {
        formattedSummary = nil
    }
    
    /// Handle the result of a file save operation.
    func onFileSaved(success: Bool, filePath: String? = nil) {
        if success {
            AppLogger.shared.info("ChatViewModel", "Journal entry saved successfully")
            isSaving = false
            formattedSummary = nil
            saveSuccess = filePath != nil ? "Journal entry saved to: \(filePath!)" : "Journal entry saved successfully"
        } else {
            AppLogger.shared.error("ChatViewModel", "Failed to save journal entry or cancelled", error: nil)
            isSaving = false
            formattedSummary = nil
            error = nil
        }
    }
    
    /// Clear the entire chat conversation and start fresh.
    func clearChat() {
        AppLogger.shared.info("ChatViewModel", "Clearing chat")
        messages = [
            Message(
                content: "Hi! I'm here to help you with your journaling today. What's on your mind?",
                isUser: false
            )
        ]
        error = nil
        saveSuccess = nil
    }
    
    /// Clear any current error message.
    func clearError() {
        error = nil
    }
    
    /// Clear any current save success message.
    func clearSaveSuccess() {
        saveSuccess = nil
    }
    
    /// Save the current chat transcript to a file.
    func saveChatTranscript() {
        guard !messages.isEmpty, !isSavingTranscript else { return }
        
        AppLogger.shared.info("ChatViewModel", "Preparing chat transcript for saving")
        isSavingTranscript = true
        error = nil
        
        Task {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
            
            var transcript = "Chat Transcript\n"
            transcript += String(repeating: "=", count: 50) + "\n\n"
            
            for message in messages {
                let sender = message.isUser ? "You" : "AI"
                let timestamp = formatter.string(from: message.timestamp)
                transcript += "[\(timestamp)] \(sender):\n"
                transcript += message.content + "\n\n"
            }
            
            AppLogger.shared.info("ChatViewModel", "Chat transcript prepared successfully")
            chatTranscript = transcript
        }
    }
    
    /// Handle the result of a transcript save operation.
    func onTranscriptSaved(success: Bool, filePath: String? = nil) {
        if success {
            AppLogger.shared.info("ChatViewModel", "Chat transcript saved successfully")
            isSavingTranscript = false
            chatTranscript = nil
            saveSuccess = filePath != nil ? "Chat transcript saved to: \(filePath!)" : "Chat transcript saved successfully"
        } else {
            AppLogger.shared.error("ChatViewModel", "Failed to save chat transcript or cancelled", error: nil)
            isSavingTranscript = false
            chatTranscript = nil
            error = nil
        }
    }
    
    /// Toggle between text and speech input mode.
    func toggleInputMode() {
        inputMode.toggle()
        AppLogger.shared.info("ChatViewModel", "Input mode changed to: \(inputMode ? "speech" : "text")")
        
        if !inputMode {
            stopListening()
        }
    }
    
    /// Toggle between text and speech output mode.
    func toggleOutputMode() {
        outputMode.toggle()
        AppLogger.shared.info("ChatViewModel", "Output mode changed to: \(outputMode ? "speech" : "text")")
        
        if !outputMode {
            stopSpeaking()
        }
    }
    
    /// Start listening for speech input.
    func startListening() {
        guard !isListening, !isLoading else { return }
        
        guard speechService.isSpeechRecognitionAvailable() else {
            error = "Speech recognition is not available on this device"
            return
        }
        
        AppLogger.shared.info("ChatViewModel", "Starting speech recognition")
        isListening = true
        error = nil
        
        Task {
            do {
                let recognizedText = try await speechService.startListening()
                if !recognizedText.isEmpty {
                    sendMessage(recognizedText)
                }
                isListening = false
            } catch {
                AppLogger.shared.error("ChatViewModel", "Speech recognition error", error: error)
                self.error = error.localizedDescription
                isListening = false
            }
        }
    }
    
    /// Stop listening for speech input.
    func stopListening() {
        if isListening {
            speechService.stopListening()
            isListening = false
            AppLogger.shared.info("ChatViewModel", "Stopped listening")
        }
    }
    
    /// Stop speaking if currently speaking.
    func stopSpeaking() {
        speechService.stopSpeaking()
        isSpeaking = false
    }
}

