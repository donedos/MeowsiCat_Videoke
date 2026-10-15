package com.outofthebox.meowsicattv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.SongItem
import com.outofthebox.meowsicattv.server.WebServerManager
import com.outofthebox.meowsicattv.ui.screens.ArtistsScreen
import com.outofthebox.meowsicattv.ui.screens.HomeScreen
import com.outofthebox.meowsicattv.ui.screens.NewSongsScreen
import com.outofthebox.meowsicattv.ui.screens.PlayerScreen
import com.outofthebox.meowsicattv.ui.screens.PlaylistScreen
import com.outofthebox.meowsicattv.ui.screens.RankingsScreen
import com.outofthebox.meowsicattv.ui.screens.SettingsScreen
import com.outofthebox.meowsicattv.ui.screens.SongsScreen
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface Screen {
    data object Home : Screen
    data class Songs(val artist: String? = null, val favoritesOnly: Boolean = false, val playlistOnly: Boolean = false) : Screen
    data object Artists : Screen
    data object NewSongs : Screen
    data object Playlist : Screen
    data class Player(val song: SongItem, val back: Screen, val initialQueue: List<SongItem> = emptyList()) : Screen
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

    var isLibraryLoading by remember { mutableStateOf(true) }
    val libraryState by produceState<LibraryRepository.Library>(
        initialValue = LibraryRepository.Library(emptyList(), emptyList(), emptyList(), emptyList(), File(rootPath)),
        key1 = rootPath,
        key2 = refreshKey
    ) {
        isLibraryLoading = true
        value = withContext(Dispatchers.IO) {
            LibraryRepository.load(File(rootPath))
        }
        isLibraryLoading = false
    }

    BackHandler(enabled = screen !is Screen.Home) {
        screen = Screen.Home
    }

    val context = LocalContext.current
    LaunchedEffect(libraryState) {
        if (libraryState.songs.isNotEmpty()) {
            WebServerManager.start(context, libraryState)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            WebServerManager.stop()
        }
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
                    isLoading = isLibraryLoading,
                    onOpenSongs = { screen = Screen.Songs() },
                    onOpenPlayer = { screen = Screen.Player(SongItem.placeholder(), target) },
                    onOpenArtists = { screen = Screen.Artists },
                    onOpenNewSongs = { screen = Screen.NewSongs },
                    onOpenFavorites = { screen = Screen.Songs(favoritesOnly = true) },
                    onOpenPlaylist = { screen = Screen.Playlist },
                    onOpenSettings = { screen = Screen.Settings },
                    onOpenRankings = { screen = Screen.Rankings },
                    onOpenSong = { song -> screen = Screen.Player(song, target) }
                )

                is Screen.Playlist -> PlaylistScreen(
                    library = libraryState,
                    onBack = { screen = Screen.Home },
                    onPlayPlaylist = { firstSong, queue -> screen = Screen.Player(song = firstSong, back = target, initialQueue = queue) }
                )

                is Screen.Songs -> SongsScreen(
                    library = libraryState,
                    onBack = { screen = Screen.Home },
                    onOpen = { song -> screen = Screen.Player(song = song, back = target) },
                    onPlayAll = { firstSong, queue -> screen = Screen.Player(song = firstSong, back = target, initialQueue = queue) },
                    artistFilter = target.artist,
                    favoritesOnly = target.favoritesOnly,
                    playlistOnly = target.playlistOnly
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
                    initialQueue = target.initialQueue,
                    onBack = { screen = target.back },
                    onHome = { screen = Screen.Home },
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