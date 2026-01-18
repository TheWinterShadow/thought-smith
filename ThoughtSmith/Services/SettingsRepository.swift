//
//  SettingsRepository.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import Combine

/// Repository for managing user settings and preferences using UserDefaults.
class SettingsRepository: ObservableObject {
    static let shared = SettingsRepository()
    
    @Published var settings: AppSettings {
        didSet {
            saveSettings()
        }
    }
    
    private let userDefaults = UserDefaults.standard
    private let settingsKey = "app_settings"
    
    private init() {
        // Load settings from UserDefaults
        if let data = userDefaults.data(forKey: settingsKey),
           let decoded = try? JSONDecoder().decode(AppSettings.self, from: data) {
            self.settings = decoded
        } else {
            self.settings = AppSettings()
        }
    }
    
    private func saveSettings() {
        if let encoded = try? JSONEncoder().encode(settings) {
            userDefaults.set(encoded, forKey: settingsKey)
        }
    }
    
    func updateSettings(_ newSettings: AppSettings) {
        settings = newSettings
    }
}

