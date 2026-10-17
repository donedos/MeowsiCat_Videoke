package com.meowsicat.android.parser

import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for how [MidiParser] assembles lyric lines.
 *
 * Covers the two raw-user-file defects:
 *  1. Soft-Karaoke lyrics delimited by a leading '/' or '\' (no '\r'/'\n') must produce one
 *     line per marker, each carrying its own start tick.
 *  2. A file whose only meta-1 events are instrument/section labels ("BASS", "VERSE") must be
 *     treated as instrumental instead of exposing those labels as lyrics.
 */
class MidiParserLyricTest {

    private val ppqn = 480

    private fun vlq(value: Int): ByteArray {
        var v = value
        val bytes = ArrayList<Byte>()
        bytes.add((v and 0x7F).toByte())
        v = v shr 7
        while (v > 0) {
            bytes.add(0, ((v and 0x7F) or 0x80).toByte())
            v = v shr 7
        }
        return bytes.toByteArray()
    }

    private fun meta(delta: Int, type: Int, payload: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(vlq(delta))
        out.write(0xFF)
        out.write(type)
        out.write(vlq(payload.size))
        out.write(payload)
        return out.toByteArray()
    }

    private fun text(delta: Int, type: Int, s: String): ByteArray =
        meta(delta, type, s.toByteArray(Charsets.ISO_8859_1))

    private fun track(vararg events: ByteArray): ByteArray {
        val body = ByteArrayOutputStream()
        for (e in events) body.write(e)
        body.write(meta(0, 0x2F, ByteArray(0)))
        val out = ByteArrayOutputStream()
        out.write("MTrk".toByteArray(Charsets.US_ASCII))
        out.write(be32(body.size()))
        out.write(body.toByteArray())
        return out.toByteArray()
    }

    private fun be32(v: Int) = byteArrayOf(
        ((v ushr 24) and 0xFF).toByte(),
        ((v ushr 16) and 0xFF).toByte(),
        ((v ushr 8) and 0xFF).toByte(),
        (v and 0xFF).toByte()
    )

    private fun smf(division: Int, vararg tracks: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("MThd".toByteArray(Charsets.US_ASCII))
        out.write(be32(6))
        out.write(byteArrayOf(0, 1)) // format 1
        out.write(byteArrayOf(((tracks.size shr 8) and 0xFF).toByte(), (tracks.size and 0xFF).toByte()))
        out.write(byteArrayOf(((division shr 8) and 0xFF).toByte(), (division and 0xFF).toByte()))
        for (t in tracks) out.write(t)
        return out.toByteArray()
    }

    @Test
    fun `soft-karaoke leading markers start a new timed line`() {
        val words = track(
            text(0, 0x03, "Words"),
            text(480, 0x01, "\\All"),
            text(240, 0x01, " the"),
            text(240, 0x01, " small"),
            text(480, 0x01, "/Things"),
            text(240, 0x01, " are"),
            text(240, 0x01, " great")
        )
        val res = MidiParser.parse(smf(ppqn, track(), words), "ISO-8859-1")

        assertEquals(2, res.lyricEventsTicks.size)
        assertEquals(480L, res.lyricEventsTicks[0].tick)
        assertEquals("All the small", res.lyricEventsTicks[0].text)
        assertEquals(1440L, res.lyricEventsTicks[1].tick)
        assertEquals("Things are great", res.lyricEventsTicks[1].text)

        val lines = LineBuilder.build(res.lyricEventsTicks, "", res.ticksPerBeat)
        assertEquals(2, lines.size)
        assertTrue("line 2 must come after line 1", lines[1].startMs > lines[0].startMs)
    }

    @Test
    fun `label-only meta-1 file is treated as instrumental`() {
        val labels = track(
            text(0, 0x01, "BASS"),
            text(0, 0x01, "PIANO"),
            text(480, 0x01, "VERSE")
        )
        val res = MidiParser.parse(smf(ppqn, track(), labels), "ISO-8859-1")
        assertTrue("non-lyric labels must not become lyrics", res.lyricEventsTicks.isEmpty())
    }

    @Test
    fun `meta-5 lyrics with carriage-return separators still split into lines`() {
        val vocal = track(
            text(0, 0x03, "Melody"),
            text(480, 0x05, "Hello "),
            text(120, 0x05, "world"),
            text(0, 0x05, "\r"),
            text(480, 0x05, "second "),
            text(120, 0x05, "line")
        )
        val res = MidiParser.parse(smf(ppqn, track(), vocal), "ISO-8859-1")

        assertEquals(2, res.lyricEventsTicks.size)
        assertEquals(480L, res.lyricEventsTicks[0].tick)
        assertEquals("Hello world", res.lyricEventsTicks[0].text)
        assertEquals(1080L, res.lyricEventsTicks[1].tick)
        assertEquals("second line", res.lyricEventsTicks[1].text)
    }

    @Test
    fun `meta-1 lyrics with bare carriage returns are accepted without a named track`() {
        val words = track(
            text(480, 0x01, "Hello "),
            text(120, 0x01, "world"),
            text(0, 0x01, "\r"),
            text(480, 0x01, "second "),
            text(120, 0x01, "line")
        )
        val res = MidiParser.parse(smf(ppqn, track(), words), "ISO-8859-1")

        assertEquals(2, res.lyricEventsTicks.size)
        assertEquals(480L, res.lyricEventsTicks[0].tick)
        assertEquals("Hello world", res.lyricEventsTicks[0].text)
        assertEquals(1080L, res.lyricEventsTicks[1].tick)
        assertEquals("second line", res.lyricEventsTicks[1].text)
    }
}
