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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.outofthebox.meowsicattv.ui.components.BannerAd
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
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meowsicat.android.storage.BehaviorSettings
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.outofthebox.meowsicattv.R

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
    contentRoot: String? = null,
    isInPipMode: Boolean = false,
    isPipSupported: Boolean = false,
    onEnterPip: () -> Unit = {},
    onPlayerStateChanged: (playing: Boolean, inPlayerScreen: Boolean) -> Unit = { _, _ -> },
    onThemeChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var rootPath by remember {
        mutableStateOf(
            contentRoot?.takeIf { it.isNotBlank() } ?: LibraryRepository.getSavedRoot(context)
        )
    }
    var showWelcome by remember {
        mutableStateOf(!BehaviorSettings.isWelcomeShown(context))
    }
    var refreshKey by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var isPlayingState by remember { mutableStateOf(false) }

    val inPlayerScreen = screen is Screen.Player
    LaunchedEffect(inPlayerScreen, isPlayingState) {
        onPlayerStateChanged(isPlayingState, inPlayerScreen)
    }

    var isLibraryLoading by remember { mutableStateOf(true) }
    val libraryState by produceState<LibraryRepository.Library>(
        initialValue = LibraryRepository.Library(emptyList(), emptyList(), emptyList(), emptyList(), File(rootPath)),
        key1 = rootPath,
        key2 = refreshKey
    ) {
        isLibraryLoading = true
        value = withContext(Dispatchers.IO) {
            LibraryRepository.load(File(rootPath), forceReload = refreshKey > 0)
        }
        isLibraryLoading = false
        isRefreshing = false
    }

    val handleRefreshLibrary: () -> Unit = {
        isRefreshing = true
        refreshKey++
    }

    BackHandler(enabled = screen !is Screen.Home) {
        screen = Screen.Home
    }

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
        val showBanner = screen !is Screen.Player
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                            isRefreshing = isRefreshing,
                            onRefreshLibrary = handleRefreshLibrary,
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
                            isRefreshing = isRefreshing,
                            onRefreshLibrary = handleRefreshLibrary,
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
                            onPlaybackStateChanged = { playing -> isPlayingState = playing }
                        )

                        is Screen.Settings -> SettingsScreen(
                            currentRoot = rootPath,
                            onSave = { newRoot ->
                                val normalized = newRoot.trim()
                                if (normalized.isNotBlank()) {
                                    LibraryRepository.setSavedRoot(context, normalized)
                                    rootPath = normalized
                                    refreshKey++
                                }
                                screen = Screen.Home
                            },
                            onThemeChanged = onThemeChanged,
                            onShowGuide = { showWelcome = true },
                            onBack = { screen = Screen.Home }
                        )

                        is Screen.Rankings -> RankingsScreen(
                            onBack = { screen = Screen.Home }
                        )
                    }
                }

                if (showWelcome) {
                    WelcomeDialog(
                        onDismiss = {
                            BehaviorSettings.setWelcomeShown(context, true)
                            showWelcome = false
                        }
                    )
                }
            }
            if (showBanner) {
                BannerAd(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun WelcomeDialog(onDismiss: () -> Unit) {
    BackHandler(onBack = onDismiss)

    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(200L)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cat_01),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Welcome to MeowsiCatTV!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "To start singing, upload your karaoke files into app storage via Settings -> Content & Database Files -> Upload Files:\n\n" +
                            "• idx/: Catalog databases (shubidx, shubblob, megmid)\n" +
                            "• songs/: Karaoke files (.mid, .kar, .mp3)\n" +
                            "• sound/: SoundFont files (.sf2)\n" +
                            "• bgv/: Background videos (.mp4, .mkv)\n" +
                            "• bgphoto/: Background photos (.jpg, .png, .webp)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}