package com.outofthebox.meowsicattv.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Playlist(
    val id: String,
    val name: String,
    val songCodes: List<Int>
)

object PlaylistStore {
    private const val PREFS = "meowsicattv_playlists"
    private const val KEY_PLAYLISTS = "custom_playlists"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getPlaylists(context: Context): List<Playlist> {
        val jsonStr = prefs(context).getString(KEY_PLAYLISTS, "[]") ?: "[]"
        val list = mutableListOf<Playlist>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", System.currentTimeMillis().toString())
                val name = obj.optString("name", "Untitled")
                val codesArr = obj.optJSONArray("songs") ?: JSONArray()
                val codes = mutableListOf<Int>()
                for (j in 0 until codesArr.length()) {
                    codes.add(codesArr.getInt(j))
                }
                list.add(Playlist(id, name, codes))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun savePlaylists(context: Context, playlists: List<Playlist>) {
        val arr = JSONArray()
        for (pl in playlists) {
            val obj = JSONObject()
            obj.put("id", pl.id)
            obj.put("name", pl.name)
            val codesArr = JSONArray()
            for (code in pl.songCodes) {
                codesArr.put(code)
            }
            obj.put("songs", codesArr)
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_PLAYLISTS, arr.toString()).apply()
    }

    fun createPlaylist(context: Context, name: String): Playlist {
        val current = getPlaylists(context).toMutableList()
        val newPl = Playlist(System.currentTimeMillis().toString(), name.trim(), emptyList())
        current.add(newPl)
        savePlaylists(context, current)
        return newPl
    }

    fun renamePlaylist(context: Context, id: String, newName: String) {
        val current = getPlaylists(context).map {
            if (it.id == id) it.copy(name = newName.trim()) else it
        }
        savePlaylists(context, current)
    }

    fun deletePlaylist(context: Context, id: String) {
        val current = getPlaylists(context).filter { it.id != id }
        savePlaylists(context, current)
    }

    fun addSongToPlaylist(context: Context, playlistId: String, songCode: Int) {
        val current = getPlaylists(context).map { pl ->
            if (pl.id == playlistId) {
                if (!pl.songCodes.contains(songCode)) {
                    pl.copy(songCodes = pl.songCodes + songCode)
                } else pl
            } else pl
        }
        savePlaylists(context, current)
    }

    fun removeSongFromPlaylist(context: Context, playlistId: String, songCode: Int) {
        val current = getPlaylists(context).map { pl ->
            if (pl.id == playlistId) {
                pl.copy(songCodes = pl.songCodes.filter { it != songCode })
            } else pl
        }
        savePlaylists(context, current)
    }

    fun getPlaylistSongs(context: Context, playlistId: String, allSongs: List<SongItem>): List<SongItem> {
        val playlists = getPlaylists(context)
        val pl = playlists.find { it.id == playlistId } ?: return emptyList()
        val codeSet = pl.songCodes.toSet()
        return allSongs.filter { codeSet.contains(it.code) }
    }
}
