# Thought Smith 🤔📝

**An AI-Powered Journaling Android App**

Thought Smith is a modern Android application that combines the benefits of journaling with AI conversation to help users explore their thoughts and feelings. The app provides an interactive chat interface where users can discuss their day, thoughts, and experiences with AI, which then generates beautifully formatted journal entries.

## 📱 What This App Does

Thought Smith transforms journaling from a solitary activity into an interactive conversation. Users chat with AI about their day, thoughts, and feelings, and the AI:
- Asks thoughtful follow-up questions
- Shows empathy and understanding
- Helps users explore their emotions deeper
- Generates clean, formatted journal entries in Markdown format
- Saves journal entries to files for future reference

## 🎯 Key Features

- **AI-Powered Conversations**: Chat with AI assistants from OpenAI, Google Gemini, or Anthropic Claude
- **Multiple AI Models**: Support for various models including GPT-4o, Gemini 1.5 Pro, Claude 4.5, and more
- **Automated Journal Generation**: AI creates formatted journal entries from conversations
- **File Export**: Save journal entries as Markdown files
- **Customizable AI Context**: Configure how the AI responds and behaves
- **Clean Material Design 3 UI**: Modern, intuitive interface following Android design guidelines
- **Comprehensive Logging**: Built-in logging system for debugging and monitoring

## 🏗️ Architecture Overview

The app follows modern Android development best practices with a clean architecture:

```
📦 Thought Smith
├── 🎯 UI Layer (Jetpack Compose)
│   ├── Screens (Chat, Settings, Logs)
│   ├── Navigation (NavGraph)
│   └── Theme (Material Design 3)
├── 🧠 ViewModel Layer (MVVM Pattern)
│   ├── ChatViewModel (Chat logic)
│   └── SettingsViewModel (Settings management)
├── 🗄️ Repository Layer (Data Management)
│   ├── AIService (AI API communication)
│   ├── SettingsRepository (User preferences)
│   └── FileStorageService (File operations)
└── 📊 Data Layer (Models & Storage)
    ├── Data Models (Message, AppSettings, AIProvider)
    ├── SharedPreferences (Settings storage)
    └── File System (Journal export)
```

## 📁 Project Structure

### Core Application Files
- **`MainActivity.kt`** - App entry point, sets up Jetpack Compose and navigation
- **`AndroidManifest.xml`** - App configuration, permissions, and metadata

### Data Layer (`/data/`)
- **`Message.kt`** - Data model for chat messages
- **`AIProvider.kt`** - Enum and models for AI providers and models
- **`AppSettings.kt`** - Data model for user settings and preferences

### Repository Layer (`/repository/`)
- **`AIService.kt`** - Handles communication with AI APIs (OpenAI, Gemini, Anthropic)
- **`SettingsRepository.kt`** - Manages user settings persistence
- **`FileStorageService.kt`** - Handles file operations for journal export

### ViewModel Layer (`/viewmodel/`)
- **`ChatViewModel.kt`** - Manages chat state and AI interactions
- **`SettingsViewModel.kt`** - Manages settings UI and persistence
- **`ViewModelFactory.kt`** - Factory for creating ViewModels with dependencies

### UI Layer (`/ui/`)
- **`/screens/`**
  - **`ChatScreen.kt`** - Main chat interface
  - **`SettingsScreen.kt`** - Configuration and preferences
  - **`LogsScreen.kt`** - App logs and debugging info
- **`/theme/`** - Material Design 3 theming (Colors, Typography, Theme)

### Navigation (`/navigation/`)
- **`NavGraph.kt`** - App navigation setup using Jetpack Navigation Compose

### Utilities (`/util/`)
- **`Logger.kt`** - Centralized logging system for debugging and monitoring

## 🔑 Setup and Configuration

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24+ (Android 7.0) minimum
- Kotlin 1.8+
- AI API keys from supported providers

### Getting Started
1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync dependencies
4. Run the app on an emulator or device
5. Configure AI API keys in Settings

### API Keys Configuration
The app requires API keys from AI providers. Configure these in the Settings screen:
- **OpenAI**: Get API key from platform.openai.com
- **Google Gemini**: Get API key from makersuite.google.com
- **Anthropic Claude**: Get API key from console.anthropic.com

## 🔧 Technical Details

### Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Navigation Compose** - Type-safe navigation
- **ViewModel & LiveData** - MVVM architecture
- **Coroutines** - Asynchronous programming
- **Gson** - JSON parsing
- **OkHttp** - HTTP client for API calls
- **Material Design 3** - UI components and theming

### Permissions
- `INTERNET` - Required for AI API communication
- `READ_EXTERNAL_STORAGE` - For reading files
- `WRITE_EXTERNAL_STORAGE` - For saving journal entries
- `MANAGE_EXTERNAL_STORAGE` - Enhanced file access on newer Android versions

### Supported AI Models

#### OpenAI
- GPT-4o, GPT-4o Mini
- GPT-4 Turbo
- GPT-3.5 Turbo

#### Google Gemini
- Gemini 1.5 Pro, Gemini 1.5 Flash
- Gemini Pro

#### Anthropic Claude
- Claude 4.5 (Opus, Sonnet, Haiku)
- Claude 4.1, Claude 4
- Claude 3.5 Haiku, Claude 3 Haiku

## 🎨 User Experience

### Chat Interface
- Clean, conversation-style interface
- Real-time message exchange with AI
- Automatic scrolling to latest messages
- Loading indicators during AI responses

### Journal Generation
- One-tap journal generation from conversations
- AI creates structured, formatted entries
- Export to Markdown files
- Customizable formatting instructions

### Settings Management
- Easy AI provider and model selection
- API key configuration
- Customizable AI behavior and context
- Output formatting preferences

## 🐛 Debugging and Logging

The app includes comprehensive logging through the `AppLogger` utility:
- **Info**: General application flow
- **Warning**: Potential issues that don't break functionality  
- **Error**: Exceptions and failures
- **Debug**: Detailed debugging information

View logs in the Logs screen within the app.

## 🔒 Privacy and Security

- API keys are stored locally on device
- Conversations are not stored permanently unless exported
- No data is shared with third parties beyond chosen AI providers
- Users have full control over their data and exports

## 🚀 Future Enhancements

Potential areas for expansion:
- Cloud backup and sync
- Multiple conversation threads
- Journal templates and prompts
- Export to additional formats (PDF, DOCX)
- Conversation history persistence
- Customizable AI personalities
- Voice-to-text integration
- Dark mode theming

## 📄 License

This project is a personal journaling application. Please respect AI provider terms of service when using their APIs.

## 🤝 Contributing

This appears to be a personal project. If you'd like to contribute:
1. Fork the repository
2. Create a feature branch
3. Make your changes with proper documentation
4. Submit a pull request

## 📞 Support

For issues or questions:
- Check the in-app logs for debugging information
- Ensure API keys are properly configured
- Verify internet connectivity for AI features
- Check AI provider service status if requests fail