package com.outofthebox.songhubatv.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.outofthebox.songhubatv.data.LibraryRepository
import com.outofthebox.songhubatv.ui.components.TopBar
import com.songhub.android.audio.SoundFontManager
import com.songhub.android.storage.AudioSettings
import com.songhub.android.storage.BgvSettings
import com.songhub.android.storage.LyricSettings
import com.songhub.android.storage.ScoreSettings
import java.io.File

@Composable
fun SettingsScreen(
    currentRoot: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var draft by rememberSaveable { mutableStateOf(currentRoot) }
    val context = LocalContext.current
    val soundFonts by produceState(
        initialValue = emptyList<File>(),
        key1 = context
    ) {
        value = SoundFontManager.listSoundFonts(context)
    }
    var selectedFont by remember {
        mutableStateOf<String?>(SoundFontManager.getSelectedSoundFontPath(context))
    }

    var lyricStyle by remember {
        mutableStateOf(LyricSettings.getLyricStyle(context))
    }

    var currentFontType by remember {
        mutableStateOf(LyricSettings.getFontType(context))
    }

    var scoreMode by remember {
        mutableStateOf(ScoreSettings.getScoreMode(context))
    }

    var difficulty by remember {
        mutableStateOf(ScoreSettings.getDifficulty(context))
    }

    var bgvMode by remember {
        mutableStateOf(BgvSettings.getMode(context))
    }

    // AudioSettings states
    var midiMasterVol by remember { mutableStateOf(AudioSettings.getMidiMasterVolume(context)) }
    var mp3MasterVol by remember { mutableStateOf(AudioSettings.getMp3MasterVolume(context)) }
    var vocalEnabled by remember { mutableStateOf(AudioSettings.isVocalEnabled(context)) }
    var reverbLevel by remember { mutableStateOf(AudioSettings.getReverb(context)) }
    var chorusLevel by remember { mutableStateOf(AudioSettings.getChorus(context)) }
    var polyphonyVoices by remember { mutableStateOf(AudioSettings.getPolyphony(context)) }
    var melodyChan by remember { mutableStateOf(AudioSettings.getMelodyChannel(context)) }
    var typingVol by remember { mutableStateOf(AudioSettings.getTypingVolumeLevel(context)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TopBar(onBack = onBack, title = "Settings")
        }

        // MIDI ENGINE SECTION
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MIDI ENGINE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(4.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingRow(
                            title = "MIDI Master Volume",
                            subtitle = "Level: $midiMasterVol%",
                            icon = Icons.Filled.VolumeUp,
                            onClick = {
                                midiMasterVol = when (midiMasterVol) {
                                    100 -> 125
                                    125 -> 150
                                    150 -> 75
                                    75 -> 50
                                    else -> 100
                                }
                                AudioSettings.setMidiMasterVolume(context, midiMasterVol)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "MP3 Master Volume",
                            subtitle = "Level: $mp3MasterVol%",
                            icon = Icons.Filled.MusicNote,
                            onClick = {
                                mp3MasterVol = when (mp3MasterVol) {
                                    100 -> 125
                                    125 -> 150
                                    150 -> 75
                                    75 -> 50
                                    else -> 100
                                }
                                AudioSettings.setMp3MasterVolume(context, mp3MasterVol)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Vocal Mode / Multiplex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vocal Mode (Multiplex)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (vocalEnabled) "Enabled: MIDI and audio active" else "Disabled: Only MP3/audio, MIDI lyrics only",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = vocalEnabled,
                                onCheckedChange = {
                                    vocalEnabled = it
                                    AudioSettings.setVocalEnabled(context, it)
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "MIDI Reverb",
                            subtitle = "Level: " + when(reverbLevel) { 0 -> "Off"; 1 -> "Low"; 2 -> "Medium"; 3 -> "High"; else -> "Default" },
                            icon = Icons.Filled.GraphicEq,
                            onClick = {
                                reverbLevel = (reverbLevel + 1) % 5
                                AudioSettings.setReverb(context, reverbLevel)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "MIDI Chorus",
                            subtitle = "Level: " + when(chorusLevel) { 0 -> "Off"; 1 -> "Low"; 2 -> "Medium"; 3 -> "High"; else -> "Default" },
                            icon = Icons.Filled.Layers,
                            onClick = {
                                chorusLevel = (chorusLevel + 1) % 5
                                AudioSettings.setChorus(context, chorusLevel)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "Polyphony",
                            subtitle = "Voices: $polyphonyVoices",
                            icon = Icons.Filled.Piano,
                            onClick = {
                                polyphonyVoices = when (polyphonyVoices) {
                                    256 -> 512
                                    512 -> 1024
                                    else -> 256
                                }
                                AudioSettings.setPolyphony(context, polyphonyVoices)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "Default Melody Channel",
                            subtitle = "Channel $melodyChan",
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            onClick = {
                                melodyChan = when (melodyChan) {
                                    1 -> 2
                                    2 -> 4
                                    4 -> 16
                                    else -> 1
                                }
                                AudioSettings.setMelodyChannel(context, melodyChan)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingRow(
                            title = "Number Typing Sound",
                            subtitle = "Volume: " + when(typingVol) { 0 -> "Off"; 1 -> "Low"; 2 -> "Mid"; else -> "High" },
                            icon = Icons.Filled.Keyboard,
                            onClick = {
                                typingVol = (typingVol + 1) % 4
                                AudioSettings.setTypingVolumeLevel(context, typingVol)
                            }
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Content location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Folder containing idx/, songs/, bgv/, bgphoto/ and sound/ (expects shubidx and shubblob inside idx/).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        label = { Text("Absolute path") }
                    )

                    Spacer(Modifier.height(14.dp))
                    Row {
                        TextButton(onClick = {
                            draft = LibraryRepository.DEFAULT_ROOT
                        }) {
                            Icon(Icons.Filled.Restore, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text("Defaults: ${LibraryRepository.DEFAULT_ROOT}")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { onSave(draft) },
                            enabled = draft.isNotBlank()
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Save and reload library",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lyrics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Lyrics presentation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "How the current lyric line is highlighted on the player.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    OptionRow(
                        name = "Modern — Spotify / Google Music style",
                        selected = lyricStyle == LyricSettings.STYLE_MODERN,
                        onClick = {
                            LyricSettings.setLyricStyle(context, LyricSettings.STYLE_MODERN)
                            lyricStyle = LyricSettings.STYLE_MODERN
                        }
                    )
                    OptionRow(
                        name = "Classic karaoke — white text filled by indigo",
                        selected = lyricStyle == LyricSettings.STYLE_KARAOKE,
                        onClick = {
                            LyricSettings.setLyricStyle(context, LyricSettings.STYLE_KARAOKE)
                            lyricStyle = LyricSettings.STYLE_KARAOKE
                        }
                    )
                }
            }
        }

        // FONT SELECTION CARD WITH DROPDOWN
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Karaoke Lyrics Font",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose the typography style for lyrics displayed during song playback (includes Fredoka featured rounded style).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    FontDropdown(
                        currentType = currentFontType,
                        onFontSelected = { newType ->
                            currentFontType = newType
                            LyricSettings.setFontType(context, newType)
                        }
                    )
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Scoring",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Record and display a score when a song finishes singing. Scores are kept per song and shown in Top scores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    OptionRow(
                        name = "On — Random score algorithm",
                        selected = scoreMode == ScoreSettings.ScoreMode.RANDOM,
                        onClick = {
                            ScoreSettings.setScoreMode(context, ScoreSettings.ScoreMode.RANDOM)
                            scoreMode = ScoreSettings.ScoreMode.RANDOM
                        }
                    )
                    OptionRow(
                        name = "On — Microphone estimation",
                        selected = scoreMode == ScoreSettings.ScoreMode.MICROPHONE,
                        onClick = {
                            ScoreSettings.setScoreMode(context, ScoreSettings.ScoreMode.MICROPHONE)
                            scoreMode = ScoreSettings.ScoreMode.MICROPHONE
                        }
                    )
                    OptionRow(
                        name = "Off — No scoring",
                        selected = scoreMode == ScoreSettings.ScoreMode.OFF,
                        onClick = {
                            ScoreSettings.setScoreMode(context, ScoreSettings.ScoreMode.OFF)
                            scoreMode = ScoreSettings.ScoreMode.OFF
                        }
                    )

                    if (scoreMode != ScoreSettings.ScoreMode.OFF) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Scoring difficulty",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        OptionRow(
                            name = "Easy — Generously high scores",
                            selected = difficulty == ScoreSettings.Difficulty.EASY,
                            onClick = {
                                ScoreSettings.setDifficulty(context, ScoreSettings.Difficulty.EASY)
                                difficulty = ScoreSettings.Difficulty.EASY
                            }
                        )
                        OptionRow(
                            name = "Medium — Balanced karaoke scoring",
                            selected = difficulty == ScoreSettings.Difficulty.MEDIUM,
                            onClick = {
                                ScoreSettings.setDifficulty(context, ScoreSettings.Difficulty.MEDIUM)
                                difficulty = ScoreSettings.Difficulty.MEDIUM
                            }
                        )
                        OptionRow(
                            name = "Hard — Strict scoring",
                            selected = difficulty == ScoreSettings.Difficulty.HARD,
                            onClick = {
                                ScoreSettings.setDifficulty(context, ScoreSettings.Difficulty.HARD)
                                difficulty = ScoreSettings.Difficulty.HARD
                            }
                        )
                    }
                }
            }
        }

        item {
            val bgvDir = remember(currentRoot) {
                BgvSettings.getBgvDirectory(context, currentRoot)
            }
            val mp4Count = remember(bgvDir) {
                if (bgvDir != null && bgvDir.exists()) {
                    bgvDir.walkTopDown().count { it.isFile && it.extension.equals("mp4", ignoreCase = true) }
                } else 0
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Background Video (BGV)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Play .mp4 background videos during song playback from the bgv/ folder. ($mp4Count .mp4 files found)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    OptionRow(
                        name = "On — Play .mp4 background videos from bgv/",
                        selected = bgvMode == BgvSettings.MODE_VIDEOS,
                        onClick = {
                            BgvSettings.setMode(context, BgvSettings.MODE_VIDEOS)
                            bgvMode = BgvSettings.MODE_VIDEOS
                        }
                    )
                    OptionRow(
                        name = "Off — Default dark gradient background",
                        selected = bgvMode != BgvSettings.MODE_VIDEOS,
                        onClick = {
                            BgvSettings.setMode(context, 3) // Off
                            bgvMode = 3
                        }
                    )
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Sound font (.sf2)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose the sound font used to play MIDI. Fonts are read from the sound/ folder inside SongHub. Applies on the next playback.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    SoundFontRow(
                        name = "Auto — first .sf2 found",
                        selected = selectedFont == null,
                        onClick = {
                            SoundFontManager.clearSelectedSoundFont(context)
                            selectedFont = null
                        }
                    )
                    if (soundFonts.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No .sf2 files found. Add them to a SongHub/sound/ folder.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        soundFonts.forEach { file ->
                            SoundFontRow(
                                name = file.name,
                                selected = selectedFont == file.absolutePath,
                                onClick = {
                                    SoundFontManager.setSelectedSoundFont(context, file.absolutePath)
                                    selectedFont = file.absolutePath
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Storage permission",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "On Android 11+, grant “All files access” so the app can read your SongHub content folders outside the app sandbox.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class FontOption(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontDropdown(
    currentType: Int,
    onFontSelected: (Int) -> Unit
) {
    val fontOptions = listOf(
        FontOption(0, "Videoke (Default)"),
        FontOption(10, "Fredoka (Rounded Karaoke - Featured Style)"),
        FontOption(20, "Montserrat Bold"),
        FontOption(21, "Poppins Bold"),
        FontOption(1, "Roboto Bold"),
        FontOption(22, "Quicksand"),
        FontOption(4, "Arial Black")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = fontOptions.find { it.id == currentType }?.name ?: "Videoke (Default)"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Font") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fontOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onFontSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SoundFontRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OptionRow(name = name, selected = selected, onClick = onClick)
}

@Composable
private fun OptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = if (selected) "Selected" else null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            }
        )
        Spacer(Modifier.padding(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}