package com.outofthebox.songhubatv.data

import java.io.File

data class SongItem(
    val code: Int,
    val title: String,
    val singer: String,
    val style: String,
    val language: String,
    val isUser: Boolean,
    val isMp3: Boolean,
    val songFile: File,
    val byteOffset: Long,
    val byteLength: Long,
    val mp3File: File? = null,
    val mp3Offset: Long = 0,
    val mp3Length: Long = 0
) {
    fun displayTitle(): String = when {
        code == 0 -> "Select a song"
        title.isBlank() -> "Song #$code"
        else -> title
    }

    fun displaySinger(): String = when {
        code == 0 -> "000000"
        singer.isBlank() -> "Unknown"
        else -> singer
    }

    fun displayCode(): String = "%06d".format(code)

    fun sortableCode(): Long = if (isUser) code.toLong() else code.toLong()

    companion object {
        fun placeholder(): SongItem = SongItem(
            code = 0,
            title = "Select a song",
            singer = "000000",
            style = "",
            language = "",
            isUser = false,
            isMp3 = false,
            songFile = File(""),
            byteOffset = 0,
            byteLength = 0
        )
    }
}
