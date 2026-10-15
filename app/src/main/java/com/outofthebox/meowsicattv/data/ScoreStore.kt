package com.outofthebox.meowsicattv.data

import android.content.Context
import java.util.TreeMap

object ScoreStore {
    private const val PREFS = "meowsicattv_scores"

    data class ScoreEntry(
        val code: Int,
        val title: String,
        val singer: String,
        val score: Int,
        val time: Long
    )

    fun record(context: Context, song: SongItem, durationMs: Double, calculatedScore: Int? = null) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "song:${song.code}"
        val existing = prefs.getInt(key, -1)
        val score = calculatedScore ?: (72 + (song.code % 29))
        if (score > existing) {
            prefs.edit()
                .putInt(key, score)
                .putLong(key + ":t", System.currentTimeMillis())
                .putString(key + ":title", song.displayTitle())
                .putString(key + ":singer", song.displaySinger())
                .putLong("song:${song.code}:dur", durationMs.toLong())
                .apply()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun all(context: Context): List<ScoreEntry> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val map = TreeMap<Int, ScoreEntry>()
        prefs.all.forEach { (k, _) ->
            if (k.startsWith("song:") && !k.endsWith(":t") && !k.endsWith(":title")
                && !k.endsWith(":singer") && !k.endsWith(":dur")
            ) {
                val code = k.removePrefix("song:").toIntOrNull() ?: return@forEach
                val score = prefs.getInt(k, 0)
                map[code] = ScoreEntry(
                    code = code,
                    title = prefs.getString(k + ":title", "Song #$code") ?: "Song #$code",
                    singer = prefs.getString(k + ":singer", "") ?: "",
                    score = score,
                    time = prefs.getLong(k + ":t", 0)
                )
            }
        }
        return map.values.sortedByDescending { it.score }
    }
}