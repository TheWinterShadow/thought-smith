//
//  SettingsScreen.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI

struct SettingsScreen: View {
    @StateObject private var viewModel = SettingsViewModel()
    @EnvironmentObject private var navigationState: NavigationState
    @State private var selectedTab = 0
    @State private var showingHelp = false
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("Settings Tab", selection: $selectedTab) {
                Text("Context").tag(0)
                Text("Text API").tag(1)
                Text("Speech").tag(2)
            }
            .pickerStyle(.segmented)
            .padding()
            
            TabView(selection: $selectedTab) {
                ContextInfoTab(viewModel: viewModel)
                    .tag(0)
                TextAPITab(viewModel: viewModel)
                    .tag(1)
                SpeechTab(viewModel: viewModel)
                    .tag(2)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Back") {
                    navigationState.navigateBack()
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack(spacing: 16) {
                    Button(action: { showingHelp = true }) {
                        Image(systemName: "questionmark.circle")
                    }
                    
                    Button(action: {
                        navigationState.navigate(to: .logs)
                    }) {
                        Text("Logs")
                    }
                }
            }
        }
        .sheet(isPresented: $showingHelp) {
            HelpGuideView()
        }
        .alert("Success", isPresented: .constant(viewModel.saveSuccess)) {
            Button("OK") { viewModel.clearSaveSuccess() }
        } message: {
            Text("Settings saved successfully!")
        }
    }
}

// MARK: - Context Info Tab
struct ContextInfoTab: View {
    @ObservedObject var viewModel: SettingsViewModel
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("AI Context")
                        .font(.headline)
                    Text("Define how the AI should behave and what role it should take in your conversations.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    TextEditor(text: Binding(
                        get: { viewModel.settings.aiContext },
                        set: { viewModel.updateAiContext($0) }
                    ))
                    .frame(minHeight: 120)
                }
                .padding(.vertical, 8)
            }
            
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Output Format Instructions")
                        .font(.headline)
                    Text("Instructions for how the AI should format the journal entry when you save it.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    TextEditor(text: Binding(
                        get: { viewModel.settings.outputFormatInstructions },
                        set: { viewModel.updateOutputFormat($0) }
                    ))
                    .frame(minHeight: 100)
                }
                .padding(.vertical, 8)
            }
            
            Section {
                Button(action: { viewModel.saveSettings() }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView()
                        }
                        Text("Save Settings")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }
}

// MARK: - Text API Tab
struct TextAPITab: View {
    @ObservedObject var viewModel: SettingsViewModel
    
    var body: some View {
        Form {
            Section("AI Provider") {
                ForEach(AIProvider.allCases, id: \.self) { provider in
                    HStack {
                        Text(provider.displayName)
                        Spacer()
                        if viewModel.settings.aiProvider == provider {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        viewModel.updateProvider(provider)
                    }
                }
            }
            
            Section("Model") {
                ForEach(viewModel.availableModels) { model in
                    VStack(alignment: .leading) {
                        HStack {
                            Text(model.displayName)
                                .fontWeight(.medium)
                            Spacer()
                            if viewModel.settings.aiModel == model.modelName {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.blue)
                            }
                        }
                        Text(model.modelName)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        viewModel.updateModel(model.modelName)
                    }
                }
            }
            
            Section {
                TextField("Enter your API key", text: Binding(
                    get: { viewModel.settings.apiKey },
                    set: { viewModel.updateApiKey($0) }
                ))
                .textContentType(.password)
                .autocapitalization(.none)
                .autocorrectionDisabled()
            } header: {
                Text("API Key")
            } footer: {
                Text("Enter your API key for the selected AI provider.")
            }
            
            Section {
                Button(action: { viewModel.saveSettings() }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView()
                        }
                        Text("Save Settings")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }
}

// MARK: - Speech Tab
struct SpeechTab: View {
    @ObservedObject var viewModel: SettingsViewModel
    
    var body: some View {
        Form {
            Section {
                ForEach(TTSProvider.allCases, id: \.self) { provider in
                    HStack {
                        Text(provider.displayName)
                        Spacer()
                        if viewModel.settings.ttsProvider == provider {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        viewModel.updateTTSProvider(provider)
                    }
                }
            } header: {
                Text("Text-to-Speech Provider")
            } footer: {
                Text("Choose your preferred TTS provider. Local is free and offline. Remote options provide more natural voices.")
            }
            
            // Provider-specific settings
            if viewModel.settings.ttsProvider == .openAI {
                Section("OpenAI TTS Configuration") {
                    TextField("Model (e.g., tts-1 or tts-1-hd)", text: Binding(
                        get: { viewModel.settings.ttsOpenAIModel },
                        set: { viewModel.updateTTSOpenAIModel($0) }
                    ))
                    TextField("API Key", text: Binding(
                        get: { viewModel.settings.ttsOpenAIApiKey },
                        set: { viewModel.updateTTSOpenAIApiKey($0) }
                    ))
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                }
            }
            
            if viewModel.settings.ttsProvider == .gemini {
                Section("Gemini TTS Configuration") {
                    TextField("Model", text: Binding(
                        get: { viewModel.settings.ttsGeminiModel },
                        set: { viewModel.updateTTSGeminiModel($0) }
                    ))
                    TextField("Voice Name (e.g., Kore, Aoede)", text: Binding(
                        get: { viewModel.settings.ttsGeminiVoiceName },
                        set: { viewModel.updateTTSGeminiVoiceName($0) }
                    ))
                    TextField("API Key", text: Binding(
                        get: { viewModel.settings.ttsGeminiApiKey },
                        set: { viewModel.updateTTSGeminiApiKey($0) }
                    ))
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                }
            }
            
            if viewModel.settings.ttsProvider == .anthropic {
                Section("Anthropic TTS Configuration") {
                    Text("Note: Anthropic TTS API is not yet publicly available.")
                        .font(.caption)
                        .foregroundColor(.red)
                    TextField("Model", text: Binding(
                        get: { viewModel.settings.ttsAnthropicModel },
                        set: { viewModel.updateTTSAnthropicModel($0) }
                    ))
                    TextField("API Key", text: Binding(
                        get: { viewModel.settings.ttsAnthropicApiKey },
                        set: { viewModel.updateTTSAnthropicApiKey($0) }
                    ))
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                }
            }
            
            if viewModel.settings.ttsProvider == .awsPolly {
                Section("AWS Polly Configuration") {
                    TextField("AWS Access Key ID", text: Binding(
                        get: { viewModel.settings.awsAccessKey },
                        set: { viewModel.updateAWSAccessKey($0) }
                    ))
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                    
                    TextField("AWS Secret Access Key", text: Binding(
                        get: { viewModel.settings.awsSecretKey },
                        set: { viewModel.updateAWSSecretKey($0) }
                    ))
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                    
                    TextField("AWS Region (e.g., us-east-1)", text: Binding(
                        get: { viewModel.settings.awsRegion },
                        set: { viewModel.updateAWSRegion($0) }
                    ))
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                }
            }
            
            Section {
                Button(action: { viewModel.saveSettings() }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView()
                        }
                        Text("Save Settings")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }
}

