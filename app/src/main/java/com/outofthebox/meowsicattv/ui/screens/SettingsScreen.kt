package com.outofthebox.meowsicattv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import java.util.Locale
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.compose.material.icons.filled.UploadFile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.outofthebox.meowsicattv.server.WebServerManager
import com.outofthebox.meowsicattv.ui.components.TopBar
import com.outofthebox.meowsicattv.ui.lyricFontFamily
import com.meowsicat.android.audio.SoundFontManager
import com.meowsicat.android.storage.AudioSettings
import com.meowsicat.android.storage.BehaviorSettings
import com.meowsicat.android.storage.BgvSettings
import com.meowsicat.android.storage.LyricSettings
import com.meowsicat.android.storage.ScoreSettings
import com.meowsicat.android.storage.StorageScanner
import androidx.documentfile.provider.DocumentFile
import android.os.Environment
import java.io.File
import androidx.compose.material.icons.filled.Info

@Composable
fun SettingsScreen(
    currentRoot: String,
    onSave: (String) -> Unit,
    onThemeChanged: (Int) -> Unit = {},
    onShowGuide: () -> Unit,
    onBack: () -> Unit
) {
    var draft by rememberSaveable(currentRoot) { mutableStateOf(currentRoot) }
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

    var showManageFilesDialog by remember { mutableStateOf(false) }

    var currentTheme by remember {
        mutableIntStateOf(BehaviorSettings.getAppTheme(context))
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

    var staticPhotoPath by remember {
        mutableStateOf(BgvSettings.getStaticPhotoPath(context))
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
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val path = uriToAbsolutePath(uri)
            if (path != null) {
                draft = path
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()
                onSave(path)
            }
        }
    }

    val uploadFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val targetRootPath = draft.ifBlank { currentRoot }
            val rootDir = File(targetRootPath).apply { if (!exists()) mkdirs() }
            val idxDir = File(rootDir, "idx").apply { if (!exists()) mkdirs() }
            val songsDir = File(rootDir, "songs").apply { if (!exists()) mkdirs() }
            val soundDir = File(rootDir, "sound").apply { if (!exists()) mkdirs() }
            val bgvDir = File(rootDir, "bgv").apply { if (!exists()) mkdirs() }
            val bgphotoDir = File(rootDir, "bgphoto").apply { if (!exists()) mkdirs() }
            val videosDir = File(rootDir, "videos").apply { if (!exists()) mkdirs() }

            var count = 0
            for (uri in uris) {
                try {
                    val name = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
                    val lower = name.lowercase()
                    val target = when {
                        lower.contains("shubidx") || lower.contains("shubblob") || lower.contains("megmid") || lower.endsWith(".idx") -> File(idxDir, name)
                        lower.endsWith(".mid") || lower.endsWith(".kar") || lower.endsWith(".mp3") -> File(songsDir, name)
                        lower.endsWith(".sf2") -> File(soundDir, name)
                        lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") -> File(bgvDir, name)
                        lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") -> File(bgphotoDir, name)
                        else -> File(rootDir, name)
                    }
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    count++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (count > 0) {
                onSave(rootDir.absolutePath)
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

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                TextButton(onClick = onShowGuide) {
                    Icon(Icons.Filled.Info, contentDescription = "Setup Guide")
                    Spacer(Modifier.width(8.dp))
                    Text("View Setup Guide", fontWeight = FontWeight.Bold)
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
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Content & Database Files",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Switch between storage locations (In-App Sandbox, External Storage, or USB Storage for Android TV) to manage database files, song lists, backgrounds, and soundfonts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    StorageLocationDropdown(
                        currentPath = draft,
                        onPathSelected = { newPath ->
                            draft = newPath
                            val activeDir = File(newPath)
                            if (!activeDir.exists()) {
                                activeDir.mkdirs()
                            }
                            onSave(newPath)
                        },
                        onPickFolderRequested = {
                            try {
                                folderPickerLauncher.launch(null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dirExists = File(draft).exists()
                            Icon(
                                imageVector = if (dirExists) Icons.Filled.Check else Icons.Filled.Folder,
                                contentDescription = null,
                                tint = if (dirExists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Active Folder Path:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = draft,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            try {
                                uploadFilesLauncher.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload Files (shubblob, shubidx, etc.)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showManageFilesDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Folder, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View / Manage Uploaded Files", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = {
                                try {
                                    folderPickerLauncher.launch(null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FolderOpen, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Browse Custom Folder", maxLines = 1)
                            }
                        }

                        TextButton(
                            onClick = {
                                val defaultPath = LibraryRepository.defaultRoot(context).absolutePath
                                draft = defaultPath
                                onSave(defaultPath)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Restore, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reset to In-App Default", maxLines = 1)
                            }
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

        // APP COLOR THEME CARD WITH DROPDOWN
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "App Color Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Customize the accent colors and highlights of the app based on iOS color schemes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))
                    AppThemeDropdown(
                        currentTheme = currentTheme,
                        onThemeSelected = { newTheme ->
                            BehaviorSettings.setAppTheme(context, newTheme)
                            currentTheme = newTheme
                            onThemeChanged(newTheme)
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
            val photoDir = remember(currentRoot) {
                BgvSettings.getBgPhotoDirectory(context, currentRoot)
            }
            val photoCount = remember(photoDir) {
                if (photoDir != null && photoDir.exists()) {
                    photoDir.walkTopDown().count { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
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
                            text = "Background",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Play .mp4 background videos from bgv/ ($mp4Count found), static background photos from bgphoto/ ($photoCount found), or live camera feed.",
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

                    if (bgvMode == BgvSettings.MODE_STATIC_PHOTO) {
                        StaticPhotoDropdown(
                            context = context,
                            currentRoot = currentRoot,
                            currentPath = staticPhotoPath,
                            onPhotoSelected = { path ->
                                staticPhotoPath = path
                                BgvSettings.setStaticPhotoPath(context, path)
                            }
                        )
                    } else if (bgvMode == BgvSettings.MODE_CAMERA) {
                        Spacer(Modifier.height(10.dp))
                        var cameraPermissionGranted by remember {
                            mutableStateOf(
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            )
                        }
                        val cameraPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            cameraPermissionGranted = isGranted
                        }

                        if (!cameraPermissionGranted) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Camera permission required for camera background",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                                ) {
                                    Text("Grant")
                                }
                            }
                        } else {
                            val availableLenses = remember(context) { BgvSettings.getAvailableCameraLenses(context) }
                            var currentLens by remember { mutableIntStateOf(BgvSettings.getCameraLens(context)) }

                            if (availableLenses.isNotEmpty()) {
                                CameraLensDropdown(
                                    availableLenses = availableLenses,
                                    currentLens = currentLens,
                                    onLensSelected = { lens ->
                                        currentLens = lens
                                        BgvSettings.setCameraLens(context, lens)
                                    }
                                )
                            } else {
                                Text(
                                    text = "No cameras detected on this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
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
                        text = "Choose the sound font used to play MIDI.",
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

        // KARAOKE BROADCAST (EXPERIMENTAL) CARD
        item {
            var broadcastEnabled by remember { mutableStateOf(BehaviorSettings.isBroadcastEnabled(context)) }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.SettingsRemote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Broadcast (Experimental)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (broadcastEnabled) "Karaoke streaming enabled for mobile web clients" else "Karaoke streaming disabled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Switch(
                            checked = broadcastEnabled,
                            onCheckedChange = { enabled ->
                                broadcastEnabled = enabled
                                BehaviorSettings.setBroadcastEnabled(context, enabled)
                                WebServerManager.setBroadcastEnabled(enabled)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showManageFilesDialog) {
        ManageFilesDialog(
            currentRoot = draft.ifBlank { currentRoot },
            onDismiss = { showManageFilesDialog = false },
            onFilesChanged = {
                onSave(draft.ifBlank { currentRoot })
            }
        )
    }
}

@Composable
private fun ManageFilesDialog(
    currentRoot: String,
    onDismiss: () -> Unit,
    onFilesChanged: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val rootDir = remember(currentRoot) { File(currentRoot) }
    var selectedCategory by remember { mutableStateOf("All") }
    var refreshCount by remember { mutableIntStateOf(0) }

    val categories = listOf("All", "idx", "songs", "sound", "bgv", "bgphoto", "videos")

    val fileList = remember(rootDir, selectedCategory, refreshCount) {
        val list = mutableListOf<Pair<String, File>>()
        val folders = if (selectedCategory == "All") {
            listOf("idx", "songs", "sound", "bgv", "bgphoto", "videos")
        } else {
            listOf(selectedCategory)
        }
        for (folder in folders) {
            val dir = File(rootDir, folder)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.filter { it.isFile }?.forEach { file ->
                    list.add(folder to file)
                }
            }
        }
        list.sortedBy { it.second.name.lowercase() }
    }

    val closeFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100L)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Uploaded Files (${fileList.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .focusRequester(closeFocusRequester)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Files list
                if (fileList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No uploaded files found in $selectedCategory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(fileList, key = { "${it.first}_${it.second.name}" }) { (folder, file) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$folder/ • ${formatFileSize(file.length())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            if (deleteFileSafely(context, file)) {
                                                refreshCount++
                                                onFilesChanged()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete File",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

@Composable
private fun settingsDropdownColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary
)

private data class FontOption(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontDropdown(
    currentType: Int,
    onFontSelected: (Int) -> Unit
) {
    val fontOptions = listOf(
        FontOption(20, "Montserrat Bold (Default)"),
        FontOption(0, "Videoke"),
        FontOption(10, "Fredoka (Rounded Karaoke)"),
        FontOption(21, "Poppins Bold"),
        FontOption(1, "Roboto Bold"),
        FontOption(22, "Quicksand"),
        FontOption(4, "Arial Black")
    )
    var expanded by remember { mutableStateOf(false) }
    val selected = fontOptions.find { it.id == currentType }
    val selectedName = selected?.name ?: "Montserrat Bold (Default)"
    val selectedFamily = lyricFontFamily(currentType)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Font", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = selectedFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            fontOptions.forEach { option ->
                val optionFamily = lyricFontFamily(option.id)
                val isSelected = option.id == currentType
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = option.name,
                                fontFamily = optionFamily,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aa Bb Cc 123",
                                fontFamily = optionFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
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
            label = { Text("Select Lyric Style", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.style == currentStyle
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
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
            label = { Text("Select Lyric Placement", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.placement == currentPlacement
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
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
            label = { Text("Select Scoring Mode", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.mode == currentMode
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
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
        BgvOption(BgvSettings.MODE_STATIC_PHOTO, "Static background photo from bgphoto/"),
        BgvOption(BgvSettings.MODE_CAMERA, "Camera — Live camera background"),
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
            label = { Text("Select Background Mode", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.mode == currentMode
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
                    onClick = {
                        onModeSelected(option.mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class LensOption(val lens: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraLensDropdown(
    availableLenses: List<Int>,
    currentLens: Int,
    onLensSelected: (Int) -> Unit
) {
    val options = availableLenses.map { lens ->
        val name = when (lens) {
            BgvSettings.LENS_BACK -> "Rear Camera"
            BgvSettings.LENS_FRONT -> "Front Camera"
            BgvSettings.LENS_EXTERNAL -> "External Camera"
            else -> "Camera ($lens)"
        }
        LensOption(lens, name)
    }

    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.lens == currentLens }?.name ?: options.firstOrNull()?.name ?: "Select camera..."

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Camera Lens", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.lens == currentLens
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
                    onClick = {
                        onLensSelected(option.lens)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaticPhotoDropdown(
    context: Context,
    currentRoot: String,
    currentPath: String,
    onPhotoSelected: (String) -> Unit
) {
    val photos = remember(currentRoot, context) {
        BgvSettings.getAvailableBgPhotos(context, currentRoot)
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedFile = photos.find { it.absolutePath == currentPath } ?: photos.firstOrNull()
    val selectedName = selectedFile?.name ?: if (photos.isEmpty()) "No static photos found in bgphoto/" else "Select photo..."

    if (photos.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Static Photo", fontWeight = FontWeight.Bold) },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = settingsDropdownColors(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                photos.forEach { file ->
                    val isSelected = file.absolutePath == currentPath
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                        modifier = Modifier.background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                        ),
                        onClick = {
                            onPhotoSelected(file.absolutePath)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private data class ThemeOption(val themeId: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppThemeDropdown(
    currentTheme: Int,
    onThemeSelected: (Int) -> Unit
) {
    val options = listOf(
        ThemeOption(BehaviorSettings.THEME_DEFAULT, "MeowsiCat Teal (Default)"),
        ThemeOption(BehaviorSettings.THEME_IOS_BLUE, "MeowsiCat Blue"),
        ThemeOption(BehaviorSettings.THEME_IOS_PURPLE, "MeowsiCat Purple"),
        ThemeOption(BehaviorSettings.THEME_IOS_INDIGO, "MeowsiCat Indigo"),
        ThemeOption(BehaviorSettings.THEME_IOS_PINK, "MeowsiCat Pink"),
        ThemeOption(BehaviorSettings.THEME_IOS_ORANGE, "MeowsiCat Orange"),
        ThemeOption(BehaviorSettings.THEME_IOS_GREEN, "MeowsiCat Green")
    )
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.themeId == currentTheme }?.name ?: "MeowsiCat Teal (Default)"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Color Theme", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.themeId == currentTheme
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
                    onClick = {
                        onThemeSelected(option.themeId)
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
            label = { Text("Select Sound Font", fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = settingsDropdownColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            options.forEach { option ->
                val isSelected = option.path == selectedFontPath
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                    ),
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

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        result = cursor.getString(idx)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
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
        Spacer(Modifier.width(8.dp))
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
        val raw = path.substringAfter("raw:")
        return if (raw.startsWith("/")) raw else "/$raw"
    }
    val docId = runCatching { DocumentsContract.getTreeDocumentId(uri) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: path.substringAfterLast("/tree/", "").takeIf { it.isNotBlank() }
        ?: return null
    val volume = docId.substringBefore(':', missingDelimiterValue = "")
    val rel = docId.substringAfter(':', missingDelimiterValue = "").trim('/')
    val base = when {
        volume.isEmpty() || volume.equals("primary", ignoreCase = true) -> "/storage/emulated/0"
        else -> "/storage/$volume"
    }
    return if (rel.isEmpty()) base else "$base/$rel"
}

private data class StorageLocationOption(
    val id: String,
    val title: String,
    val path: String,
    val description: String,
    val isUsb: Boolean = false
)

private fun getStorageLocationOptions(context: Context, currentPath: String): List<StorageLocationOption> {
    val options = mutableListOf<StorageLocationOption>()
    val canonicalCurrent = runCatching { File(currentPath).canonicalPath }.getOrDefault(currentPath)

    // 1. In-App Data Storage
    val inAppFile = LibraryRepository.defaultRoot(context)
    val inAppPath = inAppFile.absolutePath
    options.add(
        StorageLocationOption(
            id = "IN_APP",
            title = "In-App Data Storage",
            path = inAppPath,
            description = "App private sandbox directory. Safe and requires no permissions."
        )
    )

    // 2. External Storage (Internal Shared Storage)
    val extFile = File(Environment.getExternalStorageDirectory(), "MeowsiCat")
    val extPath = extFile.absolutePath
    options.add(
        StorageLocationOption(
            id = "EXTERNAL",
            title = "External Storage (Internal Shared)",
            path = extPath,
            description = "Shared internal storage: $extPath"
        )
    )

    // 3. Discovered USB / External Storage Drives
    val candidates = StorageScanner.getCandidateRoots(context)
    val primaryExt = Environment.getExternalStorageDirectory()
    val primaryCanonical = runCatching { primaryExt.canonicalPath }.getOrDefault(primaryExt.absolutePath)

    var usbCount = 0
    for (candidate in candidates) {
        val candCanonical = runCatching { candidate.canonicalPath }.getOrDefault(candidate.absolutePath)
        if (candCanonical == primaryCanonical || candCanonical.contains("Android/data")) {
            continue
        }
        val usbMeowsiCat = File(candidate, "MeowsiCat")
        val label = StorageScanner.labelForRoot(context, candidate)
        usbCount++
        val displayTitle = if (label.contains("Storage", ignoreCase = true) || label.contains("USB", ignoreCase = true) || label.contains("SD", ignoreCase = true)) {
            "USB / External Drive ($label)"
        } else {
            "USB Storage Drive #$usbCount ($label)"
        }
        options.add(
            StorageLocationOption(
                id = "USB_${usbMeowsiCat.absolutePath}",
                title = displayTitle,
                path = usbMeowsiCat.absolutePath,
                description = "Mounted drive folder: ${usbMeowsiCat.absolutePath}",
                isUsb = true
            )
        )
    }

    // Also check scanned MgokLocations from StorageScanner.scan(context)
    val scannedLocations = StorageScanner.scan(context)
    for (loc in scannedLocations) {
        val locCanonical = runCatching { loc.root.canonicalPath }.getOrDefault(loc.root.absolutePath)
        if (options.none { runCatching { File(it.path).canonicalPath }.getOrDefault(it.path) == locCanonical }) {
            val isUsbDrive = loc.root.absolutePath.contains("/storage/") && !loc.root.absolutePath.contains("/emulated/")
            options.add(
                StorageLocationOption(
                    id = "SCANNED_${loc.root.absolutePath}",
                    title = if (isUsbDrive) "USB Storage (${loc.volumeLabel})" else loc.volumeLabel,
                    path = loc.root.absolutePath,
                    description = "Detected media folder: ${loc.root.absolutePath}",
                    isUsb = isUsbDrive
                )
            )
        }
    }

    // 4. Custom Path Option if currentPath is not in the list
    if (currentPath.isNotBlank() && options.none {
            val pCanon = runCatching { File(it.path).canonicalPath }.getOrDefault(it.path)
            pCanon == canonicalCurrent
        }) {
        options.add(
            StorageLocationOption(
                id = "CUSTOM",
                title = "Custom Folder Location",
                path = currentPath,
                description = "User specified folder: $currentPath"
            )
        )
    }

    return options
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageLocationDropdown(
    currentPath: String,
    onPathSelected: (String) -> Unit,
    onPickFolderRequested: () -> Unit
) {
    val context = LocalContext.current
    val options = remember(currentPath, context) {
        getStorageLocationOptions(context, currentPath)
    }

    val canonicalCurrent = remember(currentPath) {
        runCatching { File(currentPath).canonicalPath }.getOrDefault(currentPath)
    }

    val selectedOption = options.find {
        runCatching { File(it.path).canonicalPath }.getOrDefault(it.path) == canonicalCurrent
    } ?: options.firstOrNull()

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedOption?.title ?: "Select Storage Location",
                onValueChange = {},
                readOnly = true,
                label = { Text("Database & Content Storage Location", fontWeight = FontWeight.Bold) },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = settingsDropdownColors(),
                supportingText = {
                    Text(
                        text = selectedOption?.description ?: currentPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                options.forEach { option ->
                    val isSelected = runCatching { File(option.path).canonicalPath }.getOrDefault(option.path) == canonicalCurrent
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when {
                                            option.id == "IN_APP" -> Icons.Filled.Storage
                                            option.isUsb -> Icons.Filled.Folder
                                            else -> Icons.Filled.FolderOpen
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = option.title,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                        modifier = Modifier.background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
                        ),
                        onClick = {
                            onPathSelected(option.path)
                            expanded = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Browse / Pick Custom Folder...",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onPickFolderRequested()
                    }
                )
            }
        }
    }
}

private fun deleteFileSafely(context: Context, file: File): Boolean {
    if (file.delete()) return true
    try {
        val docFile = androidx.documentfile.provider.DocumentFile.fromFile(file)
        if (docFile.exists() && docFile.delete()) return true
    } catch (_: Exception) {}
    try {
        val uri = android.provider.MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
        if (uri != null) {
            val rows = context.contentResolver.delete(
                uri,
                "${android.provider.MediaStore.MediaColumns.DATA}=?",
                arrayOf(file.absolutePath)
            )
            if (rows > 0) return true
        }
    } catch (_: Exception) {}
    return false
}