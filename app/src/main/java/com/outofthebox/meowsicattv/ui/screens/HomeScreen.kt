package com.outofthebox.meowsicattv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outofthebox.meowsicattv.data.LibraryRepository
import com.outofthebox.meowsicattv.data.SongItem
import com.outofthebox.meowsicattv.ui.components.NetflixCard

data class CategoryCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    library: LibraryRepository.Library,
    isLoading: Boolean = false,
    onOpenSongs: () -> Unit,
    onOpenPlayer: () -> Unit = onOpenSongs,
    onOpenArtists: () -> Unit = {},
    onOpenNewSongs: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenPlaylist: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenRankings: () -> Unit = {},
    onOpenSong: (SongItem) -> Unit = {}
) {
    val isWide = LocalConfiguration.current.screenWidthDp >= 720
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(32.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = "MeowsiCat",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Karaoke for everyone",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (library.error != null) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = library.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (isLoading || library.songs.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Loading song library from MeowsiCat folder...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        } else {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${library.songs.size} songs  •  ${library.singers.size} artists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(28.dp))
        val context = LocalContext.current
        val isTvDevice = remember(context) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                context.packageManager.hasSystemFeature("android.hardware.type.television")
        }

        val items = remember(isTvDevice, onOpenPlayer, onOpenSongs, onOpenFavorites, onOpenPlaylist, onOpenSettings) {
            buildList {
                add(CategoryCardData("Karaoke Player", "Select & play songs", Icons.Filled.PlayCircle, onOpenPlayer))
                add(CategoryCardData("Song Search", "Find by title or singer", Icons.Filled.Search, onOpenSongs))
                add(CategoryCardData("Favorites", "Your tagged songs", Icons.Filled.Favorite, onOpenFavorites))
                add(CategoryCardData("Playlist", "Custom song list", Icons.AutoMirrored.Filled.QueueMusic, onOpenPlaylist))
                if (!isTvDevice) {
                    add(CategoryCardData("Remote", "Mobile remote control", Icons.Filled.SettingsRemote, { }))
                }
                add(CategoryCardData("Settings", "Content & display", Icons.Filled.Settings, onOpenSettings))
            }
        }
        if (isWide) {
            CategoryRow(items)
        } else {
            CategoryGrid(items, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryGrid(items: List<CategoryCardData>, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.chunked(2).forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pair.forEachIndexed { colIndex, item ->
                    val isFirst = rowIndex == 0 && colIndex == 0
                    val req = if (isFirst) focusRequester else remember { FocusRequester() }
                    CategoryCardFace(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .focusRequester(req)
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryRow(items: List<CategoryCardData>) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.title }) { index, item ->
            val req = if (index == 0) focusRequester else remember { FocusRequester() }
            CategoryCardFace(
                item = item,
                modifier = Modifier
                    .size(width = 240.dp, height = 150.dp)
                    .focusRequester(req)
            )
        }
    }
}

@Composable
private fun CategoryCardFace(item: CategoryCardData, modifier: Modifier) {
    NetflixCard(onClick = item.onClick, modifier = modifier) {
        CategoryCardContent(item)
    }
}

@Composable
private fun CategoryCardContent(item: CategoryCardData) {
    Column(modifier = Modifier.padding(16.dp)) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
