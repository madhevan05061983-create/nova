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

## Standalone Website Edition (`/web/`)

In addition to the Android app, Nova Voice Agent includes a full-featured, zero-dependency modern **Web Application** located in `/web/`:
- **`web/index.html`**: Semantic, accessible responsive HTML5 structure.
- **`web/style.css`**: Deep space futuristic design with dynamic waveform visualizer, glowing orb animations, and mobile-first responsive layout.
- **`web/app.js`**: Web Speech Recognition API (`webkitSpeechRecognition`), Web Speech Synthesis API (`SpeechSynthesisUtterance`), Battery API, universal web app launchers, and bidirectional Nova Live conversation.
- **Background Video**: Centers and loops the cinematic background video directly in HTML5: `https://res.cloudinary.com/lirx7iwb/video/upload/v1792684712/a_edit_1.mp4`.

### Deploying the Website

#### 1. GitHub Pages (Automated via GitHub Actions)
Every push to `main` or `master` automatically triggers `.github/workflows/pages.yml` to publish the `/web/` folder directly to GitHub Pages at:
`https://<username>.github.io/<repository>/`

#### 2. Vercel or Netlify
- In Vercel or Netlify, set the **Publish Directory** to `web`.
- No build command required (static zero-config deployment).

#### 3. Local Web Preview
```bash
# Serve locally using any static web server:
cd web && npx serve .
# or:
python3 -m http.server 8080 --directory web
```

## GitHub Actions CI/CD Deployment

This repository includes automated GitHub Actions workflows:
- `.github/workflows/pages.yml`: Automatically deploys the `/web/` site to GitHub Pages on every push.
- `.github/workflows/android.yml`: Automatically tests and compiles the Android APK on every push or pull request to `main`/`master`, and uploads the APK artifact.
- `.github/workflows/deploy.yml`: Generates release-ready APK packages and attaches them to GitHub Releases on tag pushes (`v*`) or manual triggers.
