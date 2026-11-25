# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Alarmee is a Kotlin Multiplatform library for scheduling alarms and notifications on Android and iOS. It consists of two main modules:

- **`alarmee`**: Core library with local notification support for all platforms (Android, iOS, desktop, etc.)
- **`alarmee-push`**: Extended library adding push notification support via Firebase (mobile-only)

The library uses platform-specific implementations with a common interface, following the expect/actual pattern for multiplatform code.

## Architecture

### Module Structure
- **`alarmee/`**: Core module with local notifications
  - `commonMain/`: Shared interfaces and models
  - `androidMain/`: Android-specific implementations using Android notifications
  - `iosMain/`: iOS-specific implementations using UserNotifications framework
  - `nonMobileMain/`: Non-mobile platform implementations

- **`alarmee-push/`**: Push notification extension
  - `commonMain/`: Common push notification interfaces 
  - `androidMain/`: Firebase messaging for Android
  - `iosMain/`: APNs integration for iOS

- **`sample/composeApp/`**: Demo application showcasing library usage

### Key Services
- `AlarmeeService`: Main entry point for all targets
- `MobileAlarmeeService`: Mobile-specific service with push support
- `LocalNotificationService`: Handles scheduling and local notifications
- `PushNotificationService`: Handles push notifications (mobile only)

### Platform Configuration
The library requires platform-specific configuration:
- `AlarmeeAndroidPlatformConfiguration`: Android notification channels and icons
- `AlarmeeIosPlatformConfiguration`: iOS-specific settings

## Common Development Commands

### Build and Testing
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :alarmee:build
./gradlew :alarmee-push:build

# Check for dependency updates
./gradlew dependencyUpdates

# Generate documentation
./gradlew dokkaHtml
```

### iOS Development
```bash
# Sync CocoaPods dependencies
./gradlew :alarmee:podGenMacX64
./gradlew :alarmee-push:podGenMacX64
```

### Sample App
```bash
# Run Android sample
./gradlew :sample:composeApp:installDebug

# iOS sample must be run through Xcode
open sample/iosApp/iosApp.xcodeproj
```

## Project Configuration

- **Kotlin**: 2.2.0
- **Compose Multiplatform**: 1.8.2
- **Android SDK**: Min 24, Compile 35
- **iOS Deployment Target**: 15.0 (`alarmee-push`)
- **Java Compatibility**: VERSION_21

## Key Files
- `buildSrc/src/main/kotlin/ProjectConfiguration.kt`: Centralized project configuration
- `gradle/libs.versions.toml`: Dependency version catalog
- `settings.gradle.kts`: Module declarations

## Firebase Setup

For push notifications, Firebase configuration is required:
- Android: `google-services.json` in sample app
- iOS: `GoogleService-Info.plist` and notification service extension

## Publishing

Library is published to Maven Central under group `io.github.tweener` with modules:
- `alarmee`: Core library
- `alarmee-push`: Push notification extension