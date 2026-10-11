package com.outofthebox.songhubatv

import com.songhub.android.model.Fragment
import com.songhub.android.parser.MidiParser
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.abs

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun noteMatching_snapsStartAndEndTicks() {
        val tolerance = 120L
        val span = MidiParser.NoteSpan(1920L, 2400L)
        val fragment = Fragment(1910L, 1000.0, "Hello") // lyric event tick slightly before NoteOn

        var bestSpan: MidiParser.NoteSpan? = null
        var bestDist = Long.MAX_VALUE
        val dist = abs(span.onTick - fragment.absTick)
        if (dist <= tolerance && dist < bestDist) {
            bestDist = dist
            bestSpan = span
        }

        assertNotNull(bestSpan)
        assertEquals(1920L, bestSpan!!.onTick)
        assertEquals(2400L, bestSpan.offTick)
    }
}
