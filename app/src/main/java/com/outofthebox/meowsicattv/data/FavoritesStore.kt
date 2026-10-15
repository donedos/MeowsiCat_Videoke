package com.outofthebox.meowsicattv.data

import android.content.Context
import android.content.SharedPreferences

object FavoritesStore {
    private const val PREFS = "meowsicattv_favorites"
    private const val KEY_FAVORITE_CODES = "favorite_song_codes"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun isFavorite(context: Context, songCode: Int): Boolean {
        if (songCode == 0) return false
        val set = prefs(context).getStringSet(KEY_FAVORITE_CODES, emptySet()) ?: emptySet()
        return set.contains(songCode.toString())
    }

    fun toggleFavorite(context: Context, songCode: Int): Boolean {
        if (songCode == 0) return false
        val set = prefs(context).getStringSet(KEY_FAVORITE_CODES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val codeStr = songCode.toString()
        val newState = if (set.contains(codeStr)) {
            set.remove(codeStr)
            false
        } else {
            set.add(codeStr)
            true
        }
        prefs(context).edit().putStringSet(KEY_FAVORITE_CODES, set).apply()
        return newState
    }

    fun getFavorites(context: Context, allSongs: List<SongItem>): List<SongItem> {
        val set = prefs(context).getStringSet(KEY_FAVORITE_CODES, emptySet()) ?: emptySet()
        val codes = set.mapNotNull { it.toIntOrNull() }.toSet()
        return allSongs.filter { codes.contains(it.code) }
    }
}
