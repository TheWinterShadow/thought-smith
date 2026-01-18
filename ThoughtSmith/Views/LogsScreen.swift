//
//  LogsScreen.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI

struct LogsScreen: View {
    @EnvironmentObject private var navigationState: NavigationState
    @State private var logs: [AppLogger.LogEntry] = []
    @State private var timer: Timer?
    
    var body: some View {
        List {
            if logs.isEmpty {
                Text("No logs available")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .listRowSeparator(.hidden)
            } else {
                ForEach(logs.reversed()) { logEntry in
                    LogEntryItem(logEntry: logEntry)
                }
            }
        }
        .navigationTitle("App Logs")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Back") {
                    navigationState.navigateBack()
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    AppLogger.shared.clearLogs()
                    logs = AppLogger.shared.getLogs()
                }) {
                    Image(systemName: "trash")
                }
            }
        }
        .onAppear {
            logs = AppLogger.shared.getLogs()
            // Refresh logs periodically
            timer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { _ in
                logs = AppLogger.shared.getLogs()
            }
        }
        .onDisappear {
            timer?.invalidate()
        }
    }
}

struct LogEntryItem: View {
    let logEntry: AppLogger.LogEntry
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(logEntry.level.rawValue)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(colorForLevel(logEntry.level))
                Spacer()
                Text(logEntry.tag)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
            
            Text(logEntry.message)
                .font(.system(.caption, design: .monospaced))
                .foregroundColor(.primary)
        }
        .padding(.vertical, 4)
        .listRowBackground(backgroundColorForLevel(logEntry.level))
    }
    
    private func colorForLevel(_ level: AppLogger.LogLevel) -> Color {
        switch level {
        case .debug: return .gray
        case .info: return .blue
        case .warning: return .orange
        case .error: return .red
        }
    }
    
    private func backgroundColorForLevel(_ level: AppLogger.LogLevel) -> Color {
        switch level {
        case .debug: return Color.gray.opacity(0.1)
        case .info: return Color.blue.opacity(0.1)
        case .warning: return Color.orange.opacity(0.1)
        case .error: return Color.red.opacity(0.1)
        }
    }
}

