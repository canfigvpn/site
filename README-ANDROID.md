# FLOCH3 Android app

This folder contains the native Android WebView wrapper for:

- Website: https://floch3.blogix.ir/
- App name: FLOCH3
- Application ID: ir.ef3.floch3
- Minimum Android: 7.0 (API 24)
- Compile / target SDK: 35
- Language: Kotlin
- Build: Gradle 8.9 + Android Gradle Plugin 8.7.3 + JDK 17

## Features

- Loads the FLOCH3 website inside the app; normal HTTP and HTTPS navigation stays in the WebView.
- Does not launch Chrome for ordinary website links.
- Keeps website cookies and DOM storage in the app's WebView data.
- Supports Android's document picker for website file-upload controls.
- Supports downloads through Android DownloadManager.
- Supports HTML5 full-screen video.
- Handles the Android Back button using the WebView navigation history.
- Shows an offline / load-error screen with a retry button.
- Rejects invalid TLS certificates rather than bypassing HTTPS security.
- Uses a dark FLOCH3 launcher icon and theme.
- Keeps the existing website files, including the root index.html, unchanged.

## Build locally

Install JDK 17, Android SDK Platform 35, Build Tools 35.0.0, and Gradle 8.9, then run:

```bash
gradle --no-daemon assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Build on GitHub

Open **Actions → FLOCH3 Android APK → Run workflow**. The workflow also runs automatically when app source files are pushed to the configured branches. Download the artifact named **FLOCH3-debug-APK** from the completed run.

## Notes

- The website itself is online, so an internet connection is required to load new pages and content.
- WebView is an embedded Android component. It does not open Chrome for regular HTTP/HTTPS navigation and its page history is separate from Chrome's history.
- File selection and downloads are handled by Android system components. A file picker may therefore appear as an Android system sheet, not as a browser window.
- Website camera / microphone capture permissions are not granted automatically.
- The debug APK is suitable for testing and direct installation. A signed release build should be configured separately before publishing.
