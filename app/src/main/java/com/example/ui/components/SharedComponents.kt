package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Modifier.warmAtmosphereBackdrop(): Modifier {
    val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    val tertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
    val bg = MaterialTheme.colorScheme.background
    return this
        .background(bg)
        .drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary, Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.08f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.9f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary, Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.45f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.1f, size.height * 0.45f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiary, Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.88f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.85f, size.height * 0.88f)
            )
        }
}

@Composable
fun StarRatingRow(
    earnedStars: Int,
    maxStars: Int = 3,
    starSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val starColor = AppTheme.extraColors.starGold
    val emptyColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= earnedStars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = null,
                tint = if (i <= earnedStars) starColor else emptyColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun EpisodeThumbnail(
    thumbnailUrl: String,
    durationText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.extraColors.coralContainer)
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = stringResource(R.string.cd_episode_thumb),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        if (durationText.isNotBlank()) {
            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun GameResultCelebration(
    gameTitle: String,
    earnedStars: Int,
    scoreText: String,
    encouragementMessage: String,
    onPlayAgain: () -> Unit,
    onBackToEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val confettiColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        AppTheme.extraColors.starGold,
        AppTheme.extraColors.lavender
    )
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiPhase"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .warmAtmosphereBackdrop(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = 24
            for (i in 0 until count) {
                val seed = (i * 47 + 19) % 100
                val baseX = (seed / 100f) * size.width
                val fall = ((phase + (i * 0.13f)) % 1f) * size.height
                val drift = sin((phase * 6.28f + i).toDouble()).toFloat() * 18f
                val radius = (5f + (i % 4) * 2.5f) * pulse
                val color = confettiColors[i % confettiColors.size].copy(alpha = 0.45f)
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset((baseX + drift).coerceIn(0f, size.width), fall)
                )
            }
        }

        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppTheme.extraColors.sunbeamContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = AppTheme.extraColors.starGold,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (earnedStars) {
                        3 -> "ممتازة يا بطلة!"
                        2 -> "رائعة جداً!"
                        1 -> "بداية جميلة!"
                        else -> "محاولة رائعة!"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = gameTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                StarRatingRow(
                    earnedStars = earnedStars,
                    maxStars = 3,
                    starSize = 44.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = AppTheme.extraColors.mintContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = encouragementMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("play_again_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.play_again),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackToEpisode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("back_to_episode_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.back_to_episode),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePlaylistDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onResetDefault: () -> Unit
) {
    var inputText by remember(currentUrl) { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.playlist_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "يمكنكِ لصق رابط قائمة تشغيل يوتيوب أو رابط حلقة واحدة لتجهيز ملخصاتها وألعابها.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(stringResource(R.string.playlist_dialog_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_url_input"),
                    shape = RoundedCornerShape(14.dp)
                )
                TextButton(
                    onClick = {
                        onResetDefault()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("reset_default_playlist_button")
                ) {
                    Text(text = stringResource(R.string.playlist_dialog_reset))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSubmitUrl(inputText.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_playlist_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.playlist_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.playlist_dialog_cancel))
            }
        }
    )
}
