package com.outofthebox.meowsicattv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.meowsicat.android.storage.BehaviorSettings
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.outofthebox.meowsicattv.data.FavoritesStore
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.SongItem
import com.outofthebox.meowsicattv.ui.components.FocusableCard
import com.outofthebox.meowsicattv.ui.components.MicBadge
import com.outofthebox.meowsicattv.ui.components.TopBar

enum class SongTab { ALL, NEW }
enum class SortMode { TITLE, CODE, ARTIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    library: LibraryRepository.Library,
    isRefreshing: Boolean = false,
    onRefreshLibrary: () -> Unit = {},
    onBack: () -> Unit,
    onOpen: (SongItem) -> Unit,
    onPlayAll: (SongItem, List<SongItem>) -> Unit = { song, _ -> onOpen(song) },
    artistFilter: String? = null,
    favoritesOnly: Boolean = false,
    playlistOnly: Boolean = false
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SongTab.ALL) }
    var sortMode by remember { mutableStateOf(SortMode.TITLE) }

    val filtered = remember(library.songs, query, sortMode, artistFilter, selectedTab, favoritesOnly, playlistOnly, context) {
        val baseList = when {
            favoritesOnly -> FavoritesStore.getFavorites(context, library.songs)
            playlistOnly -> library.songs.take(100)
            selectedTab == SongTab.NEW -> library.songs.sortedWith(compareByDescending<SongItem> { it.code }).take(300)
            else -> library.songs
        }
        baseList.filter { song ->
            (artistFilter == null || song.singer == artistFilter) &&
                (query.isBlank() || song.title.contains(query, true) || song.singer.contains(query, true))
        }.sortedWith(
            when (sortMode) {
                SortMode.TITLE -> compareBy({ it.title.lowercase() }, { it.singer.lowercase() })
                SortMode.CODE -> compareByDescending<SongItem> { it.code }
                SortMode.ARTIST -> compareBy({ it.singer.lowercase() }, { it.title.lowercase() })
            }
        )
    }

    val pageTitle = when {
        artistFilter != null -> artistFilter
        favoritesOnly -> "Favorite Songs"
        playlistOnly -> "Playlist"
        else -> "Song Search"
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefreshLibrary,
        modifier = Modifier.fillMaxSize()
    ) {
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
            TopBar(onBack = onBack, title = pageTitle)
            if (favoritesOnly || playlistOnly) {
                var isShuffle by remember { mutableStateOf(BehaviorSettings.isShuffleEnabled(context)) }
                var repeatMode by remember { mutableStateOf(BehaviorSettings.getRepeatMode(context)) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (filtered.isNotEmpty()) {
                                val listToPlay = if (isShuffle) filtered.shuffled() else filtered
                                onPlayAll(listToPlay.first(), listToPlay.drop(1))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Play All (${filtered.size})", fontWeight = FontWeight.Bold)
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
            if (artistFilter == null && !favoritesOnly && !playlistOnly) {
                SearchBar(query = query, onQueryChange = { query = it }, onClear = { query = "" })
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedTab == SongTab.ALL,
                            onClick = { selectedTab = SongTab.ALL },
                            label = { Text("All Songs") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTab == SongTab.NEW,
                            onClick = { selectedTab = SongTab.NEW },
                            label = { Text("New Songs") }
                        )
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Sort:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        SortToggle("Title", sortMode == SortMode.TITLE) { sortMode = SortMode.TITLE }
                    }
                    item {
                        SortToggle("Code", sortMode == SortMode.CODE) { sortMode = SortMode.CODE }
                    }
                    item {
                        SortToggle("Artist", sortMode == SortMode.ARTIST) { sortMode = SortMode.ARTIST }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = "${filtered.size} songs",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No songs found", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.code }) { song ->
                        SongListItem(song = song, onClick = { onOpen(song) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(200L)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .focusRequester(focusRequester),
        placeholder = { Text("Search songs or artists…") },
        shape = RoundedCornerShape(28.dp),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onClear()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun SortToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun ListItemActionButton(
    onClick: () -> Unit,
    defaultBg: Color = Color.Transparent,
    focusedBg: Color = MaterialTheme.colorScheme.primary,
    focusedBorderColor: Color = Color.White,
    modifier: Modifier = Modifier.size(34.dp),
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        targetValue = if (isFocused) focusedBg else defaultBg,
        label = "itemActionBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.18f else 1f,
        label = "itemActionScale"
    )

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(if (isFocused) 8.dp else 0.dp, shape)
            .clip(shape)
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) focusedBorderColor else Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SongListItem(song: SongItem, onClick: () -> Unit) {
    val context = LocalContext.current
    var isFav by remember(song.code) {
        mutableStateOf(FavoritesStore.isFavorite(context, song.code))
    }

    FocusableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = song.displayCode(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.displaySinger(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (song.isUser) {
                MicBadge()
                Spacer(Modifier.width(8.dp))
            }

            ListItemActionButton(
                onClick = {
                    isFav = FavoritesStore.toggleFavorite(context, song.code)
                },
                defaultBg = if (isFav) Color(0xFFF472B6).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                focusedBg = Color(0xFFEC4899),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) Color(0xFFF472B6) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            ListItemActionButton(
                onClick = onClick,
                defaultBg = Color(0xFF059669),
                focusedBg = Color(0xFF10B981),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
