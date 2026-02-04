# Contributing to Thought Smith

Thank you for your interest in contributing to Thought Smith! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

1. Check the [existing issues](../../issues) to see if the bug has already been reported
2. If not, [open a new issue](../../issues/new) with the following information:
   - Platform (Android or iOS) and version
   - Steps to reproduce
   - Expected vs. actual behavior
   - Screenshots or logs if applicable

### Suggesting Features

1. Check [existing issues](../../issues) for similar suggestions
2. Open a new issue with the `enhancement` label
3. Describe the feature, why it would be useful, and any implementation ideas

### Submitting Code

1. Fork the repository
2. Switch to the appropriate platform branch (`android` or `ios`)
3. Create a feature branch from the platform branch:
   ```
   git checkout -b feature/your-feature-name
   ```
4. Make your changes with clear, descriptive commit messages
5. Test your changes thoroughly on the target platform
6. Submit a pull request against the platform branch (not `main`)

## Branch Structure

- `main` -- Documentation and project information only
- `android` -- Android application source code
- `ios` -- iOS application source code

**Important:** Pull requests with code changes should target the `android` or `ios` branch, not `main`. PRs targeting `main` should only contain documentation changes.

## Code Guidelines

### General

- Write clear, self-documenting code
- Follow the existing code style and patterns in each platform branch
- Keep changes focused -- one feature or fix per pull request

### Android (Kotlin)

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Jetpack Compose for UI components
- Follow MVVM architecture patterns

### iOS (Swift)

- Follow [Swift API Design Guidelines](https://www.swift.org/documentation/api-design-guidelines/)
- Use SwiftUI for UI components
- Follow the existing project architecture

## Commit Messages

- Use clear, descriptive commit messages
- Start with a verb in imperative mood (e.g., "Add", "Fix", "Update", "Remove")
- Keep the first line under 72 characters
- Add a body for complex changes explaining the "why"

## Questions?

If you have questions about contributing, feel free to open an issue for discussion.
