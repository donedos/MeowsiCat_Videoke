package com.outofthebox.meowsicattv.data

import com.meowsicat.android.model.Fragment
import com.meowsicat.android.model.LyricLine
import com.meowsicat.android.parser.MidiParser
import com.meowsicat.android.parser.TempoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

class InstrumentalBreakDetectionTest {

    private val ppqn = 480
    private val tempoMap = listOf(TempoUtils.TempoEntry(0L, 500000L, 0.0))

    private fun line(absMs: Double, endMs: Double = absMs, text: String = "la"): LyricLine {
        val l = LyricLine(absMs)
        l.endMs = endMs
        l.fragments.add(Fragment(absMs.roundToLong(), absMs, text))
        return l
    }

    /** One channel with continuous notes covering [fromMs, toMs]. */
    private fun notes(fromMs: Double, toMs: Double): Array<List<MidiParser.NoteSpan>?> {
        val arr = arrayOfNulls<List<MidiParser.NoteSpan>>(16)
        val on = TempoUtils.msToTick(tempoMap, fromMs, ppqn).roundToLong()
        val off = TempoUtils.msToTick(tempoMap, toMs, ppqn).roundToLong()
        arr[0] = listOf(MidiParser.NoteSpan(on, off))
        return arr
    }

    @Test
    fun `long gap between lines with music is a break`() {
        val lines = listOf(
            line(1000.0, 6000.0, "hello"),
            line(14000.0, 17000.0, "world")
        )
        val breaks = InstrumentalBreaks.detect(lines, notes(1000.0, 17000.0), tempoMap, ppqn)
        assertEquals(1, breaks.size)
        assertEquals(6000.0, breaks[0].startMs, 1.0)
        assertEquals(14000.0, breaks[0].endMs, 1.0)
    }

    @Test
    fun `short gap between lines is not a break`() {
        val lines = listOf(line(1000.0, 4000.0), line(5000.0, 9000.0))
        val breaks = InstrumentalBreaks.detect(lines, notes(0.0, 20000.0), tempoMap, ppqn)
        assertTrue(breaks.isEmpty())
    }

    @Test
    fun `long silence with no music is not a break`() {
        val lines = listOf(line(1000.0, 6000.0), line(14000.0, 17000.0))
        // Notes end before the gap: the accompaniment has stopped, so it is not an instrumental break.
        val breaks = InstrumentalBreaks.detect(lines, notes(0.0, 900.0), tempoMap, ppqn)
        assertTrue(breaks.isEmpty())
    }

    @Test
    fun `marker line joins the surrounding break`() {
        val lines = listOf(
            line(1000.0, 6000.0, "verse"),
            line(8000.0, 8200.0, "music"),
            line(14000.0, 17000.0, "chorus")
        )
        val breaks = InstrumentalBreaks.detect(lines, notes(0.0, 20000.0), tempoMap, ppqn)
        assertEquals(1, breaks.size)
        assertEquals(6000.0, breaks[0].startMs, 1.0)
        assertEquals(14000.0, breaks[0].endMs, 1.0)
    }

    @Test
    fun `insufficient or missing data yields no breaks`() {
        assertTrue(InstrumentalBreaks.detect(emptyList(), notes(0.0, 1000.0), tempoMap, ppqn).isEmpty())
        assertTrue(InstrumentalBreaks.detect(listOf(line(0.0, 1000.0)), notes(0.0, 1000.0), tempoMap, ppqn).isEmpty())
        // No note data at all: fall back to trusting the gap.
        val lines = listOf(line(1000.0, 6000.0), line(14000.0, 17000.0))
        assertEquals(1, InstrumentalBreaks.detect(lines, null, tempoMap, ppqn).size)
    }

    @Test
    fun `span contains is half-open`() {
        val span = MsSpan(1000.0, 2000.0)
        assertTrue(1000.0 in span)
        assertTrue(1999.9 in span)
        assertTrue(2000.0 !in span)
    }
}
