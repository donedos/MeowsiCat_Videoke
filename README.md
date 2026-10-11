# SongHubATV

**SongHubATV** is a karaoke player for **Android TV** that reads and plays *SongHub* content
directly from a shared content folder on your device. Built with **Jetpack Compose** and **Kotlin**,
it's optimized for remote control / D-pad navigation (Leanback) and also supports Picture-in-Picture
playback.

## Features

- 🎤 **Local SongHub library** — scans a `SongHub` content folder (default
  `/storage/emulated/0/SongHub`) and indexes songs, artists, styles, and languages.
- 📀 **Browsing** — Home, Songs, Artists, and New Songs screens with D-pad focus navigation.
- ▶️ **Playback** — plays karaoke MIDI + MP3 tracks via **Media3 (ExoPlayer)**, with a built-in
  MIDI bass engine and selectable **SoundFonts** for MIDI output.
- 📜 **Lyrics & scoring** — real-time lyric display and song scoring with a live score overlay.
- 🏆 **Rankings** — persists your scores and shows a per-song rankings board.
- 🎛️ **Settings** — configure the library folder, audio, background, lyric, and scoring options,
  and pick the active SoundFont.
- 📺 **TV-first** — Leanback launcher entry point, D-pad / remote friendly, plus
  Picture-in-Picture support so a song keeps playing in a small window.

## Requirements

| Item | Value |
| ---- | ----- |
| minSdk | 26 |
| targetSdk / compileSdk | 37 |
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Java compatibility | 11 |

On **Android 11+**, the app requests *"All files access"* (`MANAGE_EXTERNAL_STORAGE`) so it can read
your SongHub content folders outside of scoped storage. Grant this permission when prompted.

## Content Layout

Place your SongHub content in a `SongHub` folder on the device. The app expects:

```
SongHub/
├── (song / MIDI / MP3 content files)
├── sound/        # optional .sf2 SoundFonts used for MIDI playback
└── ...           # index + user content scanned at runtime
```

The active root path can be changed in **Settings**. If the catalog is missing, the app reports
where to put the content so it can be found again.

## Building

```bash
# Build a debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest
```

Or open the project in **Android Studio**, then run the `app` configuration on an Android TV
emulator or a TV device.

### Project layout

```
app/
├── src/main/java/com/outofthebox/songhubatv/
│   ├── MainActivity.kt          # entry point, PiP + permission handling
│   ├── data/                    # library repo, lyric loading, score store, model
│   └── ui/
│       ├── App.kt               # navigation + state
│       ├── screens/             # Home, Songs, Artists, NewSongs, Player, Settings, Rankings
│       ├── components/          # shared TV-friendly UI components
│       └── theme/               # Compose theme
└── src/main/java/com/songhub/android/   # bundled karaoke engine (MIDI, parser, scoring)
```

## Tech Stack

- **Jetpack Compose** + Material3, with `androidx.tv` (Leanback) foundation/material libraries
- **Media3** (ExoPlayer) for audio playback
- **Coil** for image loading
- Bundled `com.songhub.android` karaoke engine for MIDI parsing, lyric building, and scoring

## License

The **Lilita One** font used by the app is released under the
[Open Font License](licenses/LilitaOne-OFL.txt).
