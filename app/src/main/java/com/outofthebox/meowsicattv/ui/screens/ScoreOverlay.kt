package com.outofthebox.meowsicattv.ui.screens

import android.content.Context
import android.graphics.Paint
import android.media.MediaPlayer
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meowsicat.android.song.ScoreCalculator
import com.meowsicat.android.storage.ScoreSettings
import com.outofthebox.meowsicattv.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

private val CONFETTI_COLORS = intArrayOf(
    0xFFFFDF35.toLong().toInt(), 0xFFFF00FF.toLong().toInt(), 0xFF00FF00.toLong().toInt(),
    0xFF00BFFF.toLong().toInt(), 0xFFFF6EC7.toLong().toInt(), 0xFFFF4500.toLong().toInt()
)
private val BALLOON_COLORS = intArrayOf(
    0xFFFF1744.toLong().toInt(), 0xFF29B6F6.toLong().toInt(), 0xFFFFD54F.toLong().toInt(), 0xFF66BB6A.toLong().toInt(),
    0xFFAB47BC.toLong().toInt(), 0xFFFF7043.toLong().toInt(), 0xFFEC407A.toLong().toInt()
)
private val BURST_COLORS = intArrayOf(
    0xFFFF0000.toLong().toInt(), 0xFFFFFFFF.toInt(), 0xFFFFD700.toLong().toInt(), 0xFF0000FF.toLong().toInt(), 0xFFFF69B4.toLong().toInt()
)

private val ScoreNumberFont = FontFamily(Font(R.font.score_number))
private val ScoreGreetFont = FontFamily(Font(R.font.score_greet))

private class Confetti {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var size = 0f
    var rotation = 0f
    var vr = 0f
    var color = 0
}

private class BurstingStar {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var size = 0f
    var alpha = 255
    var delay = 0L
    var color = -1
}

private class Balloon {
    var x = 0f
    var y = 0f
    var vy = 0f
    var width = 0f
    var height = 0f
    var swaySpeed = 0f
    var phaseOffset = 0f
    var delay = 0L
    var color = 0
}

private class PoppingLike {
    var x = 0f
    var y = 0f
    var delay = 0L
    var scale = 0f
    var alpha = 255
}

private class ScoreOverlayState {
    val confetti = ArrayList<Confetti>()
    val burstStars = ArrayList<BurstingStar>()
    val balloons = ArrayList<Balloon>()
    val likes = ArrayList<PoppingLike>()
}

/** Number of stars (0..5) earned for a score — used by the modern star row. */
private fun starCountForScore(score: Int): Int = when {
    score >= 91 -> 5
    score >= 81 -> 4
    score >= 71 -> 3
    score >= 61 -> 2
    score >= 41 -> 1
    else -> 0
}

private val ACCENT_GOLD = Color(0xFFFFC93C)
private val ACCENT_GOLD_LIGHT = Color(0xFFFFF6D6)

/** Accent colour for the reveal, graded by score band. */
private fun accentFor(score: Int): Color = when {
    score <= 60 -> Color(0xFFFF5252)
    score <= 80 -> Color(0xFFFFB74D)
    score <= 90 -> Color(0xFFFFE066)
    score <= 99 -> Color(0xFF7DE87D)
    else -> ACCENT_GOLD
}

private fun accentLightFor(score: Int): Color = when {
    score <= 60 -> Color(0xFFFFB4AB)
    score <= 80 -> Color(0xFFFFE0B2)
    score <= 90 -> Color(0xFFFFF6C9)
    score <= 99 -> Color(0xFFCCFFCC)
    else -> ACCENT_GOLD_LIGHT
}

private fun fanfareRes(score: Int): Int = when {
    score == 100 -> R.raw.score_100
    score >= 91 -> R.raw.score_91_99
    score >= 81 -> R.raw.score_81_90
    score >= 76 -> R.raw.score_76_80
    score >= 61 -> R.raw.score_61_75
    else -> 0
}

private fun screenSize(context: Context): Size {
    val metrics = context.resources.displayMetrics
    return Size(metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat())
}

private fun seedConfetti(state: ScoreOverlayState, random: Random, size: Size) {
    for (i in 0 until 100) {
        val c = Confetti()
        c.x = random.nextFloat() * size.width
        c.y = -random.nextFloat() * size.height
        c.vx = (random.nextFloat() - 0.5f) * 10f
        c.vy = random.nextFloat() * 15f + 10f
        c.size = random.nextFloat() * 20f + 10f
        c.rotation = random.nextFloat() * 360f
        c.vr = (random.nextFloat() - 0.5f) * 20f
        c.color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.size)]
        state.confetti.add(c)
    }
}

private fun seedBurst(state: ScoreOverlayState, random: Random, cx: Float, cy: Float) {
    for (i in 0 until 80) {
        val s = BurstingStar()
        s.x = cx
        s.y = cy
        val angle = random.nextDouble() * 2.0 * PI
        val speed = random.nextFloat() * 10f + 4f
        s.vx = (cos(angle) * speed).toFloat()
        s.vy = (sin(angle) * speed).toFloat()
        s.size = random.nextFloat() * 30f + 15f
        s.delay = random.nextLong(400L)
        s.color = BURST_COLORS[random.nextInt(BURST_COLORS.size)]
        state.burstStars.add(s)
    }
}

private fun seedBalloons(state: ScoreOverlayState, random: Random, size: Size) {
    for (i in 0 until 35) {
        val b = Balloon()
        b.x = random.nextFloat() * 0.85f * size.width + 0.075f * size.width
        b.y = size.height + random.nextFloat() * 250f + 40f
        b.vy = -(random.nextFloat() * 3f + 2.2f)
        b.width = random.nextFloat() * 20f + 35f
        b.height = b.width * 1.25f
        b.swaySpeed = random.nextFloat() * 0.04f + 0.02f
        b.phaseOffset = random.nextFloat() * 10f
        b.delay = random.nextLong(1400L) + 200L
        b.color = BALLOON_COLORS[random.nextInt(BALLOON_COLORS.size)]
        state.balloons.add(b)
    }
}

private fun seedLikes(state: ScoreOverlayState, random: Random) {
    for (i in 0 until 25) {
        val l = PoppingLike()
        l.x = random.nextFloat() * 0.8f + 0.1f
        l.y = random.nextFloat() * 0.8f + 0.1f
        l.delay = random.nextLong(1500L)
        state.likes.add(l)
    }
}

private fun starAt(cx: Float, cy: Float, size: Float): Path {
    val half = size / 2f
    val inner = half / 2.5f
    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) half else inner
        val angle = i * 0.6283185307179586
        val px = (cx + sin(angle) * radius).toFloat()
        val py = (cy - cos(angle) * radius).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

private fun DrawScope.drawBalloon(b: Balloon) {
    val cx = b.x
    val cy = b.y + b.height / 2f
    val sway = sin(b.phaseOffset.toDouble()).toFloat() * 10f
    val string = Path()
    string.moveTo(cx, cy)
    string.quadraticTo(cx + sway, cy + 20f, cx - sway / 2f, cy + 45f)
    drawPath(string, color = Color(0xAAFFFFFF), style = Stroke(width = 3f))
    drawCircle(Color(b.color), radius = 4f, center = Offset(cx, cy + 2f))
    drawOval(
        color = Color(b.color),
        topLeft = Offset(cx - b.width / 2f, b.y - b.height / 2f),
        size = Size(b.width, b.height)
    )
    drawOval(
        color = Color(0x78FFFFFF),
        topLeft = Offset(cx - b.width * 0.28f, b.y - b.height * 0.3f),
        size = Size(b.width * 0.26f, b.height * 0.22f)
    )
}

/**
 * Modern animated "score dial": a glowing ring that sweeps to the achieved score with the
 * big gradient number counting up inside it.
 */
@Composable
private fun ScoreDial(
    score: Int,
    displayScore: Int,
    arcReveal: Float,
    accent: Color,
    accentLight: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(264.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                )
            )

            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            val sweep = 360f * (score.coerceIn(0, 100) / 100f) * arcReveal
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(accentLight, accent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$displayScore",
                style = TextStyle(
                    fontFamily = ScoreNumberFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 88.sp,
                    brush = Brush.linearGradient(listOf(accentLight, accent))
                )
            )
            Text(
                text = "OUT OF 100",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun StarRating(score: Int, progress: Float, accent: Color) {
    val count = starCountForScore(score)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (i in 0 until 5) {
            val earned = i < count
            val fill = if (earned) (progress - i).coerceIn(0f, 1f) else 1f
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (earned) accent else Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        if (earned) {
                            val s = 0.35f + 0.65f * fill
                            scaleX = s
                            scaleY = s
                            alpha = 0.2f + 0.8f * fill
                        }
                    }
            )
        }
    }
}

@Composable
fun ScoreOverlay(
    score: Int,
    songTitle: String,
    songSinger: String,
    onSingAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state = remember { ScoreOverlayState() }
    val random = remember { Random(System.nanoTime()) }
    var feedbackStarted by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var shakeX by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var activePlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val accent = remember(score) { accentFor(score) }
    val accentLight = remember(score) { accentLightFor(score) }

    val rootAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(500),
        label = "scoreReveal"
    )

    val contentEnter by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow),
        label = "contentEnter"
    )

    val arcReveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(1400, easing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)),
        label = "arcReveal"
    )

    val displayScore by animateIntAsState(
        targetValue = if (revealed) score else 0,
        animationSpec = tween(1500, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)),
        label = "displayScore"
    )

    val starProgress by animateFloatAsState(
        targetValue = if (feedbackStarted) starCountForScore(score).toFloat() else 0f,
        animationSpec = tween(
            durationMillis = (starCountForScore(score) * 220).coerceAtLeast(1),
            easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
        ),
        label = "starProgress"
    )

    val messageAlpha by animateFloatAsState(
        targetValue = if (feedbackStarted) 1f else 0f,
        animationSpec = tween(600),
        label = "messageAlpha"
    )

    DisposableEffect(Unit) {
        onDispose {
            activePlayer?.release()
        }
    }

    // Short beat after the song ends before the score reveal, so the transition from
    // playback -> scoring feels intentional. Held here (inside the overlay) rather than
    // in the playback poll loop so it is not cancelled when that effect tears down.
    LaunchedEffect(Unit) {
        delay(1000)
        revealed = true
    }

    LaunchedEffect(revealed) {
        if (!revealed) return@LaunchedEffect
        delay(1500)
        feedbackStarted = true
    }

    LaunchedEffect(feedbackStarted) {
        if (!feedbackStarted) return@LaunchedEffect
        if (score <= 60) {
            for (v in listOf(10f, -10f, 10f, -10f, 0f)) {
                shakeX = v
                delay(100)
            }
            shakeX = 0f
        }
    }

    LaunchedEffect(feedbackStarted) {
        if (!feedbackStarted) return@LaunchedEffect
        val size = screenSize(context)
        when {
            score <= 60 -> Unit
            score <= 80 -> Unit
            score <= 90 -> seedLikes(state, random)
            score <= 99 -> seedConfetti(state, random, size)
            else -> {
                seedBurst(state, random, size.width / 2f, size.height / 2f)
                seedBalloons(state, random, size)
            }
        }
        val resId = fanfareRes(score)
        if (resId != 0 && ScoreSettings.getFanfareLevel(context) > 0) {
            val volume = when (ScoreSettings.getFanfareLevel(context)) {
                ScoreSettings.FANFARE_LOW -> 0.3f
                ScoreSettings.FANFARE_MID -> 0.6f
                else -> 1.0f
            }
            activePlayer?.release()
            activePlayer = MediaPlayer.create(context, resId)?.also {
                it.setVolume(volume, volume)
                it.setOnCompletionListener { mp -> mp.release() }
                it.start()
            }
        }
        kotlinx.coroutines.delay(8000)
        onDismiss()
    }

    var lastNanos = -1L
    LaunchedEffect(Unit) {
        while (true) {
            val now = withFrameNanos { it }
            val frames = if (lastNanos < 0) 1f else ((now - lastNanos) / 16_666_667L).toFloat().coerceIn(0.1f, 6f)
            lastNanos = now
            if (feedbackStarted) {
                state.confetti.forEach { c ->
                    c.x += c.vx * frames
                    c.y += c.vy * frames
                    c.rotation += c.vr * frames
                }
                state.burstStars.forEach { s ->
                    if (s.delay > 0) s.delay -= 1
                    if (s.delay <= 0) {
                        s.x += s.vx * frames
                        s.y += s.vy * frames
                        s.vx *= 0.97f
                        s.vy *= 0.97f
                        s.alpha = (s.alpha - 2).coerceAtLeast(0)
                    }
                }
                state.balloons.forEach { b ->
                    if (b.delay > 0) b.delay -= 1
                    if (b.delay <= 0) {
                        b.phaseOffset += b.swaySpeed * frames
                        b.y += b.vy * frames
                        b.x += sin(b.phaseOffset) * 0.8f * frames
                    }
                }
                state.likes.forEach { l ->
                    if (l.delay > 0) l.delay -= 1
                    if (l.delay <= 0) {
                        if (l.scale < 1.2f) l.scale += 0.08f * frames
                        l.alpha = (l.alpha - 5).coerceAtLeast(0)
                    }
                }
                val bottom = screenSize(context).height
                state.confetti.removeAll { it.y > bottom + 50f }
                state.burstStars.removeAll { it.delay <= 0 && it.alpha <= 0 }
                state.balloons.removeAll { it.delay <= 0 && it.y + it.height < -100f }
                state.likes.removeAll { it.delay <= 0 && it.alpha <= 0 }
            }
            frameTick++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = rootAlpha }
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B1430), Color(0xFF07060D))
                )
            )
            .background(Color(0x99000000))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            frameTick
            val w = this.size.width
            val h = this.size.height

            state.confetti.forEach { c ->
                withTransform({
                    translate(c.x, c.y)
                    rotate(c.rotation)
                }) {
                    drawRect(
                        color = Color(c.color),
                        topLeft = Offset(-c.size / 2f, -c.size / 2f),
                        size = Size(c.size, c.size)
                    )
                }
            }

            state.burstStars.forEach { s ->
                if (s.delay <= 0 && s.alpha > 0) {
                    drawPath(starAt(s.x, s.y, s.size), Color(s.color).copy(alpha = s.alpha / 255f))
                }
            }

            state.balloons.forEach { b ->
                if (b.delay <= 0) drawBalloon(b)
            }

            state.likes.forEach { l ->
                if (l.delay <= 0 && l.alpha > 0) {
                    withTransform({
                        translate(l.x * w, l.y * h)
                        scale(l.scale, l.scale)
                    }) {
                        drawIntoCanvas { canvas ->
                            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                            textPaint.textAlign = Paint.Align.CENTER
                            textPaint.textSize = 60.sp.toPx()
                            textPaint.alpha = l.alpha
                            canvas.nativeCanvas.drawText("\uD83D\uDC4D", 0f, 0f, textPaint)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 44.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = songTitle,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = songSinger,
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = shakeX
                    val s = 0.86f + 0.14f * contentEnter
                    scaleX = s
                    scaleY = s
                    alpha = contentEnter
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreDial(
                score = score,
                displayScore = displayScore,
                arcReveal = arcReveal,
                accent = accent,
                accentLight = accentLight
            )
            Spacer(Modifier.height(22.dp))
            StarRating(score = score, progress = starProgress, accent = accent)
            Spacer(Modifier.height(18.dp))
            Text(
                text = ScoreCalculator.getMessageForScore(score),
                color = accentLight,
                fontFamily = ScoreGreetFont,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .graphicsLayer { alpha = messageAlpha }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSingAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color(0xFF1B1430)
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sing Again", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(20.dp))
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Close")
            }
        }
    }
}
