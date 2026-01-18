# Test Files Setup Instructions

## Files Created

I've created the following test files in `/repo/`:

### Unit Test Files (for thoughtsmithTests target):
1. **thoughtsmithTests.swift** - Main unit tests for models, navigation, and enums
2. **ViewModelTests.swift** - Tests for SettingsViewModel and ChatViewModel
3. **ServiceTests.swift** - Tests for all service classes

### UI Test Files (for thoughtsmithUITests target):
- **thoughtsmithUITests.swift** - Already exists and updated

## Next Steps

### 1. Add Files to thoughtsmithTests Target

In Xcode:

1. **Right-click** on the `thoughtsmithTests` folder in Project Navigator
2. Select **Add Files to "thoughtsmith"...**
3. Navigate to your project folder and add these files:
   - `thoughtsmithTests.swift`
   - `ViewModelTests.swift`
   - `ServiceTests.swift`

4. **Important**: When adding, make sure:
   - ✅ **Target Membership** is set to `thoughtsmithTests` only
   - ✅ **Copy items if needed** is checked
   - ✅ Files are added to the `thoughtsmithTests` group

### 2. Verify UI Test Target

The UI test file should already be in place at:
- `thoughtsmithUITests/thoughtsmithUITests.swift`

### 3. Delete Old Test Files

If there are any test files in the wrong location (like in Views folder), delete them:

1. Find any folders named `thoughtsmithTests` or `thoughtsmithUITests` under **Views**
2. Right-click → **Delete**
3. Choose **Move to Trash**

### 4. Build and Run Tests

1. **Clean Build Folder**: `Shift + Cmd + K`
2. **Build for Testing**: `Shift + Cmd + U`
3. **Run Tests**: `Cmd + U`

## Alternative: Manual File Creation

If adding files doesn't work, you can create them manually:

### For thoughtsmithTests:

1. Right-click `thoughtsmithTests` folder → **New File**
2. Choose **Swift File**
3. Name it (e.g., `thoughtsmithTests.swift`)
4. Make sure **Target** is set to `thoughtsmithTests`
5. Copy the content from `/repo/thoughtsmithTests.swift`
6. Paste into the new file
7. Repeat for `ViewModelTests.swift` and `ServiceTests.swift`

## Troubleshooting

### Error: "Unable to find module dependency: 'XCTest'"

**Solution**: 
- Make sure files are in the test target, not the main app target
- Check Target Membership in File Inspector (Cmd + Option + 1)

### Error: "@testable import thoughtsmith" fails

**Solution**:
- Select main app target
- Go to Build Settings
- Search for "Enable Testability"
- Set to **Yes** for Debug configuration

### Tests not showing up

**Solution**:
- Clean build folder: `Shift + Cmd + K`
- Rebuild: `Cmd + B`
- Check Test Navigator: `Cmd + 6`

## Expected Test Structure

After setup, your Project Navigator should look like:

```
thoughtsmith/
├── thoughtsmith/                    # Main app target
│   ├── ThoughtSmithApp.swift
│   ├── Views/
│   │   ├── ContentView.swift
│   │   ├── ChatScreen.swift
│   │   └── SettingsScreen.swift
│   ├── ViewModels/
│   ├── Services/
│   └── Models/
├── thoughtsmithTests/               # Unit test target ✅
│   ├── thoughtsmithTests.swift      # ← Add this
│   ├── ViewModelTests.swift         # ← Add this
│   └── ServiceTests.swift           # ← Add this
└── thoughtsmithUITests/             # UI test target ✅
    └── thoughtsmithUITests.swift    # ← Already there
```

## Running Specific Tests

- **All tests**: `Cmd + U`
- **Single test class**: Click diamond next to class name
- **Single test method**: Click diamond next to test method
- **From Test Navigator**: `Cmd + 6`, then click play button

## What These Tests Cover

### thoughtsmithTests.swift
- ✅ Message model creation and encoding
- ✅ AppSettings defaults and persistence
- ✅ Navigation state management
- ✅ Enum cases and display names
- ✅ Error descriptions
- ✅ Performance benchmarks

### ViewModelTests.swift
- ✅ SettingsViewModel state and updates
- ✅ ChatViewModel message management
- ✅ All settings update methods
- ✅ TTS provider configuration

### ServiceTests.swift
- ✅ Singleton service instances
- ✅ FileStorageService functionality
- ✅ SettingsRepository persistence
- ✅ SpeechService state management
- ✅ AppLogger functionality

### thoughtsmithUITests.swift
- ✅ App launch and navigation
- ✅ Settings screen interaction
- ✅ Tab navigation
- ✅ Input field testing
- ✅ Button interactions
- ✅ Accessibility checks
- ✅ Performance metrics

## Success Criteria

You'll know everything is set up correctly when:

1. ✅ No compilation errors in test files
2. ✅ Test Navigator shows all test classes
3. ✅ Running tests shows green checkmarks
4. ✅ Code coverage report is generated

## Next Steps After Setup

1. Run all tests to establish baseline
2. Fix any failing tests
3. Add more tests as you develop features
4. Maintain test coverage above 70%
5. Run tests before each commit

Good luck! 🚀
