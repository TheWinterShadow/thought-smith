//
//  thoughtsmithTests.swift
//  thoughtsmithTests
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import XCTest
@testable import thoughtsmith

/// Main test suite for Thought Smith app
final class thoughtsmithTests: XCTestCase {
    
    override func setUpWithError() throws {
        // Put setup code here. This method is called before each test method.
    }
    
    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after each test method.
    }
    
    // MARK: - Message Model Tests
    
    func testMessageCreation() {
        let content = "Hello, world!"
        let message = Message(content: content, isUser: true)
        
        XCTAssertEqual(message.content, content)
        XCTAssertTrue(message.isUser)
        XCTAssertNotNil(message.id)
    }
    
    func testMessageCreationWithTimestamp() {
        let timestamp = Date(timeIntervalSince1970: 1000000)
        let message = Message(content: "Test", isUser: false, timestamp: timestamp)
        
        XCTAssertEqual(message.timestamp, timestamp)
        XCTAssertFalse(message.isUser)
    }
    
    func testMessageCodable() throws {
        let message = Message(content: "Encode me", isUser: true)
        
        let encoder = JSONEncoder()
        let data = try encoder.encode(message)
        
        let decoder = JSONDecoder()
        let decodedMessage = try decoder.decode(Message.self, from: data)
        
        XCTAssertEqual(decodedMessage.content, message.content)
        XCTAssertEqual(decodedMessage.isUser, message.isUser)
        XCTAssertEqual(decodedMessage.id, message.id)
    }
    
    // MARK: - AppSettings Tests
    
    func testAppSettingsDefaults() {
        let settings = AppSettings()
        
        XCTAssertEqual(settings.aiProvider, .openAI)
        XCTAssertEqual(settings.aiModel, "gpt-4o-mini")
        XCTAssertEqual(settings.apiKey, "")
        XCTAssertEqual(settings.ttsProvider, .local)
        XCTAssertFalse(settings.aiContext.isEmpty)
        XCTAssertFalse(settings.outputFormatInstructions.isEmpty)
    }
    
    func testAppSettingsCodable() throws {
        var settings = AppSettings()
        settings.apiKey = "test-key-123"
        settings.aiModel = "gpt-4"
        settings.ttsProvider = .openAI
        
        let encoder = JSONEncoder()
        let data = try encoder.encode(settings)
        
        let decoder = JSONDecoder()
        let decodedSettings = try decoder.decode(AppSettings.self, from: data)
        
        XCTAssertEqual(decodedSettings.apiKey, "test-key-123")
        XCTAssertEqual(decodedSettings.aiModel, "gpt-4")
        XCTAssertEqual(decodedSettings.ttsProvider, .openAI)
    }
    
    func testAppSettingsTTSConfiguration() {
        var settings = AppSettings()
        
        settings.ttsOpenAIApiKey = "openai-key"
        settings.ttsGeminiApiKey = "gemini-key"
        settings.awsAccessKey = "aws-access"
        settings.awsSecretKey = "aws-secret"
        settings.awsRegion = "us-west-2"
        
        XCTAssertEqual(settings.ttsOpenAIApiKey, "openai-key")
        XCTAssertEqual(settings.ttsGeminiApiKey, "gemini-key")
        XCTAssertEqual(settings.awsAccessKey, "aws-access")
        XCTAssertEqual(settings.awsSecretKey, "aws-secret")
        XCTAssertEqual(settings.awsRegion, "us-west-2")
    }
    
    // MARK: - Navigation Tests
    
    @MainActor
    func testNavigationStateInitial() {
        let navState = NavigationState()
        XCTAssertTrue(navState.path.isEmpty)
    }
    
    @MainActor
    func testNavigationStateNavigate() {
        let navState = NavigationState()
        
        navState.navigate(to: .settings)
        XCTAssertEqual(navState.path.count, 1)
        
        navState.navigate(to: .logs)
        XCTAssertEqual(navState.path.count, 2)
    }
    
    @MainActor
    func testNavigationStateBack() {
        let navState = NavigationState()
        
        navState.navigate(to: .settings)
        navState.navigate(to: .logs)
        XCTAssertEqual(navState.path.count, 2)
        
        navState.navigateBack()
        XCTAssertEqual(navState.path.count, 1)
        
        navState.navigateBack()
        XCTAssertTrue(navState.path.isEmpty)
    }
    
    @MainActor
    func testNavigationStateBackWhenEmpty() {
        let navState = NavigationState()
        
        navState.navigateBack()
        XCTAssertTrue(navState.path.isEmpty)
    }
    
    // MARK: - Enum Tests
    
    func testScreenEnum() {
        let settings = Screen.settings
        let logs = Screen.logs
        
        XCTAssertEqual(settings, .settings)
        XCTAssertEqual(logs, .logs)
        XCTAssertNotEqual(settings, logs)
    }
    
    func testTTSProviderAllCases() {
        let allProviders = TTSProvider.allCases
        
        XCTAssertTrue(allProviders.contains(.local))
        XCTAssertGreaterThan(allProviders.count, 0)
    }
    
    func testTTSProviderDisplayNames() {
        for provider in TTSProvider.allCases {
            XCTAssertFalse(provider.displayName.isEmpty,
                          "Provider \(provider) should have a display name")
        }
    }
    
    func testAIProviderAllCases() {
        let allProviders = AIProvider.allCases
        
        XCTAssertTrue(allProviders.contains(.openAI))
        XCTAssertGreaterThan(allProviders.count, 0)
    }
    
    func testAIProviderDisplayNames() {
        for provider in AIProvider.allCases {
            XCTAssertFalse(provider.displayName.isEmpty,
                          "Provider \(provider) should have a display name")
        }
    }
    
    // MARK: - SpeechServiceError Tests
    
    func testSpeechServiceErrorDescriptions() {
        let notAvailable = SpeechServiceError.notAvailable
        XCTAssertNotNil(notAvailable.errorDescription)
        XCTAssertTrue(notAvailable.errorDescription?.contains("not available") == true)
        
        let authDenied = SpeechServiceError.authorizationDenied
        XCTAssertNotNil(authDenied.errorDescription)
        XCTAssertTrue(authDenied.errorDescription?.contains("denied") == true)
        
        let alreadyListening = SpeechServiceError.alreadyListening
        XCTAssertNotNil(alreadyListening.errorDescription)
        
        let setupFailed = SpeechServiceError.setupFailed
        XCTAssertNotNil(setupFailed.errorDescription)
    }
    
    // MARK: - Performance Tests
    
    func testPerformanceMessageCreation() throws {
        measure {
            for _ in 0..<1000 {
                _ = Message(content: "Test", isUser: true)
            }
        }
    }
    
    func testPerformanceSettingsEncoding() throws {
        let settings = AppSettings()
        let encoder = JSONEncoder()
        
        measure {
            _ = try? encoder.encode(settings)
        }
    }
}
