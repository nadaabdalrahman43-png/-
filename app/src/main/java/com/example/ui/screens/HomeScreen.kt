package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.ui.components.ChangePlaylistDialog
import com.example.ui.components.EpisodeThumbnail
import com.example.ui.components.StarRatingRow
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.EpisodeFilter
import com.example.ui.viewmodel.GenerationState
import com.example.ui.viewmodel.PlaylistLoadState

@Composable
fun HomeScreen(
    playlistTitle: String,
    sourceUrl: String,
    episodes: List<EpisodeItem>,
    allEpisodesCount: Int,
    totalStars: Int,
    readyCount: Int,
    loadState: PlaylistLoadState,
    generationStates: Map<String, GenerationState>,
    selectedFilter: EpisodeFilter,
    searchQuery: String,
    onFilterSelected: (EpisodeFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onRefreshClick: () -> Unit,
    onLoadCustomUrl: (String) -> Unit,
    onResetDefaultPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlaylistDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        ChangePlaylistDialog(
            currentUrl = sourceUrl,
            onDismiss = { showPlaylistDialog = false },
            onSubmitUrl = onLoadCustomUrl,
            onResetDefault = onResetDefaultPlaylist
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .warmAtmosphereBackdrop(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroBannerHeader(
                    playlistTitle = playlistTitle,
                    allEpisodesCount = allEpisodesCount,
                    readyCount = readyCount,
                    totalStars = totalStars,
                    onRefreshClick = onRefreshClick,
                    onChangePlaylistClick = { showPlaylistDialog = true }
                )
            }

            item {
                SearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }

            when (loadState) {
                is PlaylistLoadState.Loading -> {
                    if (episodes.isEmpty()) {
                        item {
                            PlaylistLoadingCard()
                        }
                    }
                }
                is PlaylistLoadState.Error -> {
                    item {
                        PlaylistErrorCard(
                            message = loadState.message,
                            onRetry = onRefreshClick,
                            onChangeLink = { showPlaylistDialog = true }
                        )
                    }
                }
                is PlaylistLoadState.Ready -> Unit
            }

            if (episodes.isEmpty() && loadState is PlaylistLoadState.Ready) {
                item {
                    EmptyFilterCard(
                        hasSearch = searchQuery.isNotBlank() || selectedFilter != EpisodeFilter.ALL,
                        onClearFilters = {
                            onSearchQueryChange("")
                            onFilterSelected(EpisodeFilter.ALL)
                        }
                    )
                }
            }

            items(episodes, key = { it.videoId }) { episode ->
                EpisodeListCard(
                    episode = episode,
                    isGenerating = generationStates[episode.videoId] is GenerationState.Loading,
                    onClick = { onEpisodeClick(episode) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HeroBannerHeader(
    playlistTitle: String,
    allEpisodesCount: Int,
    readyCount: Int,
    totalStars: Int,
    onRefreshClick: () -> Unit,
    onChangePlaylistClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1795201308406),
                    contentDescription = stringResource(R.string.cd_hero_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.56f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onRefreshClick,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("refresh_playlist_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.playlist_refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Surface(
                        onClick = onChangePlaylistClick,
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("change_playlist_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.playlist_change),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_greeting),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playlistTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.94f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    icon = Icons.Rounded.MenuBook,
                    label = "$allEpisodesCount حلقة",
                    containerColor = AppTheme.extraColors.coralContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Rounded.AutoAwesome,
                    label = "$readyCount ملخّصة",
                    containerColor = AppTheme.extraColors.mintContainer,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Rounded.Star,
                    label = "$totalStars نجمة",
                    containerColor = AppTheme.extraColors.sunbeamContainer,
                    contentColor = AppTheme.extraColors.starGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: EpisodeFilter,
    onFilterSelected: (EpisodeFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("ابحثي عن حلقة بالاسم...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "مسح البحث")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("episode_search_field")
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                EpisodeFilter.ALL to "كل الحلقات",
                EpisodeFilter.READY to "جاهزة للعب",
                EpisodeFilter.NEW to "جديدة"
            )
            filters.forEach { (filter, title) ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun EpisodeListCard(
    episode: EpisodeItem,
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("episode_card_${episode.episodeNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EpisodeThumbnail(
                thumbnailUrl = episode.thumbnailUrl,
                durationText = episode.durationText,
                modifier = Modifier
                    .width(116.dp)
                    .height(82.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "حلقة ${episode.episodeNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (isGenerating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "جاري التحضير...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (episode.isReady) {
                        Surface(
                            color = AppTheme.extraColors.mintContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "جاهزة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = episode.summary?.friendlyTitle?.takeIf { it.isNotBlank() } ?: episode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (episode.isReady) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SportsEsports,
                                contentDescription = null,
                                tint = AppTheme.extraColors.lavender,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "3 ألعاب • ${episode.totalStars}/${episode.maxStars} ⭐",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StarRatingRow(
                            earnedStars = (episode.totalStars / 3).coerceIn(0, 3),
                            starSize = 16.dp
                        )
                    } else {
                        Text(
                            text = "اضغطي لتجهيز ملخص سهل و3 ألعاب ممتعة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistLoadingCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(R.string.playlist_loading),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaylistErrorCard(
    message: String,
    onRetry: () -> Unit,
    onChangeLink: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.extraColors.coralContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("retry_playlist_button")
                ) {
                    Text(stringResource(R.string.retry))
                }
                Button(
                    onClick = onChangeLink,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.playlist_change))
                }
            }
        }
    }
}

@Composable
private fun EmptyFilterCard(
    hasSearch: Boolean,
    onClearFilters: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = AppTheme.extraColors.starGold,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (hasSearch) "لا توجد حلقات مطابقة للبحث أو التصنيف" else "لا توجد حلقات حالياً",
                style = MaterialTheme.typography.titleMedium
            )
            if (hasSearch) {
                Button(onClick = onClearFilters, shape = RoundedCornerShape(14.dp)) {
                    Text("عرض كل الحلقات")
                }
            }
        }
    }
}
