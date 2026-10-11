package com.outofthebox.songhubatv.ui.screens

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
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
    libraryRoot: File = LibraryRepository.defaultRoot(),
    isInPipMode: Boolean = false,
    isPipSupported: Boolean = false,
    onEnterPip: () -> Unit = {},
    onPlaybackStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val parsed by produceState(
        initialValue = LyricLoader.Parsed(emptyList()),
        key1 = song.code
    ) {
        value = LyricLoader.load(song)
    }

    if (parsed.error != null && parsed.lines.isEmpty()) {
        LyricUnavailable(song = song, error = parsed.error, onBack = onBack)
        return
    }

    LyricPlayer(
        song = song,
        lines = parsed.lines,
        tempoMap = parsed.tempoMap,
        ticksPerBeat = parsed.ticksPerBeat,
        totalMs = parsed.totalMs,
        context = context,
        libraryRoot = libraryRoot,
        onBack = onBack,
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
                text = song.displaySinger(),
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
    lines: List<LyricLine>,
    tempoMap: List<TempoUtils.TempoEntry>,
    ticksPerBeat: Int,
    totalMs: Double,
    context: Context,
    libraryRoot: File,
    onBack: () -> Unit,
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

    var playing by rememberSaveable { mutableStateOf(true) }
    var audioReady by rememberSaveable { mutableStateOf(false) }
    var audioNote by rememberSaveable { mutableStateOf<String?>(null) }
    var seeking by rememberSaveable { mutableStateOf(false) }
    var posMs by rememberSaveable { mutableDoubleStateOf(0.0) }
    var posTick by rememberSaveable { mutableLongStateOf(0L) }
    var scored by rememberSaveable { mutableStateOf(false) }
    var audioDurationMs by rememberSaveable { mutableDoubleStateOf(0.0) }
    var scoreResult by remember { mutableStateOf<ScoreResult?>(null) }
    val lyricStyle = remember { LyricSettings.getLyricStyle(context) }

    // Controls visibility (auto-hide after 3 seconds, tap to show)
    var controlsVisible by rememberSaveable { mutableStateOf(true) }

    // Key, Tempo, Melody states
    var keyShift by rememberSaveable { mutableIntStateOf(0) }
    var temShift by rememberSaveable { mutableIntStateOf(0) }
    var melVol by rememberSaveable { mutableIntStateOf(0) }

    val effectiveTotalMs = remember(totalMs, audioDurationMs) {
        maxOf(totalMs, audioDurationMs)
    }

    val bgvDir = remember(context, libraryRoot) {
        BgvSettings.getBgvDirectory(context, libraryRoot.absolutePath)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Auto-hide controls after 3 seconds when playing
    LaunchedEffect(controlsVisible, playing) {
        if (controlsVisible && playing) {
            delay(3000L)
            controlsVisible = false
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
        if (posMs == 0.0) {
            keyShift = 0
            temShift = 0
            melVol = 0
            engine.setKey(0)
            engine.setTempo(100f)
            engine.setMelodyVolume(0)
        } else {
            engine.setKey(keyShift)
            engine.setTempo((100 + temShift * 5).toFloat())
            engine.setMelodyVolume(melVol)
        }
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
                if (posMs > 0.0) {
                    engine.seekMs(posMs)
                }
                Log.i("PlayerScreen", "MIDI loaded: ${song.displayTitle()} via $sf, durationMs: $dur")
            } else {
                Log.w("PlayerScreen", "MIDI unavailable: ${song.displayTitle()}")
            }
        }
    }

    LaunchedEffect(playing, audioReady, effectiveTotalMs) {
        while (playing) {
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
                    playing = false
                    posMs = if (effectiveTotalMs > 0.0) effectiveTotalMs else posMs
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
                    playing = false
                    posMs = effectiveTotalMs
                }
            }
        }
        if (audioReady && engine.isPlaying()) {
            engine.pause()
        }
    }

    // Derived every time the transport moves instead of relaunching a coroutine each frame.
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
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isLandscape) 0.55f else 0.45f))
        )

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
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(8.dp))
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
                                text = "${song.displaySinger()} • ${song.code}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isPipSupported) {
                            IconButton(onClick = onEnterPip) {
                                Icon(
                                    imageVector = Icons.Filled.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White
                                )
                            }
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
                    if (lines.isEmpty()) {
                        Text("No lyrics", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    } else if (lyricStyle == LyricSettings.STYLE_KARAOKE) {
                        KaraokeStack(
                            lines = lines,
                            lineIndex = lineIndex,
                            posMs = posMs,
                            currentTick = posTick,
                            fillColor = KaraokeHighlight,
                            fontSizeSp = LyricSettings.getFontSizeLandscape(context)
                        )
                    } else {
                        LyricStack(lines = lines, lineIndex = lineIndex)
                    }
                }

                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = fmtMs(posMs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(onClick = {
                                        engine.restart()
                                        posMs = 0.0
                                        scored = false
                                        playing = true
                                    }) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "Restart", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (playing) {
                                                playing = false
                                            } else {
                                                if (posMs >= effectiveTotalMs || (audioReady && engine.isFinished())) {
                                                    engine.restart()
                                                    posMs = 0.0
                                                    scored = false
                                                }
                                                playing = true
                                            }
                                        },
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (playing) "Pause" else "Play",
                                            modifier = Modifier.size(32.dp),
                                            tint = KaraokeHighlight
                                        )
                                    }
                                }

                                Text(
                                    text = fmtMs(effectiveTotalMs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            AdjustmentBar(
                                keyShift = keyShift,
                                temShift = temShift,
                                melVol = melVol,
                                onKeyDecrease = {
                                    if (keyShift > -8) {
                                        keyShift--
                                        engine.setKey(keyShift)
                                    }
                                },
                                onKeyIncrease = {
                                    if (keyShift < 8) {
                                        keyShift++
                                        engine.setKey(keyShift)
                                    }
                                },
                                onTemDecrease = {
                                    if (temShift > -8) {
                                        temShift--
                                        engine.setTempo((100 + temShift * 5).toFloat())
                                    }
                                },
                                onTemIncrease = {
                                    if (temShift < 8) {
                                        temShift++
                                        engine.setTempo((100 + temShift * 5).toFloat())
                                    }
                                },
                                onMelDecrease = {
                                    if (melVol > 0) {
                                        melVol -= 20
                                        engine.setMelodyVolume(melVol)
                                    }
                                },
                                onMelIncrease = {
                                    if (melVol < 100) {
                                        melVol += 20
                                        engine.setMelodyVolume(melVol)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 16.dp),
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
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Spacer(Modifier.width(12.dp))
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
                                    append(song.displaySinger())
                                    if (song.style.isNotBlank()) append(" • ").append(song.style)
                                    append(" • ").append(song.code)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isPipSupported) {
                            IconButton(onClick = onEnterPip) {
                                Icon(
                                    imageVector = Icons.Filled.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture"
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (lines.isEmpty()) {
                    Text("No lyrics", style = MaterialTheme.typography.titleLarge)
                } else if (lyricStyle == LyricSettings.STYLE_KARAOKE) {
                    KaraokeStack(
                        lines = lines,
                        lineIndex = lineIndex,
                        posMs = posMs,
                        currentTick = posTick,
                        fillColor = KaraokeHighlight,
                        fontSizeSp = LyricSettings.getFontSizePortrait(context)
                    )
                } else {
                    LyricStack(lines = lines, lineIndex = lineIndex)
                }

                Spacer(Modifier.weight(1f))

                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AdjustmentBar(
                            keyShift = keyShift,
                            temShift = temShift,
                            melVol = melVol,
                            onKeyDecrease = {
                                if (keyShift > -8) {
                                    keyShift--
                                    engine.setKey(keyShift)
                                }
                            },
                            onKeyIncrease = {
                                if (keyShift < 8) {
                                    keyShift++
                                    engine.setKey(keyShift)
                                }
                            },
                            onTemDecrease = {
                                if (temShift > -8) {
                                    temShift--
                                    engine.setTempo((100 + temShift * 5).toFloat())
                                }
                            },
                            onTemIncrease = {
                                if (temShift < 8) {
                                    temShift++
                                    engine.setTempo((100 + temShift * 5).toFloat())
                                }
                            },
                            onMelDecrease = {
                                if (melVol > 0) {
                                    melVol -= 20
                                    engine.setMelodyVolume(melVol)
                                }
                            },
                            onMelIncrease = {
                                if (melVol < 100) {
                                    melVol += 20
                                    engine.setMelodyVolume(melVol)
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = {
                                engine.restart()
                                posMs = 0.0
                                scored = false
                                playing = true
                            }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Restart")
                            }
                            IconButton(
                                onClick = {
                                    if (playing) {
                                        playing = false
                                    } else {
                                        if (posMs >= effectiveTotalMs || (audioReady && engine.isFinished())) {
                                            engine.restart()
                                            posMs = 0.0
                                            scored = false
                                        }
                                        playing = true
                                    }
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (playing) "Pause" else "Play",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = fmtMs(posMs),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = fmtMs(effectiveTotalMs),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (audioNote != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = audioNote!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        if (scoreResult != null) {
            val res = scoreResult!!
            ScoreOverlay(
                score = res.score,
                songTitle = song.displayTitle(),
                songSinger = song.displaySinger(),
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
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BackgroundVideoPlayer(
    bgvDir: File?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (BgvSettings.getMode(context) == 3) return // BGV disabled in Settings

    val videoFiles = remember(bgvDir) {
        if (bgvDir == null || !bgvDir.exists() || !bgvDir.isDirectory) {
            emptyList()
        } else {
            bgvDir.walkTopDown().filter { file ->
                file.isFile && file.extension.equals("mp4", ignoreCase = true)
            }.toList()
        }
    }

    if (videoFiles.isEmpty()) return

    val randomVideo = remember(videoFiles) {
        videoFiles.random()
    }

    val exoPlayer = remember(context, randomVideo) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(randomVideo.toURI().toString()))
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
private fun AdjustmentBar(
    keyShift: Int,
    temShift: Int,
    melVol: Int,
    onKeyDecrease: () -> Unit,
    onKeyIncrease: () -> Unit,
    onTemDecrease: () -> Unit,
    onTemIncrease: () -> Unit,
    onMelDecrease: () -> Unit,
    onMelIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdjustControl(
            label = "KEY",
            valueDisplay = if (keyShift > 0) "+$keyShift" else "$keyShift",
            onDecrease = onKeyDecrease,
            onIncrease = onKeyIncrease,
            modifier = Modifier.weight(1f)
        )
        AdjustControl(
            label = "TEM",
            valueDisplay = if (temShift > 0) "+$temShift" else "$temShift",
            onDecrease = onTemDecrease,
            onIncrease = onTemIncrease,
            modifier = Modifier.weight(1f)
        )
        AdjustControl(
            label = "MEL",
            valueDisplay = "$melVol",
            onDecrease = onMelDecrease,
            onIncrease = onMelIncrease,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdjustControl(
    label: String,
    valueDisplay: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Decrease $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = valueDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Increase $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberLyricFontFamily(): FontFamily {
    val context = LocalContext.current
    return remember(context) {
        val customPath = LyricSettings.getCustomFontPath(context)
        if (!customPath.isNullOrEmpty()) {
            try {
                val customFile = File(customPath)
                if (customFile.exists()) {
                    return@remember FontFamily(Font(customFile))
                }
            } catch (_: Exception) {
            }
        }
        try {
            FontFamily(Font(R.font.videoke))
        } catch (_: Exception) {
            FontFamily.Default
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

    // Alternating 2-line videoke slot logic:
    // If lineIndex is even (% 2 == 0): Slot 1 (Top) is Active, Slot 2 (Bottom) is Upcoming.
    // If lineIndex is odd (% 2 == 1): Slot 1 (Top) is Upcoming, Slot 2 (Bottom) is Active.
    val isEven = lineIndex % 2 == 0
    val topLine = if (isEven) lines.getOrNull(lineIndex) else lines.getOrNull(lineIndex + 1)
    val bottomLine = if (isEven) lines.getOrNull(lineIndex + 1) else lines.getOrNull(lineIndex)

    LaunchedEffect(lineIndex) {
        val dbg = lines.getOrNull(lineIndex) ?: return@LaunchedEffect
        Log.i("KaraokeDebug", "line=$lineIndex start=${dbg.startMs} end=${dbg.endMs} frags=${dbg.fragments.size}")
        dbg.fragments.forEachIndexed { i, f ->
            Log.i("KaraokeDebug", "  f$i '${f.text.replace("\n", "\\n")}' abs=${f.absMs} end=${f.endMs} durT=${f.durationTicks}")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Slot 1: Top Line
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

        // Slot 2: Bottom Line
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
            // Unfilled text: black outline behind white fill
            Text(text, style = strokeStyle, color = Color.Black, maxLines = 1, softWrap = false)
            Text(text, style = style, color = Color.White, maxLines = 1, softWrap = false)
            // Filled text: white outline behind indigo fill, clipped to progress
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

/**
 * Distance, in unscaled text pixels, that the melody has advanced through [line].
 *
 * Every syllable contributes its own measured width: completed syllables add their full width and
 * the syllable currently being sung adds a time-weighted fraction of its width. Instrumental gaps
 * between syllables hold the wipe still, so the highlight tracks the vocals rather than the clock.
 */
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

    if (currentTick > 0L) {
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

private fun fmtMs(ms: Double): String {
    val totalSeconds = (ms / 1000).toInt()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}