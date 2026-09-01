# Build the APK on GitHub — no Android Studio needed

1. Create a new GitHub repository, preferably named `LocalMusicPlayer`.
2. Extract this ZIP on your PC.
3. Open the extracted `MusicPlayerAndroid` folder.
4. Upload the **contents** of that folder to your GitHub repository (not the outer folder itself).
5. Open the repository's **Actions** tab.
6. Select **Build Android APK** and click **Run workflow**.
7. Wait for the green check mark.
8. Open the completed workflow run, scroll to **Artifacts**, and download `LocalMusicPlayer-debug-apk`.
9. Extract the downloaded artifact and install `app-debug.apk` on your Android phone.

The GitHub Actions workflow builds with JDK 17, Gradle 8.7, and Android Gradle Plugin 8.6.1.
