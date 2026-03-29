# for

```swift
class for
```

### `getOpenAIResponse`

```swift
func getOpenAIResponse(messages: [Message], model: String, apiKey: String, systemContext: String)
```

### `getGeminiResponse`

```swift
func getGeminiResponse(messages: [Message], model: String, apiKey: String, systemContext: String)
```

### `getAnthropicResponse`

```swift
func getAnthropicResponse(messages: [Message], model: String, apiKey: String, systemContext: String)
```

### `getOpenAITTSAudio`

```swift
func getOpenAITTSAudio(text: String, apiKey: String, voice: String = "nova", model: String = "tts-1")
```

### `getGeminiTTSAudio`

```swift
func getGeminiTTSAudio(text: String, apiKey: String, model: String = "gemini-2.5-flash-preview-tts", voiceName: String = "Kore")
```

### `getAnthropicTTSAudio`

```swift
func getAnthropicTTSAudio(text: String, apiKey: String)
```

### `getAWSPollyTTSAudio`

```swift
func getAWSPollyTTSAudio(text: String, accessKey: String, secretKey: String, region: String, voiceId: String = "Joanna", engine: String = "neural")
```

