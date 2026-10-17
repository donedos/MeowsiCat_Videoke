package com.outofthebox.meowsicattv.data

import android.util.Log
import com.songhub.android.database.NativeLib
import com.meowsicat.android.model.CountdownGroup
import com.meowsicat.android.model.Fragment
import com.meowsicat.android.model.LyricLine
import com.meowsicat.android.parser.CountdownGrouper
import com.meowsicat.android.parser.LineBuilder
import com.meowsicat.android.parser.MidiParser
import com.meowsicat.android.parser.TempoUtils
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.abs

object LyricLoader {
    private val lyricCache = ConcurrentHashMap<Long, Parsed>()
    private const val MAX_FILL_MS = 5000.0
    private const val ALIGN_WINDOW = 80L
    private const val MIN_MELODY_RATIO = 0.5
    private const val MIN_SPAN_LEN = 40L
    private const val MIN_FINAL_GAP = 60L

    // A guide-melody channel sings a monophonic line, so its notes are not stacked on the
    // same onset (unlike chords/pads) and are not percussion one-shots (unlike drum channels).
    private const val MIN_MELODY_NOTE_TICKS = 8L
    private const val MAX_MELODY_POLY_FRAC = 0.5
    // Among channels that track the lyric onsets nearly as well as the best one, the sung
    // line is the most sustained; fast arpeggios/runs align too but hold notes far less.
    private const val MELODY_COUNT_BAND = 0.8

    data class Parsed(
        val lines: List<LyricLine>,
        val countdowns: List<CountdownGroup> = emptyList(),
        val tempoMap: List<TempoUtils.TempoEntry> = emptyList(),
        val ticksPerBeat: Int = 0,
        val totalMs: Double = 0.0,
        val error: String? = null,
        /** True when the MIDI parsed cleanly but carries no vocal lyric events (instrumental). */
        val instrumental: Boolean = false,
        /** Interior windows where the music plays but nobody sings (mid-song instrumental breaks). */
        val instrumentalBreaks: List<MsSpan> = emptyList()
    )

    suspend fun load(song: SongItem, wipeChannel: Int = 0): Parsed = withContext(Dispatchers.IO) {
        if (song.code == 0) {
            return@withContext Parsed(emptyList())
        }
        val cacheKey = song.code.toLong() * 32L + wipeChannel.coerceIn(0, 16)
        if (lyricCache.containsKey(cacheKey)) {
            return@withContext lyricCache[cacheKey]!!
        }
        val parsed = try {
            val bytes = midiBytes(song) ?: throw IOException("Lyrics unavailable for this title")
            val result = MidiParser.parse(bytes, song.title)
            val lines = LineBuilder.build(result.lyricEventsTicks, "", result.ticksPerBeat)
            val tempoMap = result.tempoMap
            val ppqn = result.ticksPerBeat
            if (tempoMap.isNotEmpty() && ppqn > 0) {
                val guideSpans = pickMelodySpans(result, wipeChannel)?.let { legatoSpans(it) }
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
            val breaks = if (result.lyricEventsTicks.isEmpty()) {
                emptyList()
            } else {
                InstrumentalBreaks.detect(lines, result.noteRunsByChannel, tempoMap, ppqn)
            }
            if (breaks.isNotEmpty()) {
                Log.d(
                    "LyricLoader",
                    "Instrumental breaks for ${song.code} \"${song.title}\": " +
                        breaks.joinToString { "@${it.startMs.toInt()}-${it.endMs.toInt()}ms" }
                )
            }
            val parsed = Parsed(
                lines = lines,
                countdowns = countdowns,
                tempoMap = tempoMap,
                ticksPerBeat = ppqn,
                totalMs = totalMs,
                instrumental = result.lyricEventsTicks.isEmpty(),
                instrumentalBreaks = breaks
            )
            parsed
        } catch (t: Throwable) {
            Parsed(emptyList(), error = t.message ?: t.toString())
        }
        lyricCache[cacheKey] = parsed
        parsed
    }

    private data class ChannelScore(
        val channel: Int,
        val count: Int,
        val sumDist: Long,
        val notes: Int,
        val preLyric: Int,
        val medDur: Long,
        val polyFrac: Double
    )

    private fun pickMelodySpans(result: MidiParser.ParseResult, wipeChannel: Int = 0): List<MidiParser.NoteSpan>? {
        val races = result.noteRunsByChannel ?: return null
        if (wipeChannel in 1..16) {
            val forced = races.getOrNull(wipeChannel - 1)
            if (!forced.isNullOrEmpty()) return forced
        }
        val lyricTicks = result.lyricEventsTicks.map { it.tick }
        if (lyricTicks.isEmpty()) return null
        val firstLyricTick = lyricTicks.min()
        val required = Math.ceil(lyricTicks.size * MIN_MELODY_RATIO).toInt().coerceAtLeast(1)

        // If the file names its lead track ("Melody"/"MELODY"/"Key..."), that is the guide
        // melody by convention; trust it over the acoustic heuristics below.
        val namedRank = result.melodyChannelRank
        if (namedRank != null) {
            var bestRank = 0
            for (c in namedRank.indices) {
                if (namedRank[c] > bestRank && !races.getOrNull(c).isNullOrEmpty()) bestRank = namedRank[c]
            }
            if (bestRank > 0) {
                var bestChannel = -1
                var bestCount = -1
                for (c in namedRank.indices) {
                    if (namedRank[c] != bestRank) continue
                    val spans = races.getOrNull(c) ?: continue
                    if (spans.isEmpty()) continue
                    var count = 0
                    for (lyricTick in lyricTicks) {
                        if (spans.any { abs(it.onTick - lyricTick) <= ALIGN_WINDOW }) count++
                    }
                    if (count > bestCount) {
                        bestCount = count
                        bestChannel = c
                    }
                }
                if (bestChannel >= 0) return races[bestChannel]
            }
        }

        val scores = ArrayList<ChannelScore>(races.size)
        for (channel in 0 until races.size) {
            if (channel == 9) continue
            val spans = races[channel] ?: continue
            if (spans.isEmpty()) continue
            var count = 0
            var sumDist = 0L
            for (lyricTick in lyricTicks) {
                var nearest = Long.MAX_VALUE
                for (span in spans) {
                    val dist = abs(span.onTick - lyricTick)
                    if (dist <= ALIGN_WINDOW && dist < nearest) nearest = dist
                }
                if (nearest != Long.MAX_VALUE) {
                    count++
                    sumDist += nearest
                } else {
                    sumDist += ALIGN_WINDOW + 1
                }
            }
            val preLyric = spans.count { it.onTick < firstLyricTick - ALIGN_WINDOW }
            val durs = LongArray(spans.size) { spans[it].offTick - spans[it].onTick }
            durs.sort()
            val medDur = durs[spans.size / 2]
            val onsetBuckets = HashMap<Long, Int>(spans.size)
            for (span in spans) onsetBuckets[span.onTick] = (onsetBuckets[span.onTick] ?: 0) + 1
            var poly = 0
            for (span in spans) if ((onsetBuckets[span.onTick] ?: 1) > 1) poly++
            val polyFrac = poly.toDouble() / spans.size
            scores.add(ChannelScore(channel, count, sumDist, spans.size, preLyric, medDur, polyFrac))
        }
        if (scores.isEmpty()) return null

        // Prefer channels that behave like a sung melody: monophonic, sustained (not
        // percussion one-shots), and tracking the lyric onsets. Fall back to plain
        // alignment scoring when nothing qualifies (e.g. unusual arrangements).
        val melodic = scores.filter {
            it.medDur >= MIN_MELODY_NOTE_TICKS &&
                it.polyFrac <= MAX_MELODY_POLY_FRAC &&
                it.count >= required
        }
        val base = if (melodic.isNotEmpty()) melodic else scores.filter { it.count >= required }
        if (base.isEmpty()) return null

        // The guide melody starts with the sung section, so it has no notes during an
        // instrumental intro; accompaniment/rhythm channels usually run through the intro.
        val voiceLike = base.filter { it.preLyric == 0 }
        val candidates = if (voiceLike.isNotEmpty()) voiceLike else base
        val band = candidates.filter { it.count >= (candidates.maxOf { c -> c.count } * MELODY_COUNT_BAND).toInt() }
        val cmp = compareBy<ChannelScore> { it.medDur }
            .thenBy { it.polyFrac }
            .thenBy { it.count }
            .thenBy { it.sumDist }
            .thenBy { it.notes }
        return races[(band.maxWithOrNull(cmp) ?: return null).channel]
    }

    // Some files encode the melody as very short (staccato) notes even though the syllables
    // are sung legato; using the raw note-off then collapses words to a few ticks. When most
    // notes end well before the next onset, extend each note to the next onset so the fill
    // tracks the syllables instead.
    private fun legatoSpans(spans: List<MidiParser.NoteSpan>): List<MidiParser.NoteSpan> {
        if (spans.size < 2) return spans
        val sorted = spans.sortedBy { it.onTick }
        var tight = 0
        for (i in 0 until sorted.size - 1) {
            val dur = sorted[i].offTick - sorted[i].onTick
            val gap = sorted[i + 1].onTick - sorted[i].onTick
            if (gap > 0 && dur * 2 < gap) tight++
        }
        if (tight * 2 <= sorted.size) return spans
        val out = ArrayList<MidiParser.NoteSpan>(sorted.size)
        for (i in sorted.indices) {
            val on = sorted[i].onTick
            val off = if (i + 1 < sorted.size) maxOf(sorted[i].offTick, sorted[i + 1].onTick) else sorted[i].offTick
            out.add(MidiParser.NoteSpan(on, off))
        }
        return out
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
                        guideSpans.ifEmpty { timingSpans },
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
        // Melody notes whose onset belongs to this line. Include a note landing exactly on
        // the line start so the first syllable is not dropped.
        val notes = timingSpans
            .filter { it.onTick >= windowStart && it.onTick < windowEnd }
            .sortedBy { it.onTick }
        if (notes.isEmpty()) return null
        val spanEnd = notes.maxOf { it.offTick }
        if (spanEnd - windowStart < MIN_FINAL_GAP) return null

        val n = words.size
        val m = notes.size
        val totalLen = words.sumOf { it.length }.coerceAtLeast(1)
        val out = ArrayList<Fragment>(n)
        var prevTick = windowStart

        if (m >= n) {
            // Map each word onto a contiguous run of melody notes, weighted by word length.
            // Every word is guaranteed at least one note, so the fill stays locked to the
            // sung melody instead of an arbitrary onset grid (which starves short words).
            var noteCursor = 0
            var cumLen = 0
            for (w in 0 until n) {
                cumLen += words[w].length
                val isLast = w == n - 1
                var groupEnd = Math.round(m.toDouble() * cumLen / totalLen).toInt()
                groupEnd = groupEnd.coerceIn(noteCursor + 1, m - (n - 1 - w))
                if (isLast) groupEnd = m
                val endTick = maxOf(prevTick + 1, notes[groupEnd - 1].offTick)
                addSubFragment(out, fragment, words[w], prevTick, endTick, isLast, nextLine, tempoMap, ppqn)
                prevTick = endTick
                noteCursor = groupEnd
            }
        } else {
            // Fewer melody notes than words: interpolate boundaries across the melodic span
            // and snap to a note onset only when that does not shrink a word below its fair
            // share of the remaining span.
            val onsets = notes.map { it.onTick }.distinct().sorted()
            var cumLen = 0
            for (w in 0 until n) {
                cumLen += words[w].length
                val isLast = w == n - 1
                val endTick: Long
                if (isLast) {
                    endTick = maxOf(prevTick + 1, spanEnd)
                } else {
                    val target = windowStart + ((spanEnd - windowStart) * cumLen) / totalLen
                    val snapped = nearestOnset(onsets, target, prevTick)
                    val fairShare = prevTick + (spanEnd - prevTick) / (n - w)
                    endTick = maxOf(snapped ?: target, fairShare).coerceIn(prevTick + 1, spanEnd)
                }
                addSubFragment(out, fragment, words[w], prevTick, endTick, isLast, nextLine, tempoMap, ppqn)
                prevTick = endTick
            }
        }
        if (out.size < 2) return null
        return out
    }

    private fun addSubFragment(
        out: MutableList<Fragment>,
        parent: Fragment,
        word: String,
        startTick: Long,
        endTick: Long,
        isLast: Boolean,
        nextLine: LyricLine,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int
    ) {
        val startMs = if (startTick == parent.absTick) {
            parent.absMs
        } else {
            TempoUtils.tickToMs(tempoMap, startTick, ppqn)
        }
        val sub = Fragment(startTick, startMs, word)
        sub.isParenthesized = parent.isParenthesized
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
        val windowStart = fragment.absTick
        val windowEndTick = listOfNotNull(
            nextAbsMs?.let { Math.round(TempoUtils.msToTick(tempoMap, it, ppqn)) },
            nextLineStartMs?.let { Math.round(TempoUtils.msToTick(tempoMap, it, ppqn)) }
        ).minOrNull() ?: windowStart + Math.round(
            TempoUtils.msToTick(tempoMap, fragment.absMs + MAX_FILL_MS, ppqn)
        )
        if (windowEndTick <= windowStart) return
        val noteEnd = findNoteEndInWindow(spans, windowStart, windowEndTick)
            ?: findNoteEnd(spans, fragment.absTick)
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

    private fun findNoteEndInWindow(
        spans: List<MidiParser.NoteSpan>,
        startTick: Long,
        endTick: Long
    ): Long? {
        var best: Long? = null
        for (span in spans) {
            if (span.onTick < startTick || span.onTick >= endTick) continue
            if (best == null || span.offTick > best) best = span.offTick
        }
        return best
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