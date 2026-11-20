package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.ui.components.GameResultCelebration
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchGameScreen(
    episode: EpisodeItem,
    onBack: () -> Unit,
    onGameCompleted: (Int) -> Unit
) {
    BackHandler(onBack = onBack)

    val pairs = episode.gamePack?.matchPairs.orEmpty()
    if (pairs.isEmpty()) {
        onBack()
        return
    }

    var gameSeed by remember { mutableIntStateOf(0) }
    val shuffledRightIndices = remember(gameSeed, pairs) {
        pairs.indices.toList().shuffled()
    }

    var selectedLeftIndex by remember(gameSeed) { mutableStateOf<Int?>(null) }
    var matchedIndices by remember(gameSeed) { mutableStateOf<Set<Int>>(emptySet()) }
    var mistakes by remember(gameSeed) { mutableIntStateOf(0) }
    var feedbackMessage by remember(gameSeed) { mutableStateOf<String?>(null) }
    var isMismatchFeedback by remember(gameSeed) { mutableStateOf(false) }

    val allMatched = matchedIndices.size == pairs.size
    val earnedStars = remember(mistakes, allMatched) {
        when {
            !allMatched -> 0
            mistakes <= 1 -> 3
            mistakes <= 3 -> 2
            else -> 1
        }
    }

    LaunchedEffect(allMatched) {
        if (allMatched) {
            delay(450L)
            onGameCompleted(earnedStars)
        }
    }

    if (allMatched) {
        GameResultCelebration(
            gameTitle = stringResource(R.string.game_match_title),
            earnedStars = earnedStars,
            scoreText = "طابقتِ كل الأزواج (${pairs.size}/${pairs.size}) • أخطاء: $mistakes",
            encouragementMessage = when (earnedStars) {
                3 -> "ملكة التوصيل بلا منازع! ذاكرتك وتركيزك ممتازان ✨"
                2 -> "عمل جميل جداً! وصلتِ كل فكرة بمعناها الصحيح."
                else -> "أحسنتِ بإكمال اللعبة! جربي مرة أخرى لجمع 3 نجوم كاملة."
            },
            onPlayAgain = { gameSeed++ },
            onBackToEpisode = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.game_match_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("match_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    Surface(
                        color = AppTheme.extraColors.lavenderContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "${matchedIndices.size} / ${pairs.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppTheme.extraColors.lavender,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = AppTheme.extraColors.lavenderContainer,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "اضغطي على فكرة من العمود الأول، ثم اختاري معناها الصحيح من العمود الثاني!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                AnimatedVisibility(visible = feedbackMessage != null) {
                    Surface(
                        color = if (isMismatchFeedback) {
                            AppTheme.extraColors.coralContainer
                        } else {
                            AppTheme.extraColors.mintContainer
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = feedbackMessage.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isMismatchFeedback) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                AppTheme.extraColors.correctGreen
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "الفكرة",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        pairs.forEachIndexed { idx, pair ->
                            val isMatched = idx in matchedIndices
                            val isSelected = selectedLeftIndex == idx
                            MatchCardItem(
                                text = pair.concept,
                                isMatched = isMatched,
                                isSelected = isSelected,
                                accentColor = MaterialTheme.colorScheme.primary,
                                testTag = "match_left_$idx",
                                onClick = {
                                    if (!isMatched) {
                                        selectedLeftIndex = idx
                                        feedbackMessage = null
                                    }
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "المعنى / التطبيق",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppTheme.extraColors.lavender,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        shuffledRightIndices.forEach { originalIdx ->
                            val pair = pairs[originalIdx]
                            val isMatched = originalIdx in matchedIndices
                            MatchCardItem(
                                text = pair.meaning,
                                isMatched = isMatched,
                                isSelected = false,
                                accentColor = AppTheme.extraColors.lavender,
                                testTag = "match_right_$originalIdx",
                                onClick = {
                                    if (!isMatched) {
                                        val left = selectedLeftIndex
                                        if (left == null) {
                                            isMismatchFeedback = true
                                            feedbackMessage = "اختاري فكرة من عمود «الفكرة» أولاً ✨"
                                        } else if (left == originalIdx) {
                                            matchedIndices = matchedIndices + originalIdx
                                            selectedLeftIndex = null
                                            isMismatchFeedback = false
                                            feedbackMessage = "تطابق رائع! أحسنتِ 🌟"
                                        } else {
                                            mistakes++
                                            isMismatchFeedback = true
                                            feedbackMessage = "قريبة جداً! جربي اختياراً آخر 💡"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MatchCardItem(
    text: String,
    isMatched: Boolean,
    isSelected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    testTag: String,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isMatched -> AppTheme.extraColors.mintContainer
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "matchCardBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isMatched -> AppTheme.extraColors.correctGreen
            isSelected -> accentColor
            else -> MaterialTheme.colorScheme.outline
        },
        label = "matchCardBorder"
    )

    Surface(
        onClick = onClick,
        enabled = !isMatched,
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected || isMatched) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isMatched) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = AppTheme.extraColors.correctGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isMatched) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
