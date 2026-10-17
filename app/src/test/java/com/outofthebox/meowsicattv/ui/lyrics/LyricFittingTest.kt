package com.outofthebox.meowsicattv.ui.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricFittingTest {

    @Test
    fun `single line that fits is not shrunk`() {
        assertEquals(1f, fitScaleForLines(listOf(400f, 300f), maxWidthPx = 800f, maxLines = 1), 0.0001f)
    }

    @Test
    fun `single line too wide shrinks to fit`() {
        val scale = fitScaleForLines(listOf(600f, 600f), maxWidthPx = 600f, maxLines = 1)
        assertEquals(0.5f, scale, 0.0001f)
        assertTrue(1200f * scale <= 600f + 0.5f)
    }

    @Test
    fun `single extremely wide line shrinks to the floor`() {
        val scale = fitScaleForLines(listOf(1000f, 1000f, 1000f), maxWidthPx = 300f, maxLines = 1)
        assertEquals(MIN_LYRIC_SCALE, scale, 0.0001f)
    }

    @Test
    fun `wrapped phrase that fits keeps full size`() {
        assertEquals(1f, fitScaleForLines(listOf(500f, 500f), maxWidthPx = 600f, maxLines = 2), 0.0001f)
    }

    @Test
    fun `wrapped phrase that overflows shrinks so every visual line fits`() {
        val widths = listOf(700f, 700f)
        val maxWidthPx = 600f
        val maxLines = 2
        val scale = fitScaleForLines(widths, maxWidthPx, maxLines)

        assertTrue(scale < 1f)
        assertTrue(scale >= MIN_LYRIC_SCALE)
        val ranges = greedyRanges(widths.map { it * scale }, maxWidthPx)
        assertTrue("ranges=$ranges", ranges.size <= maxLines)
        for (r in ranges) {
            val lineWidth = r.map { widths[it] }.sum() * scale
            assertTrue("lineWidth=$lineWidth", lineWidth <= maxWidthPx + 0.5f)
        }
    }

    @Test
    fun `greedy wrap breaks on word boundaries within width`() {
        val ranges = greedyRanges(listOf(200f, 200f, 200f, 200f), maxWidthPx = 450f)
        assertEquals(listOf(0 until 2, 2 until 4), ranges)
    }

    @Test
    fun `widened merges overflow into the last allowed line`() {
        val merged = widened(listOf(0..0, 1..1, 2..2, 3..3), maxLines = 2)
        assertEquals(listOf(0 until 1, 1..3), merged)
    }

    @Test
    fun `fitsAllRanges honours both width and line count`() {
        assertTrue(fitsAllRanges(listOf(200f, 200f), maxWidthPx = 450f, maxLines = 1))
        assertTrue(!fitsAllRanges(listOf(200f, 200f, 200f), maxWidthPx = 450f, maxLines = 1))
        assertTrue(fitsAllRanges(listOf(200f, 200f, 200f), maxWidthPx = 450f, maxLines = 2))
    }
}
