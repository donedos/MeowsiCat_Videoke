package com.outofthebox.meowsicattv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.Playlist
import com.outofthebox.meowsicattv.data.PlaylistStore
import com.outofthebox.meowsicattv.data.SongItem
import com.meowsicat.android.storage.BehaviorSettings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne

@Composable
fun PlaylistScreen(
    library: LibraryRepository.Library,
    onBack: () -> Unit,
    onPlayPlaylist: (SongItem, List<SongItem>) -> Unit
) {
    val context = LocalContext.current
    var playlists by remember { mutableStateOf(PlaylistStore.getPlaylists(context)) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialogFor by remember { mutableStateOf<Playlist?>(null) }

    if (selectedPlaylist != null) {
        PlaylistDetailScreen(
            playlist = selectedPlaylist!!,
            library = library,
            onBack = { selectedPlaylist = null },
            onPlayPlaylist = onPlayPlaylist,
            onUpdate = { playlists = PlaylistStore.getPlaylists(context) }
        )
        return
    }

    Column(
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "My Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Playlist", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No custom playlists yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tap the + button above to create your first playlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists, key = { it.id }) { pl ->
                    val songs = PlaylistStore.getPlaylistSongs(context, pl.id, library.songs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { selectedPlaylist = pl }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${songs.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2DD4BF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (songs.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onPlayPlaylist(songs.first(), songs.drop(1))
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF059669))
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            IconButton(
                                onClick = { showRenameDialogFor = pl },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155))
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = {
                                    PlaylistStore.deletePlaylist(context, pl.id)
                                    playlists = PlaylistStore.getPlaylists(context)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF991B1B))
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = { Text("Playlist name...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            PlaylistStore.createPlaylist(context, playlistName)
                            playlists = PlaylistStore.getPlaylists(context)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRenameDialogFor != null) {
        val pl = showRenameDialogFor!!
        var newName by remember { mutableStateOf(pl.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialogFor = null },
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            PlaylistStore.renamePlaylist(context, pl.id, newName)
                            playlists = PlaylistStore.getPlaylists(context)
                            showRenameDialogFor = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlaylistDetailScreen(
    playlist: Playlist,
    library: LibraryRepository.Library,
    onBack: () -> Unit,
    onPlayPlaylist: (SongItem, List<SongItem>) -> Unit,
    onUpdate: () -> Unit
) {
    val context = LocalContext.current
    var songs by remember(playlist) { mutableStateOf(PlaylistStore.getPlaylistSongs(context, playlist.id, library.songs)) }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    Column(
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { showAddSongsDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Songs", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (songs.isNotEmpty()) {
                var isShuffle by remember { mutableStateOf(BehaviorSettings.isShuffleEnabled(context)) }
                var repeatMode by remember { mutableStateOf(BehaviorSettings.getRepeatMode(context)) }

                Button(
                    onClick = {
                        val listToPlay = if (isShuffle) songs.shuffled() else songs
                        onPlayPlaylist(listToPlay.first(), listToPlay.drop(1))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Play All (${songs.size})", fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        isShuffle = !isShuffle
                        BehaviorSettings.setShuffleEnabled(context, isShuffle)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isShuffle) Color(0xFF2DD4BF) else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) Color(0xFF003731) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        repeatMode = (repeatMode + 1) % 3
                        BehaviorSettings.setRepeatMode(context, repeatMode)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (repeatMode != 0) Color(0xFF2DD4BF) else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (repeatMode == 2) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != 0) Color(0xFF003731) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (repeatMode != 0) {
                            Text(
                                text = if (repeatMode == 2) "1" else "All",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF003731)
                            )
                        }
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playlist is empty. Tap + to add songs.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(songs, key = { it.code }) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.displayTitle(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${song.displaySinger()} • #${song.displayCode()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2DD4BF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                PlaylistStore.removeSongFromPlaylist(context, playlist.id, song.code)
                                songs = PlaylistStore.getPlaylistSongs(context, playlist.id, library.songs)
                                onUpdate()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF991B1B))
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddSongsDialog) {
        var query by remember { mutableStateOf("") }
        val results = remember(library.songs, query) {
            if (query.isBlank()) library.songs.take(30)
            else library.songs.filter {
                it.title.contains(query, true) || it.singer.contains(query, true) || it.code.toString().contains(query)
            }.take(30)
        }

        AlertDialog(
            onDismissRequest = { showAddSongsDialog = false },
            title = { Text("Add Songs to ${playlist.name}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search song title or code...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(results, key = { it.code }) { song ->
                            val currentPls = PlaylistStore.getPlaylists(context)
                            val currentPl = currentPls.find { it.id == playlist.id }
                            val inPlaylist = currentPl?.songCodes?.contains(song.code) == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(song.displayTitle(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                    Text("${song.displaySinger()} • #${song.code}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2DD4BF), maxLines = 1)
                                }
                                Button(
                                    onClick = {
                                        if (inPlaylist) {
                                            PlaylistStore.removeSongFromPlaylist(context, playlist.id, song.code)
                                        } else {
                                            PlaylistStore.addSongToPlaylist(context, playlist.id, song.code)
                                        }
                                        songs = PlaylistStore.getPlaylistSongs(context, playlist.id, library.songs)
                                        onUpdate()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (inPlaylist) Color(0xFF991B1B) else Color(0xFF059669)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(if (inPlaylist) "Added" else "Add", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAddSongsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
