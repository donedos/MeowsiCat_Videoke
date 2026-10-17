package com.outofthebox.meowsicattv.ui.screens

import androidx.activity.compose.BackHandler
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import com.outofthebox.meowsicattv.ui.components.InstrumentalBadge
import com.outofthebox.meowsicattv.ui.components.rememberIsTvDevice
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.QrCode
import com.outofthebox.meowsicattv.data.FavoritesStore
import com.outofthebox.meowsicattv.server.RemoteCommandBus
import com.outofthebox.meowsicattv.ui.components.QrCodeImage
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import com.outofthebox.meowsicattv.R
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.meowsicat.android.audio.BassMidiEngine
import com.meowsicat.android.audio.MicScoringSession
import com.meowsicat.android.audio.SoundFontManager
import com.meowsicat.android.model.CountdownGroup
import com.meowsicat.android.model.LyricLine
import com.meowsicat.android.parser.TempoUtils
import com.meowsicat.android.song.ScoreCalculator
import com.meowsicat.android.storage.AudioSettings
import com.meowsicat.android.storage.BehaviorSettings
import com.meowsicat.android.storage.BgvSettings
import com.meowsicat.android.storage.LyricSettings
import com.meowsicat.android.storage.ScoreSettings
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.LyricLoader
import com.outofthebox.meowsicattv.data.MsSpan
import com.outofthebox.meowsicattv.data.SongItem
import com.outofthebox.meowsicattv.data.ScoreStore
import com.outofthebox.meowsicattv.ui.lyricFontFamily
import com.outofthebox.meowsicattv.ui.lyrics.MIN_LYRIC_SCALE
import com.outofthebox.meowsicattv.ui.lyrics.fitScaleForLines
import com.outofthebox.meowsicattv.ui.lyrics.wrapFragmentRanges
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Videoke highlight colour: the "sung" portion of the lyric (indigo). */
private val KaraokeHighlight = Color(0xFF4F46E5)

/** Width reserved on the right edge of the landscape layout for the custom keyboard. */
private val CustomKeyboardWidth = 420.dp

/** Fixed key height so the keyboard keeps a compact, previous-style footprint. */
private val KeyboardKeyHeight = 40.dp

/** Frame interval used while polling the MIDI transport so the wipe stays smooth. */
private const val AUDIO_POLL_MS = 16L

/** Allowed key-shift range, in semitones. */
private const val KEY_SHIFT_MIN = -10
private const val KEY_SHIFT_MAX = 10

/** Allowed tempo-shift range, in steps of 5% (so -10 => 50%, +10 => 150%). */
private const val TEMPO_SHIFT_MIN = -10
private const val TEMPO_SHIFT_MAX = 10

@Composable
fun PlayerScreen(
    song: SongItem,
    initialQueue: List<SongItem> = emptyList(),
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    library: LibraryRepository.Library = LibraryRepository.Library(emptyList(), emptyList(), emptyList(), emptyList(), File("")),
    libraryRoot: File = library.root,
    isInPipMode: Boolean = false,
    isPipSupported: Boolean = false,
    onEnterPip: () -> Unit = {},
    onPlaybackStateChanged: (Boolean) -> Unit = {}
) {
    var activeSong by remember(song) { mutableStateOf(song) }
    var selectCounter by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val wipeChannel = AudioSettings.getWipeMelodyChannel(context)
    val parsed by produceState(
        initialValue = LyricLoader.Parsed(emptyList()),
        key1 = activeSong.code,
        key2 = selectCounter,
        key3 = wipeChannel
    ) {
        value = LyricLoader.load(activeSong, wipeChannel)
    }

    if (parsed.error != null && parsed.lines.isEmpty() && activeSong.code != 0) {
        LyricUnavailable(song = activeSong, error = parsed.error, onBack = onBack)
        return
    }

    LyricPlayer(
        song = activeSong,
        selectCounter = selectCounter,
        initialQueue = initialQueue,
        library = library,
        lines = parsed.lines,
        countdowns = parsed.countdowns,
        tempoMap = parsed.tempoMap,
        ticksPerBeat = parsed.ticksPerBeat,
        totalMs = parsed.totalMs,
        isInstrumental = parsed.instrumental && activeSong.code != 0,
        instrumentalBreaks = parsed.instrumentalBreaks,
        context = context,
        libraryRoot = libraryRoot,
        onBack = onBack,
        onHome = onHome,
        onSelectSong = { newSong ->
            activeSong = newSong
            selectCounter++
        },
        isInPipMode = isInPipMode,
        isPipSupported = isPipSupported,
        onEnterPip = onEnterPip,
        onPlaybackStateChanged = onPlaybackStateChanged
    )
}

@Composable
private fun LyricUnavailable(song: SongItem, error: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = song.displayTitle(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (song.code == 0) "000000" else song.displaySinger(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            if (song.isMp3) {
                Text(
                    text = "Audio preview is not part of this UI build.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = error ?: "No lyrics available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Press back", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LyricPlayer(
    song: SongItem,
    selectCounter: Int = 0,
    initialQueue: List<SongItem> = emptyList(),
    library: LibraryRepository.Library,
    lines: List<LyricLine>,
    countdowns: List<CountdownGroup>,
    tempoMap: List<TempoUtils.TempoEntry>,
    ticksPerBeat: Int,
    totalMs: Double,
    isInstrumental: Boolean = false,
    instrumentalBreaks: List<MsSpan> = emptyList(),
    context: Context,
    libraryRoot: File,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSelectSong: (SongItem) -> Unit,
    isInPipMode: Boolean,
    isPipSupported: Boolean,
    onEnterPip: () -> Unit,
    onPlaybackStateChanged: (Boolean) -> Unit
) {
    val engine = remember(context) { BassMidiEngine(context) }
    val micSession = remember(context) { MicScoringSession(context) }
    DisposableEffect(song.code, selectCounter) {
        onDispose {
            engine.pause()
            engine.release()
            micSession.stopCapturing()
        }
    }

    var playing by rememberSaveable { mutableStateOf(song.code != 0) }
    var audioReady by rememberSaveable { mutableStateOf(false) }
    var audioNote by rememberSaveable { mutableStateOf<String?>(null) }
    var seeking by rememberSaveable { mutableStateOf(false) }
    var posMs by rememberSaveable { mutableDoubleStateOf(0.0) }
    var posTick by rememberSaveable { mutableLongStateOf(0L) }
    var scored by rememberSaveable { mutableStateOf(false) }
    var audioDurationMs by rememberSaveable { mutableDoubleStateOf(0.0) }
    // Score overlay pending state: -1 = none, otherwise the score to present.
    // rememberSaveable so the overlay survives config changes / PiP toggles.
    var pendingScore by rememberSaveable { mutableIntStateOf(-1) }
    // Bumped when the same song is replayed without a queue advance (REPEAT_ONE).
    // The end-of-song state writes (playing=false -> true, same song.code) coalesce
    // into a single recomposition, so the poll loop's keys would otherwise be
    // unchanged and the loop would stay torn down.
    var replayEpoch by remember { mutableIntStateOf(0) }
    val lyricStyle = remember { LyricSettings.getLyricStyle(context) }

    // Remote keypad, reservation & search state
    var codeInput by rememberSaveable { mutableStateOf("") }
    val typedSongPreview = remember(codeInput, library.songs) {
        if (codeInput.isBlank()) null
        else {
            val code = codeInput.toIntOrNull()
            if (code != null) library.songs.find { it.code == code } else null
        }
    }
    var reservationQueue by remember { mutableStateOf<List<SongItem>>(initialQueue) }

    DisposableEffect(reservationQueue, song, playing) {
        RemoteCommandBus.getReservationQueue = { reservationQueue }
        RemoteCommandBus.getCurrentSong = { song }
        RemoteCommandBus.getIsPlaying = { playing }
        onDispose {
            RemoteCommandBus.getReservationQueue = null
            RemoteCommandBus.getCurrentSong = null
            RemoteCommandBus.getIsPlaying = null
        }
    }
    var showSearchDialog by rememberSaveable { mutableStateOf(false) }
    var showRsvDialog by rememberSaveable { mutableStateOf(false) }
    var showFavDialog by rememberSaveable { mutableStateOf(false) }
    var favoriteSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var showQrDialog by rememberSaveable { mutableStateOf(false) }
    var statusToast by remember { mutableStateOf<String?>(null) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val isTvDevice = rememberIsTvDevice()
    // Controls visibility
    var controlsVisible by rememberSaveable { mutableStateOf(!isTvDevice) }

    // Height (px) of the portrait lyrics area. Shrinks when the on-screen keypad is shown, which
    // is used to auto-shrink the lyric font so phrases are not clipped.
    var portraitLyricHeightPx by remember { mutableIntStateOf(0) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (codeInput.isNotEmpty()) {
            codeInput = ""
        } else if (controlsVisible) {
            controlsVisible = false
        } else {
            if (now - lastBackPressTime < 2000L) {
                onHome()
            } else {
                lastBackPressTime = now
                statusToast = "Press back again to exit to main menu"
            }
        }
    }

    var bgvIndex by rememberSaveable { mutableIntStateOf(0) }

    // Key, Tempo, Melody states
    var keyShift by rememberSaveable { mutableIntStateOf(0) }
    var temShift by rememberSaveable { mutableIntStateOf(0) }
    var melVol by rememberSaveable { mutableIntStateOf(AudioSettings.getMelodyVolume(context)) }
    var vocalActive by rememberSaveable { mutableStateOf(melVol > 0) }

    // Melody (guide vocal) volume is persisted so the player restores the user's last chosen
    // level across songs and app restarts.
    val applyMelodyVolume: (Int) -> Unit = { value ->
        melVol = value.coerceIn(0, 100)
        vocalActive = melVol > 0
        engine.setMelodyVolume(melVol)
        AudioSettings.setMelodyVolume(context, melVol)
    }
    val toggleVocal: () -> Unit = {
        val nextOn = !vocalActive
        melVol = if (nextOn) AudioSettings.getMelodyVolume(context).let { if (it > 0) it else 80 } else 0
        vocalActive = nextOn
        engine.setMelodyVolume(melVol)
    }

    val effectiveTotalMs = remember(totalMs, audioDurationMs) {
        maxOf(totalMs, audioDurationMs)
    }

    val bgvDir = remember(context, libraryRoot) {
        BgvSettings.getBgvDirectory(context, libraryRoot.absolutePath)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Auto-hide status toast after 2.5s
    LaunchedEffect(statusToast) {
        if (statusToast != null) {
            delay(2500L)
            statusToast = null
        }
    }

    // Refresh the favorites list whenever the favorites panel is opened (also covers
    // recomposition after a configuration change while the panel is showing).
    LaunchedEffect(showFavDialog) {
        if (showFavDialog) {
            favoriteSongs = FavoritesStore.getFavorites(context, library.songs)
        }
    }

    DisposableEffect(playing) {
        val window = (context as? Activity)?.window
        if (playing) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(playing) {
        onPlaybackStateChanged(playing)
        if (!playing) {
            engine.pause()
        } else if (audioReady && !engine.isPlaying() && song.code != 0 && !engine.hasEnded()) {
            engine.play()
        }
    }

    LaunchedEffect(song.code, selectCounter) {
        audioReady = false
        audioDurationMs = 0.0
        posMs = 0.0
        posTick = 0L
        if (song.code == 0) {
            audioNote = null
            playing = false
            return@LaunchedEffect
        }
        keyShift = 0
        temShift = 0
        melVol = AudioSettings.getMelodyVolume(context)
        vocalActive = melVol > 0
        engine.setKey(0)
        engine.setTempo(100f)
        engine.setMelodyVolume(melVol)

        withContext(Dispatchers.IO) {
            val sf = SoundFontManager.getOrAutoPickSoundFont(context)
            Log.i("PlayerScreen", "SoundFont: $sf")
            if (sf != null) {
                engine.loadSoundFont(sf)
            }
            val bytes = LyricLoader.midiBytes(song)
            val loaded = bytes != null && engine.loadMidi(bytes)
            audioReady = loaded
            audioNote = if (loaded) null else "No audio for this title — lyrics only"
            if (loaded) {
                val dur = engine.getDurationMs()
                audioDurationMs = dur
                engine.restart()
                engine.seekMs(0.0)
                Log.i("PlayerScreen", "MIDI loaded: ${song.displayTitle()} via $sf, durationMs: $dur")
            } else {
                Log.w("PlayerScreen", "MIDI unavailable: ${song.displayTitle()}")
            }
        }
    }

    // Helper functions for playback & queue actions
    fun handleDigitInput(char: Char) {
        if (char == '0' && codeInput.isNotBlank() && typedSongPreview == null) {
            codeInput = ""
            statusToast = "Code Reset"
        } else if (codeInput.length >= 6) {
            codeInput = (codeInput + char).takeLast(6)
        } else {
            codeInput += char
        }
    }

    // Live view of the selected song + a scope for the idle transition delay.
    // Declared before playNextSongInQueue so the local function can reference them.
    val latestSong by rememberUpdatedState(song)
    val transitionScope = rememberCoroutineScope()

    fun playNextSongInQueue() {
        posMs = 0.0
        posTick = 0L
        scored = false
        pendingScore = -1
        micSession.reset()
        val repeatMode = BehaviorSettings.getRepeatMode(context)
        if (repeatMode == 2 && song.code != 0) {
            // REPEAT_ONE
            engine.restart()
            posMs = 0.0
            posTick = 0L
            scored = false
            playing = true
            replayEpoch++
            statusToast = "Repeating Song: ${song.displayTitle()}"
            return
        }

        if (reservationQueue.isNotEmpty()) {
            val nextSong = reservationQueue.first()
            reservationQueue = reservationQueue.drop(1)
            onSelectSong(nextSong)
            playing = true
            statusToast = "Now Playing: ${nextSong.displayTitle()}"
        } else {
            if (repeatMode == 1) {
                // REPEAT_ALL: favorites or library
                val sourceList = FavoritesStore.getFavorites(context, library.songs).ifEmpty { library.songs.take(50) }
                if (sourceList.isNotEmpty()) {
                    val list = if (BehaviorSettings.isShuffleEnabled(context)) sourceList.shuffled() else sourceList
                    val nextSong = list.first()
                    reservationQueue = list.drop(1)
                    onSelectSong(nextSong)
                    playing = true
                    statusToast = "Repeating Playlist: ${nextSong.displayTitle()}"
                    return
                }
            }
            playing = false
            // Let the finished screen settle for a moment before swapping in the
            // idle placeholder, for a smoother end-of-song transition. The guard
            // re-checks live state so a new selection during the delay wins.
            val stoppedCode = song.code
            transitionScope.launch {
                delay(800)
                if (!playing && pendingScore < 0 && reservationQueue.isEmpty() && latestSong.code == stoppedCode) {
                    onSelectSong(SongItem.placeholder())
                    statusToast = "Playback Stopped — Select a Song"
                }
            }
        }
    }

    /** Computes, persists and (when possible) presents the end-of-song score.
     *  Returns true if the score overlay was scheduled; false means the caller
     *  should advance the queue itself (scoring off, too short, or PiP mode). */
    fun tryPresentScore(): Boolean {
        val mode = ScoreSettings.getScoreMode(context)
        if (scored || mode == ScoreSettings.ScoreMode.OFF || song.code == 0 || posMs <= 3000.0) {
            return false
        }
        val difficulty = ScoreSettings.getDifficulty(context)
        val calculatedScore = if (mode == ScoreSettings.ScoreMode.MICROPHONE) {
            val stats = micSession.finish()
            if (stats != null) {
                ScoreCalculator.computeMicScore(
                    stats.voiceActivityRatio,
                    stats.loudnessConsistency,
                    stats.avgLevelDb,
                    difficulty
                )
            } else {
                // Mic unavailable or take too short — fall back to the simulated score.
                ScoreCalculator.computeMicScore(difficulty)
            }
        } else {
            ScoreCalculator.generateRandomScore(difficulty)
        }
        ScoreStore.record(context, song, effectiveTotalMs, calculatedScore)
        scored = true
        // PiP never composes the overlay; record the score and let the queue advance.
        if (isInPipMode) return false
        pendingScore = calculatedScore
        return true
    }

    fun handleSongFinished() {
        if (!tryPresentScore()) {
            playNextSongInQueue()
        }
    }

    /**
     * Halts the current song and returns to the idle placeholder without restarting the song,
     * advancing the queue, or presenting a score. Used as the fallback for a manual Stop when
     * no score is due.
     */
    fun stopPlayback() {
        engine.stop()
        playing = false
        scored = true
        pendingScore = -1
        micSession.reset()
        onSelectSong(SongItem.placeholder())
        statusToast = "Playback Stopped — Select a Song"
    }

    /**
     * Manual Stop (on-screen button and remote "stop"). With scoring enabled this ends the song
     * like a natural finish so the score is shown; with scoring off it halts and returns to the
     * idle placeholder instead of restarting the song or advancing the queue.
     */
    fun stopCurrentSong() {
        engine.stop()
        playing = false
        if (!tryPresentScore()) {
            stopPlayback()
        }
    }

    // Microphone scoring capture lifecycle: clear stale levels on song change,
    // capture while playing in MICROPHONE mode, and release the mic otherwise.
    LaunchedEffect(song.code, selectCounter) {
        micSession.stopCapturing()
        micSession.reset()
    }
    LaunchedEffect(playing, song.code) {
        if (!playing || song.code == 0) {
            micSession.stopCapturing()
            return@LaunchedEffect
        }
        val mode = ScoreSettings.getScoreMode(context)
        if (mode == ScoreSettings.ScoreMode.MICROPHONE) {
            micSession.start()
        } else {
            micSession.stopCapturing()
        }
    }

    LaunchedEffect(library, song.code, selectCounter) {
        RemoteCommandBus.onCommand = { action, params ->
            when (action) {
                "play" -> {
                    playing = true
                    if (song.code == 0) {
                        if (reservationQueue.isNotEmpty()) {
                            playNextSongInQueue()
                        } else if (codeInput.isNotBlank()) {
                            val code = codeInput.toIntOrNull()
                            val found = if (code != null) library.songs.find { it.code == code } else null
                            if (found != null) {
                                onSelectSong(found)
                                posMs = 0.0
                                scored = false
                                pendingScore = -1
                                playing = true
                                codeInput = ""
                                statusToast = "Playing: ${found.displayTitle()}"
                            }
                        }
                    } else {
                        if (audioReady && !engine.isPlaying()) engine.play()
                    }
                }
                "pause" -> {
                    playing = false
                    engine.pause()
                }
                "stop" -> {
                    stopCurrentSong()
                }
                "next" -> {
                    if (reservationQueue.isNotEmpty()) {
                        playNextSongInQueue()
                    } else {
                        statusToast = "No reserved songs in queue"
                    }
                }
                "prev" -> {
                    engine.restart()
                    engine.seekMs(0.0)
                    posMs = 0.0
                    posTick = 0L
                }
                "cancel" -> {
                    if (codeInput.isNotEmpty()) codeInput = ""
                }
                "reserve" -> {
                    val codeStr = params["code"]
                    val priority = params["priority"] == "true"
                    val code = codeStr?.toIntOrNull()
                    if (code != null) {
                        val found = library.songs.find { it.code == code }
                        if (found != null) {
                            if (priority) {
                                reservationQueue = listOf(found) + reservationQueue
                                statusToast = "1st Priority: ${found.displayTitle()}"
                            } else {
                                reservationQueue = reservationQueue + found
                                statusToast = "Reserved #${code}: ${found.displayTitle()}"
                            }
                            if (song.code == 0) {
                                playNextSongInQueue()
                            }
                        }
                    }
                }
                "play_code" -> {
                    val codeStr = params["code"]
                    val code = codeStr?.toIntOrNull()
                    if (code != null) {
                        val found = library.songs.find { it.code == code }
                        if (found != null) {
                            if (song.code == found.code) {
                                playing = true
                                if (audioReady && !engine.isPlaying()) engine.play()
                            } else {
                                onSelectSong(found)
                            }
                            posMs = 0.0
                            scored = false
                            pendingScore = -1
                            playing = true
                            codeInput = ""
                            statusToast = "Playing: ${found.displayTitle()}"
                        }
                    }
                }
                "remove_reserved" -> {
                    val indexStr = params["index"]
                    val codeStr = params["code"]
                    val index = indexStr?.toIntOrNull()
                    val code = codeStr?.toIntOrNull()
                    if (index != null && index in reservationQueue.indices) {
                        val item = reservationQueue[index]
                        reservationQueue = reservationQueue.toMutableList().apply { removeAt(index) }
                        statusToast = "Removed: ${item.displayTitle()}"
                    } else if (code != null) {
                        val item = reservationQueue.find { it.code == code }
                        if (item != null) {
                            reservationQueue = reservationQueue - item
                            statusToast = "Removed: ${item.displayTitle()}"
                        }
                    }
                }
            }
        }
    }

    fun handleReservePress(firstPriority: Boolean) {
        if (codeInput.isBlank()) {
            statusToast = "Type a song code first"
            return
        }
        val targetCode = codeInput.toIntOrNull()
        val found = if (targetCode != null) library.songs.find { it.code == targetCode } else null
        if (found != null) {
            if (firstPriority) {
                reservationQueue = listOf(found) + reservationQueue
                statusToast = "1st Priority: ${found.displayTitle()}"
            } else {
                reservationQueue = reservationQueue + found
                statusToast = "Reserved #${found.code}: ${found.displayTitle()}"
            }
            codeInput = ""
            if (song.code == 0) {
                playNextSongInQueue()
            }
        } else {
            statusToast = "Song #${codeInput} not found"
            codeInput = ""
        }
    }

    fun handlePlayPress() {
        if (codeInput.isNotBlank()) {
            val targetCode = codeInput.toIntOrNull()
            val found = if (targetCode != null) library.songs.find { it.code == targetCode } else null
            if (found != null) {
                if (song.code == found.code) {
                    playing = !playing
                } else {
                    onSelectSong(found)
                    posMs = 0.0
                    scored = false
                    pendingScore = -1
                    playing = true
                }
                codeInput = ""
                statusToast = if (playing) "Playing: ${found.displayTitle()}" else "Paused"
            } else {
                statusToast = "Song #${codeInput} not found"
                codeInput = ""
            }
        } else {
            if (song.code == 0 && reservationQueue.isNotEmpty()) {
                playNextSongInQueue()
            } else if (song.code != 0) {
                playing = !playing
                statusToast = if (playing) "Playing" else "Paused"
            }
        }
    }

    fun handleStopPress() {
        stopCurrentSong()
    }

    LaunchedEffect(playing, audioReady, effectiveTotalMs, song.code, ticksPerBeat, tempoMap, selectCounter, replayEpoch) {
        while (playing && song.code != 0) {
            if (audioReady && !seeking && !engine.isPlaying() && !engine.hasEnded()) {
                engine.play()
            }
            delay(if (audioReady) AUDIO_POLL_MS else 33L)
            if (!playing) break
            if (audioReady) {
                if (!seeking) {
                    val tick = engine.getTickPosition()
                    if (tick >= 0) {
                        posTick = tick
                        // Derive file-time ms from the file-tick via the tempo map so that
                        // posMs stays in the same domain as lyric startMs/endMs/pipMs.
                        // engine.getPositionMs() is byte->seconds and is scaled by
                        // BASS_ATTRIB_MIDI_SPEED (TEM), which desyncs the lyric wipe guards.
                        posMs = if (tempoMap.isNotEmpty() && ticksPerBeat > 0) {
                            TempoUtils.tickToMs(tempoMap, tick, ticksPerBeat)
                        } else {
                            val ms = engine.getPositionMs()
                            if (ms > 0.0) ms else posMs
                        }
                    }
                }
                val ended = engine.hasEnded() || (effectiveTotalMs > 0.0 && posMs >= effectiveTotalMs)
                if (ended) {
                    engine.stop()
                    playing = false
                    // Run synchronously: a suspension here would be cancelled when
                    // playing flips to false (a key of this effect), so the score
                    // presentation / queue advance would never fire. The ~1s beat
                    // before the score reveal now lives inside ScoreOverlay.
                    handleSongFinished()
                    break
                }
            } else {
                posMs += 33.0
                if (effectiveTotalMs > 0.0 && posMs >= effectiveTotalMs) {
                    engine.stop()
                    playing = false
                    handleSongFinished()
                    break
                }
            }
        }
    }

    val lineIndex = remember(lines, posMs) {
        if (lines.isEmpty()) {
            0
        } else {
            var idx = 0
            for (i in lines.indices) {
                if (posMs >= lines[i].startMs - 1.0) idx = i else break
            }
            idx
        }
    }

    val inBreak = !isInstrumental && instrumentalBreaks.any { posMs in it }

    if (isInPipMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (lines.isNotEmpty()) {
                    if (lyricStyle == LyricSettings.STYLE_KARAOKE) {
                        KaraokeStack(
                            lines = lines,
                            lineIndex = lineIndex,
                            posMs = posMs,
                            currentTick = posTick,
                            fillColor = KaraokeHighlight,
                            fontSizeSp = LyricSettings.getFontSizePortrait(context)
                        )
                    } else {
                        val current = lines.getOrNull(lineIndex)
                        Text(
                            text = current?.fullText() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KaraokeHighlight,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        return
    }

    val isAnyDialogActive = showSearchDialog || showRsvDialog || showQrDialog || showFavDialog
    val focusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }
    val digitFocusRequesters = remember { ('0'..'9').associateWith { FocusRequester() } }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val nativeKey = event.nativeKeyEvent.keyCode
                    if (isAnyDialogActive && nativeKey != KeyEvent.KEYCODE_BACK) {
                        return@onKeyEvent false
                    }
                    val digit = when (nativeKey) {
                        KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> '0'
                        KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> '1'
                        KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> '2'
                        KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> '3'
                        KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> '4'
                        KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> '5'
                        KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> '6'
                        KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> '7'
                        KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> '8'
                        KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> '9'
                        else -> null
                    }
                    if (digit != null) {
                        handleDigitInput(digit)
                        controlsVisible = true
                        coroutineScope.launch {
                            delay(120L)
                            runCatching { digitFocusRequesters[digit]?.requestFocus() }
                        }
                        true
                    } else if (
                        nativeKey == KeyEvent.KEYCODE_DPAD_UP ||
                        nativeKey == KeyEvent.KEYCODE_DPAD_DOWN ||
                        nativeKey == KeyEvent.KEYCODE_DPAD_LEFT ||
                        nativeKey == KeyEvent.KEYCODE_DPAD_RIGHT ||
                        nativeKey == KeyEvent.KEYCODE_DPAD_CENTER ||
                        nativeKey == KeyEvent.KEYCODE_ENTER
                    ) {
                        if (!controlsVisible) {
                            controlsVisible = true
                            coroutineScope.launch {
                                delay(120L)
                                runCatching { searchFocusRequester.requestFocus() }
                            }
                            true
                        } else {
                            false
                        }
                    } else if (nativeKey == KeyEvent.KEYCODE_BACK) {
                        if (isAnyDialogActive) {
                            if (showSearchDialog) showSearchDialog = false
                            else if (showRsvDialog) showRsvDialog = false
                            else if (showQrDialog) showQrDialog = false
                            else if (showFavDialog) showFavDialog = false
                        } else if (codeInput.isNotEmpty()) {
                            codeInput = ""
                        } else if (controlsVisible) {
                            controlsVisible = false
                        } else {
                            val now = System.currentTimeMillis()
                            if (now - lastBackPressTime < 2000L) {
                                onHome()
                            } else {
                                lastBackPressTime = now
                                statusToast = "Press back again to exit to main menu"
                            }
                        }
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF16222E), MaterialTheme.colorScheme.background)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!isAnyDialogActive) {
                            controlsVisible = !controlsVisible
                        }
                    }
                )
            }
    ) {
        BackgroundVideoPlayer(
            bgvDir = bgvDir,
            bgvIndex = bgvIndex,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isLandscape) 0.55f else 0.45f))
        )

        // Toast placed later in tree so it renders on top


        if (isLandscape) {
            // Content stays centered, but slides left to make room for the right-docked
            // keyboard while it is visible. Once the keyboard hides, it recenters.
            val reservedEnd = animateDpAsState(
                targetValue = when {
                    showQrDialog -> 420.dp + 24.dp
                    showSearchDialog -> 420.dp + 24.dp
                    showRsvDialog -> 420.dp + 24.dp
                    showFavDialog -> 420.dp + 24.dp
                    controlsVisible -> CustomKeyboardWidth + 24.dp
                    else -> 24.dp
                },
                label = "keyboardInset"
            ).value
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = reservedEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopSongHeader(
                    song = song,
                    reservationQueue = reservationQueue,
                    codeInput = codeInput,
                    typedSongPreview = typedSongPreview,
                    isInstrumental = isInstrumental,
                    inBreak = inBreak,
                    isLandscape = true
                )

                Spacer(Modifier.height(8.dp))

                val lyricPlacement = remember(context) { LyricSettings.getLyricPlacement(context) }
                val firstLineStart = lines.firstOrNull()?.startMs ?: 6000.0
                val showSongIntroCard = song.code != 0 && posMs < minOf(5000.0, firstLineStart - 300.0)
                val isSongActive = song.code != 0 && !showSongIntroCard && lines.isNotEmpty()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = if (isSongActive && lyricPlacement == LyricSettings.PLACEMENT_BOTTOM) Alignment.BottomCenter else Alignment.Center
                ) {
                    PlayerCenterContent(
                        song = song,
                        codeInput = codeInput,
                        typedSongPreview = typedSongPreview,
                        lines = lines,
                        countdowns = countdowns,
                        posMs = posMs,
                        currentTick = posTick,
                        lineIndex = lineIndex,
                        lyricStyle = lyricStyle,
                        isInstrumental = isInstrumental,
                        inBreak = inBreak,
                        isPortrait = false,
                        context = context
                    )
                }
            }
        } else {
            val isPortraitDialogActive = showSearchDialog || showRsvDialog || showQrDialog || showFavDialog
            val reservedBottom = animateDpAsState(
                targetValue = if (isPortraitDialogActive) 320.dp else 16.dp,
                label = "portraitBottomInset"
            ).value

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = reservedBottom),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopSongHeader(
                    song = song,
                    reservationQueue = reservationQueue,
                    codeInput = codeInput,
                    typedSongPreview = typedSongPreview,
                    isInstrumental = isInstrumental,
                    inBreak = inBreak,
                    isLandscape = false
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .then(if (isPortraitDialogActive) Modifier.padding(top = 16.dp) else Modifier)
                        .onSizeChanged { portraitLyricHeightPx = it.height },
                    contentAlignment = if (isPortraitDialogActive) Alignment.TopCenter else Alignment.Center
                ) {
                    PlayerCenterContent(
                        song = song,
                        codeInput = codeInput,
                        typedSongPreview = typedSongPreview,
                        lines = lines,
                        countdowns = countdowns,
                        posMs = posMs,
                        currentTick = posTick,
                        lineIndex = lineIndex,
                        lyricStyle = lyricStyle,
                        isInstrumental = isInstrumental,
                        inBreak = inBreak,
                        isPortrait = true,
                        context = context,
                        availableHeightDp = if (portraitLyricHeightPx > 0) {
                            with(LocalDensity.current) { portraitLyricHeightPx.toDp() }
                        } else null
                    )
                }

                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = controlsVisible && !isAnyDialogActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CustomKeyboard(
                        codeInput = codeInput,
                        typedSongPreview = typedSongPreview,
                        keyShift = keyShift,
                        temShift = temShift,
                        melVol = melVol,
                        reservationCount = reservationQueue.size,
                        playing = playing,
                        vocalActive = vocalActive,
                        onKeyChange = { keyShift = it.coerceIn(KEY_SHIFT_MIN, KEY_SHIFT_MAX); engine.setKey(keyShift) },
                        onTemChange = { temShift = it.coerceIn(TEMPO_SHIFT_MIN, TEMPO_SHIFT_MAX); engine.setTempo((100 + temShift * 5).toFloat()) },
                        onMelChange = applyMelodyVolume,
                        onDigitPress = { char -> handleDigitInput(char) },
                        onQrPress = {
                            showQrDialog = true
                            controlsVisible = false
                        },
                        onCancelPress = {
                            if (codeInput.isNotEmpty()) codeInput = "" else controlsVisible = false
                        },
                        onReservePress = { firstPriority -> handleReservePress(firstPriority) },
                        onSearchPress = {
                            showSearchDialog = true
                            controlsVisible = false
                        },
                        onRsvListPress = {
                            showRsvDialog = true
                            controlsVisible = false
                        },
                        onPlayPress = { handlePlayPress() },
                        onStopPress = { handleStopPress() },
                        onNextPress = {
                            if (reservationQueue.isNotEmpty()) {
                                playNextSongInQueue()
                            } else {
                                engine.restart()
                                engine.seekMs(0.0)
                                posMs = 0.0
                                posTick = 0L
                                statusToast = "No reserved songs in queue"
                            }
                        },
                        onPrevPress = {
                            engine.restart()
                            engine.seekMs(0.0)
                            posMs = 0.0
                            posTick = 0L
                        },
                        onVocalToggle = {
                            toggleVocal()
                            statusToast = if (vocalActive) "Vocal: ON" else "Vocal: OFF"
                        },
                        onFavPress = {
                            favoriteSongs = FavoritesStore.getFavorites(context, library.songs)
                            showFavDialog = true
                            controlsVisible = false
                        },
                        onBgvToggle = {
                            bgvIndex++
                            statusToast = "BGV Switched"
                        },
                        searchFocusRequester = searchFocusRequester,
                        digitFocusRequesters = digitFocusRequesters,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Landscape-only custom keyboard docked to lower right corner. It floats over the
        // lower right region and never covers the lyrics on the left or top right.
        if (isLandscape) {
            AnimatedVisibility(
                visible = controlsVisible && !isAnyDialogActive,
                enter = fadeIn() + slideInHorizontally { fullWidth -> fullWidth },
                exit = fadeOut() + slideOutHorizontally { fullWidth -> fullWidth },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                CustomKeyboard(
                    codeInput = codeInput,
                    typedSongPreview = typedSongPreview,
                    keyShift = keyShift,
                    temShift = temShift,
                    melVol = melVol,
                    reservationCount = reservationQueue.size,
                    playing = playing,
                    vocalActive = vocalActive,
                    onKeyChange = { keyShift = it.coerceIn(KEY_SHIFT_MIN, KEY_SHIFT_MAX); engine.setKey(keyShift) },
                    onTemChange = { temShift = it.coerceIn(TEMPO_SHIFT_MIN, TEMPO_SHIFT_MAX); engine.setTempo((100 + temShift * 5).toFloat()) },
                    onMelChange = applyMelodyVolume,
                    onDigitPress = { char -> handleDigitInput(char) },
                    onQrPress = {
                        showQrDialog = true
                        controlsVisible = false
                    },
                    onCancelPress = {
                        if (codeInput.isNotEmpty()) codeInput = "" else controlsVisible = false
                    },
                    onReservePress = { firstPriority -> handleReservePress(firstPriority) },
                    onSearchPress = {
                        showSearchDialog = true
                        controlsVisible = false
                    },
                    onRsvListPress = {
                        showRsvDialog = true
                        controlsVisible = false
                    },
                    onPlayPress = { handlePlayPress() },
                    onStopPress = { handleStopPress() },
                    onNextPress = {
                        if (reservationQueue.isNotEmpty()) {
                            playNextSongInQueue()
                        } else {
                            engine.restart()
                            engine.seekMs(0.0)
                            posMs = 0.0
                            posTick = 0L
                            statusToast = "No reserved songs in queue"
                        }
                    },
                    onPrevPress = {
                        engine.restart()
                        engine.seekMs(0.0)
                        posMs = 0.0
                        posTick = 0L
                    },
                    onVocalToggle = {
                        toggleVocal()
                        statusToast = if (vocalActive) "Vocal: ON" else "Vocal: OFF"
                    },
                    onFavPress = {
                        favoriteSongs = FavoritesStore.getFavorites(context, library.songs)
                        showFavDialog = true
                        controlsVisible = false
                    },
                    onBgvToggle = {
                        bgvIndex++
                        statusToast = "BGV Switched"
                    },
                    searchFocusRequester = searchFocusRequester,
                    digitFocusRequesters = digitFocusRequesters,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .focusProperties { canFocus = !(showSearchDialog || showRsvDialog || showQrDialog || showFavDialog) }
                        .width(CustomKeyboardWidth)
                        .padding(end = 12.dp, bottom = 12.dp)
                )
            }
        }

        if (pendingScore >= 0) {
            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                ScoreOverlay(
                    score = pendingScore,
                    songTitle = song.displayTitle(),
                    songSinger = if (song.code == 0) "000000" else song.displaySinger(),
                    onSingAgain = {
                        pendingScore = -1
                        micSession.reset()
                        engine.restart()
                        posMs = 0.0
                        scored = false
                        playing = true
                    },
                    onDismiss = {
                        pendingScore = -1
                        playNextSongInQueue()
                    }
                )
            }
        }

        if (showSearchDialog) {
            QuickSearchDialog(
                songs = library.songs,
                onDismiss = { showSearchDialog = false },
                onPlaySong = { s ->
                    onSelectSong(s)
                    posMs = 0.0
                    scored = false
                    pendingScore = -1
                    playing = true
                    showSearchDialog = false
                    statusToast = "Now Playing: ${s.displayTitle()}"
                },
                onReserveSong = { s ->
                    reservationQueue = reservationQueue + s
                    statusToast = "Reserved: ${s.displayTitle()}"
                    if (song.code == 0) {
                        playNextSongInQueue()
                    }
                }
            )
        }

        if (showQrDialog) {
            QrCodeDialog(
                onDismiss = { showQrDialog = false }
            )
        }

        if (showRsvDialog) {
            ReservationListDialog(
                queue = reservationQueue,
                onDismiss = { showRsvDialog = false },
                onPlayNow = { s ->
                    reservationQueue = reservationQueue - s
                    onSelectSong(s)
                    posMs = 0.0
                    scored = false
                    pendingScore = -1
                    playing = true
                    showRsvDialog = false
                    statusToast = "Now Playing: ${s.displayTitle()}"
                },
                onMoveUp = { idx ->
                    if (idx > 0) {
                        val mutable = reservationQueue.toMutableList()
                        val item = mutable.removeAt(idx)
                        mutable.add(idx - 1, item)
                        reservationQueue = mutable
                    }
                },
                onRemove = { s ->
                    reservationQueue = reservationQueue - s
                    statusToast = "Removed from Queue"
                },
                onClearQueue = {
                    reservationQueue = emptyList()
                    statusToast = "Queue Cleared"
                }
            )
        }

        if (showFavDialog) {
            FavoritesDialog(
                songs = favoriteSongs,
                onDismiss = { showFavDialog = false },
                onPlayNow = { s ->
                    reservationQueue = reservationQueue - s
                    onSelectSong(s)
                    posMs = 0.0
                    scored = false
                    pendingScore = -1
                    playing = true
                    showFavDialog = false
                    statusToast = "Now Playing: ${s.displayTitle()}"
                },
                onReserveSong = { s ->
                    reservationQueue = reservationQueue + s
                    statusToast = "Reserved: ${s.displayTitle()}"
                    if (song.code == 0) {
                        playNextSongInQueue()
                    }
                },
                onRemoveFavorite = { s ->
                    FavoritesStore.toggleFavorite(context, s.code)
                    favoriteSongs = FavoritesStore.getFavorites(context, library.songs)
                    statusToast = "Removed from Favorites"
                }
            )
        }

        // Floating Draggable MeowsiCat (visible when controls/keypad are hidden and enabled in settings)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val floatingCatEnabled = BehaviorSettings.isFloatingCatEnabled(context)
            FloatingMeowsiCat(
                visible = !controlsVisible && floatingCatEnabled,
                onClick = { controlsVisible = true }
            )
        }

        if (statusToast != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                StatusToastNotification(message = statusToast!!)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FloatingMeowsiCat(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val catWidthPx = with(density) { 58.dp.toPx() }
    val catHeightPx = with(density) { 64.dp.toPx() }
    val paddingPx = with(density) { 32.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()

        val minX = 0f
        val maxX = (maxWidth - catWidthPx).coerceAtLeast(0f)
        val minY = 0f
        val maxY = (maxHeight - catHeightPx - 8f).coerceAtLeast(0f)

        val defaultX = (maxWidth - catWidthPx - paddingPx).coerceIn(minX, maxX)
        val defaultY = (maxHeight - catHeightPx - paddingPx).coerceIn(minY, maxY)

        var offsetX by remember(maxWidth, maxHeight) {
            mutableFloatStateOf(defaultX)
        }
        var offsetY by remember(maxWidth, maxHeight) {
            mutableFloatStateOf(defaultY)
        }

        val frames = remember {
            intArrayOf(
                R.drawable.cat_01, R.drawable.cat_01, R.drawable.cat_02, R.drawable.cat_03,
                R.drawable.cat_04, R.drawable.cat_05, R.drawable.cat_06, R.drawable.cat_07,
                R.drawable.cat_08, R.drawable.cat_09, R.drawable.cat_10, R.drawable.cat_11,
                R.drawable.cat_12, R.drawable.cat_13, R.drawable.cat_14, R.drawable.cat_15,
                R.drawable.cat_16, R.drawable.cat_17, R.drawable.cat_18, R.drawable.cat_19,
                R.drawable.cat_20
            )
        }
        var frameIndex by remember { mutableIntStateOf(0) }
        LaunchedEffect(visible) {
            if (visible) {
                while (true) {
                    delay(120L)
                    frameIndex = (frameIndex + 1) % frames.size
                }
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "catFloat")
        val bounce by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )
        val wiggle by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wiggle"
        )

        val chipShape = RoundedCornerShape(20.dp)
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), (offsetY + bounce).roundToInt()) }
                    .graphicsLayer(rotationZ = wiggle)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offsetX = (offsetX + dragAmount.x).coerceIn(minX, maxX)
                            offsetY = (offsetY + dragAmount.y).coerceIn(minY, maxY)
                        }
                    }
                    .size(width = 58.dp, height = 64.dp)
                    .shadow(14.dp, chipShape, spotColor = Color(0xFF0D9488))
                    .clip(chipShape)
                    .background(Color(0xFF022C28).copy(alpha = 0.8f))
                    .border(3.dp, Color(0xFFFBBF24), chipShape)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(frames[frameIndex]),
                    contentDescription = "MeowsiCat",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            }
        }
    }
}

private val reserveColors = listOf(
    Color(0xFF2DD4BF), // Teal
    Color(0xFFFBBF24), // Amber / Gold
    Color(0xFFF472B6), // Pink
    Color(0xFFA78BFA), // Purple
    Color(0xFF34D399), // Emerald
    Color(0xFF60A5FA), // Blue
    Color(0xFFFB923C), // Orange
    Color(0xFFE879F9)  // Fuchsia
)

@Composable
private fun TopSongHeader(
    song: SongItem,
    reservationQueue: List<SongItem>,
    codeInput: String = "",
    typedSongPreview: SongItem? = null,
    isInstrumental: Boolean = false,
    inBreak: Boolean = false,
    isLandscape: Boolean
) {
    val context = LocalContext.current
    var isFav by remember(song.code) {
        mutableStateOf(FavoritesStore.isFavorite(context, song.code))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (song.code != 0) {
                        IconButton(
                            onClick = {
                                isFav = FavoritesStore.toggleFavorite(context, song.code)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color(0xFFF472B6) else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = song.displayTitle(),
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                val subtitleString = remember(song, reservationQueue) {
                    buildAnnotatedString {
                        append(song.displayCode())
                        if (reservationQueue.isNotEmpty()) {
                            append(" • R: ")
                            append(reservationQueue.size.toString())
                            append(" | ")
                            reservationQueue.forEachIndexed { index, reservedSong ->
                                if (index > 0) append(" ")
                                val color = reserveColors[index % reserveColors.size]
                                pushStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold))
                                append(reservedSong.displayCode())
                                pop()
                            }
                        }
                    }
                }
                Text(
                    text = subtitleString,
                    style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusToastNotification(message: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
        tonalElevation = 12.dp,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

/**
 * Right-docked custom keyboard for the landscape player. It overlays the reserved right
 * region only, so the lyrics on the left are never covered. Contains the numeric keypad
 * for song-code entry plus the core karaoke actions.
 */
@Composable
private fun CustomKeyboard(
    codeInput: String,
    typedSongPreview: SongItem?,
    keyShift: Int,
    temShift: Int,
    melVol: Int,
    reservationCount: Int,
    playing: Boolean,
    vocalActive: Boolean,
    onKeyChange: (Int) -> Unit,
    onTemChange: (Int) -> Unit,
    onMelChange: (Int) -> Unit,
    onDigitPress: (Char) -> Unit,
    onQrPress: () -> Unit,
    onCancelPress: () -> Unit,
    onReservePress: (firstPriority: Boolean) -> Unit,
    onSearchPress: () -> Unit,
    onRsvListPress: () -> Unit,
    onPlayPress: () -> Unit,
    onStopPress: () -> Unit,
    onNextPress: () -> Unit,
    onPrevPress: () -> Unit,
    onVocalToggle: () -> Unit,
    onFavPress: () -> Unit,
    onBgvToggle: () -> Unit,
    searchFocusRequester: FocusRequester? = null,
    digitFocusRequesters: Map<Char, FocusRequester> = emptyMap(),
    shape: Shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.35f)),
        tonalElevation = 16.dp,
        modifier = modifier.pointerInput(Unit) { detectTapGestures { } }
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Code readout
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(
                        text = if (codeInput.isBlank()) "— — — — — —" else codeInput.padStart(6, '0'),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (codeInput.isBlank()) Color.White.copy(alpha = 0.35f) else Color(0xFFFBBF24)
                    )
                    Text(
                        text = when {
                            codeInput.isBlank() -> "Type a song code"
                            typedSongPreview != null -> "${typedSongPreview.displayTitle()} • ${typedSongPreview.displaySinger()}"
                            codeInput.length >= 6 -> "No song found"
                            else -> "Searching..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (typedSongPreview != null) accent else Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Key / Tempo / Melody adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdjustmentPill(
                    label = "K ${if (keyShift >= 0) "+$keyShift" else "$keyShift"}",
                    containerColor = Color(0xFF854D0E).copy(alpha = 0.85f),
                    onDecrease = { onKeyChange(keyShift - 1) },
                    onIncrease = { onKeyChange(keyShift + 1) },
                    decreaseEnabled = keyShift > KEY_SHIFT_MIN,
                    increaseEnabled = keyShift < KEY_SHIFT_MAX,
                    modifier = Modifier.weight(1f)
                )
                AdjustmentPill(
                    label = "T ${if (temShift >= 0) "+$temShift" else "$temShift"}",
                    containerColor = Color(0xFF065F46).copy(alpha = 0.85f),
                    onDecrease = { onTemChange(temShift - 1) },
                    onIncrease = { onTemChange(temShift + 1) },
                    decreaseEnabled = temShift > TEMPO_SHIFT_MIN,
                    increaseEnabled = temShift < TEMPO_SHIFT_MAX,
                    modifier = Modifier.weight(1f)
                )
                AdjustmentPill(
                    label = "M $melVol",
                    containerColor = primaryContainer.copy(alpha = 0.85f),
                    onDecrease = { onMelChange(maxOf(0, melVol - 20)) },
                    onIncrease = { onMelChange(minOf(100, melVol + 20)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Main keypad: numeric grid on the left, function column on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DigitKey('1', onDigitPress, digitFocusRequesters['1'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('2', onDigitPress, digitFocusRequesters['2'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('3', onDigitPress, digitFocusRequesters['3'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DigitKey('4', onDigitPress, digitFocusRequesters['4'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('5', onDigitPress, digitFocusRequesters['5'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('6', onDigitPress, digitFocusRequesters['6'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DigitKey('7', onDigitPress, digitFocusRequesters['7'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('8', onDigitPress, digitFocusRequesters['8'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        DigitKey('9', onDigitPress, digitFocusRequesters['9'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton(
                            onClick = { onReservePress(false) },
                            backgroundColor = accent,
                            focusedColor = secondary,
                            modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                        ) {
                            Text("RES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = onPrimary)
                        }
                        DigitKey('0', onDigitPress, digitFocusRequesters['0'], Modifier.weight(1f), keyHeight = KeyboardKeyHeight)
                        KeypadButton(
                            onClick = onCancelPress,
                            backgroundColor = Color(0xFF991B1B).copy(alpha = 0.85f),
                            focusedColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                        ) {
                            Text("CAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                Column(
                    modifier = Modifier.width(104.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeypadButton(
                        onClick = onQrPress,
                        backgroundColor = Color(0xFF0284C7).copy(alpha = 0.9f),
                        focusedColor = Color(0xFF38BDF8),
                        modifier = Modifier.fillMaxWidth().height(KeyboardKeyHeight)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCode,
                                contentDescription = "QR Code",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("QR", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton(
                            onClick = onFavPress,
                            backgroundColor = Color(0xFF9D174D).copy(alpha = 0.9f),
                            focusedColor = Color(0xFFF472B6),
                            modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorites",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        KeypadButton(
                            onClick = onVocalToggle,
                            backgroundColor = if (vocalActive) primaryContainer else primaryContainer.copy(alpha = 0.85f),
                            focusedColor = accent,
                            modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                        ) {
                            Icon(
                                imageVector = if (vocalActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                                contentDescription = "Vocal",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    KeypadButton(
                        onClick = onBgvToggle,
                        backgroundColor = primaryContainer.copy(alpha = 0.85f),
                        focusedColor = accent,
                        modifier = Modifier.fillMaxWidth().height(KeyboardKeyHeight)
                    ) {
                        Icon(Icons.Filled.Camera, contentDescription = "BGV Toggle", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    KeypadButton(
                        onClick = { onReservePress(true) },
                        backgroundColor = Color(0xFF059669),
                        focusedColor = Color(0xFF10B981),
                        modifier = Modifier.fillMaxWidth().height(KeyboardKeyHeight)
                    ) {
                        Text("1st RSV", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Transport + search row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton(
                    onClick = onSearchPress,
                    backgroundColor = Color(0xFF0D9488).copy(alpha = 0.85f),
                    focusedColor = accent,
                    modifier = Modifier
                        .weight(1f)
                        .height(KeyboardKeyHeight)
                        .then(if (searchFocusRequester != null) Modifier.focusRequester(searchFocusRequester) else Modifier)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                KeypadButton(
                    onClick = onRsvListPress,
                    backgroundColor = Color(0xFF0D9488),
                    focusedColor = accent,
                    modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                ) {
                    Text(
                        text = if (reservationCount > 0) "RSV ($reservationCount)" else "RSV",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                KeypadButton(
                    onClick = onPrevPress,
                    backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = accent,
                    modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                ) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                KeypadButton(
                    onClick = onPlayPress,
                    backgroundColor = accent,
                    focusedColor = Color(0xFF5EEAD4),
                    modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                ) {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color(0xFF003731),
                        modifier = Modifier.size(22.dp)
                    )
                }
                KeypadButton(
                    onClick = onNextPress,
                    backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = accent,
                    modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                KeypadButton(
                    onClick = onStopPress,
                    backgroundColor = Color(0xFF991B1B).copy(alpha = 0.85f),
                    focusedColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f).height(KeyboardKeyHeight)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdjustmentPill(
    label: String,
    containerColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    decreaseEnabled: Boolean = true,
    increaseEnabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .padding(horizontal = 2.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KeypadButton(
            onClick = onDecrease,
            enabled = decreaseEnabled,
            backgroundColor = Color.Transparent,
            focusedColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = if (decreaseEnabled) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        KeypadButton(
            onClick = onIncrease,
            enabled = increaseEnabled,
            backgroundColor = Color.Transparent,
            focusedColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = if (increaseEnabled) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun DigitKey(
    digit: Char,
    onDigitPress: (Char) -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
    keyHeight: Dp? = 38.dp
) {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val accent = MaterialTheme.colorScheme.primary
    KeypadButton(
        onClick = { onDigitPress(digit) },
        backgroundColor = primaryContainer.copy(alpha = 0.85f),
        focusedColor = accent,
        modifier = modifier
            .then(if (keyHeight != null) Modifier.height(keyHeight) else Modifier)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6FFFA)
        )
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
    focusedColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    // Interaction source drives the D-pad focus highlight so the same treatment works whether
    // the key is selected by touch or by a TV remote.
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) focusedColor else backgroundColor,
        label = "keypadContainer"
    )
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        label = "keypadScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        label = "keypadBorder"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 3.dp else 1.dp,
        label = "keypadBorderWidth"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer(scaleX = focusScale, scaleY = focusScale)
            .then(
                if (isFocused) {
                    Modifier.shadow(14.dp, shape, spotColor = focusedColor)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = containerColor,
        contentColor = Color.White,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = if (isFocused) 8.dp else 4.dp,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun DialogActionButton(
    onClick: () -> Unit,
    defaultBg: Color,
    focusedBg: Color = MaterialTheme.colorScheme.primary,
    focusedBorderColor: Color = Color.White,
    modifier: Modifier = Modifier.size(30.dp),
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        targetValue = if (isFocused) focusedBg else defaultBg,
        label = "actionBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        label = "actionScale"
    )

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(if (isFocused) 8.dp else 0.dp, shape)
            .clip(shape)
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) focusedBorderColor else Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun QuickSearchDialog(
    songs: List<SongItem>,
    onDismiss: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onReserveSong: (SongItem) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    val filtered = remember(songs, query) {
        if (query.isBlank()) songs.take(40)
        else songs.filter {
            it.title.contains(query, ignoreCase = true) || it.singer.contains(query, ignoreCase = true) || it.code.toString().contains(query)
        }.take(40)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTv = rememberIsTvDevice()
    val isRightPanel = isLandscape || isTv

    LaunchedEffect(Unit) {
        delay(150L)
        try {
            searchFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = if (isRightPanel) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        Surface(
            modifier = if (isRightPanel) {
                Modifier
                    .width(420.dp)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            },
            shape = if (isRightPanel) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            tonalElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Song Search",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    DialogActionButton(
                        onClick = onDismiss,
                        defaultBg = Color.White.copy(alpha = 0.15f),
                        focusedBg = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    placeholder = { Text("Type title, artist, or code...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filtered, key = { it.code }) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.displayTitle(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${song.displaySinger()} • #${song.displayCode()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var isFav by remember(song.code) {
                                    mutableStateOf(FavoritesStore.isFavorite(context, song.code))
                                }
                                DialogActionButton(
                                    onClick = {
                                        isFav = FavoritesStore.toggleFavorite(context, song.code)
                                    },
                                    defaultBg = Color(0xFF334155),
                                    focusedBg = Color(0xFFEC4899),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFF472B6) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // "P" Play Button
                                DialogActionButton(
                                    onClick = { onPlaySong(song) },
                                    defaultBg = Color(0xFF059669),
                                    focusedBg = Color(0xFF10B981),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Text(
                                        text = "P",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                // "R" Reserve Button
                                DialogActionButton(
                                    onClick = { onReserveSong(song) },
                                    defaultBg = Color(0xFF2563EB),
                                    focusedBg = Color(0xFF3B82F6),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Text(
                                        text = "R",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationListDialog(
    queue: List<SongItem>,
    onDismiss: () -> Unit,
    onPlayNow: (SongItem) -> Unit,
    onMoveUp: (Int) -> Unit,
    onRemove: (SongItem) -> Unit,
    onClearQueue: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTv = rememberIsTvDevice()
    val isRightPanel = isLandscape || isTv

    val closeFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100L)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = if (isRightPanel) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        Surface(
            modifier = if (isRightPanel) {
                Modifier
                    .width(420.dp)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            },
            shape = if (isRightPanel) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header (Top Right Corner: X Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reserved Songs (${queue.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    DialogActionButton(
                        onClick = onDismiss,
                        defaultBg = Color.White.copy(alpha = 0.15f),
                        focusedBg = Color(0xFFEF4444),
                        modifier = Modifier
                            .size(36.dp)
                            .focusRequester(closeFocusRequester),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Reserved Songs List Body
                if (queue.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reserved songs in queue",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(queue, key = { index, song -> "${song.code}_$index" }) { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.width(38.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.displayTitle(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.displaySinger()} • #${song.displayCode()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF059669))
                                            .clickable { onPlayNow(song) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play Now",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (index > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF334155))
                                                .clickable { onMoveUp(index) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "▲",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF991B1B))
                                            .clickable { onRemove(song) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Row (Lower Right Corner: Clear Queue Button)
                if (queue.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onClearQueue,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF991B1B),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Clear Queue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesPanelContent(
    songs: List<SongItem>,
    onDismiss: () -> Unit,
    onPlayNow: (SongItem) -> Unit,
    onReserveSong: (SongItem) -> Unit,
    onRemoveFavorite: (SongItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val closeFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100L)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites (${songs.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            DialogActionButton(
                onClick = onDismiss,
                defaultBg = Color.White.copy(alpha = 0.15f),
                focusedBg = Color(0xFFEF4444),
                modifier = Modifier
                    .size(36.dp)
                    .focusRequester(closeFocusRequester),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Clear, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorite songs yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(songs, key = { it.code }) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.displayTitle(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${song.displaySinger()} • #${song.displayCode()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Heart: remove this song from favorites
                            DialogActionButton(
                                onClick = { onRemoveFavorite(song) },
                                defaultBg = Color(0xFF334155),
                                focusedBg = Color(0xFFEC4899),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Remove from Favorites",
                                    tint = Color(0xFFF472B6),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // "P" Play now
                            DialogActionButton(
                                onClick = { onPlayNow(song) },
                                defaultBg = Color(0xFF059669),
                                focusedBg = Color(0xFF10B981),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Text(
                                    text = "P",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            // "R" Reserve
                            DialogActionButton(
                                onClick = { onReserveSong(song) },
                                defaultBg = Color(0xFF2563EB),
                                focusedBg = Color(0xFF3B82F6),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Text(
                                    text = "R",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Landscape / TV: a panel docked on the right so the lyrics stay visible on the left.
 * (In portrait the panel is rendered inline at the bottom of the layout instead — see
 * the portrait branch of LyricPlayer — so it reserves space rather than covering the lyrics.)
 */
@Composable
private fun FavoritesDialog(
    songs: List<SongItem>,
    onDismiss: () -> Unit,
    onPlayNow: (SongItem) -> Unit,
    onReserveSong: (SongItem) -> Unit,
    onRemoveFavorite: (SongItem) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTv = rememberIsTvDevice()
    val isRightPanel = isLandscape || isTv

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = if (isRightPanel) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        Surface(
            modifier = if (isRightPanel) {
                Modifier
                    .width(420.dp)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            },
            shape = if (isRightPanel) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            tonalElevation = 16.dp
        ) {
            FavoritesPanelContent(
                songs = songs,
                onDismiss = onDismiss,
                onPlayNow = onPlayNow,
                onReserveSong = onReserveSong,
                onRemoveFavorite = onRemoveFavorite,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LyricStack(lines: List<LyricLine>, lineIndex: Int) {
    val context = LocalContext.current
    val fontFamily = rememberLyricFontFamily()
    val outlineDp = remember(context) { LyricSettings.getOutlineThickness(context) }
    val outlinePx = with(LocalDensity.current) { outlineDp.dp.toPx() }

    val baseStyle = remember(fontFamily) {
        TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            textAlign = TextAlign.Center
        )
    }

    val prev = if (lineIndex - 2 >= 0) lines[lineIndex - 2] else null
    val prev2 = if (lineIndex - 1 >= 0) lines[lineIndex - 1] else null
    val current = lines.getOrNull(lineIndex)
    val next = lines.getOrNull(lineIndex + 1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DimLine(text = prev?.fullText() ?: "", baseStyle = baseStyle, outlinePx = outlinePx)
        DimLine(text = prev2?.fullText() ?: "", baseStyle = baseStyle, outlinePx = outlinePx)
        Spacer(Modifier.height(18.dp))
        AnimatedContent(
            targetState = current?.fullText() ?: "",
            transitionSpec = { scaleIn() togetherWith scaleOut() },
            label = "currentLine"
        ) { text ->
            if (text.isNotBlank()) {
                ShrinkToFitText(
                    text = text,
                    baseStyle = baseStyle,
                    fillColor = KaraokeHighlight,
                    outlinePx = outlinePx
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        DimLine(text = next?.fullText() ?: "", baseStyle = baseStyle, outlinePx = outlinePx)
    }
}

@Composable
private fun KaraokeStack(
    lines: List<LyricLine>,
    lineIndex: Int,
    posMs: Double,
    currentTick: Long,
    fillColor: Color,
    fontSizeSp: Float,
    maxLines: Int = 1
) {
    val context = LocalContext.current
    val fontFamily = rememberLyricFontFamily()
    val outlineDp = remember(context) { LyricSettings.getOutlineThickness(context) }
    val outlinePx = with(LocalDensity.current) { outlineDp.dp.toPx() }

    val baseStyle = remember(fontFamily, fontSizeSp) {
        TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.15f).sp,
            textAlign = TextAlign.Center
        )
    }

    // Portrait: two fixed slots that reserve the height of [maxLines] lines so changing the next
    // phrase never shifts the line being sung, and a long phrase keeps its font size instead of
    // being squeezed. The wipe alternates between the top and bottom slot each phrase.
    if (maxLines > 1) {
        val current = lines.getOrNull(lineIndex)
        val next = lines.getOrNull(lineIndex + 1)
        val isEven = lineIndex % 2 == 0
        val topLine = if (isEven) current else next
        val bottomLine = if (isEven) next else current
        val lineHeight = with(LocalDensity.current) { (fontSizeSp * 1.15f).sp.toDp() }
        val reservedSlot = lineHeight * maxLines + 2.dp * (maxLines - 1)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val available = maxHeight
            val slot = if (available.value.isFinite() && available.value > 0f) {
                minOf(reservedSlot, (available - 16.dp) / 2).coerceAtLeast(0.dp)
            } else {
                reservedSlot
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slot),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (topLine != null) {
                        if (isEven) {
                            KaraokeLine(
                                line = topLine,
                                posMs = posMs,
                                currentTick = currentTick,
                                fillColor = fillColor,
                                baseStyle = baseStyle,
                                outlinePx = outlinePx,
                                maxLines = maxLines
                            )
                        } else {
                            KaraokeUpcomingLine(
                                line = topLine,
                                baseStyle = baseStyle,
                                outlinePx = outlinePx,
                                maxLines = maxLines
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slot),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (bottomLine != null) {
                        if (!isEven) {
                            KaraokeLine(
                                line = bottomLine,
                                posMs = posMs,
                                currentTick = currentTick,
                                fillColor = fillColor,
                                baseStyle = baseStyle,
                                outlinePx = outlinePx,
                                maxLines = maxLines
                            )
                        } else {
                            KaraokeUpcomingLine(
                                line = bottomLine,
                                baseStyle = baseStyle,
                                outlinePx = outlinePx,
                                maxLines = maxLines
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val isEven = lineIndex % 2 == 0
    val topLine = if (isEven) lines.getOrNull(lineIndex) else lines.getOrNull(lineIndex + 1)
    val bottomLine = if (isEven) lines.getOrNull(lineIndex + 1) else lines.getOrNull(lineIndex)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (topLine != null) {
            if (isEven) {
                KaraokeLine(
                    line = topLine,
                    posMs = posMs,
                    currentTick = currentTick,
                    fillColor = fillColor,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx,
                    maxLines = maxLines
                )
            } else {
                KaraokeUpcomingLine(
                    line = topLine,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx,
                    maxLines = maxLines
                )
            }
        } else {
            Spacer(Modifier.height(fontSizeSp.dp))
        }

        Spacer(Modifier.height(if (maxLines > 1) 12.dp else 24.dp))

        if (bottomLine != null) {
            if (!isEven) {
                KaraokeLine(
                    line = bottomLine,
                    posMs = posMs,
                    currentTick = currentTick,
                    fillColor = fillColor,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx,
                    maxLines = maxLines
                )
            } else {
                KaraokeUpcomingLine(
                    line = bottomLine,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx,
                    maxLines = maxLines
                )
            }
        } else {
            Spacer(Modifier.height(fontSizeSp.dp))
        }
    }
}

/**
 * Single, unfilled karaoke line shown during the 4·3·2·1 count-in so the singer can see the
 * phrase that is about to be wiped. Rendered directly below the countdown dots.
 */
@Composable
private fun CountdownUpcomingLine(
    line: LyricLine,
    posMs: Double,
    currentTick: Long,
    fontSizeSp: Float,
    maxLines: Int = 1
) {
    val context = LocalContext.current
    val fontFamily = rememberLyricFontFamily()
    val outlineDp = remember(context) { LyricSettings.getOutlineThickness(context) }
    val outlinePx = with(LocalDensity.current) { outlineDp.dp.toPx() }
    val baseStyle = remember(fontFamily, fontSizeSp) {
        TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.15f).sp,
            textAlign = TextAlign.Center
        )
    }

    KaraokeLine(
        line = line,
        posMs = posMs,
        currentTick = currentTick,
        fillColor = KaraokeHighlight,
        baseStyle = baseStyle,
        outlinePx = outlinePx,
        maxLines = maxLines
    )
}

@Composable
private fun KaraokeUpcomingLine(
    line: LyricLine,
    baseStyle: TextStyle,
    outlinePx: Float,
    maxLines: Int = 1
) {
    if (maxLines > 1) {
        WrappedKaraokeLine(
            line = line,
            posMs = 0.0,
            currentTick = 0L,
            baseStyle = baseStyle,
            outlinePx = outlinePx,
            fillColor = Color.White,
            filled = false,
            maxLines = maxLines
        )
        return
    }
    val measurer = rememberTextMeasurer()
    val text = line.fullText()
    if (text.isBlank()) return

    val fragmentWidths = remember(measurer, text, baseStyle) {
        line.fragments.map { fragment ->
            measurer.measure(fragment.text, baseStyle, softWrap = false).size.width.toFloat()
        }
    }
    val measuredTotal = fragmentWidths.sum()

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val scale = if (measuredTotal > 0f) {
            (maxWidthPx / measuredTotal).coerceIn(MIN_LYRIC_SCALE, 1f)
        } else {
            1f
        }
        val style = if (scale < 1f) {
            baseStyle.copy(
                fontSize = (baseStyle.fontSize.value * scale).sp,
                lineHeight = (baseStyle.lineHeight.value * scale).sp
            )
        } else {
            baseStyle
        }
        val strokeStyle = style.copy(drawStyle = Stroke(width = outlinePx))

        Box {
            Text(text, style = strokeStyle, color = Color.Black, maxLines = 1, softWrap = false)
            Text(text, style = style, color = Color.White, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun KaraokeLine(
    line: LyricLine,
    posMs: Double,
    currentTick: Long,
    fillColor: Color,
    baseStyle: TextStyle,
    outlinePx: Float,
    maxLines: Int = 1
) {
    if (maxLines > 1) {
        WrappedKaraokeLine(
            line = line,
            posMs = posMs,
            currentTick = currentTick,
            baseStyle = baseStyle,
            outlinePx = outlinePx,
            fillColor = fillColor,
            filled = true,
            maxLines = maxLines
        )
        return
    }
    val measurer = rememberTextMeasurer()
    val text = line.fullText()
    val fragments = line.fragments

    val fragmentWidths = remember(measurer, text, baseStyle) {
        fragments.map { fragment ->
            measurer.measure(fragment.text, baseStyle, softWrap = false).size.width.toFloat()
        }
    }
    val measuredTotal = fragmentWidths.sum()

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val scale = if (measuredTotal > 0f) {
            (maxWidthPx / measuredTotal).coerceIn(MIN_LYRIC_SCALE, 1f)
        } else {
            1f
        }
        val style = if (scale < 1f) {
            baseStyle.copy(
                fontSize = (baseStyle.fontSize.value * scale).sp,
                lineHeight = (baseStyle.lineHeight.value * scale).sp
            )
        } else {
            baseStyle
        }
        val strokeStyle = style.copy(drawStyle = Stroke(width = outlinePx))
        val progressPx = karaokeProgressPx(line, posMs, fragmentWidths, currentTick) * scale

        Box {
            Text(text, style = strokeStyle, color = Color.Black, maxLines = 1, softWrap = false)
            Text(text, style = style, color = Color.White, maxLines = 1, softWrap = false)
            Box(
                modifier = Modifier.drawWithContent {
                    clipRect(right = progressPx.coerceIn(0f, size.width)) {
                        this@drawWithContent.drawContent()
                    }
                }
            ) {
                Text(text, style = strokeStyle, color = Color.White, maxLines = 1, softWrap = false)
                Text(text, style = style, color = fillColor, maxLines = 1, softWrap = false)
            }
        }
    }
}

/**
 * Portrait renderer: wraps a lyric line across up to [maxLines] visual lines so the font size is
 * preserved instead of being squeezed onto a single line. Each visual line is wiped independently,
 * and the wipe proceeds line by line (line 1 fills, then line 2, ...) following the same melody
 * timing as the single-line renderer.
 */
@Composable
private fun WrappedKaraokeLine(
    line: LyricLine,
    posMs: Double,
    currentTick: Long,
    baseStyle: TextStyle,
    outlinePx: Float,
    fillColor: Color,
    filled: Boolean,
    maxLines: Int
) {
    val measurer = rememberTextMeasurer()
    val fragments = line.fragments
    val text = line.fullText()
    if (text.isBlank()) return

    val widths = remember(measurer, text, baseStyle) {
        fragments.map { measurer.measure(it.text, baseStyle, softWrap = false).size.width.toFloat() }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val scale = if (widths.isEmpty()) 1f else fitScaleForLines(widths, maxWidthPx, maxLines)
        val scaledWidths = if (scale < 1f) widths.map { it * scale } else widths
        val style = if (scale < 1f) {
            baseStyle.copy(
                fontSize = (baseStyle.fontSize.value * scale).sp,
                lineHeight = (baseStyle.lineHeight.value * scale).sp
            )
        } else {
            baseStyle
        }
        val strokeStyle = style.copy(drawStyle = Stroke(width = outlinePx))
        val ranges = wrapFragmentRanges(scaledWidths, maxWidthPx, maxLines)
        val globalFill = if (filled) {
            karaokeProgressPx(line, posMs, scaledWidths, currentTick)
        } else {
            0f
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            var offset = 0f
            for (range in ranges) {
                var lineWidth = 0f
                for (idx in range) lineWidth += scaledWidths[idx]
                val localFill = (globalFill - offset).coerceIn(0f, lineWidth)
                offset += lineWidth

                val segment = buildString {
                    for (idx in range) append(fragments[idx].text)
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        Text(segment, style = strokeStyle, color = Color.Black, maxLines = 1, softWrap = false)
                        Text(segment, style = style, color = Color.White, maxLines = 1, softWrap = false)
                        if (localFill > 0f) {
                            Box(
                                modifier = Modifier.drawWithContent {
                                    clipRect(right = localFill.coerceIn(0f, size.width)) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                            ) {
                                Text(segment, style = strokeStyle, color = Color.White, maxLines = 1, softWrap = false)
                                Text(segment, style = style, color = fillColor, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DimLine(text: String, baseStyle: TextStyle, outlinePx: Float) {
    if (text.isBlank()) {
        Box(Modifier.height(12.dp))
        return
    }
    val dimBase = baseStyle.copy(fontSize = (baseStyle.fontSize.value * 0.75f).sp)
    ShrinkToFitText(
        text = text,
        baseStyle = dimBase,
        fillColor = Color.White.copy(alpha = 0.6f),
        outlinePx = outlinePx
    )
}

/**
 * Renders [text] on a single line, shrinking the font (down to [MIN_LYRIC_SCALE]) so the whole
 * phrase is always visible instead of being clipped or ellipsized. The font is never enlarged.
 */
@Composable
private fun ShrinkToFitText(
    text: String,
    baseStyle: TextStyle,
    fillColor: Color,
    outlinePx: Float,
    minScale: Float = MIN_LYRIC_SCALE
) {
    if (text.isBlank()) return
    val measurer = rememberTextMeasurer()
    val measuredWidth = remember(measurer, text, baseStyle) {
        measurer.measure(text, baseStyle, softWrap = false).size.width.toFloat()
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val scale = if (measuredWidth > 0f && maxWidthPx > 0f) {
            (maxWidthPx / measuredWidth).coerceIn(minScale, 1f)
        } else {
            1f
        }
        val style = if (scale < 1f) {
            val scaled = baseStyle.copy(fontSize = (baseStyle.fontSize.value * scale).sp)
            if (baseStyle.lineHeight.isSp) {
                scaled.copy(lineHeight = (baseStyle.lineHeight.value * scale).sp)
            } else {
                scaled
            }
        } else {
            baseStyle
        }
        val strokeStyle = style.copy(drawStyle = Stroke(width = outlinePx))
        Box {
            Text(text, style = strokeStyle, color = Color.Black, maxLines = 1, softWrap = false)
            Text(text, style = style, color = fillColor, maxLines = 1, softWrap = false)
        }
    }
}

private fun karaokeProgressPx(
    line: LyricLine,
    posMs: Double,
    fragmentWidths: List<Float>,
    currentTick: Long = 0L
): Float {
    val fragments = line.fragments
    if (fragments.isEmpty() || fragmentWidths.size != fragments.size) return 0f

    val totalWidth = fragmentWidths.sum()
    if (posMs <= line.startMs) return 0f
    if (posMs >= line.endMs) return totalWidth

    val lineDuration = line.endMs - line.startMs
    if (lineDuration <= 0) return totalWidth

    if (currentTick > 0L && fragments.all { it.endTick > it.absTick }) {
        var tickProgressed = 0f
        for (i in fragments.indices) {
            val fragment = fragments[i]
            val width = fragmentWidths[i]

            val startTick = fragment.absTick
            val endTick = fragment.endTick
            when {
                currentTick >= endTick -> tickProgressed += width
                currentTick >= startTick -> {
                    val duration = endTick - startTick
                    val ratio = if (duration > 0) {
                        ((currentTick - startTick).toDouble() / duration).coerceIn(0.0, 1.0)
                    } else {
                        1.0
                    }
                    tickProgressed += (width * ratio).toFloat()
                    break
                }
                else -> break
            }
        }
        return tickProgressed.coerceIn(0f, totalWidth)
    }

    var progressed = 0f
    for (i in fragments.indices) {
        val fragment = fragments[i]
        val width = fragmentWidths[i]

        val fStart = if (fragment.absMs > 0.0) fragment.absMs else line.startMs
        val fEnd = when {
            fragment.endMs > fStart -> fragment.endMs
            i + 1 < fragments.size && fragments[i + 1].absMs > fStart -> fragments[i + 1].absMs
            else -> line.endMs
        }

        when {
            posMs >= fEnd -> progressed += width
            posMs >= fStart -> {
                val duration = fEnd - fStart
                val ratio = if (duration > 0) {
                    ((posMs - fStart) / duration).coerceIn(0.0, 1.0)
                } else {
                    1.0
                }
                progressed += (width * ratio).toFloat()
                break
            }
            else -> break
        }
    }
    return progressed.coerceIn(0f, totalWidth)
}

@Composable
private fun rememberLyricFontFamily(): FontFamily {
    val context = LocalContext.current
    val customPath = LyricSettings.getCustomFontPath(context)
    val fontType = LyricSettings.getFontType(context)

    return remember(customPath, fontType) {
        if (!customPath.isNullAndBlank()) {
            val file = File(customPath)
            if (file.isFile) {
                try {
                    return@remember FontFamily(Font(file))
                } catch (t: Throwable) {
                    Log.w("PlayerScreen", "Failed to load font at $customPath", t)
                }
            }
        }
        lyricFontFamily(fontType)
    }
}

private fun String?.isNullAndBlank(): Boolean = this == null || this.isBlank()

@Composable
private fun QrCodeDialog(
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val ip = remember(context) { getLocalIpAddress(context) }
    val port = remember(context) { BehaviorSettings.getWebPort(context) }
    val webUrl = "http://$ip:$port"

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTv = rememberIsTvDevice()
    val isRightPanel = isLandscape || isTv

    val closeFocusRequester = remember { FocusRequester() }
    var isCloseFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100L)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = if (isRightPanel) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        Surface(
            modifier = if (isRightPanel) {
                Modifier
                    .width(400.dp)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
            },
            shape = if (isRightPanel) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Web Remote & Search",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .focusRequester(closeFocusRequester)
                            .onFocusChanged { isCloseFocused = it.isFocused }
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCloseFocused) Color(0xFFEF4444) else Color.White.copy(alpha = 0.2f))
                            .then(if (isCloseFocused) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                    ) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeImage(content = webUrl, sizeDp = 150.dp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Scan QR code on your phone to open web remote & song search",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = webUrl,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getLocalIpAddress(context: Context): String {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager != null) {
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
        }
    } catch (_: Exception) {}

    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val intf = interfaces.nextElement()
            val addrs = intf.inetAddresses
            while (addrs.hasMoreElements()) {
                val addr = addrs.nextElement()
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress ?: "192.168.1.100"
                }
            }
        }
    } catch (_: Exception) {}

    return "192.168.1.100"
}

@Composable
private fun DefaultAnimatedBackground(bgvIndex: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bgv")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val colorPalettes = remember {
        listOf(
            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311B92), Color(0xFF004D40), Color(0xFF0F172A)),
            listOf(Color(0xFF064E3B), Color(0xFF0D9488), Color(0xFF78350F), Color(0xFF047857), Color(0xFF064E3B)),
            listOf(Color(0xFF4C0519), Color(0xFF831843), Color(0xFF9A3412), Color(0xFF701A75), Color(0xFF4C0519)),
            listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF0891B2), Color(0xFF312E81), Color(0xFF1E1B4B)),
            listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF0D9488), Color(0xFF164E63), Color(0xFF0C4A6E))
        )
    }

    val activePalette = colorPalettes[Math.abs(bgvIndex) % colorPalettes.size]

    Box(
        modifier = modifier
            .background(
                Brush.sweepGradient(activePalette)
            )
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.65f))
                )
            )
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun BackgroundVideoPlayer(
    bgvDir: File?,
    bgvIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgvMode = remember(context) { BgvSettings.getMode(context) }
    val libraryRoot = remember(bgvDir, context) { bgvDir?.parentFile ?: LibraryRepository.defaultRoot(context) }

    if (bgvMode == BgvSettings.MODE_STATIC_PHOTO) {
        val staticPath = remember(context) { BgvSettings.getStaticPhotoPath(context) }
        val photos = remember(libraryRoot, bgvMode) {
            val list = mutableListOf<File>()
            val photoDir = BgvSettings.getBgPhotoDirectory(context, libraryRoot.absolutePath)
            fun collect(f: File) {
                if (f.isDirectory) {
                    f.listFiles()?.forEach { collect(it) }
                } else if (f.isFile) {
                    val lower = f.name.lowercase()
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        list.add(f)
                    }
                }
            }
            if (photoDir != null && photoDir.isDirectory) {
                collect(photoDir)
            }
            list.sortedBy { it.name.lowercase() }
        }

        val activePhoto = remember(photos, staticPath, bgvIndex) {
            if (photos.isNotEmpty()) {
                val idx = if (staticPath.isNotBlank()) {
                    val foundIdx = photos.indexOfFirst { it.absolutePath == staticPath }
                    if (foundIdx >= 0) (foundIdx + bgvIndex) % photos.size else Math.abs(bgvIndex) % photos.size
                } else {
                    Math.abs(bgvIndex) % photos.size
                }
                photos[idx]
            } else if (staticPath.isNotBlank() && File(staticPath).isFile) {
                File(staticPath)
            } else {
                null
            }
        }

        if (activePhoto != null && activePhoto.isFile) {
            AsyncImage(
                model = activePhoto,
                contentDescription = "Static Background",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
            return
        }
    }

    val videos = remember(bgvDir, bgvMode) {
        if (bgvMode != BgvSettings.MODE_VIDEOS || bgvDir == null || !bgvDir.isDirectory) emptyList()
        else {
            val list = mutableListOf<File>()
            fun collect(f: File) {
                if (f.isDirectory) {
                    f.listFiles()?.forEach { collect(it) }
                } else if (f.isFile) {
                    val lower = f.name.lowercase()
                    if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")) {
                        list.add(f)
                    }
                }
            }
            collect(bgvDir)
            list
        }
    }

    if (bgvMode != BgvSettings.MODE_VIDEOS || videos.isEmpty()) {
        DefaultAnimatedBackground(bgvIndex = bgvIndex, modifier = modifier)
        return
    }

    val activeVideo = remember(videos, bgvIndex) {
        videos[Math.abs(bgvIndex) % videos.size]
    }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }

    LaunchedEffect(activeVideo) {
        exoPlayer.setMediaItem(MediaItem.fromUri(activeVideo.absolutePath))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
        },
        modifier = modifier
    )
}

@Composable
private fun PlayerCenterContent(
    song: SongItem,
    codeInput: String,
    typedSongPreview: SongItem?,
    lines: List<LyricLine>,
    countdowns: List<CountdownGroup>,
    posMs: Double,
    currentTick: Long,
    lineIndex: Int,
    lyricStyle: Int,
    isInstrumental: Boolean = false,
    inBreak: Boolean = false,
    isPortrait: Boolean = false,
    context: Context,
    availableHeightDp: Dp? = null
) {
    // In portrait a long phrase wraps onto extra lines (keeping the font size) instead of
    // being squeezed onto one line. The user can pick 3 or 4 lines.
    val portraitMaxLines = if (LyricSettings.getFourLinePortrait(context)) 4 else 3
    val baseLyricFontSp = LyricSettings.getFontSizeLandscape(context)

    // Portrait only: when the on-screen keypad (or another overlay) reduces the vertical space,
    // shrink the lyric font so the wrapped phrase still fits in the reserved slots instead of
    // being clipped. The scale never enlarges the font (capped at 1x).
    val lyricFontSizeSp = if (isPortrait && availableHeightDp != null) {
        val density = LocalDensity.current
        val lineHeightSp = baseLyricFontSp * 1.15f
        val naturalSlotDp = with(density) {
            (lineHeightSp * portraitMaxLines + 2f * (portraitMaxLines - 1)).sp.toDp()
        }
        val naturalTotalDp = naturalSlotDp * 2
        val usableDp = (availableHeightDp - 16.dp).coerceAtLeast(0.dp)
        val scale = if (naturalTotalDp.value > 0f) {
            (usableDp.value / naturalTotalDp.value).coerceIn(0.45f, 1f)
        } else {
            1f
        }
        baseLyricFontSp * scale
    } else {
        baseLyricFontSp
    }
    val firstLineStart = lines.firstOrNull()?.startMs ?: 6000.0
    val showSongIntroCard = song.code != 0 && posMs < minOf(5000.0, firstLineStart - 300.0)

    val activeCountdown = remember(countdowns, posMs) {
        countdowns.find { cd ->
            val firstMs = if (cd.pipMs.isNotEmpty()) cd.pipMs[0] else cd.anchorMs - 2000.0
            posMs >= firstMs - 50.0 && posMs < cd.anchorMs
        }
    }
    // While the 4·3·2·1 count-in is showing we replace the instrumental pill with the
    // upcoming phrase so the singer can read it and be ready to sing.
    val countdownLine = activeCountdown?.takeIf { it.pipMs.isNotEmpty() }
    val upcomingLine = countdownLine?.lineIndex?.let { lines.getOrNull(it) }

    if (song.code == 0) {
        if (codeInput.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = codeInput.padStart(6, '0'),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFBBF24)
                )
                Spacer(Modifier.height(8.dp))
                if (typedSongPreview != null) {
                    Text(
                        text = typedSongPreview.displayTitle(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = typedSongPreview.displaySinger(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2DD4BF),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = if (codeInput.length >= 6) "Song code not found — keep typing to correct" else "Searching song code...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select a song",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "000000",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else if (showSongIntroCard) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.displayTitle(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = song.displaySinger(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2DD4BF),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isInstrumental) {
                Spacer(Modifier.height(14.dp))
                InstrumentalBadge()
            }
            if (song.style.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = song.style,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (activeCountdown != null && activeCountdown.pipMs.isNotEmpty()) {
                val pipMsList = activeCountdown.pipMs
                val beatsHit = pipMsList.count { pipTime -> posMs >= pipTime }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    for (dotIndex in 0 until 4) {
                        val isLit = dotIndex < beatsHit
                        val dotColor by animateColorAsState(
                            targetValue = if (isLit) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.25f),
                            label = "dotColor"
                        )
                        val dotScale by animateFloatAsState(
                            targetValue = if (isLit) 1.25f else 1.0f,
                            label = "dotScale"
                        )

                        Box(
                            modifier = Modifier
                                .scale(dotScale)
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                                .background(dotColor)
                                .then(
                                    if (isLit) Modifier.border(
                                        2.dp,
                                        Color.White,
                                        RoundedCornerShape(50)
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${4 - dotIndex}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isLit) Color(0xFF003731) else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (lines.isEmpty()) {
                if (isInstrumental) {
                    InstrumentalBadge()
                } else {
                    Text("No lyrics", style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            } else if (upcomingLine != null) {
                // Count-in is showing (e.g. right after an instrumental break): hide the
                // pill and preview the phrase about to be wiped, right below the 4·3·2·1 dots.
                CountdownUpcomingLine(
                    line = upcomingLine,
                    posMs = posMs,
                    currentTick = currentTick,
                    fontSizeSp = lyricFontSizeSp,
                    maxLines = if (isPortrait) portraitMaxLines else 1
                )
            } else if (inBreak) {
                Box(
                    modifier = Modifier.scale(1.6f),
                    contentAlignment = Alignment.Center
                ) {
                    InstrumentalBadge()
                }
            } else if (lyricStyle == LyricSettings.STYLE_KARAOKE) {
                KaraokeStack(
                    lines = lines,
                    lineIndex = lineIndex,
                    posMs = posMs,
                    currentTick = currentTick,
                    fillColor = KaraokeHighlight,
                    fontSizeSp = lyricFontSizeSp,
                    maxLines = if (isPortrait) portraitMaxLines else 1
                )
            } else {
                LyricStack(lines = lines, lineIndex = lineIndex)
            }
        }
    }
}

private fun fmtMs(ms: Double): String {
    val totalSeconds = (ms / 1000).toInt()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
