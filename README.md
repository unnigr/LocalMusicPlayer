# Local Music Player — Android

Android Studio project wrapping the supplied HTML music player in a WebView, with a native Android folder picker bridge.

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Connect an Android phone (USB debugging enabled) or create an emulator.
4. Run `app`.
5. For an APK: Build → Build APK(s). The debug APK is generated under `app/build/outputs/apk/debug/`.

## Notes
- The original UI/features are retained.
- Android folder selection uses the Storage Access Framework and recursively finds common audio formats.
- Folder read permission is persisted when Android allows it.
- This is an offline/local player; it does not upload music.
- The first version uses WebView audio. For fully robust lock-screen/background playback, a second native audio-engine version using Android Media3 can be added.
