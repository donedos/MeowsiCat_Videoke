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
    fun displayTitle(): String = if (title.isBlank()) "Song #$code" else title

    fun displaySinger(): String = if (singer.isBlank()) "Unknown" else singer

    fun sortableCode(): Long = if (isUser) code.toLong() else code.toLong()
}