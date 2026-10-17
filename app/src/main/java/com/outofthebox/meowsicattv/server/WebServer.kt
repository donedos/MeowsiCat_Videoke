package com.outofthebox.meowsicattv.server

import android.content.Context
import android.util.Log
import com.outofthebox.meowsicattv.data.FavoritesStore
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.SongItem
import com.meowsicat.android.storage.BehaviorSettings
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

    fun dispatch(action: String, params: Map<String, String>) {
        onCommand?.invoke(action, params)
    }
}

object WebServerManager {
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    private var isRunning = false
    private var appContext: Context? = null
    var currentLibrary: LibraryRepository.Library? = null

    fun start(context: Context, library: LibraryRepository.Library) {
        currentLibrary = library
        val app = context.applicationContext
        appContext = app
        try {
            WebServerService.start(app)
        } catch (e: Exception) {
            Log.e("WebServer", "Failed to start foreground service", e)
        }
        if (isRunning) return
        val port = BehaviorSettings.getWebPort(context)
        executor.submit {
            try {
                val ss = ServerSocket(port)
                serverSocket = ss
                isRunning = true
                Log.i("WebServer", "MeowsiCat Web Server started on port $port")
                while (isRunning) {
                    try {
                        val socket = ss.accept()
                        executor.submit { handleClient(socket, library) }
                    } catch (_: Exception) {
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("WebServer", "Failed to start server on port $port", e)
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
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
        out.print("HTTP/1.1 200 OK\r\n")
        out.print("Content-Type: $contentType\r\n")
        out.print("Content-Length: ${bytes.size}\r\n")
        out.print("Connection: close\r\n")
        out.print("\r\n")
        out.flush()
        socket.getOutputStream().write(bytes)
        socket.getOutputStream().flush()
    }

    private fun getHtmlContent(): String {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>MeowsiCat Remote & Song Search</title>
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
                body { background-color: var(--bg-dark); color: var(--text-main); padding: 16px; display: flex; flex-direction: column; align-items: center; }
                .container { width: 100%; max-width: 480px; display: flex; flex-direction: column; gap: 16px; }
                header { display: flex; align-items: center; justify-content: space-between; background: var(--panel-bg); padding: 16px; border-radius: 16px; border: 1px solid rgba(45, 212, 191, 0.3); }
                header h1 { font-size: 20px; font-weight: 800; color: var(--teal-accent); }
                .card { background: var(--panel-bg); border-radius: 16px; padding: 16px; border: 1px solid rgba(45, 212, 191, 0.2); }
                .card h2 { font-size: 16px; font-weight: 700; margin-bottom: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
                .display-box { background: #020617; border: 1.5px solid rgba(251, 191, 36, 0.6); border-radius: 12px; padding: 12px; text-align: center; margin-bottom: 12px; }
                .display-code { font-size: 32px; font-weight: 900; color: var(--gold-accent); letter-spacing: 2px; }
                .display-title { font-size: 14px; font-weight: 700; color: #FFFFFF; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .keypad-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; }
                .key-btn { background: #0F3832; color: #E6FFFA; border: 1px solid rgba(45,212,191,0.3); border-radius: 12px; padding: 16px; font-size: 20px; font-weight: bold; cursor: pointer; text-align: center; }
                .key-btn:active { background: var(--teal-accent); color: #003731; }
                .action-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
                .btn { border: none; border-radius: 12px; padding: 12px; font-size: 14px; font-weight: 800; color: white; cursor: pointer; text-align: center; }
                .btn-res { background: var(--teal-accent); color: #003731; }
                .btn-can { background: var(--red-btn); }
                .btn-rsv { background: var(--blue-btn); }
                .transport-row { display: flex; justify-content: space-between; gap: 8px; margin-top: 12px; }
                .transport-btn { flex: 1; background: #1E293B; border: 1px solid rgba(45,212,191,0.3); border-radius: 10px; padding: 10px; color: white; font-weight: bold; cursor: pointer; }
                .transport-btn.play { background: var(--green-btn); }
                .transport-btn.pause { background: #D97706; }
                .transport-btn.stop { background: var(--red-btn); }
                .transport-btn.rsv-list { background: var(--blue-btn); }
                
                /* Search Section */
                .search-input { width: 100%; padding: 12px 16px; border-radius: 12px; border: 1px solid rgba(45,212,191,0.4); background: #020617; color: white; font-size: 16px; margin-bottom: 12px; outline: none; }
                .song-list { max-height: 280px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
                .song-item { display: flex; justify-content: space-between; align-items: center; background: #1E293B; padding: 10px 14px; border-radius: 10px; border: 1px solid rgba(45,212,191,0.15); }
                .song-info { flex: 1; min-width: 0; margin-right: 8px; }
                .song-title { font-size: 14px; font-weight: bold; color: white; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .song-singer { font-size: 12px; color: var(--teal-accent); margin-top: 2px; }
                .song-btns { display: flex; gap: 6px; }
                .s-btn { padding: 6px 12px; border-radius: 8px; border: none; font-size: 12px; font-weight: bold; cursor: pointer; color: white; }
                .s-btn-play { background: var(--green-btn); }
                .s-btn-res { background: var(--blue-btn); }
                .s-btn-fav {
                    background: rgba(244, 114, 182, 0.15);
                    border: 1px solid rgba(244, 114, 182, 0.3);
                    font-size: 14px;
                    padding: 5px 8px;
                    cursor: pointer;
                    border-radius: 8px;
                }
                .s-btn-fav.active {
                    background: rgba(244, 114, 182, 0.35);
                    border-color: #F472B6;
                }
                
                .rsv-badge-btn {
                    background: var(--blue-btn);
                    color: white;
                    border: none;
                    border-radius: 12px;
                    padding: 8px 14px;
                    font-size: 13px;
                    font-weight: 800;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 6px;
                }
                .rsv-badge-btn:active { opacity: 0.8; }

                /* Modal Styles */
                .modal-overlay {
                    position: fixed;
                    top: 0; left: 0; right: 0; bottom: 0;
                    background: rgba(0, 0, 0, 0.75);
                    backdrop-filter: blur(4px);
                    display: none;
                    justify-content: center;
                    align-items: center;
                    padding: 16px;
                    z-index: 1000;
                }
                .modal-overlay.active { display: flex; }
                .modal-card {
                    background: var(--panel-bg);
                    border: 1px solid rgba(45, 212, 191, 0.4);
                    border-radius: 20px;
                    width: 100%;
                    max-width: 440px;
                    max-height: 80vh;
                    display: flex;
                    flex-direction: column;
                    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
                    overflow: hidden;
                }
                .modal-header {
                    padding: 16px;
                    background: #020617;
                    border-bottom: 1px solid rgba(45, 212, 191, 0.2);
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .modal-header h3 { font-size: 16px; font-weight: 800; color: var(--teal-accent); }
                .modal-close {
                    background: transparent;
                    border: none;
                    color: var(--text-muted);
                    font-size: 22px;
                    font-weight: bold;
                    cursor: pointer;
                    padding: 0 8px;
                }
                .modal-body {
                    padding: 16px;
                    overflow-y: auto;
                    display: flex;
                    flex-direction: column;
                    gap: 12px;
                }
                .rsv-item {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    background: #1E293B;
                    padding: 10px 14px;
                    border-radius: 12px;
                    border: 1px solid rgba(45, 212, 191, 0.15);
                }
                .rsv-num {
                    font-size: 14px;
                    font-weight: 900;
                    color: var(--gold-accent);
                    margin-right: 10px;
                }
                .rsv-info { flex: 1; min-width: 0; margin-right: 8px; }
                .rsv-title { font-size: 14px; font-weight: bold; color: white; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .rsv-singer { font-size: 12px; color: var(--teal-accent); margin-top: 2px; }
                .rsv-actions { display: flex; gap: 6px; }
                
                #toast { position: fixed; bottom: 20px; background: var(--teal-accent); color: #003731; padding: 10px 20px; border-radius: 20px; font-weight: bold; font-size: 14px; opacity: 0; transition: opacity 0.3s; pointer-events: none; }
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>MeowsiCat Remote</h1>
                    <span style="font-size: 12px; color: var(--teal-accent);">Connected</span>
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

                async function loadSongs(query) {
                    try {
                        let res = await fetch('/api/search?limit=50&q=' + encodeURIComponent(query || ''));
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
                    await sendAction('toggle_fav', { code: String(code) });
                    let input = document.getElementById('search-input');
                    loadSongs(input ? input.value : '');
                }

                let isCurrentlyPlaying = false;

                function togglePlayPause() {
                    if (currentCode && currentCode.length > 0) {
                        sendAction('play_code', { code: currentCode });
                        showToast("Playing song #" + currentCode);
                        currentCode = "";
                        updateDisplay();
                    } else if (isCurrentlyPlaying) {
                        sendAction('pause');
                    } else {
                        sendAction('play');
                    }
                }

                function pressDigit(d) {
                    if (currentCode.length >= 6) currentCode = "";
                    currentCode += d;
                    updateDisplay();
                }

                function pressCancel() {
                    if (currentCode.length > 0) {
                        currentCode = "";
                    } else {
                        sendAction('cancel');
                    }
                    updateDisplay();
                }

                async function updateDisplay() {
                    document.getElementById('code-display').innerText = currentCode.length === 0 ? "— — — — — —" : currentCode.padStart(6, '0');
                    if (currentCode.length === 0) {
                        document.getElementById('title-display').innerText = "Type a song code";
                        return;
                    }
                    let seq = ++previewSeq;
                    document.getElementById('title-display').innerText = "Searching...";
                    try {
                        let res = await fetch('/api/song?code=' + encodeURIComponent(currentCode));
                        let song = await res.json();
                        if (seq !== previewSeq) return;
                        document.getElementById('title-display').innerText =
                            (song && song.title) ? (song.title + ' • ' + song.singer) : "Searching...";
                    } catch(e) {
                        if (seq === previewSeq) document.getElementById('title-display').innerText = "Searching...";
                    }
                }

                function pressReserve(firstPriority) {
                    if (!currentCode) { showToast("Type a song code first"); return; }
                    sendAction('reserve', { code: currentCode, priority: firstPriority ? "true" : "false" });
                    currentCode = "";
                    updateDisplay();
                }

                function playSong(code) {
                    sendAction('play_code', { code: code });
                    showToast("Playing song #" + code);
                }

                function reserveSong(code) {
                    sendAction('reserve', { code: code, priority: "false" });
                    showToast("Reserved song #" + code);
                }

                async function sendAction(action, params = {}) {
                    try {
                        let body = { action: action, ...params };
                        await fetch('/api/action', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(body)
                        });
                        showToast("Action: " + action);
                        fetchQueue();
                    } catch(e) { console.error(e); }
                }

                function showToast(msg) {
                    let t = document.getElementById('toast');
                    t.innerText = msg;
                    t.style.opacity = '1';
                    setTimeout(() => { t.style.opacity = '0'; }, 2000);
                }

                let isRsvModalOpen = false;

                async function fetchQueue() {
                    try {
                        let res = await fetch('/api/queue');
                        let data = await res.json();
                        let queue = data.queue || [];
                        let current = data.current;
                        isCurrentlyPlaying = !!data.playing;

                        let btn = document.getElementById('play-pause-btn');
                        if (btn) {
                            btn.innerText = isCurrentlyPlaying ? '⏸ Pause' : '▶ Play';
                            btn.className = 'transport-btn ' + (isCurrentlyPlaying ? 'pause' : 'play');
                        }

                        document.getElementById('rsv-badge').innerText = queue.length;
                        if (isRsvModalOpen) {
                            renderRsvModal(current, queue);
                        }
                    } catch(e) { console.error(e); }
                }

                function renderRsvModal(current, queue) {
                    let curBox = document.getElementById('rsv-current-box');
                    if (current && current.code) {
                        curBox.style.display = 'block';
                        document.getElementById('rsv-current-title').innerText = '#' + String(current.code).padStart(6, '0') + ' - ' + current.title + ' (' + current.singer + ')';
                    } else {
                        curBox.style.display = 'none';
                    }

                    let listEl = document.getElementById('rsv-queue-list');
                    if (!queue || queue.length === 0) {
                        listEl.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 24px;">No reserved songs in queue</div>';
                        return;
                    }

                    let html = "";
                    queue.forEach((item, index) => {
                        let displayCode = String(item.code).padStart(6, '0');
                        html += '<div class="rsv-item">' +
                            '<span class="rsv-num">#' + (index + 1) + '</span>' +
                            '<div class="rsv-info">' +
                                '<div class="rsv-title">#' + escapeHtml(displayCode) + ' - ' + escapeHtml(item.title) + '</div>' +
                                '<div class="rsv-singer">' + escapeHtml(item.singer) + '</div>' +
                            '</div>' +
                            '<div class="rsv-actions">' +
                                '<button class="s-btn s-btn-play" onclick="playRsvItem(' + item.code + ')">Play</button>' +
                                '<button class="s-btn" style="background: var(--red-btn);" onclick="removeRsvItem(' + index + ', ' + item.code + ')">✕</button>' +
                            '</div>' +
                        '</div>';
                    });
                    listEl.innerHTML = html;
                }

                function openRsvModal() {
                    isRsvModalOpen = true;
                    document.getElementById('rsv-modal').classList.add('active');
                    fetchQueue();
                }

                function closeRsvModal(e) {
                    if (e && e.target !== document.getElementById('rsv-modal') && !e.target.classList.contains('modal-close')) return;
                    isRsvModalOpen = false;
                    document.getElementById('rsv-modal').classList.remove('active');
                }

                async function playRsvItem(code) {
                    await sendAction('play_code', { code: code });
                    fetchQueue();
                }

                async function removeRsvItem(index, code) {
                    await sendAction('remove_reserved', { index: String(index), code: String(code) });
                    fetchQueue();
                }

                loadSongs('');
                setInterval(fetchQueue, 2000);
                fetchQueue();
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
