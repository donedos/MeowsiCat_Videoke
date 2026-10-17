package com.outofthebox.meowsicattv.data

import android.content.Context
import android.util.Log
import com.meowsicat.android.database.MegidxDatabase
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject

object LibraryRepository {
    private const val TAG = "LibraryRepository"
    const val DEFAULT_ROOT = "/storage/emulated/0/MeowsiCat"

    private const val PREFS_NAME = "content_settings"
    private const val KEY_CONTENT_ROOT = "content_root"

    /** Folder(s) inside the MeowsiCat root where users drop their own MIDI/KAR files. */
    private val USER_SUBFOLDERS = listOf("song", "songs", "bgv", "bgphoto")
    private val USER_SONG_EXTENSIONS = setOf("mid", "midi", "kar", "mp3")
    private val USER_CODE_BASE = 100000
    private val TITLE_SINGER_REGEX = Regex("^(.+?)\\s*-\\s*(.+)$")
    private val LEADING_INDEX_REGEX = Regex("^\\d+\\s*-\\s*")

    private val libraryCache = ConcurrentHashMap<String, Library>()

    /** Returns the user-chosen content root, or [DEFAULT_ROOT] if none was saved. */
    fun getSavedRoot(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CONTENT_ROOT, null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_ROOT

    /** Persists the content root so it survives app restarts. */
    fun setSavedRoot(context: Context, path: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONTENT_ROOT, path)
            .apply()
    }

    data class Library(
        val songs: List<SongItem>,
        val singers: List<ArtistCount>,
        val styles: List<String>,
        val languages: List<String>,
        val root: File,
        val error: String? = null,
        val songsJson: String = ""
    ) {
        val isEmpty: Boolean get() = songs.isEmpty()
    }

    data class ArtistCount(val name: String, val count: Int)

    fun defaultRoot(): File = File(DEFAULT_ROOT)

    fun load(root: File, forceReload: Boolean = false): Library {
        val path = root.absolutePath
        if (!forceReload && libraryCache.containsKey(path)) {
            return libraryCache[path]!!
        }
        val lib = loadInternal(root)
        libraryCache[path] = lib
        return lib
    }

    private fun loadInternal(root: File): Library {
        ensureUserFolders(root)
        val idx = File(root, "idx/shubidx")
        if (!idx.isFile()) {
            val songs = collectUserSongs(root)
            return Library(
                songs = songs,
                singers = singerCounts(songs),
                styles = emptyList(),
                languages = emptyList(),
                root = root,
                error = if (songs.isEmpty()) {
                    "Catalog not found. Put SongHub content in ${root.absolutePath}"
                } else null
            )
        }
        return try {
            val db = MegidxDatabase.load(idx.absolutePath)
            val blobVolumes = findVolumes(File(root, "idx"), "shubblob").ifEmpty {
                findVolumes(File(root, "idx"), "megmid")
            }
            if (blobVolumes.isEmpty()) {
                val songs = collectUserSongs(root)
                Library(
                    songs = songs,
                    singers = singerCounts(songs),
                    styles = emptyList(),
                    languages = emptyList(),
                    root = root,
                    error = if (songs.isEmpty()) {
                        "No shubblob/megmid container found under ${File(root, "idx")}"
                    } else null
                )
            } else {
                val mp3Volumes = findVolumes(File(root, "idx"), "megvol")
                val list = ArrayList<SongItem>(db.songs.size + 64)
                db.songs.forEach { s ->
                    val singerName = if (s.singerIdx in 0 until db.singers.size) {
                        db.singers[s.singerIdx].name
                    } else {
                        ""
                    }
                    val midiOffset = s.midiByteOffset()
                    val midiLength = s.midiByteLength()
                    val midiFile = resolveVolume(blobVolumes, midiOffset)
                    val localOffset = resolveLocalOffset(blobVolumes, midiOffset)
                    var mp3File: File? = null
                    var mp3Offset = 0L
                    var mp3Length = 0L
                    if (s.isMp3 && s.mp3Vol in 0 until mp3Volumes.size) {
                        mp3File = mp3Volumes[s.mp3Vol]
                        mp3Offset = s.mp3PosSectors.toLong() * 2048L
                        mp3Length = s.mp3SizeSectors.toLong() * 2048L
                    }
                    val style = db.styleMap[s.style] ?: ""
                    val lang = if (s.langId in 0 until db.languages.size) db.languages[s.langId] else ""
                    list.add(
                        SongItem(
                            code = s.code,
                            title = s.name,
                            singer = singerName,
                            style = style,
                            language = lang,
                            isUser = false,
                            isMp3 = s.isMp3,
                            songFile = midiFile,
                            byteOffset = localOffset,
                            byteLength = midiLength,
                            mp3File = mp3File,
                            mp3Offset = mp3Offset,
                            mp3Length = mp3Length
                        )
                    )
                }
                list.addAll(collectUserSongs(root, list.maxOfOrNull { it.code } ?: 0))
                val singerMap = LinkedHashMap<String, Int>()
                list.forEach { s ->
                    if (s.singer.isNotBlank()) {
                        singerMap[s.singer] = (singerMap[s.singer] ?: 0) + 1
                    }
                }

                val arr = JSONArray()
                for (s in list) {
                    val obj = JSONObject()
                    obj.put("code", s.code)
                    obj.put("title", s.title)
                    obj.put("singer", s.singer)
                    arr.put(obj)
                }
                val songsJsonStr = arr.toString()
                try {
                    File(root, "songs.json").writeText(songsJsonStr, StandardCharsets.UTF_8)
                } catch (_: Exception) {}

                Library(
                    songs = list,
                    singers = singerMap.map { ArtistCount(it.key, it.value) }.sortedBy { it.name.lowercase() },
                    styles = db.styleMap.values.sorted(),
                    languages = db.languages.toList(),
                    root = root,
                    songsJson = songsJsonStr
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "load failed", t)
            Library(emptyList(), emptyList(), emptyList(), emptyList(), root, error = t.toString())
        }
    }

    private fun loadUserSongs(root: File): List<SongItem> {
        val json = File(root, "user_content.json")
        if (!json.isFile()) return emptyList()
        return try {
            val arr = JSONArray(json.readText())
            val list = ArrayList<SongItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val path = o.optString("path")
                val f = File(path)
                if (!f.isFile()) continue
                val ext = o.optString("ext", f.extension).lowercase()
                val isMp3 = ext == "mp3"
                list.add(
                    SongItem(
                        code = o.optInt("code"),
                        title = o.optString("title"),
                        singer = o.optString("singer"),
                        style = "",
                        language = "",
                        isUser = true,
                        isMp3 = isMp3,
                        songFile = f,
                        byteOffset = 0,
                        byteLength = if (isMp3) if (o.has("size")) o.optLong("size") else f.length() else f.length()
                    )
                )
            }
            list.distinctBy { it.code }
        } catch (t: Throwable) {
            Log.e(TAG, "user songs failed", t)
            emptyList()
        }
    }

    /**
     * Combines user songs declared in [user_content.json] with any MIDI/KAR files
     * dropped directly into the user song folder(s) under [root]. Scanned songs are
     * assigned fresh codes that do not collide with the catalog or the JSON entries.
     */
    private fun collectUserSongs(root: File, catalogMaxCode: Int = 0): List<SongItem> {
        val jsonSongs = loadUserSongs(root)
        val jsonMax = jsonSongs.maxOfOrNull { it.code } ?: 0
        val startCode = maxOf(catalogMaxCode, jsonMax, USER_CODE_BASE) + 1
        val existingPaths = jsonSongs.mapTo(HashSet()) { it.songFile.absolutePath }
        return jsonSongs + scanUserFolders(root, startCode, existingPaths)
    }

    /** Scans the user song folder(s) for .mid/.midi/.kar/.mp3 files and builds [SongItem]s. */
    private fun scanUserFolders(
        root: File,
        startCode: Int,
        excludePaths: Set<String> = emptySet()
    ): List<SongItem> {
        val found = ArrayList<SongItem>()
        var code = startCode
        val seen = HashSet<String>(excludePaths)
        for (name in USER_SUBFOLDERS) {
            val dir = File(root, name)
            if (!dir.isDirectory) continue
            val files = dir.listFiles { f ->
                f.isFile && f.extension.lowercase() in USER_SONG_EXTENSIONS
            }?.sortedBy { it.name.lowercase() } ?: continue
            for (f in files) {
                if (!seen.add(f.absolutePath)) continue
                val parsed = parseUserFileName(f.name)
                found.add(
                    SongItem(
                        code = code,
                        title = parsed.first,
                        singer = parsed.second,
                        style = "",
                        language = "",
                        isUser = true,
                        isMp3 = f.extension.equals("mp3", ignoreCase = true),
                        songFile = f,
                        byteOffset = 0,
                        byteLength = f.length()
                    )
                )
                code++
            }
        }
        return found
    }

    /** Derives "Title"/"Singer" from a filename like "001 - My Song - Some Artist.mid". */
    private fun parseUserFileName(fileName: String): Pair<String, String> {
        val base = fileName.substringBeforeLast('.', fileName)
        val stripped = base.replaceFirst(LEADING_INDEX_REGEX, "").trim()
        val match = TITLE_SINGER_REGEX.matchEntire(stripped)
        return if (match != null) {
            match.groupValues[1].trim() to match.groupValues[2].trim()
        } else {
            stripped.ifBlank { fileName } to "Unknown"
        }
    }

    /** Creates the user song folder(s) under [root] so users can drop files in. */
    private fun ensureUserFolders(root: File) {
        if (!root.isDirectory) return
        for (name in USER_SUBFOLDERS) {
            try {
                val dir = File(root, name)
                if (!dir.exists()) dir.mkdirs()
            } catch (_: Exception) {}
        }
    }

    private fun singerCounts(songs: List<SongItem>): List<ArtistCount> {
        val map = LinkedHashMap<String, Int>()
        songs.forEach { s ->
            if (s.singer.isNotBlank()) map[s.singer] = (map[s.singer] ?: 0) + 1
        }
        return map.map { ArtistCount(it.key, it.value) }.sortedBy { it.name.lowercase() }
    }

    private fun findVolumes(dir: File, prefix: String): List<File> {
        val files = dir.listFiles { f -> f.isFile && f.name.lowercase().startsWith(prefix) }
            ?.sortedBy { it.name }
            ?: return emptyList()
        return files
    }

    private fun resolveVolume(volumes: List<File>, byteOffset: Long): File {
        if (volumes.isEmpty()) return File("")
        var length = 0L
        for (v in volumes) {
            length += v.length()
            if (byteOffset < length) return v
        }
        return volumes.last()
    }

    private fun resolveLocalOffset(volumes: List<File>, byteOffset: Long): Long {
        if (volumes.isEmpty()) return 0L
        var acc = 0L
        for (v in volumes) {
            val end = v.length() + acc
            if (byteOffset < end) return byteOffset - acc
            acc = end
        }
        return byteOffset - acc
    }
}