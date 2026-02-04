# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in Thought Smith, please report it responsibly.

**Do not open a public issue for security vulnerabilities.**

Instead, please report security issues by [opening a private security advisory](../../security/advisories/new) on this repository, or by contacting the maintainer directly through GitHub.

### What to Include

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

### Response

We will acknowledge receipt of your report and provide updates as we investigate and address the issue.

## Security Considerations

### API Keys

- API keys are stored locally on your device using platform-native secure storage
- Never share your API keys or commit them to source control
- The app does not transmit your API keys to any server other than the chosen AI provider

### Data Privacy

- All user data is stored locally on the device
- Chat messages are sent directly to your chosen AI provider -- they do not pass through any intermediary server
- Journal entries are stored locally unless you explicitly export them
- See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for full details

### Network Security

- All communication with AI providers uses HTTPS/TLS encryption
- No data is sent to first-party servers

## Supported Versions

Security updates are applied to the latest version of each platform branch. We recommend always using the most recent version.
