//
//  ContentView.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI
import Combine

struct ContentView: View {
    @StateObject private var navigationState = NavigationState()
    
    var body: some View {
        NavigationStack(path: $navigationState.path) {
            ChatScreen()
                .navigationDestination(for: Screen.self) { screen in
                    switch screen {
                    case .settings:
                        SettingsScreen()
                    case .logs:
                        LogsScreen()
                    }
                }
        }
        .environmentObject(navigationState)
    }
}

// MARK: - Navigation State
class NavigationState: ObservableObject {
    @Published var path = NavigationPath()
    
    func navigate(to screen: Screen) {
        path.append(screen)
    }
    
    func navigateBack() {
        if !path.isEmpty {
            path.removeLast()
        }
    }
}

enum Screen: Hashable {
    case settings
    case logs
}

