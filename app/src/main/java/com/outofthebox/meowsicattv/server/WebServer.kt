package com.outofthebox.meowsicattv.server

import android.content.Context
import android.util.Log
import com.outofthebox.meowsicattv.data.FavoritesStore
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.SongItem
import com.meowsicat.android.storage.BehaviorSettings
import com.outofthebox.meowsicattv.R
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject

object RemoteCommandBus {
    var onCommand: ((String, Map<String, String>) -> Unit)? = null
    var getReservationQueue: (() -> List<SongItem>)? = null
    var getCurrentSong: (() -> SongItem?)? = null
    var getIsPlaying: (() -> Boolean)? = null
    var getLyricsPayload: (() -> String?)? = null
    var getState: (() -> String?)? = null

    fun dispatch(action: String, params: Map<String, String>) {
        onCommand?.invoke(action, params)
    }
}

object WebServerManager {
    @Volatile
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    @Volatile
    private var isRunning = false
    private var appContext: Context? = null
    var currentLibrary: LibraryRepository.Library? = null

    private var consecutiveBindFailures = 0
    @Volatile
    private var bindInProgress = false
    private var watchdogThread: Thread? = null

    // Gates only the /watch karaoke-streaming routes; the remote-control
    // endpoints stay available regardless. Remote control is always on.
    @Volatile
    var broadcastEnabled = true
        private set

    fun setBroadcastEnabled(enabled: Boolean) {
        broadcastEnabled = enabled
        if (!enabled) {
            AudioBroadcast.endAll()
        }
    }

    fun start(context: Context, library: LibraryRepository.Library) {
        currentLibrary = library
        val app = context.applicationContext
        appContext = app
        broadcastEnabled = BehaviorSettings.isBroadcastEnabled(app)
        try {
            WebServerService.start(app)
        } catch (e: Exception) {
            Log.e("WebServer", "Failed to start foreground service", e)
        }
        if (isRunning) return
        val port = BehaviorSettings.getWebPort(context)
        isRunning = true
        bindInProgress = true
        startServerThread(port, library)
        startWatchdog()
    }

    fun isAlive(): Boolean {
        if (bindInProgress) return true
        val ss = serverSocket
        return isRunning && ss != null && !ss.isClosed && ss.isBound
    }

    fun restart(context: Context) {
        val lib = currentLibrary ?: return
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        isRunning = true
        bindInProgress = true
        appContext = context.applicationContext
        startServerThread(BehaviorSettings.getWebPort(context), lib)
        startWatchdog()
    }

    private fun startServerThread(port: Int, library: LibraryRepository.Library) {
        executor.submit {
            var ss: ServerSocket? = null
            try {
                ss = ServerSocket(port)
                serverSocket = ss
                bindInProgress = false
                consecutiveBindFailures = 0
                Log.i("WebServer", "MeowsiCat Web Server started on port $port")
                while (isRunning && serverSocket == ss && !ss.isClosed) {
                    try {
                        val socket = ss.accept()
                        socket.soTimeout = 12000
                        executor.submit { handleClient(socket, library) }
                    } catch (e: Exception) {
                        Log.w("WebServer", "Accept failed, stopping accept loop", e)
                        break
                    }
                }
            } catch (e: Exception) {
                bindInProgress = false
                consecutiveBindFailures++
                Log.e("WebServer", "Failed to start server on port $port", e)
            } finally {
                // Clear/close the socket on ANY loop exit so the watchdog
                // can detect the dead accept loop and restart it.
                if (serverSocket == ss) {
                    try { ss?.close() } catch (_: Exception) {}
                    serverSocket = null
                }
            }
        }
    }

    private fun startWatchdog() {
        watchdogThread?.interrupt()
        watchdogThread = Thread({
            while (isRunning) {
                try {
                    Thread.sleep(3000L)
                    if (isRunning && (serverSocket == null || serverSocket!!.isClosed || !serverSocket!!.isBound)) {
                        Log.w("WebServer", "ServerSocket closed or un-bound. Watchdog auto-restarting server...")
                        val lib = currentLibrary
                        val ctx = appContext
                        if (lib != null && ctx != null) {
                            try { serverSocket?.close() } catch (_: Exception) {}
                            serverSocket = null
                            val currentPort = BehaviorSettings.getWebPort(ctx)
                            startServerThread(currentPort, lib)
                            // Back off when the port can't be bound (e.g. port
                            // occupied by another app) instead of retrying every 3s.
                            if (consecutiveBindFailures >= 3) {
                                Thread.sleep(15000L)
                            }
                        }
                    }
                } catch (_: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e("WebServer", "Watchdog error", e)
                }
            }
        }, "WebServerWatchdog").apply { start() }
    }

    fun stop() {
        isRunning = false
        bindInProgress = false
        consecutiveBindFailures = 0
        AudioBroadcast.endAll()
        try {
            watchdogThread?.interrupt()
            watchdogThread = null
            serverSocket?.close()
            serverSocket = null
        } catch (_: Exception) {}
        appContext?.let {
            try {
                WebServerService.stop(it)
            } catch (_: Exception) {}
        }
        appContext = null
    }

    private fun handleClient(socket: Socket, library: LibraryRepository.Library) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine()
            if (requestLine == null) {
                socket.close()
                return
            }
            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                socket.close()
                return
            }
            val method = parts[0]
            val path = parts[1]
            val qIndex = path.indexOf('?')
            val basePath = if (qIndex >= 0) path.substring(0, qIndex) else path
            val queryString = if (qIndex >= 0) path.substring(qIndex + 1) else ""

            var contentLength = 0
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
                if (line.lowercase().startsWith("content-length:")) {
                    contentLength = line.substring(15).trim().toIntOrNull() ?: 0
                }
            }

            var body = ""
            if (method.equals("POST", ignoreCase = true) && contentLength > 0) {
                val charBuf = CharArray(contentLength)
                var read = 0
                while (read < contentLength) {
                    val n = reader.read(charBuf, read, contentLength - read)
                    if (n < 0) break
                    read += n
                }
                body = String(charBuf, 0, read)
            }

            val out = PrintWriter(socket.getOutputStream(), true)

            when {
                basePath == "/api/queue" || basePath == "/api/reservations" -> {
                    val song = RemoteCommandBus.getCurrentSong?.invoke()
                    val queue = RemoteCommandBus.getReservationQueue?.invoke() ?: emptyList()
                    val playing = RemoteCommandBus.getIsPlaying?.invoke() ?: false
                    val obj = JSONObject()
                    obj.put("playing", playing)
                    if (song != null && song.code != 0) {
                        obj.put("current", songJson(song))
                    } else {
                        obj.put("current", JSONObject.NULL)
                    }
                    val qArr = JSONArray()
                    for (s in queue) {
                        qArr.put(songJson(s))
                    }
                    obj.put("queue", qArr)
                    sendResponse(out, socket, "application/json; charset=UTF-8",
                        obj.toString().toByteArray(StandardCharsets.UTF_8))
                }
                basePath.startsWith("/api/search") -> {
                    val lib = currentLibrary ?: library
                    val q = queryParam(queryString, "q")
                    val limit = queryParam(queryString, "limit").toIntOrNull()?.coerceIn(1, 200) ?: 50
                    val arr = searchSongs(lib.songs, q, limit)
                    sendResponse(out, socket, "application/json; charset=UTF-8",
                        arr.toString().toByteArray(StandardCharsets.UTF_8))
                }
                basePath == "/api/song" -> {
                    val lib = currentLibrary ?: library
                    val code = queryParam(queryString, "code").toIntOrNull()
                    val song = if (code != null) lib.songs.firstOrNull { it.code == code } else null
                    val resp = if (song != null) songJson(song) else JSONObject()
                    sendResponse(out, socket, "application/json; charset=UTF-8",
                        resp.toString().toByteArray(StandardCharsets.UTF_8))
                }
                basePath.startsWith("/api/songs") -> {
                    var lib = currentLibrary ?: library
                    if (lib.songs.isEmpty() && lib.root.exists()) {
                        lib = LibraryRepository.load(lib.root, forceReload = true)
                        currentLibrary = lib
                    }
                    val jsonStr = lib.songsJson.ifEmpty {
                        val songsFile = File(lib.root, "songs.json")
                        if (songsFile.isFile) {
                            try { songsFile.readText(StandardCharsets.UTF_8) } catch (_: Exception) { "[]" }
                        } else {
                            val arr = JSONArray()
                            for (song in lib.songs) {
                                arr.put(songJson(song))
                            }
                            arr.toString()
                        }
                    }
                    sendResponse(out, socket, "application/json; charset=UTF-8",
                        jsonStr.toByteArray(StandardCharsets.UTF_8))
                }
                basePath.startsWith("/api/pdf/") -> {
                    val fileName = basePath.substring("/api/pdf/".length)
                    val rawResId = when (fileName.lowercase()) {
                        "sh23867.pdf" -> R.raw.sh23867
                        "sh3167.pdf" -> R.raw.sh3167
                        else -> null
                    }
                    if (rawResId != null && appContext != null) {
                        try {
                            val bytes = appContext!!.resources.openRawResource(rawResId).readBytes()
                            sendRawResponse(socket, bytes)
                        } catch (_: Exception) {
                            sendResponse(out, socket, "text/plain", "PDF error".toByteArray(StandardCharsets.UTF_8))
                        }
                    } else {
                        sendResponse(out, socket, "text/plain", "PDF not found".toByteArray(StandardCharsets.UTF_8))
                    }
                }
                basePath.startsWith("/api/action") -> {
                    try {
                        val json = JSONObject(body)
                        val action = json.optString("action", "")
                        val params = mutableMapOf<String, String>()
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            params[k] = json.optString(k, "")
                        }
                        val ctx = appContext
                        if (ctx != null && action == "toggle_fav") {
                            val code = params["code"]?.toIntOrNull()
                            if (code != null) {
                                FavoritesStore.toggleFavorite(ctx, code)
                            }
                        }
                        RemoteCommandBus.dispatch(action, params)
                    } catch (_: Exception) {}

                    sendResponse(out, socket, "application/json; charset=UTF-8",
                        "{\"status\":\"ok\"}".toByteArray(StandardCharsets.UTF_8))
                }
                basePath == "/api/lyrics" -> {
                    if (!broadcastEnabled) {
                        sendBroadcastDisabled(out)
                    } else {
                        val json = RemoteCommandBus.getLyricsPayload?.invoke()
                        sendResponse(out, socket, "application/json; charset=UTF-8",
                            (json ?: "{}").toByteArray(StandardCharsets.UTF_8))
                    }
                }
                basePath == "/api/state" -> {
                    if (!broadcastEnabled) {
                        sendBroadcastDisabled(out)
                    } else {
                        val json = RemoteCommandBus.getState?.invoke()
                        sendResponse(out, socket, "application/json; charset=UTF-8",
                            (json ?: "{}").toByteArray(StandardCharsets.UTF_8))
                    }
                }
                basePath == "/watch" -> {
                    if (!broadcastEnabled) {
                        sendResponse(out, socket, "text/html; charset=UTF-8",
                            getWatchDisabledHtml().toByteArray(StandardCharsets.UTF_8))
                    } else {
                        sendResponse(out, socket, "text/html; charset=UTF-8",
                            getWatchHtml().toByteArray(StandardCharsets.UTF_8))
                    }
                }
                basePath == "/api/audio" -> {
                    if (!broadcastEnabled) {
                        sendBroadcastDisabled(out)
                    } else {
                    // Long-lived close-delimited stream of framed PCM. Each frame:
                    // [int32 posMs LE][int32 pcmByteLen LE][int16 LE interleaved PCM].
                    // Browser derives its playback position from the per-frame posMs stamp.
                    try {
                        socket.soTimeout = 0
                        val stream = socket.getOutputStream()
                        val header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Cache-Control: no-cache\r\n" +
                            "Connection: close\r\n\r\n"
                        stream.write(header.toByteArray(StandardCharsets.UTF_8))
                        stream.flush()
                        val sub = AudioBroadcast.subscribe()
                        try {
                            while (!sub.closed) {
                                val frame = sub.poll(1000L) ?: continue
                                stream.write(frame)
                                stream.flush()
                            }
                        } finally {
                            AudioBroadcast.unsubscribe(sub)
                        }
                    } catch (_: Exception) {
                        // client disconnected or write failed; fall through to close
                    }
                    }
                }
                else -> {
                    sendResponse(out, socket, "text/html; charset=UTF-8",
                        getHtmlContent().toByteArray(StandardCharsets.UTF_8))
                }
            }
            socket.close()
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun queryParam(query: String, name: String): String {
        if (query.isEmpty()) return ""
        for (pair in query.split("&")) {
            val idx = pair.indexOf('=')
            if (idx >= 0 && pair.substring(0, idx) == name) {
                val raw = pair.substring(idx + 1)
                return try {
                    java.net.URLDecoder.decode(raw, "UTF-8")
                } catch (_: Exception) {
                    raw
                }
            }
        }
        return ""
    }

    private fun songJson(song: com.outofthebox.meowsicattv.data.SongItem): JSONObject {
        val obj = JSONObject()
        obj.put("code", song.code)
        obj.put("title", song.title)
        obj.put("singer", song.singer)
        val ctx = appContext
        val isFav = if (ctx != null) FavoritesStore.isFavorite(ctx, song.code) else false
        obj.put("isFav", isFav)
        return obj
    }

    private fun searchSongs(
        songs: List<com.outofthebox.meowsicattv.data.SongItem>,
        rawQuery: String,
        limit: Int
    ): JSONArray {
        val arr = JSONArray()
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) {
            for (song in songs.take(limit)) arr.put(songJson(song))
            return arr
        }
        val digits = if (q.all { it.isDigit() }) q.trimStart('0') else null
        for (song in songs) {
            val codeStr = song.code.toString()
            val codePadded = codeStr.padStart(6, '0')
            val match = song.title.lowercase().contains(q) ||
                song.singer.lowercase().contains(q) ||
                codeStr.contains(q) || codePadded.contains(q) ||
                (digits != null && digits.isNotEmpty() && (codeStr == digits || codePadded == digits))
            if (match) {
                arr.put(songJson(song))
                if (arr.length() >= limit) break
            }
        }
        return arr
    }

    private fun sendResponse(out: PrintWriter, socket: Socket, contentType: String, bytes: ByteArray) {
        try {
            out.print("HTTP/1.1 200 OK\r\n")
            out.print("Content-Type: $contentType\r\n")
            out.print("Content-Length: ${bytes.size}\r\n")
            out.print("Access-Control-Allow-Origin: *\r\n")
            out.print("Connection: close\r\n")
            out.print("\r\n")
            out.flush()
            socket.getOutputStream().write(bytes)
            socket.getOutputStream().flush()
        } catch (_: Exception) {}
    }

    private fun sendBroadcastDisabled(out: PrintWriter) {
        try {
            out.print("HTTP/1.1 403 Forbidden\r\n")
            out.print("Content-Type: application/json\r\n")
            out.print("Access-Control-Allow-Origin: *\r\n")
            out.print("Connection: close\r\n\r\n")
            out.print("{\"error\":\"broadcast_disabled\"}")
            out.flush()
        } catch (_: Exception) {}
    }

    private fun getWatchDisabledHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>MeowsiCat Karaoke</title>
<style>
    html, body { height: 100%; margin: 0; background: #05060a; color: #fff;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    .wrap { height: 100%; display: flex; flex-direction: column;
        align-items: center; justify-content: center; gap: 12px; text-align: center; padding: 24px; }
    .big { font-size: clamp(20px, 5vw, 34px); font-weight: 800; }
    .sub { color: rgba(255,255,255,0.6); font-size: 15px; }
</style>
</head>
<body>
<div class="wrap">
    <div class="big">Broadcast is off</div>
    <div class="sub">Enable Broadcast on the MeowsiCat home screen to stream karaoke here.</div>
</div>
</body>
</html>
        """.trimIndent()
    }

    private fun sendRawResponse(socket: Socket, bytes: ByteArray) {
        try {
            val outputStream = socket.getOutputStream()
            val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/pdf\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
            outputStream.write(header.toByteArray(StandardCharsets.UTF_8))
            outputStream.write(bytes)
            outputStream.flush()
        } catch (_: Exception) {}
    }

    private fun getHtmlContent(): String {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>MeowsiCat Remote & Songbook</title>
            <style>
                :root {
                    --bg-dark: #0B0F14;
                    --panel-bg: #0F172A;
                    --teal-accent: #2DD4BF;
                    --gold-accent: #FBBF24;
                    --green-btn: #059669;
                    --blue-btn: #2563EB;
                    --red-btn: #991B1B;
                    --text-main: #FFFFFF;
                    --text-muted: #94A3B8;
                }
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: system-ui, -apple-system, sans-serif; }
                body { background-color: var(--bg-dark); color: var(--text-main); padding: 12px; display: flex; flex-direction: column; align-items: center; min-height: 100vh; }
                
                /* Layout */
                .app-layout { width: 100%; max-width: 1400px; display: flex; flex-direction: row; gap: 16px; align-items: stretch; }
                .panel { display: flex; flex-direction: column; gap: 12px; }
                .pdf-panel { flex: 1.25; min-width: 0; }
                .remote-panel { flex: 1; min-width: 0; max-width: 500px; }

                /* Mobile Tabs Navigation */
                .mobile-tabs { display: none; width: 100%; max-width: 500px; background: #020617; border-radius: 12px; padding: 4px; border: 1px solid rgba(45, 212, 191, 0.3); margin-bottom: 12px; }
                .m-tab { flex: 1; padding: 10px; border: none; background: transparent; color: var(--text-muted); font-size: 14px; font-weight: 800; border-radius: 8px; cursor: pointer; text-align: center; }
                .m-tab.active { background: var(--teal-accent); color: #003731; }

                /* Cards */
                header { display: flex; align-items: center; justify-content: space-between; background: var(--panel-bg); padding: 14px 16px; border-radius: 16px; border: 1px solid rgba(45, 212, 191, 0.3); }
                header h1 { font-size: 18px; font-weight: 800; color: var(--teal-accent); }
                .card { background: var(--panel-bg); border-radius: 16px; padding: 16px; border: 1px solid rgba(45, 212, 191, 0.2); }
                .card h2 { font-size: 15px; font-weight: 700; margin-bottom: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }

                /* PDF Songbook Styles */
                .pdf-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
                .pdf-header h2 { font-size: 16px; font-weight: 800; color: var(--teal-accent); margin: 0; }
                .pdf-tabs { display: flex; gap: 6px; background: #020617; padding: 4px; border-radius: 10px; border: 1px solid rgba(45, 212, 191, 0.3); }
                .pdf-tab { padding: 6px 12px; border: none; background: transparent; color: var(--text-muted); font-size: 12px; font-weight: 800; border-radius: 6px; cursor: pointer; }
                .pdf-tab.active { background: var(--teal-accent); color: #003731; }
                .pdf-wrapper { width: 100%; height: calc(100vh - 120px); min-height: 480px; border-radius: 12px; overflow: hidden; background: #020617; border: 1px solid rgba(45, 212, 191, 0.2); }
                #pdf-frame { width: 100%; height: 100%; border: none; }

                /* Keypad Controls */
                .display-box { background: #020617; border: 1.5px solid rgba(251, 191, 36, 0.6); border-radius: 12px; padding: 10px; text-align: center; margin-bottom: 12px; }
                .display-code { font-size: 28px; font-weight: 900; color: var(--gold-accent); letter-spacing: 2px; }
                .display-title { font-size: 13px; font-weight: 700; color: #FFFFFF; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .keypad-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; }
                .key-btn { background: #0F3832; color: #E6FFFA; border: 1px solid rgba(45,212,191,0.3); border-radius: 12px; padding: 14px; font-size: 18px; font-weight: bold; cursor: pointer; text-align: center; }
                .key-btn:active { background: var(--teal-accent); color: #003731; }
                .action-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
                .btn { border: none; border-radius: 12px; padding: 12px; font-size: 13px; font-weight: 800; color: white; cursor: pointer; text-align: center; }
                .btn-res { background: var(--teal-accent); color: #003731; }
                .btn-can { background: var(--red-btn); }
                .btn-rsv { background: var(--blue-btn); }
                .transport-row { display: flex; justify-content: space-between; gap: 6px; margin-top: 12px; flex-wrap: wrap; }
                .transport-btn { flex: 1; min-width: 60px; background: #1E293B; border: 1px solid rgba(45,212,191,0.3); border-radius: 10px; padding: 8px 6px; color: white; font-size: 12px; font-weight: bold; cursor: pointer; }
                .transport-btn.play { background: var(--green-btn); }
                .transport-btn.pause { background: #D97706; }
                .transport-btn.stop { background: var(--red-btn); }
                .transport-btn.rsv-list { background: var(--blue-btn); }
                
                /* Search Section */
                .search-input { width: 100%; padding: 10px 14px; border-radius: 12px; border: 1px solid rgba(45,212,191,0.4); background: #020617; color: white; font-size: 15px; margin-bottom: 10px; outline: none; }
                .song-list { max-height: 240px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
                .song-item { display: flex; justify-content: space-between; align-items: center; background: #1E293B; padding: 10px 12px; border-radius: 10px; border: 1px solid rgba(45,212,191,0.15); }
                .song-info { flex: 1; min-width: 0; margin-right: 8px; }
                .song-title { font-size: 13px; font-weight: bold; color: white; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .song-singer { font-size: 11px; color: var(--teal-accent); margin-top: 2px; }
                .song-btns { display: flex; gap: 4px; }
                .s-btn { padding: 5px 10px; border-radius: 8px; border: none; font-size: 11px; font-weight: bold; cursor: pointer; color: white; }
                .s-btn-play { background: var(--green-btn); }
                .s-btn-res { background: var(--blue-btn); }
                .s-btn-fav { background: rgba(244, 114, 182, 0.15); border: 1px solid rgba(244, 114, 182, 0.3); font-size: 13px; padding: 4px 6px; cursor: pointer; border-radius: 8px; }
                .s-btn-fav.active { background: rgba(244, 114, 182, 0.35); border-color: #F472B6; }

                /* Modal Styles */
                .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.75); backdrop-filter: blur(4px); display: none; justify-content: center; align-items: center; padding: 16px; z-index: 1000; }
                .modal-overlay.active { display: flex; }
                .modal-card { background: var(--panel-bg); border: 1px solid rgba(45, 212, 191, 0.4); border-radius: 20px; width: 100%; max-width: 440px; max-height: 80vh; display: flex; flex-direction: column; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5); overflow: hidden; }
                .modal-header { padding: 14px 16px; background: #020617; border-bottom: 1px solid rgba(45, 212, 191, 0.2); display: flex; justify-content: space-between; align-items: center; }
                .modal-header h3 { font-size: 15px; font-weight: 800; color: var(--teal-accent); }
                .modal-close { background: transparent; border: none; color: var(--text-muted); font-size: 22px; font-weight: bold; cursor: pointer; padding: 0 8px; }
                .modal-body { padding: 14px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
                .rsv-item { display: flex; justify-content: space-between; align-items: center; background: #1E293B; padding: 10px 12px; border-radius: 12px; border: 1px solid rgba(45, 212, 191, 0.15); }
                .rsv-num { font-size: 13px; font-weight: 900; color: var(--gold-accent); margin-right: 8px; }
                .rsv-info { flex: 1; min-width: 0; margin-right: 8px; }
                .rsv-title { font-size: 13px; font-weight: bold; color: white; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .rsv-singer { font-size: 11px; color: var(--teal-accent); margin-top: 2px; }
                .rsv-actions { display: flex; gap: 6px; }

                /* Media Query for Mobile */
                @media (max-width: 899px) {
                    body { padding: 8px; }
                    .mobile-tabs { display: flex; }
                    .app-layout { flex-direction: column; align-items: center; }
                    .pdf-panel { width: 100%; display: none; }
                    .pdf-panel.m-active { display: flex; }
                    .remote-panel { width: 100%; max-width: 480px; display: none; }
                    .remote-panel.m-active { display: flex; }
                    .pdf-wrapper { height: calc(100vh - 140px); }
                }
                
                #toast { position: fixed; bottom: 20px; background: var(--teal-accent); color: #003731; padding: 10px 20px; border-radius: 20px; font-weight: bold; font-size: 14px; opacity: 0; transition: opacity 0.3s; pointer-events: none; z-index: 2000; }
            </style>
        </head>
        <body>
            <div class="mobile-tabs">
                <button id="m-tab-remote" class="m-tab active" onclick="showMobileView('remote')">📱 Remote Keypad</button>
                <button id="m-tab-pdf" class="m-tab" onclick="showMobileView('pdf')">📖 PDF Songbook</button>
            </div>

            <div class="app-layout">
                <!-- Left Panel: PDF Songbook Reader -->
                <div id="pdf-section" class="panel pdf-panel">
                    <div class="card" style="height: 100%; display: flex; flex-direction: column;">
                        <div class="pdf-header">
                            <h2>📖 Songbook PDF</h2>
                            <div class="pdf-tabs">
                                <button id="ver-23867-btn" class="pdf-tab active" onclick="selectPdfVersion('sh23867.pdf')">Version 23867</button>
                                <button id="ver-3167-btn" class="pdf-tab" onclick="selectPdfVersion('sh3167.pdf')">Version 3167</button>
                            </div>
                        </div>
                        <div class="pdf-wrapper">
                            <iframe id="pdf-frame" src="/api/pdf/sh23867.pdf"></iframe>
                        </div>
                    </div>
                </div>

                <!-- Right Panel: Remote Keypad & Song Search -->
                <div id="remote-section" class="panel remote-panel m-active">
                    <header>
                        <h1>MeowsiCat Remote</h1>
                        <span id="status-indicator" style="font-size: 12px; color: var(--teal-accent);">Connected</span>
                    </header>

                    <!-- Keypad Card -->
                    <div class="card">
                        <h2>Remote Keypad</h2>
                        <div class="display-box">
                            <div id="code-display" class="display-code">— — — — — —</div>
                            <div id="title-display" class="display-title">Type a song code</div>
                        </div>
                        <div class="keypad-grid">
                            <div class="key-btn" onclick="pressDigit('1')">1</div>
                            <div class="key-btn" onclick="pressDigit('2')">2</div>
                            <div class="key-btn" onclick="pressDigit('3')">3</div>
                            <div class="key-btn" onclick="pressDigit('4')">4</div>
                            <div class="key-btn" onclick="pressDigit('5')">5</div>
                            <div class="key-btn" onclick="pressDigit('6')">6</div>
                            <div class="key-btn" onclick="pressDigit('7')">7</div>
                            <div class="key-btn" onclick="pressDigit('8')">8</div>
                            <div class="key-btn" onclick="pressDigit('9')">9</div>
                            <div class="key-btn" onclick="pressDigit('0')" style="grid-column: 2;">0</div>
                        </div>
                        <div class="action-row">
                            <button class="btn btn-can" onclick="pressCancel()">CAN</button>
                            <button class="btn btn-res" onclick="pressReserve(false)">RES</button>
                            <button class="btn btn-rsv" onclick="pressReserve(true)">1st RSV</button>
                        </div>
                        <div class="transport-row">
                            <button class="transport-btn" onclick="sendAction('prev')">⏮ Prev</button>
                            <button id="play-pause-btn" class="transport-btn play" onclick="togglePlayPause()">▶ Play</button>
                            <button class="transport-btn stop" onclick="sendAction('stop')">⏹ Stop</button>
                            <button class="transport-btn rsv-list" onclick="openRsvModal()">📋 RSV (<span id="rsv-badge">0</span>)</button>
                            <button class="transport-btn" onclick="sendAction('next')">Next ⏭</button>
                        </div>
                    </div>

                    <!-- Song Search Card -->
                    <div class="card">
                        <h2>Song Search</h2>
                        <input type="text" id="search-input" class="search-input" placeholder="Search title, artist, or code..." oninput="filterSongs(this.value)">
                        <div id="song-list" class="song-list">Loading songs...</div>
                    </div>
                </div>
            </div>

            <div id="rsv-modal" class="modal-overlay" onclick="closeRsvModal(event)">
                <div class="modal-card" onclick="event.stopPropagation()">
                    <div class="modal-header">
                        <h3>📋 Reserved Songs List</h3>
                        <button class="modal-close" onclick="closeRsvModal()">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div id="rsv-current-box" style="display:none; margin-bottom: 8px; background: #020617; border: 1px solid var(--gold-accent); padding: 10px 14px; border-radius: 12px;">
                            <div style="font-size: 11px; font-weight: 800; color: var(--gold-accent); text-transform: uppercase;">▶ Currently Playing</div>
                            <div id="rsv-current-title" style="font-size: 14px; font-weight: bold; color: white; margin-top: 2px;"></div>
                        </div>
                        <div id="rsv-queue-list" style="display: flex; flex-direction: column; gap: 8px;">
                            <div style="text-align: center; color: var(--text-muted); padding: 16px;">Loading reservation list...</div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="toast">Action sent</div>

            <script>
                let currentCode = "";
                let searchTimer = null;
                let previewSeq = 0;
                let consecutiveFailures = 0;

                function selectPdfVersion(versionFile) {
                    let frame = document.getElementById('pdf-frame');
                    frame.src = '/api/pdf/' + versionFile;
                    document.getElementById('ver-23867-btn').classList.toggle('active', versionFile === 'sh23867.pdf');
                    document.getElementById('ver-3167-btn').classList.toggle('active', versionFile === 'sh3167.pdf');
                    showToast('Loaded ' + (versionFile === 'sh23867.pdf' ? 'Ver 23867' : 'Ver 3167'));
                }

                function showMobileView(view) {
                    let rPanel = document.getElementById('remote-section');
                    let pPanel = document.getElementById('pdf-section');
                    let rTab = document.getElementById('m-tab-remote');
                    let pTab = document.getElementById('m-tab-pdf');

                    if (view === 'remote') {
                        rPanel.classList.add('m-active');
                        pPanel.classList.remove('m-active');
                        rTab.classList.add('active');
                        pTab.classList.remove('active');
                    } else {
                        pPanel.classList.add('m-active');
                        rPanel.classList.remove('m-active');
                        pTab.classList.add('active');
                        rTab.classList.remove('active');
                    }
                }

                function updateStatusBadge(connected) {
                    let badge = document.getElementById('status-indicator');
                    if (badge) {
                        if (connected) {
                            badge.innerText = "Connected";
                            badge.style.color = "var(--teal-accent)";
                        } else {
                            badge.innerText = "Reconnecting...";
                            badge.style.color = "var(--gold-accent)";
                        }
                    }
                }

                async function loadSongs(query) {
                    try {
                        let res = await fetch('/api/search?limit=50&q=' + encodeURIComponent(query || ''));
                        if (!res.ok) throw new Error("HTTP " + res.status);
                        let list = await res.json();
                        renderSongs(list);
                    } catch(e) {
                        console.error(e);
                        document.getElementById('song-list').innerHTML = '<div style="padding:12px; color:#EF4444; text-align:center;">Failed to load songs</div>';
                    }
                }

                function filterSongs(query) {
                    if (searchTimer) clearTimeout(searchTimer);
                    searchTimer = setTimeout(() => loadSongs(query), 180);
                }

                function escapeHtml(s) {
                    return String(s === null || s === undefined ? '' : s)
                        .replace(/&/g, '&amp;')
                        .replace(/</g, '&lt;')
                        .replace(/>/g, '&gt;')
                        .replace(/"/g, '&quot;')
                        .replace(/'/g, '&#39;');
                }

                function renderSongs(list) {
                    if (!list || list.length === 0) {
                        document.getElementById('song-list').innerHTML = '<div style="padding:12px; color:#94A3B8; text-align:center;">No songs found</div>';
                        return;
                    }
                    let html = "";
                    list.slice(0, 50).forEach(item => {
                        let displayCode = String(item.code).padStart(6, '0');
                        let favIcon = item.isFav ? '❤️' : '🤍';
                        let favClass = item.isFav ? 's-btn-fav active' : 's-btn-fav';
                        html += '<div class="song-item"><div class="song-info"><div class="song-title">#' + escapeHtml(displayCode) + ' - ' + escapeHtml(item.title) + '</div><div class="song-singer">' + escapeHtml(item.singer) + '</div></div><div class="song-btns"><button class="' + favClass + '" onclick="toggleFav(' + item.code + ')">' + favIcon + '</button><button class="s-btn s-btn-play" onclick="playSong(' + item.code + ')">Play</button><button class="s-btn s-btn-res" onclick="reserveSong(' + item.code + ')">Res</button></div></div>';
                    });
                    document.getElementById('song-list').innerHTML = html;
                }

                async function toggleFav(code) {
                    try {
                        await fetch('/api/action', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ action: 'toggle_fav', code: String(code) })
                        });
                        showToast("Favorite toggled");
                        let query = document.getElementById('search-input').value;
                        loadSongs(query);
                    } catch(e) {
                        console.error(e);
                    }
                }

                function updateDisplay() {
                    let disp = document.getElementById('code-display');
                    if (currentCode === "") {
                        disp.innerText = "— — — — — —";
                    } else {
                        disp.innerText = currentCode.padStart(6, '0');
                    }
                    fetchPreview(currentCode);
                }

                function fetchPreview(code) {
                    let titleDisp = document.getElementById('title-display');
                    if (!code) {
                        titleDisp.innerText = "Type a song code";
                        return;
                    }
                    let mySeq = ++previewSeq;
                    fetch('/api/song?code=' + encodeURIComponent(code))
                        .then(r => r.json())
                        .then(data => {
                            if (mySeq !== previewSeq) return;
                            if (data && data.title) {
                                titleDisp.innerText = data.title + " • " + (data.singer || '');
                            } else {
                                titleDisp.innerText = "No song found";
                            }
                        })
                        .catch(() => {
                            if (mySeq === previewSeq) titleDisp.innerText = "Searching...";
                        });
                }

                function pressDigit(d) {
                    if (currentCode.length < 6) {
                        currentCode += d;
                        updateDisplay();
                    }
                }

                function pressCancel() {
                    if (currentCode.length > 0) {
                        currentCode = "";
                        updateDisplay();
                    } else {
                        sendAction('cancel');
                    }
                }

                async function pressReserve(firstPriority) {
                    if (!currentCode) {
                        showToast("Type a song code first");
                        return;
                    }
                    let codeToReserve = currentCode;
                    await sendAction('reserve', { code: codeToReserve, priority: firstPriority ? 'true' : 'false' });
                    currentCode = "";
                    updateDisplay();
                }

                async function playSong(code) {
                    let codePadded = String(code).padStart(6, '0');
                    await sendAction('play_code', { code: codePadded });
                    showToast("Playing #" + codePadded);
                }

                async function reserveSong(code) {
                    let codePadded = String(code).padStart(6, '0');
                    await sendAction('reserve', { code: codePadded });
                    showToast("Reserved #" + codePadded);
                }

                async function sendAction(actionName, extraParams) {
                    let payload = { action: actionName, ...(extraParams || {}) };
                    try {
                        let res = await fetch('/api/action', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(payload)
                        });
                        showToast("Action: " + actionName.toUpperCase());
                        pollQueue();
                    } catch(e) {
                        console.error(e);
                        showToast("Error sending action");
                    }
                }

                let isPlayingState = false;

                async function pollQueue() {
                    try {
                        let res = await fetch('/api/queue');
                        if (!res.ok) throw new Error("HTTP " + res.status);
                        let data = await res.json();
                        
                        consecutiveFailures = 0;
                        updateStatusBadge(true);

                        isPlayingState = data.playing || false;
                        let playBtn = document.getElementById('play-pause-btn');
                        if (isPlayingState) {
                            playBtn.innerText = "⏸ Pause";
                            playBtn.className = "transport-btn pause";
                        } else {
                            playBtn.innerText = "▶ Play";
                            playBtn.className = "transport-btn play";
                        }

                        let badge = document.getElementById('rsv-badge');
                        let count = data.queue ? data.queue.length : 0;
                        badge.innerText = count;

                        renderRsvModalContent(data);
                    } catch(e) {
                        consecutiveFailures++;
                        if (consecutiveFailures >= 2) {
                            updateStatusBadge(false);
                        }
                    }
                }

                async function togglePlayPause() {
                    if (isPlayingState) {
                        await sendAction('pause');
                    } else {
                        await sendAction('play');
                    }
                }

                function openRsvModal() {
                    document.getElementById('rsv-modal').classList.add('active');
                    pollQueue();
                }

                function closeRsvModal(e) {
                    document.getElementById('rsv-modal').classList.remove('active');
                }

                function renderRsvModalContent(data) {
                    let curBox = document.getElementById('rsv-current-box');
                    let curTitle = document.getElementById('rsv-current-title');
                    let qList = document.getElementById('rsv-queue-list');

                    if (data.current) {
                        curBox.style.display = 'block';
                        curTitle.innerText = '#' + String(data.current.code).padStart(6, '0') + ' - ' + data.current.title + ' (' + data.current.singer + ')';
                    } else {
                        curBox.style.display = 'none';
                    }

                    if (!data.queue || data.queue.length === 0) {
                        qList.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 16px;">No reserved songs in queue</div>';
                        return;
                    }

                    let html = "";
                    data.queue.forEach((item, idx) => {
                        let displayCode = String(item.code).padStart(6, '0');
                        html += '<div class="rsv-item"><span class="rsv-num">#' + (idx + 1) + '</span><div class="rsv-info"><div class="rsv-title">' + escapeHtml(item.title) + '</div><div class="rsv-singer">' + escapeHtml(displayCode) + ' • ' + escapeHtml(item.singer) + '</div></div><div class="rsv-actions"><button class="s-btn s-btn-play" onclick="playRsvItem(' + item.code + ')">Play</button><button class="s-btn" style="background: var(--red-btn);" onclick="removeRsvItem(' + idx + ', ' + item.code + ')">✕</button></div></div>';
                    });
                    qList.innerHTML = html;
                }

                async function playRsvItem(code) {
                    await sendAction('play_code', { code: String(code) });
                    pollQueue();
                }

                async function removeRsvItem(index, code) {
                    await sendAction('remove_reserved', { index: String(index), code: String(code) });
                    pollQueue();
                }

                function showToast(msg) {
                    let t = document.getElementById('toast');
                    t.innerText = msg;
                    t.style.opacity = '1';
                    setTimeout(() => { t.style.opacity = '0'; }, 2000);
                }

                loadSongs('');
                pollQueue();
                setInterval(pollQueue, 3000);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    private fun getWatchHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<title>MeowsiCat Karaoke</title>
<style>
    :root { color-scheme: dark; }
    * { margin: 0; padding: 0; box-sizing: border-box; }
    html, body {
        width: 100%; height: 100%; overflow: hidden;
        background: #05060a; color: #fff;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        -webkit-user-select: none; user-select: none;
        touch-action: manipulation;
    }
    #bg {
        position: fixed; inset: 0; z-index: 0;
        background: radial-gradient(120% 120% at 50% 0%, #1a2340 0%, #0a0e1a 55%, #05060a 100%);
    }
    #video {
        position: fixed; inset: 0; z-index: 1;
        width: 100%; height: 100%; object-fit: cover;
        opacity: 0; transition: opacity .6s ease;
    }
    #scrim {
        position: fixed; inset: 0; z-index: 2;
        background: rgba(0,0,0,0.5);
    }
    #stage {
        position: fixed; inset: 0; z-index: 3;
        display: flex; flex-direction: column;
        align-items: center; justify-content: center;
        padding: 4vw; text-align: center; gap: 2.2vh;
    }
    .line {
        max-width: 92vw; line-height: 1.25;
        font-weight: 800; letter-spacing: .5px;
        transition: opacity .25s ease;
    }
    #cur { font-size: clamp(28px, 6.2vw, 76px); }
    #next { font-size: clamp(20px, 4.2vw, 52px); opacity: .45; }
    .frag { position: relative; display: inline-block; white-space: pre; }
    .frag .base { color: rgba(255,255,255,0.28); }
    .frag .fill {
        position: absolute; left: 0; top: 0;
        color: #ffffff;
        text-shadow: 0 0 18px rgba(120,180,255,0.55);
        clip-path: inset(0 100% 0 0);
        will-change: clip-path;
    }
    #countdown {
        position: fixed; z-index: 4; left: 50%; top: 34%;
        transform: translate(-50%, -50%);
        font-size: clamp(60px, 16vw, 190px); font-weight: 900;
        color: #ffd34d; text-shadow: 0 0 30px rgba(255,180,40,0.6);
        opacity: 0; transition: opacity .15s ease, transform .15s ease;
        pointer-events: none;
    }
    #status {
        position: fixed; z-index: 5; left: 50%; bottom: 5vh; transform: translateX(-50%);
        font-size: 14px; color: rgba(255,255,255,0.55);
        background: rgba(0,0,0,0.35); padding: 6px 14px; border-radius: 999px;
    }
    #overlay {
        position: fixed; inset: 0; z-index: 10;
        display: flex; flex-direction: column;
        align-items: center; justify-content: center; gap: 18px;
        background: rgba(5,6,10,0.92); cursor: pointer;
    }
    #overlay .big { font-size: clamp(22px, 5vw, 40px); font-weight: 800; }
    #overlay .sub { font-size: 15px; color: rgba(255,255,255,0.6); }
    #overlay .btn {
        margin-top: 8px; padding: 14px 30px; border-radius: 999px;
        background: #2b6cff; font-size: 18px; font-weight: 700;
    }
    .hidden { display: none !important; }
</style>
</head>
<body>
<div id="bg"></div>
<video id="video" muted loop playsinline></video>
<div id="scrim"></div>
<div id="stage">
    <div id="cur" class="line"></div>
    <div id="next" class="line"></div>
</div>
<div id="countdown"></div>
<div id="status">Connecting&hellip;</div>
<div id="overlay">
    <div class="big">MeowsiCat Karaoke</div>
    <div class="sub">Stream this song's lyrics &amp; audio to your browser</div>
    <div class="btn">Tap to start</div>
</div>

<script>
(function () {
    "use strict";
    var SAMPLE_RATE = 44100, CHANNELS = 2;
    var model = null, curLineIndex = -1, lastPosMs = 0, started = false;
    var ctx = null, nextStartTime = 0, playQueue = [], curFillSpans = [];
    var curSongCode = null;

    var elCur = document.getElementById('cur');
    var elNext = document.getElementById('next');
    var elCd = document.getElementById('countdown');
    var elStatus = document.getElementById('status');
    var elOverlay = document.getElementById('overlay');
    var elVideo = document.getElementById('video');

    function setStatus(t) { elStatus.innerText = t; }

    function readI32LE(u8, off) {
        return (u8[off] | (u8[off+1] << 8) | (u8[off+2] << 16) | (u8[off+3] << 24)) | 0;
    }

    function currentPosMs() {
        var now = ctx.currentTime;
        while (playQueue.length && playQueue[0].startTime + playQueue[0].duration <= now) {
            lastPosMs = playQueue[0].posMs + playQueue[0].duration * 1000;
            playQueue.shift();
        }
        if (playQueue.length) {
            var f = playQueue[0];
            return f.posMs + Math.max(0, now - f.startTime) * 1000;
        }
        return lastPosMs;
    }

    function makeBuffer(int16) {
        var frames = int16.length / CHANNELS;
        var buf = ctx.createBuffer(CHANNELS, frames, SAMPLE_RATE);
        var L = buf.getChannelData(0), R = buf.getChannelData(1);
        for (var i = 0; i < frames; i++) {
            L[i] = int16[i*2] / 32768;
            R[i] = int16[i*2+1] / 32768;
        }
        return buf;
    }

    function onFrame(posMs, int16) {
        if (!ctx) return;
        var buf = makeBuffer(int16);
        var src = ctx.createBufferSource();
        src.buffer = buf;
        src.connect(ctx.destination);
        var t = Math.max(ctx.currentTime + 0.08, nextStartTime);
        src.start(t);
        nextStartTime = t + buf.duration;
        playQueue.push({ posMs: posMs, startTime: t, duration: buf.duration });
    }

    var acc = new Uint8Array(0);
    function onBytes(chunk) {
        var merged = new Uint8Array(acc.length + chunk.length);
        merged.set(acc); merged.set(chunk, acc.length);
        acc = merged;
        var off = 0;
        while (acc.length - off >= 8) {
            var posMs = readI32LE(acc, off);
            var pcmLen = readI32LE(acc, off + 4);
            if (pcmLen <= 0 || pcmLen > 4000000) { off = acc.length; break; }
            if (acc.length - off - 8 >= pcmLen) {
                var pcmBytes = acc.slice(off + 8, off + 8 + pcmLen);
                var int16 = new Int16Array(pcmBytes.buffer, 0, pcmLen / 2);
                onFrame(posMs, int16);
                off += 8 + pcmLen;
            } else break;
        }
        acc = acc.slice(off);
    }

    function buildFragments(line, container) {
        container.innerHTML = '';
        var spans = [];
        for (var i = 0; i < line.fragments.length; i++) {
            var f = line.fragments[i];
            var frag = document.createElement('span');
            frag.className = 'frag';
            var base = document.createElement('span');
            base.className = 'base';
            base.textContent = f.text;
            var fill = document.createElement('span');
            fill.className = 'fill';
            fill.textContent = f.text;
            frag.appendChild(base);
            frag.appendChild(fill);
            container.appendChild(frag);
            spans.push({ el: fill, absMs: f.absMs, endMs: f.endMs });
        }
        return spans;
    }

    function findLineIndex(pos) {
        if (!model || !model.lines) return -1;
        var idx = -1;
        for (var i = 0; i < model.lines.length; i++) {
            if (pos >= model.lines[i].startMs - 1.0) idx = i; else break;
        }
        return idx;
    }

    function updateLyrics(pos) {
        if (!model || !model.lines || model.lines.length === 0) {
            elCur.innerText = 'No lyrics';
            return;
        }
        var idx = findLineIndex(pos);
        if (idx < 0) {
            if (curLineIndex !== -1) {
                curLineIndex = -1;
                curFillSpans = [];
                elCur.innerHTML = '';
                elNext.innerText = '';
            }
            updateCountdown(pos);
            return;
        }
        if (idx !== curLineIndex) {
            curLineIndex = idx;
            var line = model.lines[idx];
            curFillSpans = buildFragments(line, elCur);
            if (idx + 1 < model.lines.length) {
                elNext.innerText = model.lines[idx+1].fragments.map(function(f){return f.text;}).join('');
            } else {
                elNext.innerText = '';
            }
        }
        for (var i = 0; i < curFillSpans.length; i++) {
            var s = curFillSpans[i];
            var span = s.endMs - s.absMs;
            var frac = span > 0 ? (pos - s.absMs) / span : 1;
            if (frac < 0) frac = 0; else if (frac > 1) frac = 1;
            var rightPct = (1 - frac) * 100;
            s.el.style.clipPath = 'inset(0 ' + rightPct.toFixed(2) + '% 0 0)';
        }
        updateCountdown(pos);
    }

    function updateCountdown(pos) {
        if (!model || !model.countdowns) { elCd.style.opacity = '0'; return; }
        var shown = false;
        for (var c = 0; c < model.countdowns.length; c++) {
            var cd = model.countdowns[c];
            var pips = cd.pipMs;
            if (!pips || pips.length === 0) continue;
            if (pos >= pips[0] && pos < cd.anchorMs) {
                var idx = 0;
                for (var i = 0; i < pips.length; i++) if (pos >= pips[i]) idx = i;
                var num = pips.length - idx;
                elCd.innerText = String(num);
                elCd.style.opacity = '1';
                var start = pips[idx];
                var end = (idx + 1 < pips.length) ? pips[idx+1] : cd.anchorMs;
                var p = (pos - start) / Math.max(1, end - start);
                var scale = 1.4 - 0.4 * Math.min(1, Math.max(0, p));
                elCd.style.transform = 'translate(-50%, -50%) scale(' + scale.toFixed(3) + ')';
                shown = true;
                break;
            }
        }
        if (!shown) elCd.style.opacity = '0';
    }

    function renderLoop() {
        if (ctx) {
            try { updateLyrics(currentPosMs()); } catch (e) {}
        }
        requestAnimationFrame(renderLoop);
    }

    function loadLyrics() {
        return fetch('/api/lyrics').then(function(r){ return r.json(); }).then(function(m) {
            model = m;
            curLineIndex = -1;
            curFillSpans = [];
            elCur.innerHTML = '';
            elNext.innerText = '';
            if (m.songCode) curSongCode = m.songCode;
        });
    }

    function pollState() {
        fetch('/api/state').then(function(r){ return r.json(); }).then(function(s) {
            if (s && typeof s.songCode === 'number' && s.songCode !== curSongCode) {
                loadLyrics();
            }
        }).catch(function(){});
    }

    function startAudio() {
        var audioDone = false;
        function connect() {
            fetch('/api/audio').then(function(resp) {
                var reader = resp.body.getReader();
                function pump() {
                    return reader.read().then(function(r) {
                        if (r.done) { audioDone = true; setStatus('Stream ended'); return; }
                        onBytes(r.value);
                        return pump();
                    });
                }
                return pump();
            }).catch(function() {
                if (!audioDone) { setStatus('Reconnecting audio…'); setTimeout(connect, 1200); }
            });
        }
        connect();
    }

    function start() {
        if (started) return;
        started = true;
        elOverlay.classList.add('hidden');
        try {
            var AC = window.AudioContext || window.webkitAudioContext;
            ctx = new AC({ sampleRate: SAMPLE_RATE });
            if (ctx.state === 'suspended') ctx.resume();
        } catch (e) {
            setStatus('Audio unavailable: ' + e.message);
        }
        setStatus('Waiting for song…');
        loadLyrics().then(function() { startAudio(); });
        requestAnimationFrame(renderLoop);
        setInterval(pollState, 1000);
    }

    elOverlay.addEventListener('click', start);
    elOverlay.addEventListener('touchend', function(e){ e.preventDefault(); start(); });
})();
</script>
</body>
</html>
        """.trimIndent()
    }
}
