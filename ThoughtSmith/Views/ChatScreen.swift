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
    @State private var showingFileExporter = false
    @State private var showingShareSheet = false
    @State private var documentToExport: TextDocument?
    @State private var contentToShare: String?
    @State private var showingHelp = false
    @AppStorage("hasSeenWelcome") private var hasSeenWelcome = false
    @State private var showingWelcome = false
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
                    
                    Menu {
                        Button {
                            viewModel.saveJournalEntry()
                        } label: {
                            Label("Generate Journal Entry", systemImage: "book.pages")
                        }
                        .disabled(viewModel.messages.isEmpty || viewModel.isGeneratingSummary)
                        
                        Button {
                            viewModel.saveChatTranscript()
                        } label: {
                            Label("Save Full Transcript", systemImage: "doc.text")
                        }
                        .disabled(viewModel.messages.isEmpty || viewModel.isSavingTranscript)
                    } label: {
                        if viewModel.isGeneratingSummary || viewModel.isSavingTranscript {
                            ProgressView()
                        } else {
                            Image(systemName: "square.and.arrow.down")
                        }
                    }
                    .disabled(viewModel.messages.isEmpty)
                    
                    Button(action: { showingHelp = true }) {
                        Image(systemName: "questionmark.circle")
                    }
                    
                    Button(action: { navigationState.navigate(to: .settings) }) {
                        Image(systemName: "gearshape")
                    }
                }
            }
        }
        .onAppear {
            if !hasSeenWelcome {
                showingWelcome = true
            }
        }
        .sheet(isPresented: $showingWelcome) {
            WelcomeView(isPresented: $showingWelcome)
                .onDisappear {
                    hasSeenWelcome = true
                }
        }
        .sheet(isPresented: $showingHelp) {
            HelpGuideView()
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
                        print("DEBUG: Save button clicked in preview dialog")
                        print("DEBUG: Summary length: \(summary.count)")
                        
                        // Prepare the document and content
                        let doc = TextDocument(content: summary)
                        print("DEBUG: Document created with content length: \(doc.content.count)")
                        documentToExport = doc
                        contentToShare = summary
                        
                        // Close the sheet
                        showingSummaryPreview = false
                        
                        // Try file exporter first, if it doesn't work, use share sheet
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                            print("DEBUG: About to trigger save dialog")
                            print("DEBUG: documentToExport is nil? \(documentToExport == nil)")
                            
                            #if targetEnvironment(simulator)
                            // In simulator, use share sheet which works more reliably
                            print("DEBUG: Running in simulator - using share sheet")
                            showingShareSheet = true
                            #else
                            // On real device, use file exporter
                            print("DEBUG: Running on device - using file exporter")
                            showingFileExporter = true
                            #endif
                            
                            print("DEBUG: Save dialog triggered")
                        }
                    },
                    onReject: {
                        print("DEBUG: Cancel button clicked in preview dialog")
                        viewModel.rejectSummary()
                        showingSummaryPreview = false
                    }
                )
            }
        }
        .onChange(of: viewModel.formattedSummary) { summary in
            print("DEBUG: formattedSummary changed - is nil? \(summary == nil)")
            showingSummaryPreview = summary != nil
        }
        .onChange(of: viewModel.isSaving) { isSaving in
            print("DEBUG: isSaving changed to: \(isSaving)")
        }
        .onChange(of: showingFileExporter) { showing in
            print("DEBUG: showingFileExporter changed to: \(showing)")
        }
        .fileExporter(
            isPresented: Binding(
                get: { showingFileExporter && documentToExport != nil },
                set: { newValue in
                    showingFileExporter = newValue
                    if !newValue {
                        print("DEBUG: File exporter dismissed")
                    }
                }
            ),
            document: documentToExport ?? TextDocument(content: ""),
            contentType: .plainText,
            defaultFilename: FileStorageService.shared.generateJournalEntryFilename()
        ) { result in
            print("DEBUG: File exporter result received")
            switch result {
            case .success(let url):
                print("DEBUG: File saved successfully to: \(url)")
                viewModel.onFileSaved(success: true, filePath: url.lastPathComponent)
            case .failure(let error):
                print("DEBUG: File save failed or cancelled: \(error)")
                viewModel.onFileSaved(success: false)
            }
            // Clean up
            documentToExport = nil
            showingFileExporter = false
        }
        .sheet(isPresented: $showingShareSheet) {
            if let content = contentToShare {
                ShareSheet(activityItems: [content, createTextFile(content: content)])
                    .onDisappear {
                        print("DEBUG: Share sheet dismissed")
                        viewModel.onFileSaved(success: true, filePath: "via Share Sheet")
                        contentToShare = nil
                        showingShareSheet = false
                    }
            }
        }
        .fileExporter(
            isPresented: $viewModel.isSavingTranscript,
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
        VStack(spacing: 8) {
            #if targetEnvironment(simulator)
            Text("🧪 Simulator Mode: Using test speech")
                .font(.caption)
                .foregroundColor(.orange)
                .padding(.horizontal)
            #endif
            
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
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(isListening ? "Listening... Tap to stop" : "Tap to start speaking")
                    #if targetEnvironment(simulator)
                    Text("Will generate test speech in 2s")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    #endif
                }
                .padding(.leading, 16)
                
                Spacer()
            }
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

// MARK: - Share Sheet for Simulator
import UIKit
struct ShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]
    let applicationActivities: [UIActivity]? = nil
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(
            activityItems: activityItems,
            applicationActivities: applicationActivities
        )
        return controller
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// Helper function to create a temporary file URL for sharing
func createTextFile(content: String) -> URL {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd_HH-mm-ss"
    let filename = "journal_entry_\(formatter.string(from: Date())).md"
    
    let tempDir = FileManager.default.temporaryDirectory
    let fileURL = tempDir.appendingPathComponent(filename)
    
    do {
        try content.write(to: fileURL, atomically: true, encoding: .utf8)
        print("DEBUG: Temp file created at: \(fileURL)")
        return fileURL
    } catch {
        print("DEBUG: Error creating temp file: \(error)")
        return tempDir
    }
}


