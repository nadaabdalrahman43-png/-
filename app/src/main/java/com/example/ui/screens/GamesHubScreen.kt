package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.data.model.GameType
import com.example.ui.components.StarRatingRow
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme

@Composable
fun GamesHubScreen(
    episodes: List<EpisodeItem>,
    onStartGame: (EpisodeItem, GameType) -> Unit,
    onOpenEpisodeToGenerate: (EpisodeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val readyEpisodes = episodes.filter { it.isReady }
    val nextToPrepare = episodes.firstOrNull { !it.isReady }

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
                Card(
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(166.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_game_arena_1795201308611),
                            contentDescription = stringResource(R.string.cd_games_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.62f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ساحة الألعاب المسلية",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اختاري حلقة والعبي 3 تحديات ممتعة لجمع النجوم!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.94f)
                            )
                        }
                    }
                }
            }

            if (readyEpisodes.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = AppTheme.extraColors.lavenderContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = AppTheme.extraColors.lavender,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Text(
                                text = "جهّزي أول حلقة لتفتح الألعاب!",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "عند فتح أي حلقة والضغط على «جهّزي الملخص والألعاب الآن»، ستحصلين فوراً على 3 ألعاب مسلية خاصة بها.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (nextToPrepare != null) {
                                Button(
                                    onClick = { onOpenEpisodeToGenerate(nextToPrepare) },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .height(50.dp)
                                        .testTag("prepare_first_episode_games_button")
                                ) {
                                    Text("جهّزي الحلقة ${nextToPrepare.episodeNumber} الآن")
                                }
                            }
                        }
                    }
                }
            } else {
                items(readyEpisodes, key = { it.videoId }) { episode ->
                    EpisodeGamesCard(
                        episode = episode,
                        onStartGame = { type -> onStartGame(episode, type) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun EpisodeGamesCard(
    episode: EpisodeItem,
    onStartGame: (GameType) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الحلقة ${episode.episodeNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = episode.summary?.friendlyTitle?.takeIf { it.isNotBlank() } ?: episode.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = AppTheme.extraColors.sunbeamContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${episode.totalStars}/${episode.maxStars} ⭐",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniGameTile(
                    title = "اختيارات",
                    stars = episode.quizBestStars,
                    icon = Icons.Rounded.Quiz,
                    containerColor = AppTheme.extraColors.coralContainer,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onStartGame(GameType.QUIZ) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_quiz_${episode.episodeNumber}")
                )
                MiniGameTile(
                    title = "صح وخطأ",
                    stars = episode.tfBestStars,
                    icon = Icons.Rounded.FlashOn,
                    containerColor = AppTheme.extraColors.mintContainer,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = { onStartGame(GameType.TRUE_FALSE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_tf_${episode.episodeNumber}")
                )
                MiniGameTile(
                    title = "توصيل",
                    stars = episode.matchBestStars,
                    icon = Icons.Rounded.Extension,
                    containerColor = AppTheme.extraColors.lavenderContainer,
                    accentColor = AppTheme.extraColors.lavender,
                    onClick = { onStartGame(GameType.MATCH) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hub_match_${episode.episodeNumber}")
                )
            }
        }
    }
}

@Composable
private fun MiniGameTile(
    title: String,
    stars: Int,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            StarRatingRow(earnedStars = stars, starSize = 14.dp)
        }
    }
}
