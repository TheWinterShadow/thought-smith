# Thought Smith Test Suite

Comprehensive test coverage for the Thought Smith journaling app.

## Quick Start

```bash
# Run all tests
cmd + U

# Run specific test class
# Click the diamond icon next to the test class name

# View test results
cmd + 6  # Opens Test Navigator
```

## Test Files Overview

### Unit Tests (`thoughtsmithTests` target)

#### `thoughtsmithTests.swift`
Main test suite covering:
- **Message Model**: Creation, timestamps, Codable conformance
- **AppSettings Model**: Defaults, encoding/decoding, TTS configuration
- **Navigation**: NavigationState management and transitions
- **Enums**: Screen, TTSProvider, AIProvider
- **Errors**: SpeechServiceError descriptions
- **Performance**: Message creation and settings encoding benchmarks

#### `ViewModelTests.swift`
ViewModel behavior tests:
- **SettingsViewModel**: All update methods, provider selection, model management
- **ChatViewModel**: Message handling, state management, initial state

#### `ServiceTests.swift`
Service layer tests:
- **FileStorageService**: Singleton, file system operations
- **SettingsRepository**: Persistence, updates, singleton pattern
- **SpeechService**: State management, TTS provider switching
- **AIService**: Singleton instance
- **AppLogger**: Log levels, clearing, singleton

### UI Tests (`thoughtsmithUITests` target)

#### `thoughtsmithUITests.swift`
End-to-end UI tests:
- **App Launch**: Startup verification
- **Navigation**: Settings, logs, back navigation
- **Settings Tabs**: Context, Text API, Speech tabs
- **Input Fields**: API key entry, text input
- **Chat Screen**: Message input, buttons
- **Logs Screen**: Display, clear functionality
- **Accessibility**: Labels and identifiers
- **Performance**: Launch metrics

## Test Coverage Goals

| Component | Target Coverage | Current Status |
|-----------|----------------|----------------|
| Models | 90% | ✅ |
| ViewModels | 80% | ✅ |
| Services | 70% | ✅ |
| Views | 60% | 🔄 |
| Overall | 75% | 🔄 |

## Writing New Tests

### Unit Test Example

```swift
import XCTest
@testable import thoughtsmith

final class MyTests: XCTestCase {
    
    func testSomething() {
        // Arrange
        let value = 42
        
        // Act
        let result = performCalculation(value)
        
        // Assert
        XCTAssertEqual(result, 84)
    }
    
    @MainActor
    func testViewModelState() {
        let viewModel = MyViewModel()
        XCTAssertFalse(viewModel.isLoading)
    }
}
```

### UI Test Example

```swift
import XCTest

final class MyUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    func testButtonTap() {
        let button = app.buttons["MyButton"]
        XCTAssertTrue(button.exists)
        button.tap()
        
        let result = app.staticTexts["ResultLabel"]
        XCTAssertTrue(result.waitForExistence(timeout: 2))
    }
}
```

## Common Assertions

### XCTest Assertions

```swift
// Equality
XCTAssertEqual(actual, expected)
XCTAssertNotEqual(actual, unexpected)

// Boolean
XCTAssertTrue(condition)
XCTAssertFalse(condition)

// Nil checks
XCTAssertNil(value)
XCTAssertNotNil(value)

// Numeric comparisons
XCTAssertGreaterThan(value, minimum)
XCTAssertLessThan(value, maximum)

// Throwing
XCTAssertThrowsError(try riskyOperation())
XCTAssertNoThrow(try safeOperation())
```

## Best Practices

### 1. Test Naming
- Use descriptive names: `testMessageCreationWithTimestamp()`
- Follow pattern: `test[Component][Action][ExpectedResult]()`

### 2. Test Structure
```swift
func testSomething() {
    // Arrange - Set up test data
    let input = "test"
    
    // Act - Perform the action
    let result = processInput(input)
    
    // Assert - Verify the result
    XCTAssertEqual(result, "expected")
}
```

### 3. Isolation
- Tests should not depend on each other
- Use `setUp()` and `tearDown()` for common setup
- Clean up state after tests

### 4. Async Testing
```swift
func testAsyncOperation() async throws {
    let result = await performAsyncTask()
    XCTAssertNotNil(result)
}
```

### 5. Main Actor
```swift
@MainActor
func testUIUpdate() {
    // Tests that update UI must run on main actor
}
```

## Running Tests in CI/CD

### Command Line
```bash
# All tests
xcodebuild test -scheme thoughtsmith \
  -destination 'platform=iOS Simulator,name=iPhone 15'

# Unit tests only
xcodebuild test -scheme thoughtsmith \
  -only-testing:thoughtsmithTests

# Specific test class
xcodebuild test -scheme thoughtsmith \
  -only-testing:thoughtsmithTests/ServiceTests
```

### GitHub Actions Example
```yaml
- name: Run Tests
  run: |
    xcodebuild test \
      -scheme thoughtsmith \
      -destination 'platform=iOS Simulator,name=iPhone 15' \
      -enableCodeCoverage YES
```

## Test Doubles

### Mock Example
```swift
class MockAIService: AIService {
    var generateCalled = false
    var mockResponse = "Mock response"
    
    override func generate(prompt: String) async throws -> String {
        generateCalled = true
        return mockResponse
    }
}
```

## Debugging Tests

### Print Debug Info
```swift
func testWithDebug() {
    let value = computeValue()
    print("Debug: value = \(value)")
    XCTAssertEqual(value, 42)
}
```

### Breakpoints
- Set breakpoints in test methods
- Use `po` command in debugger
- Inspect variables during test execution

### Failed Test Output
```
testMessageCreation(): XCTAssertEqual failed: 
  ("Hello") is not equal to ("World")
```

## Code Coverage

View coverage:
1. Run tests with coverage: `Cmd + U`
2. Show Report Navigator: `Cmd + 9`
3. Select latest test report
4. Click "Coverage" tab

Target coverage by file type:
- Models: 90%+
- ViewModels: 80%+
- Services: 70%+
- Views: 60%+

## Continuous Testing

### Watch Mode (Manual)
Keep Test Navigator open (`Cmd + 6`) and run tests frequently while developing.

### Pre-commit Hook
```bash
#!/bin/sh
# .git/hooks/pre-commit
xcodebuild test -scheme thoughtsmith -quiet
```

## Troubleshooting

### Tests Won't Run
1. Clean build folder: `Shift + Cmd + K`
2. Reset simulator: Device → Erase All Content and Settings
3. Check test target membership

### Import Errors
- Ensure `@testable import thoughtsmith` is present
- Check "Enable Testability" in Build Settings (Debug)

### UI Tests Flaky
- Add `waitForExistence(timeout:)` calls
- Increase timeout values
- Check for animation conflicts

### Performance Tests Fail
- Run on physical device for consistent results
- Adjust baseline expectations
- Check for background processes

## Resources

- [XCTest Documentation](https://developer.apple.com/documentation/xctest)
- [Testing Guide](https://developer.apple.com/library/archive/documentation/DeveloperTools/Conceptual/testing_with_xcode/)
- [UI Testing](https://developer.apple.com/videos/play/wwdc2015/406/)

## Maintenance

- Review and update tests when features change
- Remove obsolete tests
- Keep test suite running fast (< 30 seconds ideal)
- Monitor code coverage trends

---

**Last Updated**: January 17, 2026  
**Test Count**: 50+ tests  
**Coverage**: 75%+ (goal)
