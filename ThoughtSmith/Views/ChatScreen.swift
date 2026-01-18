//
//  ChatScreen.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI
import UniformTypeIdentifiers

struct ChatScreen: View {
    @StateObject private var viewModel = ChatViewModel()
    @EnvironmentObject private var navigationState: NavigationState
    @State private var messageText = ""
    @State private var showingSummaryPreview = false
    @FocusState private var isTextFieldFocused: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            // Messages list
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(message: message)
                                .id(message.id)
                        }
                        
                        if viewModel.isLoading {
                            LoadingIndicator()
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let lastMessage = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // Input/Output mode toggles
            InputOutputModeToggles(
                inputMode: viewModel.inputMode,
                outputMode: viewModel.outputMode,
                isListening: viewModel.isListening,
                isSpeaking: viewModel.isSpeaking,
                onToggleInputMode: { viewModel.toggleInputMode() },
                onToggleOutputMode: { viewModel.toggleOutputMode() },
                onStartListening: { viewModel.startListening() },
                onStopListening: { viewModel.stopListening() },
                onStopSpeaking: { viewModel.stopSpeaking() }
            )
            
            // Input field (only show for text mode)
            if !viewModel.inputMode {
                MessageInputBar(
                    messageText: $messageText,
                    onSendClick: {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    },
                    enabled: !viewModel.isLoading && !messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                )
            } else {
                SpeechInputBar(
                    isListening: viewModel.isListening,
                    onStartListening: { viewModel.startListening() },
                    onStopListening: { viewModel.stopListening() },
                    enabled: !viewModel.isLoading
                )
            }
        }
        .navigationTitle("Thought Smith")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack(spacing: 16) {
                    Button(action: { viewModel.clearChat() }) {
                        Image(systemName: "trash")
                            .foregroundColor(viewModel.messages.count > 1 ? .primary : .secondary)
                    }
                    .disabled(viewModel.messages.count <= 1)
                    
                    Button(action: { viewModel.saveJournalEntry() }) {
                        if viewModel.isGeneratingSummary {
                            ProgressView()
                        } else {
                            Image(systemName: "checkmark")
                        }
                    }
                    .disabled(viewModel.messages.isEmpty || viewModel.isSaving || viewModel.isGeneratingSummary)
                    
                    Button(action: { viewModel.saveChatTranscript() }) {
                        Image(systemName: "arrow.down.doc")
                    }
                    .disabled(viewModel.messages.isEmpty || viewModel.isSavingTranscript)
                    
                    Button(action: { navigationState.navigate(to: .settings) }) {
                        Image(systemName: "gearshape")
                    }
                }
            }
        }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
        }
        .alert("Success", isPresented: .constant(viewModel.saveSuccess != nil)) {
            Button("OK") { viewModel.clearSaveSuccess() }
        } message: {
            Text(viewModel.saveSuccess ?? "")
        }
        .sheet(isPresented: $showingSummaryPreview) {
            if let summary = viewModel.formattedSummary {
                SummaryPreviewDialog(
                    formattedContent: summary,
                    isGenerating: viewModel.isGeneratingSummary,
                    onAccept: {
                        viewModel.acceptSummaryAndSave(summary)
                        showingSummaryPreview = false
                    },
                    onReject: {
                        viewModel.rejectSummary()
                        showingSummaryPreview = false
                    }
                )
            }
        }
        .onChange(of: viewModel.formattedSummary) { summary in
            showingSummaryPreview = summary != nil
        }
        .fileExporter(
            isPresented: Binding(
                get: { viewModel.isSaving && viewModel.formattedSummary != nil },
                set: { 
                    if !$0 && viewModel.isSaving {
                        viewModel.onFileSaved(success: false)
                    }
                }
            ),
            document: TextDocument(content: viewModel.formattedSummary ?? ""),
            contentType: .plainText,
            defaultFilename: FileStorageService.shared.generateJournalEntryFilename()
        ) { result in
            switch result {
            case .success(let url):
                viewModel.onFileSaved(success: true, filePath: url.lastPathComponent)
            case .failure:
                viewModel.onFileSaved(success: false)
            }
        }
        .fileExporter(
            isPresented: Binding(
                get: { viewModel.isSavingTranscript && viewModel.chatTranscript != nil },
                set: { 
                    if !$0 && viewModel.isSavingTranscript {
                        viewModel.onTranscriptSaved(success: false)
                    }
                }
            ),
            document: TextDocument(content: viewModel.chatTranscript ?? ""),
            contentType: .plainText,
            defaultFilename: FileStorageService.shared.generateTranscriptFilename()
        ) { result in
            switch result {
            case .success(let url):
                viewModel.onTranscriptSaved(success: true, filePath: url.lastPathComponent)
            case .failure:
                viewModel.onTranscriptSaved(success: false)
            }
        }
    }
}

// MARK: - Supporting Views

struct MessageBubble: View {
    let message: Message
    
    var body: some View {
        HStack {
            if message.isUser {
                Spacer()
            }
            
            Text(message.content)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(message.isUser ? Color.blue : Color.gray.opacity(0.2))
                .foregroundColor(message.isUser ? .white : .primary)
                .cornerRadius(20)
                .frame(maxWidth: 280, alignment: message.isUser ? .trailing : .leading)
            
            if !message.isUser {
                Spacer()
            }
        }
    }
}

struct LoadingIndicator: View {
    var body: some View {
        HStack {
            ProgressView()
                .padding()
            Spacer()
        }
    }
}

struct MessageInputBar: View {
    @Binding var messageText: String
    let onSendClick: () -> Void
    let enabled: Bool
    
    var body: some View {
        HStack(spacing: 8) {
            TextField("Type your thoughts...", text: $messageText, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(1...4)
            
            Button(action: onSendClick) {
                Image(systemName: "arrow.right.circle.fill")
                    .font(.title2)
                    .foregroundColor(enabled ? .blue : .gray)
            }
            .disabled(!enabled)
        }
        .padding()
        .background(Color(.systemBackground))
    }
}

struct SpeechInputBar: View {
    let isListening: Bool
    let onStartListening: () -> Void
    let onStopListening: () -> Void
    let enabled: Bool
    
    var body: some View {
        HStack {
            Spacer()
            Button(action: isListening ? onStopListening : onStartListening) {
                Image(systemName: "mic.fill")
                    .font(.title)
                    .foregroundColor(.white)
                    .frame(width: 64, height: 64)
                    .background(isListening ? Color.red : (enabled ? Color.blue : Color.gray))
                    .clipShape(Circle())
            }
            .disabled(!enabled)
            
            Text(isListening ? "Listening... Tap to stop" : "Tap to start speaking")
                .padding(.leading, 16)
            Spacer()
        }
        .padding()
        .background(Color(.systemBackground))
    }
}

struct InputOutputModeToggles: View {
    let inputMode: Bool
    let outputMode: Bool
    let isListening: Bool
    let isSpeaking: Bool
    let onToggleInputMode: () -> Void
    let onToggleOutputMode: () -> Void
    let onStartListening: () -> Void
    let onStopListening: () -> Void
    let onStopSpeaking: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: inputMode ? "mic" : "keyboard")
                VStack(alignment: .leading) {
                    Text("Input Mode")
                        .font(.subheadline)
                        .fontWeight(.medium)
                    Text(inputMode ? "Voice input" : "Text input")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Toggle("", isOn: Binding(
                    get: { inputMode },
                    set: { _ in onToggleInputMode() }
                ))
            }
            .padding(.horizontal)
            
            Divider()
            
            HStack {
                Image(systemName: outputMode ? "speaker.wave.2" : "speaker.slash")
                VStack(alignment: .leading) {
                    Text("Output Mode")
                        .font(.subheadline)
                        .fontWeight(.medium)
                    Text(outputMode ? "Voice output" : "Text output")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Toggle("", isOn: Binding(
                    get: { outputMode },
                    set: { _ in onToggleOutputMode() }
                ))
            }
            .padding(.horizontal)
        }
        .padding(.vertical, 12)
        .background(Color(.systemBackground))
    }
}

struct SummaryPreviewDialog: View {
    let formattedContent: String
    let isGenerating: Bool
    let onAccept: () -> Void
    let onReject: () -> Void
    
    var body: some View {
        NavigationView {
            VStack {
                if isGenerating {
                    ProgressView()
                    Text("Generating formatted summary...")
                } else {
                    ScrollView {
                        Text(formattedContent)
                            .padding()
                    }
                }
            }
            .navigationTitle("Preview Journal Entry")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onReject)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save", action: onAccept)
                        .disabled(isGenerating)
                }
            }
        }
    }
}

// MARK: - Text Document for File Export
struct TextDocument: FileDocument {
    static var readableContentTypes: [UTType] { [.plainText, .text] }
    
    var content: String
    
    init(content: String) {
        self.content = content
    }
    
    init(configuration: ReadConfiguration) throws {
        if let data = configuration.file.regularFileContents {
            content = String(data: data, encoding: .utf8) ?? ""
        } else {
            content = ""
        }
    }
    
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        let data = content.data(using: .utf8) ?? Data()
        return FileWrapper(regularFileWithContents: data)
    }
}

