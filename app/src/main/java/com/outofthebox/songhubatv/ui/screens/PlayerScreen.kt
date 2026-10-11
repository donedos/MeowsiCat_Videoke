package com.outofthebox.songhubatv.ui.screens

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.songhub.android.audio.BassMidiEngine
import com.songhub.android.audio.SoundFontManager
import com.songhub.android.model.CountdownGroup
import com.songhub.android.model.LyricLine
import com.songhub.android.parser.TempoUtils
import com.songhub.android.song.ScoreCalculator
import com.songhub.android.storage.BgvSettings
import com.songhub.android.storage.LyricSettings
import com.songhub.android.storage.ScoreSettings
import com.outofthebox.songhubatv.R
import com.outofthebox.songhubatv.data.LibraryRepository
import com.outofthebox.songhubatv.data.LyricLoader
import com.outofthebox.songhubatv.data.SongItem
import com.outofthebox.songhubatv.data.ScoreStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private data class ScoreResult(
    val score: Int,
    val message: String
)

/** Videoke highlight colour: the "sung" portion of the lyric (indigo). */
private val KaraokeHighlight = Color(0xFF4F46E5)

/** Frame interval used while polling the MIDI transport so the wipe stays smooth. */
private const val AUDIO_POLL_MS = 16L

@Composable
fun PlayerScreen(
    song: SongItem,
    onBack: () -> Unit,
    library: LibraryRepository.Library = LibraryRepository.Library(emptyList(), emptyList(), emptyList(), emptyList(), LibraryRepository.defaultRoot()),
    libraryRoot: File = library.root,
    isInPipMode: Boolean = false,
    isPipSupported: Boolean = false,
    onEnterPip: () -> Unit = {},
    onPlaybackStateChanged: (Boolean) -> Unit = {}
) {
    var activeSong by remember(song) { mutableStateOf(song) }
    val context = LocalContext.current
    val parsed by produceState(
        initialValue = LyricLoader.Parsed(emptyList()),
        key1 = activeSong.code
    ) {
        value = LyricLoader.load(activeSong)
    }

    if (parsed.error != null && parsed.lines.isEmpty() && activeSong.code != 0) {
        LyricUnavailable(song = activeSong, error = parsed.error, onBack = onBack)
        return
    }

    LyricPlayer(
        song = activeSong,
        library = library,
        lines = parsed.lines,
        countdowns = parsed.countdowns,
        tempoMap = parsed.tempoMap,
        ticksPerBeat = parsed.ticksPerBeat,
        totalMs = parsed.totalMs,
        context = context,
        libraryRoot = libraryRoot,
        onBack = onBack,
        onSelectSong = { newSong -> activeSong = newSong },
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
    library: LibraryRepository.Library,
    lines: List<LyricLine>,
    countdowns: List<CountdownGroup>,
    tempoMap: List<TempoUtils.TempoEntry>,
    ticksPerBeat: Int,
    totalMs: Double,
    context: Context,
    libraryRoot: File,
    onBack: () -> Unit,
    onSelectSong: (SongItem) -> Unit,
    isInPipMode: Boolean,
    isPipSupported: Boolean,
    onEnterPip: () -> Unit,
    onPlaybackStateChanged: (Boolean) -> Unit
) {
    val engine = remember(context) { BassMidiEngine(context) }
    DisposableEffect(song.code) {
        onDispose {
            engine.pause()
            engine.release()
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
    var scoreResult by remember { mutableStateOf<ScoreResult?>(null) }
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
    var reservationQueue by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var showSearchDialog by rememberSaveable { mutableStateOf(false) }
    var showRsvDialog by rememberSaveable { mutableStateOf(false) }
    var statusToast by remember { mutableStateOf<String?>(null) }

    // Controls visibility
    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    var bgvIndex by rememberSaveable { mutableIntStateOf(0) }

    // Key, Tempo, Melody states
    var keyShift by rememberSaveable { mutableIntStateOf(0) }
    var temShift by rememberSaveable { mutableIntStateOf(0) }
    var melVol by rememberSaveable { mutableIntStateOf(40) }
    var vocalActive by rememberSaveable { mutableStateOf(true) }

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
    }

    LaunchedEffect(song.code) {
        posMs = 0.0
        posTick = 0L
        if (song.code == 0) {
            audioReady = false
            audioNote = null
            playing = false
            return@LaunchedEffect
        }
        keyShift = 0
        temShift = 0
        melVol = 40
        engine.setKey(0)
        engine.setTempo(100f)
        engine.setMelodyVolume(40)

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
    fun playNextSongInQueue() {
        posMs = 0.0
        posTick = 0L
        scored = false
        if (reservationQueue.isNotEmpty()) {
            val nextSong = reservationQueue.first()
            reservationQueue = reservationQueue.drop(1)
            onSelectSong(nextSong)
            playing = true
            statusToast = "Now Playing: ${nextSong.displayTitle()}"
        } else {
            playing = false
            onSelectSong(SongItem.placeholder())
            statusToast = "Playback Stopped — Select a Song"
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
                onSelectSong(found)
                posMs = 0.0
                scored = false
                playing = true
                codeInput = ""
                statusToast = "Playing: ${found.displayTitle()}"
            } else {
                statusToast = "Song #${codeInput} not found"
                codeInput = ""
            }
        } else {
            if (song.code == 0 && reservationQueue.isNotEmpty()) {
                playNextSongInQueue()
            } else {
                playing = !playing
            }
        }
    }

    LaunchedEffect(playing, audioReady, effectiveTotalMs) {
        while (playing && song.code != 0) {
            if (audioReady && !seeking && !engine.isPlaying() && !engine.hasEnded()) {
                engine.play()
            }
            delay(if (audioReady) AUDIO_POLL_MS else 33L)
            if (!playing) break
            if (audioReady) {
                if (!seeking) {
                    val tick = engine.getTickPosition()
                    if (tick >= 0) posTick = tick
                    val ms = if (tick >= 0 && ticksPerBeat > 0 && tempoMap.isNotEmpty()) {
                        TempoUtils.tickToMs(tempoMap, tick, ticksPerBeat)
                    } else {
                        engine.getPositionMs()
                    }
                    if (ms > 0.0) posMs = ms
                }
                val ended = engine.hasEnded() || (effectiveTotalMs > 0.0 && posMs >= effectiveTotalMs)
                if (ended) {
                    engine.stop()
                    val mode = ScoreSettings.getScoreMode(context)
                    if (!scored && mode != ScoreSettings.ScoreMode.OFF) {
                        val difficulty = ScoreSettings.getDifficulty(context)
                        val calculatedScore = if (mode == ScoreSettings.ScoreMode.MICROPHONE) {
                            ScoreCalculator.computeMicScore(difficulty)
                        } else {
                            ScoreCalculator.generateRandomScore(difficulty)
                        }
                        val message = ScoreCalculator.getMessageForScore(calculatedScore)
                        ScoreStore.record(context, song, effectiveTotalMs, calculatedScore)
                        scored = true
                        scoreResult = ScoreResult(calculatedScore, message)
                    }
                    playNextSongInQueue()
                }
            } else {
                posMs += 33.0
                if (effectiveTotalMs > 0.0 && posMs >= effectiveTotalMs) {
                    val mode = ScoreSettings.getScoreMode(context)
                    if (!scored && mode != ScoreSettings.ScoreMode.OFF) {
                        val difficulty = ScoreSettings.getDifficulty(context)
                        val calculatedScore = if (mode == ScoreSettings.ScoreMode.MICROPHONE) {
                            ScoreCalculator.computeMicScore(difficulty)
                        } else {
                            ScoreCalculator.generateRandomScore(difficulty)
                        }
                        val message = ScoreCalculator.getMessageForScore(calculatedScore)
                        ScoreStore.record(context, song, effectiveTotalMs, calculatedScore)
                        scored = true
                        scoreResult = ScoreResult(calculatedScore, message)
                    }
                    playNextSongInQueue()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF16222E), MaterialTheme.colorScheme.background)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible = !controlsVisible
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

        if (statusToast != null) {
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                StatusToastNotification(message = statusToast!!)
            }
        }

        if (isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
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
                                text = buildString {
                                    append("%06d".format(song.code))
                                    if (reservationQueue.isNotEmpty()) {
                                        append(" • R: ").append(reservationQueue.size)
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2DD4BF),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
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
                        context = context
                    )
                }

                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    KeypadControlPanel(
                        keyShift = keyShift,
                        temShift = temShift,
                        melVol = melVol,
                        reservationCount = reservationQueue.size,
                        playing = playing,
                        vocalActive = vocalActive,
                        onKeyChange = { keyShift = it; engine.setKey(keyShift) },
                        onTemChange = { temShift = it; engine.setTempo((100 + temShift * 5).toFloat()) },
                        onMelChange = { melVol = it; engine.setMelodyVolume(melVol) },
                        onDigitPress = { char ->
                            if (codeInput.length < 6) codeInput += char
                        },
                        onCancelPress = {
                            if (codeInput.isNotEmpty()) codeInput = "" else controlsVisible = false
                        },
                        onReservePress = { firstPriority -> handleReservePress(firstPriority) },
                        onSearchPress = { showSearchDialog = true },
                        onRsvListPress = { showRsvDialog = true },
                        onPlayPress = { handlePlayPress() },
                        onStopPress = {
                            engine.stop()
                            playNextSongInQueue()
                        },
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
                            vocalActive = !vocalActive
                            melVol = if (vocalActive) 40 else 0
                            engine.setMelodyVolume(melVol)
                            statusToast = if (vocalActive) "Vocal: ON" else "Vocal: OFF"
                        },
                        onBgvToggle = {
                            bgvIndex++
                            statusToast = "BGV Switched"
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.displayTitle(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append("%06d".format(song.code))
                                    if (reservationQueue.isNotEmpty()) {
                                        append(" • R: ").append(reservationQueue.size)
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2DD4BF),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
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
                        context = context
                    )
                }

                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    KeypadControlPanel(
                        keyShift = keyShift,
                        temShift = temShift,
                        melVol = melVol,
                        reservationCount = reservationQueue.size,
                        playing = playing,
                        vocalActive = vocalActive,
                        onKeyChange = { keyShift = it; engine.setKey(keyShift) },
                        onTemChange = { temShift = it; engine.setTempo((100 + temShift * 5).toFloat()) },
                        onMelChange = { melVol = it; engine.setMelodyVolume(melVol) },
                        onDigitPress = { char ->
                            if (codeInput.length < 6) codeInput += char
                        },
                        onCancelPress = {
                            if (codeInput.isNotEmpty()) codeInput = "" else controlsVisible = false
                        },
                        onReservePress = { firstPriority -> handleReservePress(firstPriority) },
                        onSearchPress = { showSearchDialog = true },
                        onRsvListPress = { showRsvDialog = true },
                        onPlayPress = { handlePlayPress() },
                        onStopPress = {
                            engine.stop()
                            playNextSongInQueue()
                        },
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
                            vocalActive = !vocalActive
                            melVol = if (vocalActive) 40 else 0
                            engine.setMelodyVolume(melVol)
                            statusToast = if (vocalActive) "Vocal: ON" else "Vocal: OFF"
                        },
                        onBgvToggle = {
                            statusToast = "BGV Switched"
                        }
                    )
                }
            }
        }

        if (scoreResult != null) {
            val res = scoreResult!!
            ScoreOverlay(
                score = res.score,
                songTitle = song.displayTitle(),
                songSinger = if (song.code == 0) "000000" else song.displaySinger(),
                onSingAgain = {
                    scoreResult = null
                    engine.restart()
                    posMs = 0.0
                    scored = false
                    playing = true
                },
                onDismiss = { scoreResult = null }
            )
        }

        if (showSearchDialog) {
            QuickSearchDialog(
                songs = library.songs,
                onDismiss = { showSearchDialog = false },
                onPlaySong = { s ->
                    onSelectSong(s)
                    posMs = 0.0
                    scored = false
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

        if (showRsvDialog) {
            ReservationListDialog(
                queue = reservationQueue,
                onDismiss = { showRsvDialog = false },
                onPlayNow = { s ->
                    reservationQueue = reservationQueue - s
                    onSelectSong(s)
                    posMs = 0.0
                    scored = false
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
    }
}

@Composable
private fun StatusToastNotification(message: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0D9488).copy(alpha = 0.95f),
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

@Composable
private fun KeypadControlPanel(
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
    onCancelPress: () -> Unit,
    onReservePress: (firstPriority: Boolean) -> Unit,
    onSearchPress: () -> Unit,
    onRsvListPress: () -> Unit,
    onPlayPress: () -> Unit,
    onStopPress: () -> Unit,
    onNextPress: () -> Unit,
    onPrevPress: () -> Unit,
    onVocalToggle: () -> Unit,
    onBgvToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF022C28).copy(alpha = 0.88f),
        tonalElevation = 8.dp,
        modifier = modifier
            .border(1.5.dp, Color(0xFF2DD4BF).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Key, Tempo, Melody Adjustment Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdjustmentPill(
                    label = "KEY: ${if (keyShift >= 0) "+$keyShift" else "$keyShift"}",
                    containerColor = Color(0xFF854D0E).copy(alpha = 0.85f),
                    onDecrease = { onKeyChange(keyShift - 1) },
                    onIncrease = { onKeyChange(keyShift + 1) },
                    modifier = Modifier.weight(1f)
                )

                AdjustmentPill(
                    label = "TEM: ${if (temShift >= 0) "+$temShift" else "$temShift"}",
                    containerColor = Color(0xFF065F46).copy(alpha = 0.85f),
                    onDecrease = { onTemChange(temShift - 1) },
                    onIncrease = { onTemChange(temShift + 1) },
                    modifier = Modifier.weight(1f)
                )

                AdjustmentPill(
                    label = "MEL: $melVol",
                    containerColor = Color(0xFF0D9488).copy(alpha = 0.85f),
                    onDecrease = { onMelChange(maxOf(0, melVol - 20)) },
                    onIncrease = { onMelChange(minOf(100, melVol + 20)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Search, Prev, Play/Pause, Next, Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton(
                    onClick = onSearchPress,
                    backgroundColor = Color(0xFF0D9488).copy(alpha = 0.85f),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                KeypadButton(
                    onClick = onPrevPress,
                    backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                KeypadButton(
                    onClick = onPlayPress,
                    backgroundColor = Color(0xFF2DD4BF),
                    focusedColor = Color(0xFF5EEAD4),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause", tint = Color(0xFF003731), modifier = Modifier.size(20.dp))
                }

                KeypadButton(
                    onClick = onNextPress,
                    backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                KeypadButton(
                    onClick = onStopPress,
                    backgroundColor = Color(0xFF991B1B).copy(alpha = 0.85f),
                    focusedColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // 4x4 Keypad Grid
            // Row 1: 1, 2, 3, Vocal Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DigitKey('1', onDigitPress, Modifier.weight(1f))
                DigitKey('2', onDigitPress, Modifier.weight(1f))
                DigitKey('3', onDigitPress, Modifier.weight(1f))
                KeypadButton(
                    onClick = onVocalToggle,
                    backgroundColor = if (vocalActive) Color(0xFF0D9488) else Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = if (vocalActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Vocal",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 2: 4, 5, 6, RSV List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DigitKey('4', onDigitPress, Modifier.weight(1f))
                DigitKey('5', onDigitPress, Modifier.weight(1f))
                DigitKey('6', onDigitPress, Modifier.weight(1f))
                KeypadButton(
                    onClick = onRsvListPress,
                    backgroundColor = Color(0xFF0D9488),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = if (reservationCount > 0) "RSV ($reservationCount)" else "RSV List",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Row 3: 7, 8, 9, Camera/BGV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DigitKey('7', onDigitPress, Modifier.weight(1f))
                DigitKey('8', onDigitPress, Modifier.weight(1f))
                DigitKey('9', onDigitPress, Modifier.weight(1f))
                KeypadButton(
                    onClick = onBgvToggle,
                    backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
                    focusedColor = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = "BGV Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 4: RES, 0, CAN, 1st RSV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton(
                    onClick = { onReservePress(false) },
                    backgroundColor = Color(0xFF2DD4BF),
                    focusedColor = Color(0xFF5EEAD4),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "RES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF003731)
                    )
                }

                DigitKey('0', onDigitPress, Modifier.weight(1f))

                KeypadButton(
                    onClick = onCancelPress,
                    backgroundColor = Color(0xFF991B1B).copy(alpha = 0.85f),
                    focusedColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "CAN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                KeypadButton(
                    onClick = { onReservePress(true) },
                    backgroundColor = Color(0xFF059669),
                    focusedColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "1st RSV",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
    modifier: Modifier = Modifier
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
            backgroundColor = Color.Transparent,
            focusedColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        KeypadButton(
            onClick = onIncrease,
            backgroundColor = Color.Transparent,
            focusedColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun DigitKey(
    digit: Char,
    onDigitPress: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    KeypadButton(
        onClick = { onDigitPress(digit) },
        backgroundColor = Color(0xFF0F3832).copy(alpha = 0.85f),
        focusedColor = Color(0xFF2DD4BF),
        modifier = modifier.height(38.dp)
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6FFFA)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0F3832).copy(alpha = 0.85f),
    focusedColor: Color = Color(0xFF2DD4BF),
    content: @Composable () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "scale")
    val bg by animateColorAsState(
        targetValue = if (focused) focusedColor else backgroundColor,
        label = "bg"
    )
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bg)
            .then(
                if (focused) Modifier.border(
                    2.dp,
                    Color.White,
                    shape
                ) else Modifier.border(
                    1.dp,
                    Color(0xFF2DD4BF).copy(alpha = 0.2f),
                    shape
                )
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onClick
            )
            .onFocusChanged { focused = it.isFocused },
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
    var query by remember { mutableStateOf("") }
    val filtered = remember(songs, query) {
        if (query.isBlank()) songs.take(40)
        else songs.filter {
            it.title.contains(query, ignoreCase = true) || it.singer.contains(query, ignoreCase = true) || it.code.toString().contains(query)
        }.take(40)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        tonalElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Song Search", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Clear, contentDescription = "Close", tint = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type song title, artist, or code...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.code }) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.displayTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${song.displaySinger()} • #${song.code}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        }
                        KeypadButton(
                            onClick = { onPlaySong(song) },
                            backgroundColor = Color(0xFF059669),
                            modifier = Modifier.padding(end = 8.dp).height(36.dp).width(64.dp)
                        ) {
                            Text("Play", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        KeypadButton(
                            onClick = { onReserveSong(song) },
                            backgroundColor = Color(0xFF2563EB),
                            modifier = Modifier.height(36.dp).width(64.dp)
                        ) {
                            Text("RES", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        tonalElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reserved Songs (${queue.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Row {
                    if (queue.isNotEmpty()) {
                        KeypadButton(
                            onClick = onClearQueue,
                            backgroundColor = Color(0xFF991B1B),
                            modifier = Modifier.height(36.dp).padding(end = 12.dp)
                        ) {
                            Text("Clear Queue", style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (queue.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reserved songs in queue", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(queue, key = { index, song -> "${song.code}_$index" }) { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.width(36.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.displayTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${song.displaySinger()} • #${song.code}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                            KeypadButton(
                                onClick = { onPlayNow(song) },
                                backgroundColor = Color(0xFF059669),
                                modifier = Modifier.padding(end = 6.dp).height(36.dp)
                            ) {
                                Text("Play", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                            if (index > 0) {
                                KeypadButton(
                                    onClick = { onMoveUp(index) },
                                    backgroundColor = Color(0xFF334155),
                                    modifier = Modifier.padding(end = 6.dp).height(36.dp)
                                ) {
                                    Text("▲", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                            KeypadButton(
                                onClick = { onRemove(song) },
                                backgroundColor = Color(0xFF991B1B),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Del", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricStack(lines: List<LyricLine>, lineIndex: Int) {
    val prev = if (lineIndex - 2 >= 0) lines[lineIndex - 2] else null
    val prev2 = if (lineIndex - 1 >= 0) lines[lineIndex - 1] else null
    val current = lines.getOrNull(lineIndex)
    val next = lines.getOrNull(lineIndex + 1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DimLine(text = prev?.fullText() ?: "")
        DimLine(text = prev2?.fullText() ?: "")
        Spacer(Modifier.height(18.dp))
        AnimatedContent(
            targetState = current?.fullText() ?: "",
            transitionSpec = { scaleIn() togetherWith scaleOut() },
            label = "currentLine"
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(18.dp))
        DimLine(text = next?.fullText() ?: "")
    }
}

@Composable
private fun KaraokeStack(
    lines: List<LyricLine>,
    lineIndex: Int,
    posMs: Double,
    currentTick: Long,
    fillColor: Color,
    fontSizeSp: Float
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
                    outlinePx = outlinePx
                )
            } else {
                KaraokeUpcomingLine(
                    line = topLine,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx
                )
            }
        } else {
            Spacer(Modifier.height(fontSizeSp.dp))
        }

        Spacer(Modifier.height(24.dp))

        if (bottomLine != null) {
            if (!isEven) {
                KaraokeLine(
                    line = bottomLine,
                    posMs = posMs,
                    currentTick = currentTick,
                    fillColor = fillColor,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx
                )
            } else {
                KaraokeUpcomingLine(
                    line = bottomLine,
                    baseStyle = baseStyle,
                    outlinePx = outlinePx
                )
            }
        } else {
            Spacer(Modifier.height(fontSizeSp.dp))
        }
    }
}

@Composable
private fun KaraokeUpcomingLine(
    line: LyricLine,
    baseStyle: TextStyle,
    outlinePx: Float
) {
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
            (maxWidthPx / measuredTotal).coerceIn(0.35f, 1f)
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
    outlinePx: Float
) {
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
            (maxWidthPx / measuredTotal).coerceIn(0.35f, 1f)
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

@Composable
private fun DimLine(text: String) {
    if (text.isBlank()) {
        Box(Modifier.height(12.dp))
        return
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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

    return remember(customPath) {
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
        FontFamily.Default
    }
}

private fun String?.isNullAndBlank(): Boolean = this == null || this.isBlank()

@Composable
private fun DefaultAnimatedBackground(modifier: Modifier = Modifier) {
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

    val color1 = Color(0xFF0F172A)
    val color2 = Color(0xFF1E1B4B)
    val color3 = Color(0xFF311B92)
    val color4 = Color(0xFF004D40)

    Box(
        modifier = modifier
            .background(
                Brush.sweepGradient(
                    listOf(color1, color2, color3, color4, color1)
                )
            )
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f))
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
    val videos = remember(bgvDir) {
        if (bgvDir == null || !bgvDir.isDirectory) emptyList()
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

    if (videos.isEmpty()) {
        DefaultAnimatedBackground(modifier = modifier)
        return
    }

    val activeVideo = remember(videos, bgvIndex) {
        videos[Math.abs(bgvIndex) % videos.size]
    }

    val exoPlayer = remember(context, activeVideo) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(activeVideo.absolutePath))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
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
    context: Context
) {
    val firstLineStart = lines.firstOrNull()?.startMs ?: 6000.0
    val showSongIntroCard = song.code != 0 && posMs < minOf(5000.0, firstLineStart - 300.0)

    val activeCountdown = remember(countdowns, posMs) {
        countdowns.find { cd ->
            val firstMs = if (cd.pipMs.isNotEmpty()) cd.pipMs[0] else cd.anchorMs - 2000.0
            posMs >= firstMs - 50.0 && posMs < cd.anchorMs
        }
    }

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
                    text = "Searching song code...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    } else if (song.code == 0) {
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
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
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
                Text("No lyrics", style = MaterialTheme.typography.titleLarge, color = Color.White)
            } else if (lyricStyle == LyricSettings.STYLE_KARAOKE) {
                KaraokeStack(
                    lines = lines,
                    lineIndex = lineIndex,
                    posMs = posMs,
                    currentTick = currentTick,
                    fillColor = KaraokeHighlight,
                    fontSizeSp = LyricSettings.getFontSizeLandscape(context)
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
