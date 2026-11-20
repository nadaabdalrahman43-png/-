package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EpisodeItem
import com.example.data.model.EpisodeJsonCodec

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val videoId: String,
    val playlistId: String,
    val episodeNumber: Int,
    val title: String,
    val channelName: String,
    val durationText: String,
    val thumbnailUrl: String,
    val description: String,
    val summaryJson: String? = null,
    val gamePackJson: String? = null,
    val quizBestStars: Int = 0,
    val tfBestStars: Int = 0,
    val matchBestStars: Int = 0,
    val customNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): EpisodeItem = EpisodeItem(
        videoId = videoId,
        playlistId = playlistId,
        episodeNumber = episodeNumber,
        title = title,
        channelName = channelName,
        durationText = durationText,
        thumbnailUrl = thumbnailUrl,
        description = description,
        summary = EpisodeJsonCodec.decodeSummary(summaryJson),
        gamePack = EpisodeJsonCodec.decodeGamePack(gamePackJson),
        quizBestStars = quizBestStars,
        tfBestStars = tfBestStars,
        matchBestStars = matchBestStars,
        customNotes = customNotes
    )
}

@Entity(tableName = "playlist_meta")
data class PlaylistMetaEntity(
    @PrimaryKey val id: Int = 1,
    val playlistId: String,
    val playlistTitle: String,
    val sourceUrl: String,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
