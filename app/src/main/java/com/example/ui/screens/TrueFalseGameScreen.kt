package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EpisodeItem
import com.example.ui.components.GameResultCelebration
import com.example.ui.components.warmAtmosphereBackdrop
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay

private const val TF_TIME_MILLIS = 12_000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseGameScreen(
    episode: EpisodeItem,
    onBack: () -> Unit,
    onGameCompleted: (Int) -> Unit
) {
    BackHandler(onBack = onBack)

    val statements = episode.gamePack?.trueFalseQuestions.orEmpty()
    if (statements.isEmpty()) {
        onBack()
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var remainingMs by remember { mutableIntStateOf(TF_TIME_MILLIS) }
    var userChoice by remember { mutableStateOf<Boolean?>(null) }
    var timedOut by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val answered = userChoice != null || timedOut

    LaunchedEffect(currentIndex, answered, isFinished) {
        if (!answered && !isFinished) {
            remainingMs = TF_TIME_MILLIS
            while (remainingMs > 0) {
                delay(100L)
                remainingMs = (remainingMs - 100).coerceAtLeast(0)
            }
            if (userChoice == null) {
                timedOut = true
            }
        }
    }

    val earnedStars = remember(correctCount, statements.size) {
        val ratio = correctCount.toFloat() / statements.size.coerceAtLeast(1)
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
            gameTitle = stringResource(R.string.game_tf_title),
            earnedStars = earnedStars,
            scoreText = "$correctCount من ${statements.size} إجابات سريعة صحيحة",
            encouragementMessage = when (earnedStars) {
                3 -> "يا لسرعة بديهتك! أنتِ سريعة وذكية جداً يا نجمة!"
                2 -> "رائعة! سرعة وتركيز جميلان جداً."
                else -> "تجربة مسلية! جربي مرة ثانية وستكونين أسرع وأدق."
            },
            onPlayAgain = {
                currentIndex = 0
                remainingMs = TF_TIME_MILLIS
                userChoice = null
                timedOut = false
                correctCount = 0
                isFinished = false
            },
            onBackToEpisode = onBack
        )
        return
    }

    val currentItem = statements[currentIndex]
    val secondsLeft = ((remainingMs + 999) / 1000).coerceIn(0, 12)
    val timeRatio = remainingMs.toFloat() / TF_TIME_MILLIS.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.game_tf_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tf_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    Surface(
                        color = if (secondsLeft <= 4) {
                            AppTheme.extraColors.coralContainer
                        } else {
                            AppTheme.extraColors.mintContainer
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = if (secondsLeft <= 4) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${secondsLeft}ث",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "العبارة ${currentIndex + 1} من ${statements.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "النقاط: $correctCount ⭐",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.extraColors.starGold
                    )
                }

                LinearProgressIndicator(
                    progress = { timeRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (secondsLeft <= 4) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentItem.statement,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!answered) {
                                userChoice = true
                                if (currentItem.isTrue) correctCount++
                            }
                        },
                        enabled = !answered,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.extraColors.correctGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("tf_true_button")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.true_label),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Button(
                        onClick = {
                            if (!answered) {
                                userChoice = false
                                if (!currentItem.isTrue) correctCount++
                            }
                        },
                        enabled = !answered,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.extraColors.wrongRed
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("tf_false_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.false_label),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                AnimatedVisibility(visible = answered) {
                    val wasCorrect = !timedOut && userChoice == currentItem.isTrue
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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when {
                                        timedOut -> "انتهى الوقت! الإجابة الصحيحة: ${if (currentItem.isTrue) "صح" else "خطأ"}"
                                        wasCorrect -> "إجابة سريعة وصحيحة! برافو 🌟"
                                        else -> "الإجابة الصحيحة هي: ${if (currentItem.isTrue) "صح" else "خطأ"}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wasCorrect) {
                                        AppTheme.extraColors.correctGreen
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = currentItem.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        val isLast = currentIndex == statements.lastIndex
                        Button(
                            onClick = {
                                if (isLast) {
                                    isFinished = true
                                } else {
                                    currentIndex++
                                    userChoice = null
                                    timedOut = false
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("tf_next_button")
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
