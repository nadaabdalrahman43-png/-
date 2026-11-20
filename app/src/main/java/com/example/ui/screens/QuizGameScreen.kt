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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.ui.components.GameResultCelebration
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGameScreen(
    episode: EpisodeItem,
    onBack: () -> Unit,
    onGameCompleted: (Int) -> Unit
) {
    BackHandler(onBack = onBack)

    val questions = episode.gamePack?.quizQuestions.orEmpty()
    if (questions.isEmpty()) {
        onBack()
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var streak by remember { mutableIntStateOf(0) }
    var showHint by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    val earnedStars = remember(correctCount, questions.size) {
        val ratio = correctCount.toFloat() / questions.size.coerceAtLeast(1)
        when {
            ratio >= 0.85f -> 3
            ratio >= 0.6f -> 2
            ratio > 0f -> 1
            else -> 0
        }
    }

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onGameCompleted(earnedStars)
        }
    }

    if (isFinished) {
        GameResultCelebration(
            gameTitle = stringResource(R.string.game_quiz_title),
            earnedStars = earnedStars,
            scoreText = "$correctCount من ${questions.size} إجابات صحيحة",
            encouragementMessage = when (earnedStars) {
                3 -> "عبقرية يا نجمة! فهمتِ الحلقة بكل تفاصيلها الجميلة."
                2 -> "أداء رائع جداً! خطوة صغيرة وتحصلي على النجوم الثلاث كاملة."
                else -> "بداية لطيفة! اقرئي الملخص مرة أخرى وجربي التحدي لتجمعي نجوماً أكثر."
            },
            onPlayAgain = {
                currentIndex = 0
                selectedOption = null
                correctCount = 0
                lives = 3
                streak = 0
                showHint = false
                isFinished = false
            },
            onBackToEpisode = onBack
        )
        return
    }

    val currentQuestion = questions[currentIndex]
    val answered = selectedOption != null
    val optionLetters = listOf("أ", "ب", "ج", "د")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.game_quiz_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("quiz_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = if (i <= lives) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                tint = AppTheme.extraColors.heartRose,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "السؤال ${currentIndex + 1} من ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (streak >= 2) {
                        Surface(
                            color = AppTheme.extraColors.sunbeamContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = AppTheme.extraColors.starGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "سلسلة $streak إجابات!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = AppTheme.extraColors.coralContainer,
                    strokeCap = StrokeCap.Round
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (currentQuestion.hint.isNotBlank() && !answered) {
                            if (!showHint) {
                                TextButton(
                                    onClick = { showHint = true },
                                    modifier = Modifier.testTag("quiz_hint_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lightbulb,
                                        contentDescription = null,
                                        tint = AppTheme.extraColors.starGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.show_hint))
                                }
                            } else {
                                Surface(
                                    color = AppTheme.extraColors.sunbeamContainer,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "تلميح: ${currentQuestion.hint}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                currentQuestion.options.forEachIndexed { idx, optionText ->
                    val isCorrect = idx == currentQuestion.correctIndex
                    val isSelected = selectedOption == idx

                    val containerColor by animateColorAsState(
                        targetValue = when {
                            !answered -> MaterialTheme.colorScheme.surface
                            isCorrect -> AppTheme.extraColors.mintContainer
                            isSelected -> AppTheme.extraColors.coralContainer
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        },
                        label = "optionBg"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = when {
                            !answered -> MaterialTheme.colorScheme.outline
                            isCorrect -> AppTheme.extraColors.correctGreen
                            isSelected -> AppTheme.extraColors.wrongRed
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
                        label = "optionBorder"
                    )

                    Surface(
                        onClick = {
                            if (!answered) {
                                selectedOption = idx
                                if (isCorrect) {
                                    correctCount++
                                    streak++
                                } else {
                                    streak = 0
                                    lives = (lives - 1).coerceAtLeast(0)
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = containerColor,
                        border = BorderStroke(if (answered && (isCorrect || isSelected)) 2.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (answered && isCorrect) {
                                    AppTheme.extraColors.correctGreen
                                } else if (answered && isSelected) {
                                    AppTheme.extraColors.wrongRed
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionLetters.getOrElse(idx) { "${idx + 1}" },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (answered && (isCorrect || isSelected)) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (answered && isCorrect) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = AppTheme.extraColors.correctGreen
                                )
                            } else if (answered && isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = AppTheme.extraColors.wrongRed
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = answered) {
                    val wasCorrect = selectedOption == currentQuestion.correctIndex
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (wasCorrect) {
                                    AppTheme.extraColors.mintContainer
                                } else {
                                    AppTheme.extraColors.sunbeamContainer
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (wasCorrect) "أحسنتِ يا بطلة! إجابة صحيحة ✨" else "لا بأس، تعلّمنا معلومة جديدة 💡",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wasCorrect) {
                                        AppTheme.extraColors.correctGreen
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        val isLast = currentIndex == questions.lastIndex || lives == 0
                        Button(
                            onClick = {
                                if (isLast) {
                                    isFinished = true
                                } else {
                                    currentIndex++
                                    selectedOption = null
                                    showHint = false
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("quiz_next_button")
                        ) {
                            Text(
                                text = if (isLast) {
                                    stringResource(R.string.finish_game)
                                } else {
                                    stringResource(R.string.next_question)
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
