# Thought Smith

An AI-powered journaling app that transforms journaling from a solitary activity into an interactive conversation. Chat with AI about your day, thoughts, and feelings, and let it generate beautifully formatted journal entries.

## What is Thought Smith?

Thought Smith combines journaling with AI conversation to help you explore your thoughts and feelings. The app provides an interactive chat interface where you can discuss your day with an AI assistant, which then generates structured journal entries in Markdown format.

### Key Features

- **AI-Powered Conversations** -- Chat with AI assistants from OpenAI, Google Gemini, or Anthropic Claude
- **Multiple AI Models** -- Support for GPT-4o, Gemini 1.5 Pro, Claude, and more
- **Automated Journal Generation** -- AI creates formatted journal entries from your conversations
- **Speech-to-Text & Text-to-Speech** -- Talk naturally and listen to responses
- **File Export** -- Save journal entries as Markdown files
- **Privacy-First** -- All data stays on your device; conversations go directly to your chosen AI provider using your own API keys

## Platform Branches

This repository is organized by platform. Choose the branch for your platform:

| Platform | Branch | Description |
|----------|--------|-------------|
| Android | [`android`](../../tree/android) | Native Android app built with Kotlin and Jetpack Compose |
| iOS | [`ios`](../../tree/ios) | Native iOS app built with Swift and SwiftUI |

### Getting Started

1. Switch to the branch for your platform (see table above)
2. Follow the setup instructions in that branch's README
3. Configure your AI API keys within the app's Settings screen

### API Keys

The app requires an API key from at least one supported AI provider:

- **OpenAI** -- [platform.openai.com](https://platform.openai.com)
- **Google Gemini** -- [aistudio.google.com](https://aistudio.google.com)
- **Anthropic Claude** -- [console.anthropic.com](https://console.anthropic.com)

## Documentation

- [Privacy Policy](PRIVACY_POLICY.md)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [License](LICENSE)

## Bug Reports & Feature Requests

If you encounter a bug or have a feature request, please [open an issue](../../issues/new/choose) on this repository. When reporting a bug, include:

- Which platform (Android or iOS) and version you're using
- Steps to reproduce the issue
- What you expected to happen vs. what actually happened
- Any relevant screenshots or logs (available in the app's Logs screen)

## Help & Support

- **In-app logs** -- Check the Logs screen within the app for debugging information
- **API issues** -- Ensure your API keys are valid and the provider's service is operational
- **Connectivity** -- The app requires an internet connection for AI features
- **Issues** -- Search [existing issues](../../issues) or open a new one

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
