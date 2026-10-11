package com.outofthebox.songhubatv.data

import com.songhub.android.database.NativeLib
import com.songhub.android.model.CountdownGroup
import com.songhub.android.model.Fragment
import com.songhub.android.model.LyricLine
import com.songhub.android.parser.CountdownGrouper
import com.songhub.android.parser.LineBuilder
import com.songhub.android.parser.MidiParser
import com.songhub.android.parser.TempoUtils
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToLong

object LyricLoader {
    private val lyricCache = ConcurrentHashMap<Int, Parsed>()
    private const val MAX_FILL_MS = 5000.0
    private const val ALIGN_WINDOW = 80L
    private const val MIN_MELODY_RATIO = 0.5
    private const val MIN_SPAN_LEN = 40L
    private const val MIN_FINAL_GAP = 60L

    data class Parsed(
        val lines: List<LyricLine>,
        val countdowns: List<CountdownGroup> = emptyList(),
        val tempoMap: List<TempoUtils.TempoEntry> = emptyList(),
        val ticksPerBeat: Int = 0,
        val totalMs: Double = 0.0,
        val error: String? = null
    )

    suspend fun load(song: SongItem): Parsed = withContext(Dispatchers.IO) {
        if (song.code == 0) {
            return@withContext Parsed(emptyList())
        }
        if (lyricCache.containsKey(song.code)) {
            return@withContext lyricCache[song.code]!!
        }
        val parsed = try {
            val bytes = midiBytes(song) ?: throw IOException("Lyrics unavailable for this title")
            val result = MidiParser.parse(bytes, song.title)
            val lines = LineBuilder.build(result.lyricEventsTicks, "", result.ticksPerBeat)
            val tempoMap = result.tempoMap
            val ppqn = result.ticksPerBeat
            if (tempoMap.isNotEmpty() && ppqn > 0) {
                val guideSpans = pickMelodySpans(result)
                val timingSpans = collectTimingSpans(result)
                if (guideSpans != null || timingSpans.isNotEmpty()) {
                    applyMelodyTiming(lines, guideSpans ?: emptyList(), timingSpans, tempoMap, ppqn)
                } else {
                    for (line in lines) {
                        for (fragment in line.fragments) {
                            if (fragment.endMs > fragment.absMs) {
                                fragment.endTick =
                                    Math.round(TempoUtils.msToTick(tempoMap, fragment.endMs, ppqn))
                            }
                        }
                    }
                }
            }
            val countdowns = try {
                CountdownGrouper.build(result.countdownEventsTicks, lines, tempoMap, ppqn)
            } catch (t: Throwable) {
                emptyList()
            }
            val totalMs = lines.maxOfOrNull { it.endMs } ?: 0.0
            Parsed(
                lines = lines,
                countdowns = countdowns,
                tempoMap = tempoMap,
                ticksPerBeat = ppqn,
                totalMs = totalMs
            )
        } catch (t: Throwable) {
            Parsed(emptyList(), error = t.message ?: t.toString())
        }
        lyricCache[song.code] = parsed
        parsed
    }

    private fun pickMelodySpans(result: MidiParser.ParseResult): List<MidiParser.NoteSpan>? {
        val races = result.noteRunsByChannel ?: return null
        val lyricTicks = result.lyricEventsTicks.map { it.tick }
        if (lyricTicks.isEmpty()) return null
        var bestChannel = -1
        var bestCount = 0
        var bestNotes = Int.MAX_VALUE
        for (channel in 0 until races.size) {
            if (channel == 9) continue
            val spans = races[channel] ?: continue
            var count = 0
            outer@ for (lyricTick in lyricTicks) {
                for (span in spans) {
                    if (abs(span.onTick - lyricTick) <= ALIGN_WINDOW) {
                        count++
                        continue@outer
                    }
                }
            }
            if (count > bestCount || (count == bestCount && spans.size < bestNotes)) {
                bestCount = count
                bestChannel = channel
                bestNotes = spans.size
            }
        }
        if (bestCount < Math.ceil(lyricTicks.size * MIN_MELODY_RATIO).toInt() ||
            bestCount < 1
        ) {
            return null
        }
        Log.i("KaraokeDebug", "melody channel=$bestChannel hit=${bestCount}/${lyricTicks.size}")
        return races[bestChannel]
    }

    private fun collectTimingSpans(result: MidiParser.ParseResult): List<MidiParser.NoteSpan> {
        val races = result.noteRunsByChannel ?: return emptyList()
        val out = ArrayList<MidiParser.NoteSpan>()
        for (channel in 0 until races.size) {
            if (channel == 9) continue
            val spans = races[channel] ?: continue
            if (spans.isEmpty()) continue
            val byLen = spans.sortedBy { it.offTick - it.onTick }
            val median = byLen[byLen.size / 2].offTick - byLen[byLen.size / 2].onTick
            if (median < MIN_SPAN_LEN) continue
            out.addAll(spans)
        }
        return out
    }

    private fun applyMelodyTiming(
        lines: List<LyricLine>,
        guideSpans: List<MidiParser.NoteSpan>,
        timingSpans: List<MidiParser.NoteSpan>,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int
    ) {
        for (lineIdx in lines.indices) {
            val line = lines[lineIdx]
            if (line.fragments.isEmpty()) continue
            val fragments = line.fragments
            val nextLine = if (lineIdx + 1 < lines.size) lines[lineIdx + 1] else null
            val nextLineStartMs = nextLine?.startMs
            if (fragments.size == 1) {
                val fragment = fragments[0]
                val words = splitWords(fragment.text)
                val split = if (words.size >= 2 && fragment.rubyData.isEmpty() && nextLine != null) {
                    splitWordsTimed(
                        fragment,
                        words,
                        timingSpans,
                        nextLine,
                        tempoMap,
                        ppqn
                    )
                } else {
                    null
                }
                if (split != null) {
                    line.fragments = split
                    line.startMs = line.fragments[0].absMs
                    line.endMs = line.fragments[line.fragments.size - 1].endMs
                    continue
                }
            }
            for (i in fragments.indices) {
                bindFragmentByNote(
                    fragments[i],
                    guideSpans.ifEmpty { timingSpans },
                    tempoMap,
                    ppqn,
                    if (i + 1 < fragments.size) fragments[i + 1].absMs else null,
                    nextLineStartMs
                )
            }
            line.startMs = fragments[0].absMs
            line.endMs = fragments[fragments.size - 1].endMs
        }
    }

    private fun splitWords(text: String): List<String> {
        val matcher = Regex("\\S+\\s*").findAll(text)
        val words = matcher.map { it.value }.toMutableList()
        if (words.isEmpty()) return listOf(text)
        val parenOnly = Regex("^[()\\s]+$")
        while (words.size > 1 && parenOnly.matches(words[0].trim())) {
            words[1] = words[0] + words[1]
            words.removeAt(0)
        }
        while (words.size > 1 && parenOnly.matches(words[words.size - 1].trim())) {
            words[words.size - 2] = words[words.size - 2] + words[words.size - 1]
            words.removeAt(words.size - 1)
        }
        return words
    }

    private fun splitWordsTimed(
        fragment: Fragment,
        words: List<String>,
        timingSpans: List<MidiParser.NoteSpan>,
        nextLine: LyricLine,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int
    ): List<Fragment>? {
        val windowStart = fragment.absTick
        val windowEnd = nextLine.fragments[0].absTick
        if (windowEnd <= windowStart) return null
        val inWindow = timingSpans.filter { it.onTick > windowStart && it.onTick < windowEnd }
        if (inWindow.isEmpty()) return null
        val maxOff = inWindow.maxOf { it.offTick }
        val finalOnset = inWindow.filter { it.offTick == maxOff }.minOf { it.onTick }
        if (finalOnset - windowStart < MIN_FINAL_GAP) return null
        val availOnsets = inWindow.map { it.onTick }.filter { it < finalOnset }.distinct().sorted()

        val totalLen = words.sumOf { it.length }
        val nonLastLen = totalLen - words[words.size - 1].length
        val out = ArrayList<Fragment>(words.size)
        var prevTick = windowStart
        var consumed = 0
        for (w in words.indices) {
            val isLast = w == words.size - 1
            val startTick = prevTick
            var endTick: Long
            if (isLast) {
                endTick = minOf(windowEnd, inWindow.filter { it.onTick == finalOnset }.maxOf { it.offTick })
            } else {
                consumed += words[w].length
                val frac = if (nonLastLen > 0) {
                    consumed.toDouble() / nonLastLen
                } else {
                    (w + 1.0) / words.size
                }
                val target = windowStart + ((finalOnset - windowStart) * frac).roundToLong()
                val candidate = if (target >= finalOnset) {
                    finalOnset
                } else {
                    nearestOnset(availOnsets, target, prevTick)
                }
                endTick = candidate ?: target.coerceAtLeast(prevTick + 1)
            }
            if (endTick <= startTick) endTick = startTick + 1
            val startMs = if (startTick == windowStart) {
                fragment.absMs
            } else {
                TempoUtils.tickToMs(tempoMap, startTick, ppqn)
            }
            val sub = Fragment(startTick, startMs, words[w])
            sub.isParenthesized = fragment.isParenthesized
            var endMs = TempoUtils.tickToMs(tempoMap, endTick, ppqn)
            if (isLast) {
                endMs = minOf(endMs, nextLine.startMs, sub.absMs + MAX_FILL_MS)
            }
            if (endMs <= sub.absMs) endMs = sub.absMs + 1000.0
            sub.endMs = endMs
            sub.endTick = Math.round(TempoUtils.msToTick(tempoMap, endMs, ppqn))
            val duration = sub.endTick - sub.absTick
            if (duration > 0) {
                sub.durationTicks = duration
                sub.invDuration = 1.0 / duration.toDouble()
            }
            out.add(sub)
            prevTick = endTick
        }
        if (out.size < 2) return null
        return out
    }

    private fun nearestOnset(onsets: List<Long>, target: Long, prev: Long): Long? {
        var best: Long? = null
        var bestDist = Long.MAX_VALUE
        for (onset in onsets) {
            if (onset <= prev) continue
            val dist = abs(onset - target)
            if (dist < bestDist) {
                bestDist = dist
                best = onset
            }
        }
        return best
    }

    private fun bindFragmentByNote(
        fragment: Fragment,
        spans: List<MidiParser.NoteSpan>,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int,
        nextAbsMs: Double?,
        nextLineStartMs: Double?
    ) {
        val noteEnd = findNoteEnd(spans, fragment.absTick)
        if (noteEnd == null || noteEnd <= fragment.absTick) return
        val endMs = minOf(
            TempoUtils.tickToMs(tempoMap, noteEnd, ppqn),
            fragment.absMs + MAX_FILL_MS,
            nextAbsMs ?: Double.MAX_VALUE,
            nextLineStartMs ?: Double.MAX_VALUE
        )
        if (endMs <= fragment.absMs) return
        fragment.endMs = endMs
        val endTick = Math.round(TempoUtils.msToTick(tempoMap, endMs, ppqn))
        fragment.endTick = endTick
        val duration = endTick - fragment.absTick
        if (duration > 0) {
            fragment.durationTicks = duration
            fragment.invDuration = 1.0 / duration.toDouble()
        }
    }

    private fun findNoteEnd(spans: List<MidiParser.NoteSpan>, tick: Long): Long? {
        var best: Long? = null
        var bestOnTick = Long.MIN_VALUE
        for (span in spans) {
            if (span.offTick > tick && span.onTick in bestOnTick..tick) {
                bestOnTick = span.onTick
                best = span.offTick
            }
        }
        return best
    }

    fun midiBytes(song: SongItem): ByteArray? = try {
        if (song.isUser) {
            File(song.songFile.absolutePath).readBytes()
        } else {
            val raf = RandomAccessFile(song.songFile, "r")
            try {
                val buf = ByteArray(song.byteLength.toInt())
                raf.seek(song.byteOffset)
                raf.readFully(buf)
                NativeLib.processMidiData(buf, false)
            } finally {
                raf.close()
            }
        }
    } catch (t: Throwable) {
        null
    }
}