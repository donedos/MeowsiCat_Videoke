package com.outofthebox.songhubatv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.outofthebox.songhubatv.data.LibraryRepository
import com.outofthebox.songhubatv.data.SongItem
import com.outofthebox.songhubatv.ui.screens.ArtistsScreen
import com.outofthebox.songhubatv.ui.screens.HomeScreen
import com.outofthebox.songhubatv.ui.screens.NewSongsScreen
import com.outofthebox.songhubatv.ui.screens.PlayerScreen
import com.outofthebox.songhubatv.ui.screens.RankingsScreen
import com.outofthebox.songhubatv.ui.screens.SettingsScreen
import com.outofthebox.songhubatv.ui.screens.SongsScreen
import java.io.File

sealed interface Screen {
    data object Home : Screen
    data class Songs(val artist: String? = null) : Screen
    data object Artists : Screen
    data object NewSongs : Screen
    data class Player(val song: SongItem, val back: Screen) : Screen
    data object Settings : Screen
    data object Rankings : Screen
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    contentRoot: String = LibraryRepository.DEFAULT_ROOT,
    isInPipMode: Boolean = false,
    isPipSupported: Boolean = false,
    onEnterPip: () -> Unit = {},
    onPlaybackStateChanged: (Boolean) -> Unit = {}
) {
    var rootPath by remember { mutableStateOf(contentRoot) }
    var refreshKey by remember { mutableStateOf(0) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    val libraryState by produceState<LibraryRepository.Library>(
        initialValue = LibraryRepository.load(File(rootPath)),
        key1 = rootPath,
        key2 = refreshKey
    ) {
        value = LibraryRepository.load(File(rootPath))
    }

    BackHandler(enabled = screen !is Screen.Home) {
        screen = Screen.Home
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "nav"
        ) { target ->
            when (target) {
                is Screen.Home -> HomeScreen(
                    library = libraryState,
                    onOpenSongs = { screen = Screen.Songs() },
                    onOpenPlayer = { screen = Screen.Player(SongItem.placeholder(), target) },
                    onOpenArtists = { screen = Screen.Artists },
                    onOpenNewSongs = { screen = Screen.NewSongs },
                    onOpenSettings = { screen = Screen.Settings },
                    onOpenRankings = { screen = Screen.Rankings },
                    onOpenSong = { song -> screen = Screen.Player(song, target) }
                )

                is Screen.Songs -> SongsScreen(
                    library = libraryState,
                    onBack = { screen = Screen.Home },
                    onOpen = { song -> screen = Screen.Player(song, target) },
                    artistFilter = target.artist
                )

                is Screen.Artists -> ArtistsScreen(
                    library = libraryState,
                    onBack = { screen = Screen.Home },
                    onPickArtist = { artist -> screen = Screen.Songs(artist) }
                )

                is Screen.NewSongs -> NewSongsScreen(
                    library = libraryState,
                    onBack = { screen = Screen.Home },
                    onOpen = { song -> screen = Screen.Player(song, target) }
                )

                is Screen.Player -> PlayerScreen(
                    song = target.song,
                    onBack = { screen = target.back },
                    library = libraryState,
                    libraryRoot = libraryState.root,
                    isInPipMode = isInPipMode,
                    isPipSupported = isPipSupported,
                    onEnterPip = onEnterPip,
                    onPlaybackStateChanged = onPlaybackStateChanged
                )

                is Screen.Settings -> SettingsScreen(
                    currentRoot = rootPath,
                    onSave = { newRoot ->
                        rootPath = newRoot
                        refreshKey++
                        screen = Screen.Home
                    },
                    onBack = { screen = Screen.Home }
                )

                is Screen.Rankings -> RankingsScreen(
                    onBack = { screen = Screen.Home }
                )
            }
        }
    }
}