//
//  ViewModelTests.swift
//  thoughtsmithTests
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import XCTest
@testable import thoughtsmith

/// Test suite for ViewModels
final class ViewModelTests: XCTestCase {
    
    // MARK: - SettingsViewModel Tests
    
    @MainActor
    func testSettingsViewModelInitialState() {
        let viewModel = SettingsViewModel()
        
        XCTAssertFalse(viewModel.isLoading)
        XCTAssertFalse(viewModel.saveSuccess)
        XCTAssertNotNil(viewModel.settings)
    }
    
    @MainActor
    func testSettingsViewModelUpdateProvider() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateProvider(.anthropic)
        XCTAssertEqual(viewModel.settings.aiProvider, .anthropic)
        
        viewModel.updateProvider(.gemini)
        XCTAssertEqual(viewModel.settings.aiProvider, .gemini)
    }
    
    @MainActor
    func testSettingsViewModelUpdateModel() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateModel("gpt-4")
        XCTAssertEqual(viewModel.settings.aiModel, "gpt-4")
    }
    
    @MainActor
    func testSettingsViewModelUpdateApiKey() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateApiKey("test-key-123")
        XCTAssertEqual(viewModel.settings.apiKey, "test-key-123")
    }
    
    @MainActor
    func testSettingsViewModelUpdateAiContext() {
        let viewModel = SettingsViewModel()
        
        let newContext = "You are a helpful assistant."
        viewModel.updateAiContext(newContext)
        XCTAssertEqual(viewModel.settings.aiContext, newContext)
    }
    
    @MainActor
    func testSettingsViewModelUpdateOutputFormat() {
        let viewModel = SettingsViewModel()
        
        let newFormat = "Format as JSON"
        viewModel.updateOutputFormat(newFormat)
        XCTAssertEqual(viewModel.settings.outputFormatInstructions, newFormat)
    }
    
    @MainActor
    func testSettingsViewModelUpdateTTSProvider() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateTTSProvider(.openAI)
        XCTAssertEqual(viewModel.settings.ttsProvider, .openAI)
        
        viewModel.updateTTSProvider(.gemini)
        XCTAssertEqual(viewModel.settings.ttsProvider, .gemini)
    }
    
    @MainActor
    func testSettingsViewModelUpdateTTSOpenAISettings() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateTTSOpenAIApiKey("openai-key")
        viewModel.updateTTSOpenAIModel("tts-1-hd")
        
        XCTAssertEqual(viewModel.settings.ttsOpenAIApiKey, "openai-key")
        XCTAssertEqual(viewModel.settings.ttsOpenAIModel, "tts-1-hd")
    }
    
    @MainActor
    func testSettingsViewModelUpdateTTSGeminiSettings() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateTTSGeminiApiKey("gemini-key")
        viewModel.updateTTSGeminiModel("gemini-model")
        viewModel.updateTTSGeminiVoiceName("Aoede")
        
        XCTAssertEqual(viewModel.settings.ttsGeminiApiKey, "gemini-key")
        XCTAssertEqual(viewModel.settings.ttsGeminiModel, "gemini-model")
        XCTAssertEqual(viewModel.settings.ttsGeminiVoiceName, "Aoede")
    }
    
    @MainActor
    func testSettingsViewModelUpdateAWSSettings() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateAWSAccessKey("aws-access")
        viewModel.updateAWSSecretKey("aws-secret")
        viewModel.updateAWSRegion("us-west-2")
        
        XCTAssertEqual(viewModel.settings.awsAccessKey, "aws-access")
        XCTAssertEqual(viewModel.settings.awsSecretKey, "aws-secret")
        XCTAssertEqual(viewModel.settings.awsRegion, "us-west-2")
    }
    
    @MainActor
    func testSettingsViewModelAvailableModels() {
        let viewModel = SettingsViewModel()
        
        viewModel.updateProvider(.openAI)
        XCTAssertFalse(viewModel.availableModels.isEmpty)
        
        viewModel.updateProvider(.anthropic)
        XCTAssertFalse(viewModel.availableModels.isEmpty)
    }
    
    // MARK: - ChatViewModel Tests
    
    @MainActor
    func testChatViewModelInitialState() {
        let viewModel = ChatViewModel()
        
        // Should start with a welcome message
        XCTAssertFalse(viewModel.messages.isEmpty)
        XCTAssertFalse(viewModel.isProcessing)
        XCTAssertFalse(viewModel.isRecording)
        XCTAssertFalse(viewModel.isSpeaking)
    }
    
    @MainActor
    func testChatViewModelAddMessage() {
        let viewModel = ChatViewModel()
        
        let initialCount = viewModel.messages.count
        
        let message = Message(content: "Test message", isUser: true)
        viewModel.messages.append(message)
        
        XCTAssertEqual(viewModel.messages.count, initialCount + 1)
        XCTAssertEqual(viewModel.messages.last?.content, "Test message")
        XCTAssertTrue(viewModel.messages.last?.isUser == true)
    }
    
    @MainActor
    func testChatViewModelMultipleMessages() {
        let viewModel = ChatViewModel()
        
        viewModel.messages.append(Message(content: "User 1", isUser: true))
        viewModel.messages.append(Message(content: "AI 1", isUser: false))
        viewModel.messages.append(Message(content: "User 2", isUser: true))
        
        XCTAssertGreaterThanOrEqual(viewModel.messages.count, 3)
    }
}
