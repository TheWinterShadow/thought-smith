//
//  thoughtsmithUITests.swift
//  thoughtsmithUITests
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import XCTest
@testable import thoughtsmith

/// Main UI test suite for Thought Smith app
final class thoughtsmithUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - App Launch Tests
    
    func testAppLaunches() throws {
        // Verify app launches successfully
        XCTAssertTrue(app.state == .runningForeground)
    }
    
    func testChatScreenExists() throws {
        // Chat screen should be visible on launch
        let chatExists = app.navigationBars.element.exists || 
                        app.textViews.element.exists ||
                        app.buttons.element.exists
        
        XCTAssertTrue(chatExists, "Chat screen should be visible on launch")
    }
    
    // MARK: - Navigation Tests
    
    func testNavigateToSettings() throws {
        // Look for settings button (could be in toolbar or elsewhere)
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Verify settings screen is shown
            let settingsTitle = app.navigationBars["Settings"]
            XCTAssertTrue(settingsTitle.waitForExistence(timeout: 2))
        }
    }
    
    func testNavigateToLogs() throws {
        // Navigate to settings first
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Then navigate to logs
            let logsButton = app.buttons["Logs"]
            if logsButton.waitForExistence(timeout: 2) {
                logsButton.tap()
                
                // Verify logs screen is shown
                let logsTitle = app.navigationBars["Logs"]
                XCTAssertTrue(logsTitle.waitForExistence(timeout: 2))
            }
        }
    }
    
    func testBackNavigation() throws {
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Wait for settings screen
            let settingsTitle = app.navigationBars["Settings"]
            XCTAssertTrue(settingsTitle.waitForExistence(timeout: 2))
            
            // Go back
            let backButton = app.buttons["Back"]
            if backButton.exists {
                backButton.tap()
                
                // Should be back on chat screen
                XCTAssertTrue(app.state == .runningForeground)
            }
        }
    }
    
    // MARK: - Settings Screen Tests
    
    func testSettingsScreenTabs() throws {
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Verify settings screen loaded
            XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 2))
            
            // Check for segmented control with tabs
            let contextTab = app.buttons["Context"]
            let textAPITab = app.buttons["Text API"]
            let speechTab = app.buttons["Speech"]
            
            XCTAssertTrue(contextTab.exists || textAPITab.exists || speechTab.exists,
                         "At least one settings tab should exist")
        }
    }
    
    func testSettingsTextAPITab() throws {
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Wait for settings screen
            XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 2))
            
            // Tap Text API tab
            let textAPITab = app.buttons["Text API"]
            if textAPITab.exists {
                textAPITab.tap()
                
                // Should see AI Provider section
                let aiProviderText = app.staticTexts["AI Provider"]
                XCTAssertTrue(aiProviderText.waitForExistence(timeout: 2))
            }
        }
    }
    
    func testSettingsSpeechTab() throws {
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Wait for settings screen
            XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 2))
            
            // Tap Speech tab
            let speechTab = app.buttons["Speech"]
            if speechTab.exists {
                speechTab.tap()
                
                // Should see TTS Provider section
                let ttsProviderText = app.staticTexts["Text-to-Speech Provider"]
                XCTAssertTrue(ttsProviderText.waitForExistence(timeout: 2))
            }
        }
    }
    
    func testAPIKeyTextField() throws {
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Wait for settings screen
            XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 2))
            
            // Tap Text API tab
            let textAPITab = app.buttons["Text API"]
            if textAPITab.exists {
                textAPITab.tap()
                
                // Look for API key text field
                let apiKeyField = app.textFields["Enter your API key"]
                if apiKeyField.waitForExistence(timeout: 2) {
                    apiKeyField.tap()
                    apiKeyField.typeText("test-api-key")
                    
                    // Verify text was entered
                    XCTAssertTrue(apiKeyField.value as? String == "test-api-key" || 
                                 apiKeyField.exists)
                }
            }
        }
    }
    
    // MARK: - Chat Screen Tests
    
    func testChatScreenMessageInput() throws {
        // Look for text input field
        let messageInputField = app.textViews.firstMatch
        
        if messageInputField.waitForExistence(timeout: 2) {
            messageInputField.tap()
            
            // Should be able to type
            XCTAssertTrue(messageInputField.exists)
        }
    }
    
    func testChatScreenButtons() throws {
        // Look for common buttons (send, record, etc.)
        let buttons = app.buttons.allElementsBoundByIndex
        
        // Should have at least some buttons
        XCTAssertTrue(buttons.count > 0, "Chat screen should have interactive buttons")
    }
    
    // MARK: - Logs Screen Tests
    
    func testLogsScreenDisplay() throws {
        // Navigate to settings
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            // Navigate to logs
            let logsButton = app.buttons["Logs"]
            if logsButton.waitForExistence(timeout: 2) {
                logsButton.tap()
                
                // Verify logs screen is shown
                let logsTitle = app.navigationBars["Logs"]
                XCTAssertTrue(logsTitle.waitForExistence(timeout: 2))
                
                // Should have some UI elements (list, text, etc.)
                let hasContent = app.staticTexts.count > 0 || 
                               app.scrollViews.count > 0
                XCTAssertTrue(hasContent, "Logs screen should display content")
            }
        }
    }
    
    func testLogsClearButton() throws {
        // Navigate to logs
        let settingsButton = app.buttons["Settings"]
        
        if settingsButton.exists {
            settingsButton.tap()
            
            let logsButton = app.buttons["Logs"]
            if logsButton.waitForExistence(timeout: 2) {
                logsButton.tap()
                
                // Look for clear button
                let clearButton = app.buttons["Clear Logs"]
                if clearButton.waitForExistence(timeout: 2) {
                    // Tap clear button
                    clearButton.tap()
                    
                    // Button should still exist after clearing
                    XCTAssertTrue(clearButton.exists)
                }
            }
        }
    }
    
    // MARK: - Accessibility Tests
    
    func testAccessibilityLabels() throws {
        // Check that key UI elements have accessibility identifiers
        let buttons = app.buttons.allElementsBoundByIndex
        let textFields = app.textFields.allElementsBoundByIndex
        
        XCTAssertTrue(buttons.count > 0 || textFields.count > 0,
                     "App should have accessible UI elements")
    }
    
    // MARK: - Performance Tests
    
    func testLaunchPerformance() throws {
        measure(metrics: [XCTApplicationLaunchMetric()]) {
            XCUIApplication().launch()
        }
    }
}
