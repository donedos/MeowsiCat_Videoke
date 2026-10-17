package com.outofthebox.meowsicattv.data

import com.meowsicat.android.model.LyricLine
import com.meowsicat.android.parser.MidiParser
import com.meowsicat.android.parser.TempoUtils

/** A time window during which the song keeps playing but nobody sings. */
data class MsSpan(val startMs: Double, val endMs: Double) {
    operator fun contains(ms: Double): Boolean = ms >= startMs && ms < endMs
}

/**
 * Detects mid-song instrumental breaks from the lyric timeline.
 *
 * A break is an interior stretch where the vocal line stops and resumes: a raw gap between the
 * last syllable of one line and the first syllable of the next, long enough that it cannot be an
 * ordinary breath. Start of the window is clamped to the previous line's rendered fill end so the
 * karaoke wipe is never cut short; the window ends exactly where the next line begins.
 */
object InstrumentalBreaks {
    /** Raw silence-between-syllables that qualifies as a break (below this is normal phrasing). */
    const val MIN_RAW_GAP_MS = 6000.0
    /** The visible window (after the previous line's fill) must last at least this long. */
    const val MIN_VISIBLE_MS = 1500.0
    /** Guard against files whose lyric track simply resumes much later (not a real break). */
    const val MAX_BREAK_MS = 60000.0

    /** Parenthesised text is stripped upstream, so markers arrive bare ("music", "instrumental"). */
    private val MARKER_TEXTS = setOf(
        "music", "musics", "instrumental", "instrumentals", "instr",
        "instrumental break", "music break", "interlude", "no lyrics"
    )

    fun detect(
        lines: List<LyricLine>,
        noteRuns: Array<out List<MidiParser.NoteSpan>?>?,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int
    ): List<MsSpan> {
        if (lines.size < 2) return emptyList()
        val real = lines.filterNot { isMarkerLine(it) }
        if (real.size < 2) return emptyList()

        val out = ArrayList<MsSpan>()
        for (i in 0 until real.size - 1) {
            val prev = real[i]
            val next = real[i + 1]
            val prevFrag = prev.fragments.lastOrNull() ?: continue
            val nextFrag = next.fragments.firstOrNull() ?: continue
            val rawEnd = prevFrag.absMs
            val nextStart = nextFrag.absMs
            val rawGap = nextStart - rawEnd
            if (rawGap < MIN_RAW_GAP_MS || rawGap > MAX_BREAK_MS) continue
            if (!musicPlays(noteRuns, tempoMap, ppqn, rawEnd, nextStart)) continue
            val startMs = maxOf(prev.endMs, rawEnd)
            val endMs = nextStart
            if (endMs - startMs < MIN_VISIBLE_MS) continue
            out.add(MsSpan(startMs, endMs))
        }
        return out
    }

    /** Marker lines (e.g. a "(music)" lyric event) are not real lyrics: their span joins the break. */
    private fun isMarkerLine(line: LyricLine): Boolean {
        val text = line.fullText().trim().lowercase()
            .trim('(', ')', '[', ']', ' ', '.', '-', '~', '\u266a', '\u266b')
        if (text.isEmpty()) return true
        if (text in MARKER_TEXTS) return true
        if (text.startsWith("instrumental")) return true
        return text.none { it.isLetterOrDigit() }
    }

    /** True when any channel has a note overlapping the window (i.e. the music keeps going). */
    private fun musicPlays(
        noteRuns: Array<out List<MidiParser.NoteSpan>?>?,
        tempoMap: List<TempoUtils.TempoEntry>,
        ppqn: Int,
        fromMs: Double,
        toMs: Double
    ): Boolean {
        if (noteRuns == null || noteRuns.isEmpty()) return true
        if (tempoMap.isEmpty() || ppqn <= 0) return true
        val fromTick = Math.round(TempoUtils.msToTick(tempoMap, fromMs, ppqn))
        val toTick = Math.round(TempoUtils.msToTick(tempoMap, toMs, ppqn))
        if (toTick <= fromTick) return true
        for (channel in noteRuns.indices) {
            val spans = noteRuns[channel] ?: continue
            for (span in spans) {
                if (span.offTick > fromTick && span.onTick < toTick) return true
            }
        }
        return false
    }
}
