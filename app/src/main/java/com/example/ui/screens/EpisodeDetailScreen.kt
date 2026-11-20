package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.data.model.EpisodeSummary
import com.example.data.model.GameType
import com.example.ui.components.EpisodeThumbnail
import com.example.ui.components.StarRatingRow
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.GenerationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    episode: EpisodeItem,
    initialTab: Int,
    generationState: GenerationState,
    onBack: () -> Unit,
    onGenerate: () -> Unit,
    onSaveNotesAndRegenerate: (String) -> Unit,
    onLaunchGame: (GameType) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var activeTab by rememberSaveable(episode.videoId) { mutableIntStateOf(initialTab) }
    var showNotesDialog by remember { mutableStateOf(false) }

    if (showNotesDialog) {
        NotesDialog(
            initialNotes = episode.customNotes,
            onDismiss = { showNotesDialog = false },
            onSave = { notes ->
                showNotesDialog = false
                onSaveNotesAndRegenerate(notes)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الحلقة ${episode.episodeNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier.testTag("add_notes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = stringResource(R.string.add_notes_cta),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(episode.youtubeUrl))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.testTag("open_youtube_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = stringResource(R.string.watch_on_youtube),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .warmAtmosphereBackdrop(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    EpisodeHeaderCard(
                        episode = episode,
                        onWatchYouTube = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(episode.youtubeUrl))
                                )
                            }
                        }
                    )
                }

                if (generationState is GenerationState.Error) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.extraColors.coralContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = generationState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Button(
                                    onClick = onGenerate,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("retry_generate_button")
                                ) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }

                if (generationState is GenerationState.Loading) {
                    item {
                        GeneratingCard()
                    }
                } else if (!episode.isReady) {
                    item {
                        PrepareEpisodePromptCard(
                            onGenerate = onGenerate,
                            onAddNotes = { showNotesDialog = true }
                        )
                    }
                } else {
                    item {
                        DetailSegmentTabs(
                            activeTab = activeTab,
                            onSelectTab = { activeTab = it }
                        )
                    }

                    if (activeTab == 0) {
                        val summary = episode.summary!!
                        item {
                            SummaryIntroCard(summary = summary)
                        }

                        item {
                            Text(
                                text = "أهم النقاط السريعة",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        itemsIndexed(summary.keyPoints) { index, point ->
                            KeyPointCard(index = index + 1, title = point.title, detail = point.detail)
                        }

                        item {
                            PracticalTipAndQuoteSection(
                                summary = summary,
                                onGoToGames = { activeTab = 1 },
                                onRegenerate = onGenerate,
                                onEditNotes = { showNotesDialog = true }
                            )
                        }
                    } else {
                        item {
                            GamesTabSection(
                                episode = episode,
                                onLaunchGame = onLaunchGame
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun EpisodeHeaderCard(
    episode: EpisodeItem,
    onWatchYouTube: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EpisodeThumbnail(
                    thumbnailUrl = episode.thumbnailUrl,
                    durationText = episode.durationText,
                    modifier = Modifier
                        .width(124.dp)
                        .height(86.dp)
                        .clickable(onClick = onWatchYouTube)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = episode.summary?.friendlyTitle?.takeIf { it.isNotBlank() } ?: episode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (episode.channelName.isNotBlank()) {
                        Text(
                            text = episode.channelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onWatchYouTube,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("watch_youtube_button")
            ) {
                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.watch_on_youtube),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PrepareEpisodePromptCard(
    onGenerate: () -> Unit,
    onAddNotes: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                shape = CircleShape,
                color = AppTheme.extraColors.coralContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "جاهزة لاكتشاف هذه الحلقة؟",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "سنقرأ تفاصيل الحلقة ونجهّز لكِ ملخصاً بسيطاً ولطيفاً مناسباً لبنات الإعدادي، مع 3 ألعاب مسلية لجمع النجوم!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onGenerate,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_summary_button")
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.generate_summary_cta),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            TextButton(
                onClick = onAddNotes,
                modifier = Modifier.testTag("add_notes_before_generate_button")
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.add_notes_cta))
            }
        }
    }
}

@Composable
private fun GeneratingCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
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
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = stringResource(R.string.generating_content),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "لحظات صغيرة وننتهي من الملخص + مغامرة الاختيارات + صح وخطأ + التوصيل السحري ✨",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailSegmentTabs(
    activeTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SegmentPill(
                title = stringResource(R.string.summary_tab),
                icon = Icons.Rounded.MenuBook,
                selected = activeTab == 0,
                onClick = { onSelectTab(0) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_summary")
            )
            SegmentPill(
                title = stringResource(R.string.games_tab),
                icon = Icons.Rounded.SportsEsports,
                selected = activeTab == 1,
                onClick = { onSelectTab(1) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_games")
            )
        }
    }
}

@Composable
private fun SegmentPill(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryIntroCard(summary: EpisodeSummary) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = AppTheme.extraColors.coralContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = summary.hookIntro,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Text(
                text = "الخلاصة ببساطة",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = summary.easyStorySummary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun KeyPointCard(
    index: Int,
    title: String,
    detail: String
) {
    val badgeColors = listOf(
        AppTheme.extraColors.coralContainer to MaterialTheme.colorScheme.primary,
        AppTheme.extraColors.mintContainer to MaterialTheme.colorScheme.secondary,
        AppTheme.extraColors.lavenderContainer to AppTheme.extraColors.lavender,
        AppTheme.extraColors.sunbeamContainer to AppTheme.extraColors.starGold
    )
    val (bg, fg) = badgeColors[(index - 1) % badgeColors.size]

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleMedium,
                    color = fg,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PracticalTipAndQuoteSection(
    summary: EpisodeSummary,
    onGoToGames: () -> Unit,
    onRegenerate: () -> Unit,
    onEditNotes: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.extraColors.sunbeamContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = AppTheme.extraColors.starGold
                    )
                    Text(
                        text = "تطبيق عملي ليومك",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = summary.practicalTip,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.extraColors.mintContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = summary.encouragingQuote,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onGoToGames,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("go_to_games_button")
        ) {
            Icon(Icons.Rounded.SportsEsports, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "يلا نلعب على الحلقة ونجمع النجوم!",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onEditNotes,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("أضيفي ملاحظات", maxLines = 1)
            }
            OutlinedButton(
                onClick = onRegenerate,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("regenerate_summary_button")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.regenerate_cta), maxLines = 1)
            }
        }
    }
}

@Composable
private fun GamesTabSection(
    episode: EpisodeItem,
    onLaunchGame: (GameType) -> Unit
) {
    val pack = episode.gamePack ?: return
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GameLaunchCard(
            title = stringResource(R.string.game_quiz_title),
            description = stringResource(R.string.game_quiz_desc),
            metaLabel = "${pack.quizQuestions.size} أسئلة • 3 قلوب حياة",
            stars = episode.quizBestStars,
            icon = Icons.Rounded.Quiz,
            containerColor = AppTheme.extraColors.coralContainer,
            accentColor = MaterialTheme.colorScheme.primary,
            testTag = "launch_quiz_game_button",
            onClick = { onLaunchGame(GameType.QUIZ) }
        )
        GameLaunchCard(
            title = stringResource(R.string.game_tf_title),
            description = stringResource(R.string.game_tf_desc),
            metaLabel = "${pack.trueFalseQuestions.size} عبارات • مؤقت 12 ثانية",
            stars = episode.tfBestStars,
            icon = Icons.Rounded.FlashOn,
            containerColor = AppTheme.extraColors.mintContainer,
            accentColor = MaterialTheme.colorScheme.secondary,
            testTag = "launch_tf_game_button",
            onClick = { onLaunchGame(GameType.TRUE_FALSE) }
        )
        GameLaunchCard(
            title = stringResource(R.string.game_match_title),
            description = stringResource(R.string.game_match_desc),
            metaLabel = "${pack.matchPairs.size} أزواج للتوصيل السريع",
            stars = episode.matchBestStars,
            icon = Icons.Rounded.Extension,
            containerColor = AppTheme.extraColors.lavenderContainer,
            accentColor = AppTheme.extraColors.lavender,
            testTag = "launch_match_game_button",
            onClick = { onLaunchGame(GameType.MATCH) }
        )
    }
}

@Composable
private fun GameLaunchCard(
    title: String,
    description: String,
    metaLabel: String,
    stars: Int,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = containerColor,
                    modifier = Modifier.size(58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = metaLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                StarRatingRow(earnedStars = stars, starSize = 20.dp)
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(testTag)
            ) {
                Text(
                    text = stringResource(R.string.start_game),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun NotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember(initialNotes) { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.notes_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "إذا كان لديكِ أفكار أو مواقف معينة من الحلقة تحبين إضافتها في الملخص والألعاب، اكتبيها هنا.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text(stringResource(R.string.notes_dialog_hint)) },
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("episode_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(notes) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_notes_button")
            ) {
                Text(stringResource(R.string.notes_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.playlist_dialog_cancel))
            }
        }
    )
}
