//
//  FileStorageService.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import Foundation
import UniformTypeIdentifiers

/// Service for handling file operations, particularly saving journal entries.
class FileStorageService {
    static let shared = FileStorageService()
    
    private init() {}
    
    /// Save content to a file using the document picker.
    /// This is a helper that returns the content to be saved.
    /// The actual file saving is handled by the UI using UIDocumentPickerViewController.
    func prepareContentForSaving(_ content: String) -> Data? {
        return content.data(using: .utf8)
    }
    
    /// Generate a default filename for journal entries.
    func generateJournalEntryFilename() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd_HH-mm-ss"
        return "journal_entry_\(formatter.string(from: Date())).md"
    }
    
    /// Generate a default filename for chat transcripts.
    func generateTranscriptFilename() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd_HH-mm-ss"
        return "chat_transcript_\(formatter.string(from: Date())).txt"
    }
}

