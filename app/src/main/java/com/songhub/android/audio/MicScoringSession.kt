package com.songhub.android.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.log10
import kotlin.math.sqrt

/** Aggregated statistics over one continuous microphone take. */
data class MicScoreStats(
    /** Fraction of ~50 ms frames whose level sits clearly above the ambient noise floor. */
    val voiceActivityRatio: Double,
    /** 0..1 — how steady the singer's volume was while singing (1 = very steady). */
    val loudnessConsistency: Double,
    /** Average level of the active frames in dBFS (e.g. -18.0). */
    val avgLevelDb: Double,
    /** Number of ~50 ms frames captured. */
    val capturedFrames: Int
)

/**
 * Captures the singer's voice with [AudioRecord] while a song plays and
 * aggregates level statistics used by [com.songhub.android.song.ScoreCalculator]
 * for the MICROPHONE scoring mode.
 */
class MicScoringSession(private val context: Context) {

    private val frameLevels = ArrayList<Double>(4096)

    @Volatile
    private var capturing = false
    private var audioRecord: AudioRecord? = null
    private var captureThread: Thread? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /** Starts capturing. Safe to call repeatedly; returns false when the mic is unavailable. */
    @Synchronized
    fun start(): Boolean {
        if (capturing) return true
        if (!hasPermission()) return false
        return try {
            val sampleRate = 16000
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return false
            val bufSize = maxOf(minBuf, sampleRate / 10) // ≥100 ms of headroom
            val record = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .build()
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return false
            }
            audioRecord = record
            capturing = true
            captureThread = Thread({
                try {
                    record.startRecording()
                    val buf = ShortArray(bufSize / 2)
                    while (capturing) {
                        val read = record.read(buf, 0, buf.size)
                        if (read > 0) {
                            var sumSq = 0.0
                            for (i in 0 until read) {
                                val s = buf[i] / 32768.0
                                sumSq += s * s
                            }
                            val rms = sqrt(sumSq / read)
                            val db = if (rms > 1e-7) 20.0 * log10(rms) else -90.0
                            synchronized(frameLevels) { frameLevels.add(db.coerceAtLeast(-90.0)) }
                        } else if (read < 0) {
                            break
                        }
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Mic capture thread failed", t)
                }
            }, "MicScoringCapture").also { it.start() }
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to start mic capture", t)
            stopCapturing()
            false
        }
    }

    /** Stops capturing but keeps the collected levels for a later [finish]. */
    @Synchronized
    fun stopCapturing() {
        capturing = false
        try {
            captureThread?.join(500)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        captureThread = null
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * Stops capturing and returns statistics over everything captured since the
     * last [reset], or null when there is not enough usable audio (mic missing,
     * permission denied, or a take shorter than ~3 seconds).
     */
    fun finish(): MicScoreStats? {
        stopCapturing()
        val levels: List<Double>
        synchronized(frameLevels) { levels = frameLevels.toList() }
        if (levels.size < MIN_FRAMES) return null
        val sorted = levels.sorted()
        val noiseFloorDb = sorted[(sorted.size * 0.10).toInt().coerceIn(0, sorted.size - 1)]
        val gateDb = noiseFloorDb + GATE_MARGIN_DB
        val active = levels.filter { it >= gateDb }
        if (active.isEmpty()) {
            return MicScoreStats(0.0, 0.0, -90.0, levels.size)
        }
        val mean = active.average()
        var variance = 0.0
        for (level in active) {
            val d = level - mean
            variance += d * d
        }
        val std = sqrt(variance / active.size)
        val consistency = ((CONSISTENCY_FLOOR_DB - std) / (CONSISTENCY_FLOOR_DB - CONSISTENCY_IDEAL_DB))
            .coerceIn(0.0, 1.0)
        return MicScoreStats(
            voiceActivityRatio = active.size.toDouble() / levels.size,
            loudnessConsistency = consistency,
            avgLevelDb = mean,
            capturedFrames = levels.size
        )
    }

    /** Drops any captured levels (call when a new song starts or on Sing Again). */
    fun reset() {
        synchronized(frameLevels) { frameLevels.clear() }
    }

    companion object {
        private const val TAG = "MicScoringSession"
        private const val MIN_FRAMES = 60 // ~3 s of 50 ms frames
        private const val GATE_MARGIN_DB = 12.0
        private const val CONSISTENCY_IDEAL_DB = 3.0
        private const val CONSISTENCY_FLOOR_DB = 15.0
    }
}
