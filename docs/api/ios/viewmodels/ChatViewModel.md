# ChatViewModel

```swift
class ChatViewModel
```

### `sendMessage`

```swift
func sendMessage(_ userMessage: String)
```

### `saveJournalEntry`

```swift
func saveJournalEntry()
```

### `acceptSummaryAndSave`

```swift
func acceptSummaryAndSave(_ formattedContent: String)
```

### `rejectSummary`

```swift
func rejectSummary()
```

### `onFileSaved`

```swift
func onFileSaved(success: Bool, filePath: String? = nil)
```

### `clearChat`

```swift
func clearChat()
```

### `clearError`

```swift
func clearError()
```

### `clearSaveSuccess`

```swift
func clearSaveSuccess()
```

### `saveChatTranscript`

```swift
func saveChatTranscript()
```

### `onTranscriptSaved`

```swift
func onTranscriptSaved(success: Bool, filePath: String? = nil)
```

### `toggleInputMode`

```swift
func toggleInputMode()
```

### `toggleOutputMode`

```swift
func toggleOutputMode()
```

### `startListening`

```swift
func startListening()
```

### `stopListening`

```swift
func stopListening()
```

### `stopSpeaking`

```swift
func stopSpeaking()
```

