//
//  Logger.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import os.log

/// Centralized logging utility for the Thought Smith application.
class AppLogger {
    static let shared = AppLogger()
    private let logger = Logger(subsystem: "com.thewintershadow.thoughtsmith", category: "App")
    
    private let maxLogEntries = 1000
    private var logEntries: [LogEntry] = []
    private let queue = DispatchQueue(label: "com.thoughtsmith.logger", attributes: .concurrent)
    
    private init() {}
    
    /// Represents a single log entry with metadata.
    struct LogEntry: Identifiable {
        let id = UUID()
        let timestamp: Date
        let level: LogLevel
        let tag: String
        let message: String
        
        func formatted() -> String {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
            let timeStr = formatter.string(from: timestamp)
            return "[\(timeStr)] [\(level.rawValue)] [\(tag)] \(message)"
        }
    }
    
    /// Available log levels in order of severity.
    enum LogLevel: String {
        case debug = "DEBUG"
        case info = "INFO"
        case warning = "WARNING"
        case error = "ERROR"
    }
    
    private func log(_ level: LogLevel, tag: String, message: String) {
        let entry = LogEntry(timestamp: Date(), level: level, tag: tag, message: message)
        
        queue.async(flags: .barrier) {
            self.logEntries.append(entry)
            if self.logEntries.count > self.maxLogEntries {
                self.logEntries.removeFirst()
            }
        }
        
        // Also log to system logger
        let osLogType: OSLogType = {
            switch level {
            case .debug: return .debug
            case .info: return .info
            case .warning: return .default
            case .error: return .error
            }
        }()
        logger.log(level: osLogType, "[\(tag)] \(message)")
    }
    
    func debug(_ tag: String, _ message: String) {
        log(.debug, tag: tag, message: message)
    }
    
    func info(_ tag: String, _ message: String) {
        log(.info, tag: tag, message: message)
    }
    
    func warning(_ tag: String, _ message: String) {
        log(.warning, tag: tag, message: message)
    }
    
    func error(_ tag: String, _ message: String, error: Error? = nil) {
        let fullMessage = error != nil ? "\(message)\n\(error!.localizedDescription)" : message
        log(.error, tag: tag, message: fullMessage)
    }
    
    func getLogs() -> [LogEntry] {
        return queue.sync {
            return logEntries
        }
    }
    
    func clearLogs() {
        queue.async(flags: .barrier) {
            self.logEntries.removeAll()
        }
        info("Logger", "Logs cleared")
    }
    
    func getLogsAsText() -> String {
        return queue.sync {
            return logEntries.map { $0.formatted() }.joined(separator: "\n")
        }
    }
}

