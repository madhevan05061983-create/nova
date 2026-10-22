# Nova Voice Agent

A modern Android AI Voice Assistant built with Jetpack Compose, Kotlin, and Firebase.

## Key Features

- **Cinematic Looping Video Background**: High-performance video background with automatic aspect-ratio scaling and battery-saver toggle.
- **Universal App Launcher**: Open any application on the device by voice (e.g., *"Open Camera"*, *"Open Settings"*, *"Open Spotify"*, *"Open Chrome"*).
- **Voice Intelligence & Speech Synthesis**: Real-time voice interaction with visual waveform frequency bars, Android `TextToSpeech` output, and speech speed adjustment.
- **Nova Live Conversation**: Dedicated hands-free bidirectional voice stream with instant interruption.
- **System Tasks & Telemetry**: Battery status reports, settings shortcuts, voice note taking, web searches, and time queries.
- **Firebase Firestore & Authentication**: Real-time cloud sync for conversation history and notes with zero-trust security rules.

## Building and Running Locally

### Prerequisites

- Android Studio Jellyfish or newer
- JDK 17
- Android SDK 36 (minSdk 24)

### Build Commands

```bash
# Ensure execution permissions for the Gradle wrapper
chmod +x gradlew

# Run Unit and Robolectric Tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions CI/CD Deployment

This repository includes automated GitHub Actions workflows:
- `.github/workflows/android.yml`: Automatically tests and compiles the APK on every push or pull request to `main`/`master`, and uploads the APK artifact.
- `.github/workflows/deploy.yml`: Generates release-ready APK packages and attaches them to GitHub Releases on tag pushes (`v*`) or manual triggers.
