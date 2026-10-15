package com.outofthebox.meowsicattv.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QrCode
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.ui.components.TopBar
import com.songhub.android.audio.SoundFontManager
import com.songhub.android.storage.AudioSettings
import com.songhub.android.storage.BehaviorSettings
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

    var lyricPlacement by remember {
        mutableStateOf(LyricSettings.getLyricPlacement(context))
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

    var fanfareEnabled by remember {
        mutableStateOf(ScoreSettings.isFanfareEnabled(context))
    }

    var bgvMode by remember {
        mutableStateOf(BgvSettings.getMode(context))
    }

    var floatingCatEnabled by remember {
        mutableStateOf(BehaviorSettings.isFloatingCatEnabled(context))
    }

    // AudioSettings states
    var midiMasterVol by remember { mutableStateOf(AudioSettings.getMidiMasterVolume(context)) }
    var mp3MasterVol by remember { mutableStateOf(AudioSettings.getMp3MasterVolume(context)) }
    var vocalEnabled by remember { mutableStateOf(AudioSettings.isVocalEnabled(context)) }
    var reverbLevel by remember { mutableStateOf(AudioSettings.getReverb(context)) }
    var chorusLevel by remember { mutableStateOf(AudioSettings.getChorus(context)) }
    var polyphonyVoices by remember { mutableStateOf(AudioSettings.getPolyphony(context)) }
    var melodyChan by remember { mutableStateOf(AudioSettings.getMelodyChannel(context)) }
    var wipeChan by remember { mutableStateOf(AudioSettings.getWipeMelodyChannel(context)) }
    var typingVol by remember { mutableStateOf(AudioSettings.getTypingVolumeLevel(context)) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val path = uriToAbsolutePath(uri)
            if (path != null) {
                draft = path
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MidiVolumeDropdown(
                            title = "MIDI Master Volume",
                            currentVol = midiMasterVol,
                            onVolSelected = {
                                midiMasterVol = it
                                AudioSettings.setMidiMasterVolume(context, it)
                            }
                        )

                        MidiVolumeDropdown(
                            title = "MP3 Master Volume",
                            currentVol = mp3MasterVol,
                            onVolSelected = {
                                mp3MasterVol = it
                                AudioSettings.setMp3MasterVolume(context, it)
                            }
                        )

                        // Vocal Mode / Multiplex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
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

                        ReverbChorusDropdown(
                            title = "MIDI Reverb",
                            currentLevel = reverbLevel,
                            onLevelSelected = {
                                reverbLevel = it
                                AudioSettings.setReverb(context, it)
                            }
                        )

                        ReverbChorusDropdown(
                            title = "MIDI Chorus",
                            currentLevel = chorusLevel,
                            onLevelSelected = {
                                chorusLevel = it
                                AudioSettings.setChorus(context, it)
                            }
                        )

                        PolyphonyDropdown(
                            currentVoices = polyphonyVoices,
                            onVoicesSelected = {
                                polyphonyVoices = it
                                AudioSettings.setPolyphony(context, it)
                            }
                        )

                        MelodyChannelDropdown(
                            currentChan = melodyChan,
                            onChanSelected = {
                                melodyChan = it
                                AudioSettings.setMelodyChannel(context, it)
                            }
                        )

                        WipeChannelDropdown(
                            currentWipe = wipeChan,
                            onWipeSelected = {
                                wipeChan = it
                                AudioSettings.setWipeMelodyChannel(context, it)
                            }
                        )

                        TypingVolumeDropdown(
                            currentVol = typingVol,
                            onVolSelected = {
                                typingVol = it
                                AudioSettings.setTypingVolumeLevel(context, it)
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

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            try {
                                folderPickerLauncher.launch(null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Locate Folder Manually")
                    }

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
                    LyricPresentationDropdown(
                        currentStyle = lyricStyle,
                        onStyleSelected = { newStyle ->
                            LyricSettings.setLyricStyle(context, newStyle)
                            lyricStyle = newStyle
                        }
                    )
                }
            }
        }

        // LYRIC PLACEMENT CARD WITH DROPDOWN
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Lyric Placement (Landscape & TV)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose whether lyrics are aligned to the center or displayed at the bottom part of the screen in landscape and TV modes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    LyricPlacementDropdown(
                        currentPlacement = lyricPlacement,
                        onPlacementSelected = { newPlacement ->
                            LyricSettings.setLyricPlacement(context, newPlacement)
                            lyricPlacement = newPlacement
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
                    val micPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { }
                    ScoringDropdown(
                        currentMode = scoreMode,
                        onModeSelected = { newMode ->
                            ScoreSettings.setScoreMode(context, newMode)
                            scoreMode = newMode
                            if (newMode == ScoreSettings.ScoreMode.MICROPHONE &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                micPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                            }
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

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "End-of-song fanfare",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (fanfareEnabled) {
                                        "Plays a celebratory sound with the score"
                                    } else {
                                        "Silent when the score appears"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = fanfareEnabled,
                                onCheckedChange = {
                                    fanfareEnabled = it
                                    ScoreSettings.setFanfareLevel(
                                        context,
                                        if (it) ScoreSettings.FANFARE_MID else ScoreSettings.FANFARE_OFF
                                    )
                                }
                            )
                        }
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
                    BgvDropdown(
                        currentMode = bgvMode,
                        onModeSelected = { newMode ->
                            BgvSettings.setMode(context, newMode)
                            bgvMode = newMode
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
                    SoundFontDropdown(
                        selectedFontPath = selectedFont,
                        soundFontFiles = soundFonts,
                        onFontSelected = { path ->
                            if (path == null) {
                                SoundFontManager.clearSelectedSoundFont(context)
                            } else {
                                SoundFontManager.setSelectedSoundFont(context, path)
                            }
                            selectedFont = path
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
                            Icons.Filled.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Floating Animated Cat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Show the floating draggable animated MeowsiCat on the player screen when controls are hidden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newState = !floatingCatEnabled
                                floatingCatEnabled = newState
                                BehaviorSettings.setFloatingCatEnabled(context, newState)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Floating Cat",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (floatingCatEnabled) "Enabled — Cat bounces when controls hide" else "Disabled — Hidden",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = floatingCatEnabled,
                            onCheckedChange = {
                                floatingCatEnabled = it
                                BehaviorSettings.setFloatingCatEnabled(context, it)
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
                            Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Storage Permission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "On Android 11+, grant “All files access” so the app can read your MeowsiCat content folders outside the app sandbox.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // WEB REMOTE SERVER PORT CARD
        item {
            var webPort by remember { mutableStateOf(BehaviorSettings.getWebPort(context).toString()) }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "Web Remote Server Port",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Set the network port used for the mobile web remote & QR code song search (default port: 5678).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = webPort,
                        onValueChange = { input ->
                            webPort = input
                            val portNum = input.toIntOrNull()
                            if (portNum != null && portNum in 1024..65535) {
                                BehaviorSettings.setWebPort(context, portNum)
                            }
                        },
                        label = { Text("Port Number") },
                        placeholder = { Text("5678") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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

private data class LyricStyleOption(val style: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricPresentationDropdown(
    currentStyle: Int,
    onStyleSelected: (Int) -> Unit
) {
    val options = listOf(
        LyricStyleOption(LyricSettings.STYLE_KARAOKE, "Classic karaoke — white text filled by indigo"),
        LyricStyleOption(LyricSettings.STYLE_MODERN, "Modern — Spotify / Google Music style")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.style == currentStyle }?.name ?: "Classic karaoke — white text filled by indigo"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Lyric Style") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onStyleSelected(option.style)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class LyricPlacementOption(val placement: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricPlacementDropdown(
    currentPlacement: Int,
    onPlacementSelected: (Int) -> Unit
) {
    val options = listOf(
        LyricPlacementOption(LyricSettings.PLACEMENT_CENTER, "Center"),
        LyricPlacementOption(LyricSettings.PLACEMENT_BOTTOM, "Bottom")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.placement == currentPlacement }?.name ?: "Center"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Lyric Placement") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onPlacementSelected(option.placement)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class ScoreModeOption(val mode: ScoreSettings.ScoreMode, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoringDropdown(
    currentMode: ScoreSettings.ScoreMode,
    onModeSelected: (ScoreSettings.ScoreMode) -> Unit
) {
    val options = listOf(
        ScoreModeOption(ScoreSettings.ScoreMode.OFF, "Off — No scoring"),
        ScoreModeOption(ScoreSettings.ScoreMode.RANDOM, "On — Random score algorithm"),
        ScoreModeOption(ScoreSettings.ScoreMode.MICROPHONE, "On — Listen to my voice (microphone)")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.mode == currentMode }?.name ?: "Off — No scoring"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Scoring Mode") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onModeSelected(option.mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class BgvOption(val mode: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BgvDropdown(
    currentMode: Int,
    onModeSelected: (Int) -> Unit
) {
    val options = listOf(
        BgvOption(BgvSettings.MODE_VIDEOS, "On — Play .mp4 background videos from bgv/"),
        BgvOption(3, "Off — Default dark gradient background")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.mode == currentMode }?.name ?: "Off — Default dark gradient background"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Background Mode") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onModeSelected(option.mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class SoundFontOption(val path: String?, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundFontDropdown(
    selectedFontPath: String?,
    soundFontFiles: List<File>,
    onFontSelected: (String?) -> Unit
) {
    val options = remember(soundFontFiles) {
        buildList {
            add(SoundFontOption(null, "Auto — first .sf2 found"))
            soundFontFiles.forEach { file ->
                add(SoundFontOption(file.absolutePath, file.name))
            }
        }
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.path == selectedFontPath }?.name ?: "Auto — first .sf2 found"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Sound Font") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onFontSelected(option.path)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MidiVolumeDropdown(
    title: String,
    currentVol: Int,
    onVolSelected: (Int) -> Unit
) {
    val options = listOf(50, 75, 100, 125, 150)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = "$currentVol%",
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { vol ->
                DropdownMenuItem(
                    text = { Text("$vol%") },
                    onClick = {
                        onVolSelected(vol)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReverbChorusDropdown(
    title: String,
    currentLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    val options = listOf(
        0 to "Off",
        1 to "Low",
        2 to "Medium",
        3 to "High",
        4 to "Default"
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.first == currentLevel }?.second ?: "Default"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.second) },
                    onClick = {
                        onLevelSelected(opt.first)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PolyphonyDropdown(
    currentVoices: Int,
    onVoicesSelected: (Int) -> Unit
) {
    val options = listOf(256, 512, 1024)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = "$currentVoices Voices",
            onValueChange = {},
            readOnly = true,
            label = { Text("Polyphony") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { voices ->
                DropdownMenuItem(
                    text = { Text("$voices Voices") },
                    onClick = {
                        onVoicesSelected(voices)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MelodyChannelDropdown(
    currentChan: Int,
    onChanSelected: (Int) -> Unit
) {
    val options = listOf(1, 2, 4, 16)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = "Channel $currentChan",
            onValueChange = {},
            readOnly = true,
            label = { Text("Default Melody Channel") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { chan ->
                DropdownMenuItem(
                    text = { Text("Channel $chan") },
                    onClick = {
                        onChanSelected(chan)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WipeChannelDropdown(
    currentWipe: Int,
    onWipeSelected: (Int) -> Unit
) {
    val options = buildList {
        add(0 to "Auto (detect melody)")
        for (i in 1..16) {
            add(i to "Channel $i")
        }
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.first == currentWipe }?.second ?: "Auto (detect melody)"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Lyric Wipe Channel") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.second) },
                    onClick = {
                        onWipeSelected(opt.first)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypingVolumeDropdown(
    currentVol: Int,
    onVolSelected: (Int) -> Unit
) {
    val options = listOf(
        0 to "Off",
        1 to "Low",
        2 to "Mid",
        3 to "High"
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.first == currentVol }?.second ?: "Mid"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Number Typing Sound") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.second) },
                    onClick = {
                        onVolSelected(opt.first)
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

private fun uriToAbsolutePath(uri: Uri): String? {
    val path = uri.path ?: return null
    if (path.contains("raw:")) {
        return path.substringAfter("raw:")
    }
    if (path.contains("primary:")) {
        val sub = path.substringAfter("primary:")
        return "/storage/emulated/0/$sub"
    }
    val split = path.split(":")
    if (split.size > 1) {
        val rel = split.last()
        return "/storage/emulated/0/$rel"
    }
    return null
}