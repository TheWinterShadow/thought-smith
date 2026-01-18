# iOS Project Setup Guide

This guide will help you set up the Thought Smith iOS project in Xcode.

## Creating the Xcode Project

Since this repository contains the source code but not the Xcode project file, you'll need to create a new Xcode project and add these files.

### Option 1: Create New Xcode Project (Recommended)

1. Open Xcode
2. Select **File > New > Project**
3. Choose **iOS > App**
4. Configure the project:
   - **Product Name**: `ThoughtSmith`
   - **Interface**: SwiftUI
   - **Language**: Swift
   - **Bundle Identifier**: `com.thewintershadow.thoughtsmith`
   - **Minimum Deployment**: iOS 16.0
5. Save the project in the repository root directory
6. Delete the default `ContentView.swift` and `ThoughtSmithApp.swift` files that Xcode creates
7. Add all files from the `ThoughtSmith/` folder to your Xcode project:
   - Drag the `ThoughtSmith` folder into Xcode
   - Make sure "Copy items if needed" is **unchecked** (files are already in the repo)
   - Select "Create groups" (not folder references)
   - Add to target: ThoughtSmith

### Option 2: Use Existing Project Structure

If you prefer to work with the existing file structure:

1. Create a new Xcode project as described above
2. Move the project file (`ThoughtSmith.xcodeproj`) to the repository root
3. Add all source files from `ThoughtSmith/` to the project

## Project Configuration

### Required Settings

1. **Signing & Capabilities**:
   - Select your development team
   - Enable "Automatically manage signing"

2. **Info.plist**:
   - The `Info.plist` file is already configured with required permissions
   - Ensure it's included in your Xcode project

3. **Deployment Target**:
   - Minimum iOS Version: 16.0
   - This is required for SwiftUI NavigationStack and other modern APIs

### Capabilities

The app requires the following capabilities (configured in Info.plist):
- Speech Recognition
- Microphone Access

These are automatically requested at runtime when needed.

## Building and Running

1. Select a target device or simulator (iOS 16.0+)
2. Build the project (⌘B)
3. Run the app (⌘R)

## Project Structure

```
ThoughtSmith/
├── ThoughtSmithApp.swift      # App entry point
├── ContentView.swift           # Main navigation
├── Models/                     # Data models
│   ├── Message.swift
│   ├── AIProvider.swift
│   ├── AppSettings.swift
│   └── TTSProvider.swift
├── Services/                   # Business logic
│   ├── AIService.swift
│   ├── SettingsRepository.swift
│   ├── FileStorageService.swift
│   └── SpeechService.swift
├── ViewModels/                 # View models
│   ├── ChatViewModel.swift
│   └── SettingsViewModel.swift
├── Views/                      # SwiftUI views
│   ├── ChatScreen.swift
│   ├── SettingsScreen.swift
│   └── LogsScreen.swift
├── Utilities/                  # Utilities
│   └── Logger.swift
└── Info.plist                  # App configuration
```

## Troubleshooting

### Build Errors

- **Missing imports**: Ensure all Swift files are added to the Xcode project target
- **Info.plist not found**: Make sure `Info.plist` is included in the project and set as the Info.plist file in Build Settings
- **Deployment target**: Ensure minimum iOS version is set to 16.0

### Runtime Issues

- **Speech recognition not working**: Check that microphone and speech recognition permissions are granted in Settings > Privacy
- **File export not working**: Ensure the app has proper file access permissions
- **API errors**: Verify API keys are correctly configured in Settings

## Next Steps

1. Configure your API keys in the app Settings
2. Test speech recognition and text-to-speech features
3. Try generating a journal entry from a conversation
4. Check the Logs screen for any issues

## Notes

- The app uses UserDefaults for settings persistence
- All API keys are stored locally on the device
- Speech recognition requires internet connection for some features
- File export uses iOS document picker for user-selected locations

