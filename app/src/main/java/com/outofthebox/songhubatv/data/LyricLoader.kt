package com.outofthebox.songhubatv.data

import com.songhub.android.database.NativeLib
import com.songhub.android.model.LyricLine
import com.songhub.android.parser.LineBuilder
import com.songhub.android.parser.MidiParser
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object LyricLoader {
    private val lyricCache = ConcurrentHashMap<Int, Parsed>()

    data class Parsed(
        val lines: List<LyricLine>,
        val tempoMap: List<Any> = emptyList(),
        val totalMs: Double = 0.0,
        val error: String? = null
    )

    suspend fun load(song: SongItem): Parsed = withContext(Dispatchers.IO) {
        if (lyricCache.containsKey(song.code)) {
            return@withContext lyricCache[song.code]!!
        }
        val parsed = try {
            val bytes = midiBytes(song) ?: throw IOException("Lyrics unavailable for this title")
            val result = MidiParser.parse(bytes, song.title)
            val lines = LineBuilder.build(result.lyricEventsTicks, "", result.ticksPerBeat)
            val totalMs = lines.maxOfOrNull { it.endMs } ?: 0.0
            Parsed(lines = lines, totalMs = totalMs)
        } catch (t: Throwable) {
            Parsed(emptyList(), error = t.message ?: t.toString())
        }
        lyricCache[song.code] = parsed
        parsed
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