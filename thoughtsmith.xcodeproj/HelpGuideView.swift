//
//  HelpGuideView.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI

struct HelpGuideView: View {
    @Environment(\.dismiss) var dismiss
    @State private var selectedGuide: GuideType = .gettingStarted
    
    enum GuideType: String, CaseIterable {
        case gettingStarted = "Getting Started"
        case apiSetup = "API Setup"
        case customContext = "Custom Context"
        case journalStructure = "Journal Structure"
        case troubleshooting = "Troubleshooting"
        
        var icon: String {
            switch self {
            case .gettingStarted: return "sparkles"
            case .apiSetup: return "key.fill"
            case .customContext: return "text.bubble.fill"
            case .journalStructure: return "book.pages.fill"
            case .troubleshooting: return "wrench.and.screwdriver.fill"
            }
        }
    }
    
    var body: some View {
        NavigationView {
            HStack(spacing: 0) {
                // Sidebar
                List(GuideType.allCases, id: \.self, selection: $selectedGuide) { guide in
                    Label(guide.rawValue, systemImage: guide.icon)
                        .tag(guide)
                }
                .listStyle(.sidebar)
                .frame(width: 200)
                
                // Content
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        MarkdownContentView(guide: selectedGuide)
                    }
                    .padding()
                }
                .frame(maxWidth: .infinity)
            }
            .navigationTitle("Help & Guides")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct MarkdownContentView: View {
    let guide: HelpGuideView.GuideType
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            switch guide {
            case .gettingStarted:
                GettingStartedGuide()
            case .apiSetup:
                APISetupGuide()
            case .customContext:
                CustomContextGuide()
            case .journalStructure:
                JournalStructureGuide()
            case .troubleshooting:
                TroubleshootingGuide()
            }
        }
    }
}

// MARK: - Getting Started Guide
struct GettingStartedGuide: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Getting Started")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Welcome to Thought Smith! 🌟")
                .font(.title2)
                .foregroundColor(.secondary)
            
            Divider()
            
            GuideSection(
                title: "What is Thought Smith?",
                content: "Thought Smith is an AI-powered journaling companion that helps you capture and organize your thoughts through natural conversation. Use text or voice input to have meaningful dialogues, then transform your conversations into beautiful journal entries."
            )
            
            GuideSection(
                title: "Quick Start",
                steps: [
                    "Set up your API key (see API Setup guide)",
                    "Optionally customize your AI context",
                    "Start chatting about your day",
                    "Generate a formatted journal entry",
                    "Save to your device"
                ]
            )
            
            GuideSection(
                title: "Features",
                bullets: [
                    "Text and voice input modes",
                    "Customizable AI personality and context",
                    "Flexible journal entry formatting",
                    "Multiple AI provider support (OpenAI, Anthropic, Gemini)",
                    "Save as files or share directly"
                ]
            )
        }
    }
}

// MARK: - API Setup Guide
struct APISetupGuide: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("API Setup")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Configure your AI provider")
                .font(.title2)
                .foregroundColor(.secondary)
            
            Divider()
            
            GuideSection(
                title: "Why do I need an API key?",
                content: "Thought Smith uses AI services to power conversations and generate journal entries. You'll need an API key from your chosen provider (OpenAI, Anthropic, or Google Gemini) to use the app."
            )
            
            GuideSection(
                title: "Getting an OpenAI API Key",
                steps: [
                    "Go to platform.openai.com",
                    "Sign up or log in to your account",
                    "Navigate to API Keys in your account settings",
                    "Click 'Create new secret key'",
                    "Copy the key (you won't be able to see it again!)",
                    "Paste it into Thought Smith Settings > Text API > API Key"
                ]
            )
            
            GuideSection(
                title: "Getting an Anthropic API Key",
                steps: [
                    "Go to console.anthropic.com",
                    "Sign up or log in",
                    "Go to API Keys section",
                    "Create a new API key",
                    "Copy and paste into Thought Smith settings"
                ]
            )
            
            GuideSection(
                title: "Getting a Google Gemini API Key",
                steps: [
                    "Go to makersuite.google.com/app/apikey",
                    "Sign in with your Google account",
                    "Click 'Create API Key'",
                    "Copy and paste into Thought Smith settings"
                ]
            )
            
            InfoBox(
                type: .warning,
                content: "Keep your API key secure! Never share it with others. API usage may incur costs depending on your provider's pricing."
            )
            
            GuideSection(
                title: "Choosing a Model",
                content: "Each provider offers different models with varying capabilities:\n\n• GPT-4: Most capable, higher cost\n• GPT-3.5: Fast and economical\n• Claude Opus: Excellent for nuanced conversations\n• Gemini Pro: Good balance of quality and speed"
            )
        }
    }
}

// MARK: - Custom Context Guide
struct CustomContextGuide: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Custom Context")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Personalize your AI companion")
                .font(.title2)
                .foregroundColor(.secondary)
            
            Divider()
            
            GuideSection(
                title: "What is AI Context?",
                content: "The AI context is a set of instructions that defines how your AI companion behaves, responds, and helps you journal. Think of it as giving personality and purpose to your journaling assistant."
            )
            
            GuideSection(
                title: "Default Context",
                content: "By default, Thought Smith comes with a thoughtful journaling assistant that:\n\n• Asks reflective questions\n• Encourages deeper thinking\n• Maintains a supportive tone\n• Focuses on personal growth"
            )
            
            GuideSection(
                title: "Customizing Your Context",
                steps: [
                    "Go to Settings",
                    "Select the 'Context' tab",
                    "Edit the 'AI System Context' text field",
                    "Click 'Save' to apply changes"
                ]
            )
            
            GuideSection(
                title: "Example Contexts",
                content: "**Gratitude Journal:**\n\"You are a gratitude-focused journaling companion. Help users identify and reflect on things they're grateful for. Ask about positive moments, people who made a difference, and small joys they experienced.\"\n\n**Productivity Coach:**\n\"You are a productivity-focused journaling assistant. Help users reflect on their accomplishments, identify obstacles, and plan their next steps. Be encouraging but focused on action and results.\"\n\n**Mindfulness Guide:**\n\"You are a mindfulness journaling companion. Guide users to be present with their thoughts and feelings without judgment. Ask gentle questions about their emotional state and physical sensations.\""
            )
            
            InfoBox(
                type: .tip,
                content: "Experiment with different contexts! You can always revert to the default or try something new."
            )
        }
    }
}

// MARK: - Journal Structure Guide
struct JournalStructureGuide: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Journal Structure")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Format your journal entries")
                .font(.title2)
                .foregroundColor(.secondary)
            
            Divider()
            
            GuideSection(
                title: "What is Output Format?",
                content: "The output format controls how your conversation is transformed into a journal entry. You can specify exactly how you want your entries to look and what information to include."
            )
            
            GuideSection(
                title: "Default Format",
                content: "The default format creates entries with:\n\n• Date and time header\n• Main reflection section\n• Key insights or takeaways\n• Mood or emotional state\n• Action items (if applicable)"
            )
            
            GuideSection(
                title: "Customizing Your Format",
                steps: [
                    "Go to Settings",
                    "Select the 'Context' tab",
                    "Scroll to 'Output Format Instructions'",
                    "Describe how you want entries formatted",
                    "Save your changes"
                ]
            )
            
            GuideSection(
                title: "Format Examples",
                content: "**Bullet Points:**\n\"Format the conversation as a bullet-pointed journal entry with sections: Main Events, Thoughts, Feelings, and Gratitudes.\"\n\n**Narrative Style:**\n\"Convert the conversation into a flowing narrative journal entry, written in first person, that captures the essence of today's reflections.\"\n\n**Structured Template:**\n\"Format as:\n# [Date]\n## Summary\n[Brief overview]\n## Detailed Reflection\n[Main content]\n## Mood: [emoji and description]\n## Tomorrow's Focus\n[Action items]\""
            )
            
            InfoBox(
                type: .tip,
                content: "You can use Markdown formatting in your instructions! The AI will generate entries with headers, lists, bold text, and more."
            )
            
            GuideSection(
                title: "Testing Your Format",
                content: "After changing your format:\n\n1. Have a short conversation\n2. Generate a journal entry\n3. Preview the result\n4. Adjust your instructions if needed\n5. Try again until you're happy!"
            )
        }
    }
}

// MARK: - Troubleshooting Guide
struct TroubleshootingGuide: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Troubleshooting")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Common issues and solutions")
                .font(.title2)
                .foregroundColor(.secondary)
            
            Divider()
            
            TroubleshootingItem(
                problem: "App crashes when switching to voice input",
                solution: "This usually means microphone permissions weren't granted. Go to iOS Settings > Thought Smith > Microphone and enable access. If you're on the simulator, voice input uses test phrases instead."
            )
            
            TroubleshootingItem(
                problem: "AI doesn't respond or shows an error",
                solutions: [
                    "Check that your API key is correctly entered in Settings",
                    "Verify your API key is active on the provider's website",
                    "Ensure you have available API credits/quota",
                    "Check your internet connection",
                    "Try a different model"
                ]
            )
            
            TroubleshootingItem(
                problem: "File save dialog doesn't appear (simulator)",
                solution: "The file exporter doesn't work reliably in the iOS Simulator. The app automatically uses the Share Sheet instead when running in the simulator. On a real device, you'll see the proper file picker."
            )
            
            TroubleshootingItem(
                problem: "Voice recognition doesn't detect speech (simulator)",
                solution: "The simulator doesn't have access to your Mac's microphone. When running in simulator, the app uses test phrases instead. To test real voice input, run on a physical iOS device."
            )
            
            TroubleshootingItem(
                problem: "Journal entries are too short or too long",
                solution: "Adjust your Output Format Instructions in Settings. You can specify desired length, level of detail, and structure. Try phrases like 'Keep entries concise, around 200 words' or 'Create detailed entries with multiple paragraphs'."
            )
            
            TroubleshootingItem(
                problem: "AI responses feel repetitive or unhelpful",
                solution: "Try customizing your AI Context in Settings. A more specific context helps the AI understand what kind of journaling assistance you need. See the Custom Context guide for examples."
            )
            
            InfoBox(
                type: .info,
                content: "Still having issues? Check the Logs (Settings > Logs) for detailed error messages that can help identify the problem."
            )
        }
    }
}

// MARK: - Helper Components
struct GuideSection: View {
    let title: String
    var content: String?
    var steps: [String]?
    var bullets: [String]?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.title3)
                .fontWeight(.semibold)
            
            if let content = content {
                Text(content)
                    .foregroundColor(.primary)
            }
            
            if let steps = steps {
                VStack(alignment: .leading, spacing: 6) {
                    ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                        HStack(alignment: .top, spacing: 8) {
                            Text("\(index + 1).")
                                .fontWeight(.semibold)
                                .foregroundColor(.blue)
                            Text(step)
                        }
                    }
                }
            }
            
            if let bullets = bullets {
                VStack(alignment: .leading, spacing: 6) {
                    ForEach(bullets, id: \.self) { bullet in
                        HStack(alignment: .top, spacing: 8) {
                            Text("•")
                                .fontWeight(.bold)
                            Text(bullet)
                        }
                    }
                }
            }
        }
    }
}

struct InfoBox: View {
    enum InfoType {
        case info, tip, warning
        
        var color: Color {
            switch self {
            case .info: return .blue
            case .tip: return .green
            case .warning: return .orange
            }
        }
        
        var icon: String {
            switch self {
            case .info: return "info.circle.fill"
            case .tip: return "lightbulb.fill"
            case .warning: return "exclamationmark.triangle.fill"
            }
        }
    }
    
    let type: InfoType
    let content: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: type.icon)
                .foregroundColor(type.color)
                .font(.title3)
            
            Text(content)
                .font(.callout)
        }
        .padding()
        .background(type.color.opacity(0.1))
        .cornerRadius(8)
    }
}

struct TroubleshootingItem: View {
    let problem: String
    var solution: String?
    var solutions: [String]?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 8) {
                Image(systemName: "exclamationmark.circle.fill")
                    .foregroundColor(.red)
                Text(problem)
                    .fontWeight(.semibold)
            }
            
            if let solution = solution {
                Text(solution)
                    .foregroundColor(.secondary)
                    .padding(.leading, 28)
            }
            
            if let solutions = solutions {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(solutions, id: \.self) { sol in
                        HStack(alignment: .top, spacing: 6) {
                            Text("•")
                            Text(sol)
                        }
                        .foregroundColor(.secondary)
                    }
                }
                .padding(.leading, 28)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}
