//
//  ServiceTests.swift
//  thoughtsmithTests
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import XCTest
@testable import thoughtsmith

/// Test suite for Services
final class ServiceTests: XCTestCase {
    
    // MARK: - FileStorageService Tests
    
    func testFileStorageServiceSingleton() {
        let service1 = FileStorageService.shared
        let service2 = FileStorageService.shared
        
        XCTAssertTrue(service1 === service2)
    }
    
    func testDocumentsDirectoryExists() {
        let documentsDir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
        XCTAssertNotNil(documentsDir)
    }
    
    // MARK: - SettingsRepository Tests
    
    func testSettingsRepositorySingleton() {
        let repo1 = SettingsRepository.shared
        let repo2 = SettingsRepository.shared
        
        XCTAssertTrue(repo1 === repo2)
    }
    
    func testSettingsRepositoryHasSettings() {
        let repo = SettingsRepository.shared
        XCTAssertNotNil(repo.settings)
    }
    
    func testSettingsRepositoryUpdate() {
        let repo = SettingsRepository.shared
        
        var newSettings = AppSettings()
        newSettings.apiKey = "test-update-key"
        
        repo.updateSettings(newSettings)
        
        XCTAssertEqual(repo.settings.apiKey, "test-update-key")
    }
    
    // MARK: - SpeechService Tests
    
    @MainActor
    func testSpeechServiceSingleton() {
        let service1 = SpeechService.shared
        let service2 = SpeechService.shared
        
        XCTAssertTrue(service1 === service2)
    }
    
    @MainActor
    func testSpeechServiceInitialState() {
        let service = SpeechService.shared
        
        XCTAssertFalse(service.isListening)
        XCTAssertFalse(service.isSpeaking)
    }
    
    @MainActor
    func testSpeechServiceSetTTSProvider() {
        let service = SpeechService.shared
        
        // Should not crash
        service.setTTSProvider(.local)
        service.setTTSProvider(.openAI)
        service.setTTSProvider(.gemini)
    }
    
    @MainActor
    func testSpeechServiceStopSpeaking() {
        let service = SpeechService.shared
        
        // Should not crash when not speaking
        service.stopSpeaking()
        XCTAssertFalse(service.isSpeaking)
    }
    
    @MainActor
    func testSpeechServiceStopListening() {
        let service = SpeechService.shared
        
        // Should not crash when not listening
        service.stopListening()
        XCTAssertFalse(service.isListening)
    }
    
    @MainActor
    func testSpeechServiceCleanup() {
        let service = SpeechService.shared
        
        // Should not crash
        service.cleanup()
        XCTAssertFalse(service.isListening)
        XCTAssertFalse(service.isSpeaking)
    }
    
    // MARK: - AIService Tests
    
    @MainActor
    func testAIServiceSingleton() {
        let service1 = AIService.shared
        let service2 = AIService.shared
        
        XCTAssertTrue(service1 === service2)
    }
    
    // MARK: - AppLogger Tests
    
    @MainActor
    func testAppLoggerSingleton() {
        let logger1 = AppLogger.shared
        let logger2 = AppLogger.shared
        
        XCTAssertTrue(logger1 === logger2)
    }
    
    @MainActor
    func testAppLoggerClearLogs() {
        let logger = AppLogger.shared
        
        logger.info("Test", "Message 1")
        logger.info("Test", "Message 2")
        
        // Clear logs
        logger.clearLogs()
        XCTAssertTrue(logger.logs.isEmpty)
    }
    
    @MainActor
    func testAppLoggerLogLevels() {
        let logger = AppLogger.shared
        logger.clearLogs()
        
        logger.debug("Test", "Debug message")
        logger.info("Test", "Info message")
        logger.warning("Test", "Warning message")
        logger.error("Test", "Error message")
        
        // Should have logged messages
        XCTAssertGreaterThan(logger.logs.count, 0)
    }
}
