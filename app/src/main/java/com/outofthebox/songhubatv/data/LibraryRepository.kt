package com.outofthebox.songhubatv.data

import android.util.Log
import com.songhub.android.database.MegidxDatabase
import com.songhub.android.database.MegSong
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray

object LibraryRepository {
    private const val TAG = "LibraryRepository"
    const val DEFAULT_ROOT = "/storage/emulated/0/SongHub"

    private val libraryCache = ConcurrentHashMap<String, Library>()

    data class Library(
        val songs: List<SongItem>,
        val singers: List<ArtistCount>,
        val styles: List<String>,
        val languages: List<String>,
        val root: File,
        val error: String? = null
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
        val idx = File(root, "idx/shubidx")
        if (!idx.isFile()) {
            return Library(emptyList(), emptyList(), emptyList(), emptyList(), root,
                error = "Catalog not found. Put SongHub content in ${root.absolutePath}")
        }
        return try {
            val db = MegidxDatabase.load(idx.absolutePath)
            val blobVolumes = findVolumes(File(root, "idx"), "shubblob").ifEmpty {
                findVolumes(File(root, "idx"), "megmid")
            }
            if (blobVolumes.isEmpty()) {
                Library(emptyList(), emptyList(), emptyList(), emptyList(), root,
                    error = "No shubblob/megmid container found under ${File(root, "idx")}")
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
                list.addAll(loadUserSongs(root))
                val singerMap = LinkedHashMap<String, Int>()
                list.forEach { s ->
                    if (s.singer.isNotBlank()) {
                        singerMap[s.singer] = (singerMap[s.singer] ?: 0) + 1
                    }
                }
                Library(
                    songs = list,
                    singers = singerMap.map { ArtistCount(it.key, it.value) }.sortedBy { it.name.lowercase() },
                    styles = db.styleMap.values.sorted(),
                    languages = db.languages.toList(),
                    root = root
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