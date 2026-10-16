# MeowsiCatTV

**MeowsiCatTV** is a feature-rich karaoke player for **Android TV** and Android devices that reads and plays *MeowsiCat / SongHub* content directly from a shared content folder on your device. Built with **Jetpack Compose** and **Kotlin**, it is optimized for remote control / D-pad navigation and includes a mobile web remote server, QR code discovery, reservation queue management, and Picture-in-Picture support.

## Features

- 🎤 **Local Library & Swipe-to-Refresh** — scans the content folder (default `/storage/emulated/0/MeowsiCat`) and indexes songs, artists, styles, and languages. Swipe down on the Main menu or Song Search to instantly rescan and load newly added files (`song/`, `songs/`, `bgv/`, `bgphoto/`).
- 🌐 **Web App Remote & QR Search** — Scan the on-screen QR code on your phone to open a mobile web remote and search songs, reserve items, view the reservation queue, and control playback. Includes a foreground notification with a **Stop** action button.
- 📀 **Browsing & Search** — Home, Songs, Artists, New Songs, Favorites, and Playlist screens with smooth D-pad focus navigation and vertical song lists.
- ▶️ **Playback & Transport** — plays karaoke MIDI + MP3 tracks via **Media3 (ExoPlayer)**, with an advanced MIDI engine, selectable **SoundFonts (.sf2)**, and Play/Pause synchronization.
- 📜 **Lyrics & Positioning** — real-time lyric highlighting, custom styling, and user-configurable **Lyric Placement** (Center or Bottom) in Settings for TV and landscape displays.
- 🎨 **Backgrounds** — support for `.mp4` background videos (`bgv/`) or static background photos (`bgphoto/`) with BGV button cycling.
- 🏆 **Scoring & Rankings** — microphone or random song scoring with a smooth transition delay before the score overlay appears, plus persistent rankings.
- 📺 **TV-first & PiP** — D-pad / remote friendly, with intelligent Picture-in-Picture support (activates when on the Player Screen or actively playing).

## Requirements

| Item | Value |
| ---- | ----- |
| minSdk | 26 |
| targetSdk / compileSdk | 37 |
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Java compatibility | 11 |

On **Android 11+**, the app requests *"All files access"* (`MANAGE_EXTERNAL_STORAGE`) so it can read content folders outside of scoped storage. Grant this permission when prompted.

## Content Layout

Place your content in a `MeowsiCat` (or `SongHub`) folder on the device. The app expects:

```
MeowsiCat/
├── song/ or songs/    # MIDI (.mid), .kar, and MP3 files dropped by user
├── bgv/               # Background videos (.mp4, .mkv, .avi)
├── bgphoto/           # Static background photos (.jpg, .png)
├── sound/             # Optional .sf2 SoundFonts used for MIDI playback
└── idx/               # shubidx / shubblob catalog indexes
```

The active root path can be changed in **Settings**.

## Building & Versioning

```bash
# Build a debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest
```

### Version v0.4.1

This release includes enhanced web remote integration, notification remote control with stop broadcast, swipe-down auto-scanning, customizable lyric placement, and polished TV d-pad navigation.

## License

The **Lilita One** font used by the app is released under the
[Open Font License](licenses/LilitaOne-OFL.txt).
