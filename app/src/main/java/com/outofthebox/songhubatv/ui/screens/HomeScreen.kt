package com.outofthebox.songhubatv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outofthebox.songhubatv.data.LibraryRepository
import com.outofthebox.songhubatv.data.SongItem
import com.outofthebox.songhubatv.ui.components.FocusableCard

data class CategoryCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    library: LibraryRepository.Library,
    onOpenSongs: () -> Unit,
    onOpenArtists: () -> Unit,
    onOpenNewSongs: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRankings: () -> Unit,
    onOpenSong: (SongItem) -> Unit = {}
) {
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
            .verticalScroll(rememberScrollState())
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
                    text = "SongHub",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Karaoke for every song",
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
        } else {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${library.songs.size} songs  •  ${library.singers.size} artists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(28.dp))
        CategoryRow(
            items = listOf(
                CategoryCardData("Songs", "Browse full catalog", Icons.Filled.LibraryMusic, onOpenSongs),
                CategoryCardData("New Songs", "Latest additions", Icons.Filled.Star, onOpenNewSongs),
                CategoryCardData("Artists", "Browse by singer", Icons.Filled.Person, onOpenArtists),
                CategoryCardData("Rankings", "Top scores", Icons.Filled.EmojiEvents, onOpenRankings),
                CategoryCardData("Settings", "Content & display", Icons.Filled.Settings, onOpenSettings)
            )
        )

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = Color(0x22FFFFFF))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Now trending",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(library.songs.take(30)) { song ->
                TrendingCard(song.title, song.singer, onClick = { onOpenSong(song) })
            }
        }
    }
}

@Composable
private fun CategoryRow(items: List<CategoryCardData>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(items, key = { it.title }) { item ->
            FocusableCard(
                onClick = item.onClick,
                modifier = Modifier
                    .size(width = 220.dp, height = 140.dp)
                    .padding(14.dp)
            ) {
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
        }
    }
}

@Composable
private fun TrendingCard(title: String, singer: String, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier
            .size(width = 260.dp, height = 160.dp)
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                text = singer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}