package com.outofthebox.meowsicattv.ui

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/**
 * Maps a [com.meowsicat.android.storage.LyricSettings] font type id to the [FontFamily]
 * used to render karaoke lyrics.
 *
 * The featured fonts (Fredoka, Montserrat, Poppins, Quicksand) are not bundled with the
 * app, so each type is approximated with the closest installed system font family. When a
 * real font file is added to `res/font`, replace the corresponding branch with
 * `FontFamily(Font(R.font.<name>))`.
 */
internal fun lyricFontFamily(type: Int): FontFamily = when (type) {
    10 -> deviceFont("casual")
    20 -> deviceFont("sans-serif")
    21 -> deviceFont("sans-serif-condensed")
    22 -> deviceFont("sans-serif-light")
    1 -> deviceFont("sans-serif-medium")
    4 -> deviceFont("sans-serif-black")
    else -> FontFamily.Default
}

private fun deviceFont(familyName: String): FontFamily =
    FontFamily(Font(familyName = DeviceFontFamilyName(familyName)))
