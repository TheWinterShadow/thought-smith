# GitHub Actions Configuration Documentation
#
# This directory contains automated workflows for the Thought Smith Android application.
# These workflows handle continuous integration, code quality, and release automation.
#
# Workflow Files:
# - ci.yml: Continuous integration with linting, testing, and build verification
# - build-apk.yml: APK building and GitHub releases
# - quality.yml: Code quality analysis and security scanning
#
# @author TheWinterShadow
# @since 1.0.0

# Thought Smith - GitHub Actions Workflows

This directory contains GitHub Actions workflows for automated CI/CD, code quality analysis, and release management.

## 📋 Available Workflows

### 1. **CI - Build and Test** (`ci.yml`)
**Purpose**: Continuous Integration for code validation
**Triggers**: 
- Push to main branch
- Pull requests to main
- Manual dispatch

**Features**:
- **Kotlin Linting**: ktlint code style validation
- **Android Lint**: Static analysis for Android-specific issues
- **Unit Tests**: Automated test execution
- **Instrumented Tests**: Android emulator testing
- **Build Verification**: Debug APK compilation
- **Gradle Caching**: Faster builds with dependency caching

**Jobs**:
1. `lint` - Code linting and static analysis
2. `test` - Unit test execution
3. `build` - Build verification
4. `instrumented-test` - Android emulator tests

### 2. **Build Release APK** (`build-apk.yml`)
**Purpose**: Build signed release APKs for distribution
**Triggers**: 
- Manual dispatch with version input
- Version tags (v1.0.0, v1.1.0, etc.)

**Features**:
- **Signed APK Building**: Production-ready APK generation
- **Automatic Versioning**: Version management from tags or input
- **GitHub Releases**: Automatic release creation with APK attachment
- **Changelog Generation**: Git commit-based release notes
- **Debug Builds**: Optional debug APK for testing

**Requirements** (GitHub Secrets):
- `KEYSTORE_FILE`: Base64 encoded Android keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Signing key alias
- `KEY_PASSWORD`: Signing key password

**Usage**:
```bash
# Trigger manual build
gh workflow run build-apk.yml -f version_name=1.0.0 -f version_code=10000

# Or create a version tag
git tag v1.0.0
git push origin v1.0.0
```

### 3. **Code Quality Analysis** (`quality.yml`)
**Purpose**: Comprehensive code quality and security analysis
**Triggers**: 
- Pull requests to main
- Weekly schedule (Mondays 6 AM UTC)
- Manual dispatch

**Features**:
- **Dependency Scanning**: Security vulnerability detection
- **Code Quality**: Static analysis and complexity metrics
- **Test Coverage**: Coverage reporting and analysis
- **Build Analysis**: APK size monitoring and recommendations
- **PR Comments**: Automated quality reports on pull requests

**Jobs**:
1. `dependency-scan` - Security vulnerability scanning
2. `code-quality` - Static analysis and linting
3. `test-coverage` - Test coverage analysis
4. `build-analysis` - APK size and performance metrics
5. `quality-summary` - Consolidated results and PR comments

## 🚀 Getting Started

### 1. Setup Release Signing
To enable APK building, add these secrets to your repository:

```bash
# Generate base64 encoded keystore
base64 -w 0 your-keystore.jks > keystore_base64.txt

# Add to GitHub repository secrets:
KEYSTORE_FILE=<content of keystore_base64.txt>
KEYSTORE_PASSWORD=<your keystore password>
KEY_ALIAS=<your key alias>
KEY_PASSWORD=<your key password>
```

### 2. Enable Workflows
Workflows automatically activate when you:
- Push code changes
- Create pull requests
- Create version tags
- Manually trigger via GitHub Actions UI

### 3. Monitor Results
- **Actions Tab**: View workflow runs and results
- **Pull Requests**: Automated quality reports
- **Releases**: Download built APKs
- **Artifacts**: Access build outputs and reports

## 📊 Workflow Status Badges

Add these badges to your README.md:

```markdown
[![CI](https://github.com/TheWinterShadow/thought-smith/actions/workflows/ci.yml/badge.svg)](https://github.com/TheWinterShadow/thought-smith/actions/workflows/ci.yml)
[![Build APK](https://github.com/TheWinterShadow/thought-smith/actions/workflows/build-apk.yml/badge.svg)](https://github.com/TheWinterShadow/thought-smith/actions/workflows/build-apk.yml)
[![Quality](https://github.com/TheWinterShadow/thought-smith/actions/workflows/quality.yml/badge.svg)](https://github.com/TheWinterShadow/thought-smith/actions/workflows/quality.yml)
```

## 🔧 Customization

### Adding New Checks
To add new quality checks:
1. Modify `quality.yml` to include new analysis tools
2. Add new jobs or steps as needed
3. Update artifact collection for new reports

### Changing Build Variants
To build different variants:
1. Modify `build-apk.yml` gradle commands
2. Update APK paths and naming
3. Adjust signing configuration as needed

### Modifying Test Configuration
To change test execution:
1. Update `ci.yml` test commands
2. Modify emulator configuration for instrumented tests
3. Add new test report collection

## 📈 Performance Optimization

### Caching Strategy
- **Gradle Dependencies**: Cached by checksum of build files
- **Android SDK**: Cached across workflow runs
- **Emulator AVD**: Cached for faster instrumented tests

### Parallel Execution
- Jobs run in parallel where possible
- Dependencies between jobs ensure proper order
- Concurrency limits prevent resource conflicts

### Resource Usage
- **CI Workflows**: ~5-10 minutes per run
- **APK Builds**: ~3-5 minutes per build
- **Quality Analysis**: ~10-15 minutes per run

## 🔒 Security Considerations

- **Secrets Management**: All sensitive data stored in GitHub Secrets
- **Branch Protection**: Workflows enforce quality before merge
- **Dependency Scanning**: Regular security vulnerability checks
- **Minimal Permissions**: Workflows use least-privilege access

## 🐛 Troubleshooting

### Common Issues
1. **Keystore Issues**: Ensure base64 encoding is correct
2. **Build Failures**: Check Gradle compatibility and dependencies
3. **Test Failures**: Verify emulator configuration and timeouts
4. **Permission Errors**: Check repository and workflow permissions

### Debug Tips
- Use workflow dispatch for manual testing
- Check artifact uploads for detailed reports
- Review job logs for specific error messages
- Verify secret configuration in repository settings

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android CI/CD Best Practices](https://developer.android.com/studio/build/building-cmdline)
- [Gradle Build Optimization](https://docs.gradle.org/current/userguide/performance.html)