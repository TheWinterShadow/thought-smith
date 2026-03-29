# SpeechService

```swift
class SpeechService
```

### `setupSpeechRecognizer`

```swift
func setupSpeechRecognizer()
```

### `isSpeechRecognitionAvailable`

```swift
func isSpeechRecognitionAvailable() -> Bool
```

### `requestAuthorization`

```swift
func requestAuthorization()
```

### `startListening`

```swift
func startListening()
```

### `stopListening`

```swift
func stopListening()
```

### `setTTSProvider`

```swift
func setTTSProvider(_ provider: TTSProvider)
```

### `speak`

```swift
func speak(_ text: String, settings: AppSettings)
```

### `speakLocal`

```swift
func speakLocal(_ text: String)
```

### `speakOpenAI`

```swift
func speakOpenAI(_ text: String, apiKey: String, model: String)
```

### `speakGemini`

```swift
func speakGemini(_ text: String, apiKey: String, model: String, voiceName: String)
```

### `speakAnthropic`

```swift
func speakAnthropic(_ text: String, apiKey: String)
```

### `speakAWSPolly`

```swift
func speakAWSPolly(_ text: String, accessKey: String, secretKey: String, region: String)
```

### `playAudioData`

```swift
func playAudioData(_ audioData: Data, isMP3: Bool, isPCM: Bool = false)
```

### `convertPCMToWAV`

```swift
func convertPCMToWAV(_ pcmData: Data)
```

### `stopSpeaking`

```swift
func stopSpeaking()
```

### `isCurrentlySpeaking`

```swift
func isCurrentlySpeaking() -> Bool
```

### `cleanup`

```swift
func cleanup()
```

### `speechSynthesizer`

```swift
func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance)
```

### `speechSynthesizer`

```swift
func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance)
```

### `audioPlayerDidFinishPlaying`

```swift
func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool)
```

