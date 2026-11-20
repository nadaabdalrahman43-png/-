package com.example.data.repository

import com.example.data.local.EpisodeDao
import com.example.data.local.EpisodeEntity
import com.example.data.local.PlaylistMetaEntity
import com.example.data.model.EpisodeItem
import com.example.data.model.EpisodeJsonCodec
import com.example.data.model.GameType
import com.example.data.remote.EpisodeAiService
import com.example.data.remote.YouTubePlaylistService
import com.example.data.remote.YouTubeTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class EpisodeRepository(
    private val dao: EpisodeDao,
    private val youtubeService: YouTubePlaylistService,
    private val aiService: EpisodeAiService
) {

    val activePlaylistMeta: Flow<PlaylistMetaEntity?> = dao.observeActivePlaylist()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeEpisodes: Flow<List<EpisodeItem>> = dao.observeActivePlaylist()
        .flatMapLatest { meta ->
            val playlistId = meta?.playlistId ?: YouTubePlaylistService.DEFAULT_PLAYLIST_ID
            dao.observeEpisodesForPlaylist(playlistId).map { list ->
                list.map { it.toDomain() }
            }
        }

    fun observeEpisode(videoId: String): Flow<EpisodeItem?> =
        dao.observeEpisodeById(videoId).map { it?.toDomain() }

    suspend fun ensureDefaultPlaylistLoaded(): Result<String> {
        val existing = dao.getActivePlaylist()
        val playlistId = existing?.playlistId ?: YouTubePlaylistService.DEFAULT_PLAYLIST_ID
        val cachedEpisodes = dao.getEpisodesForPlaylistOnce(playlistId)
        return if (cachedEpisodes.isNotEmpty() && existing != null) {
            Result.success(existing.playlistTitle)
        } else {
            syncPlaylistByTarget(
                target = YouTubeTarget.Playlist(playlistId),
                sourceUrl = existing?.sourceUrl ?: YouTubePlaylistService.DEFAULT_PLAYLIST_URL
            )
        }
    }

    suspend fun refreshActivePlaylist(): Result<String> {
        val current = dao.getActivePlaylist()
        val sourceUrl = current?.sourceUrl ?: YouTubePlaylistService.DEFAULT_PLAYLIST_URL
        val target = youtubeService.parseYouTubeTarget(sourceUrl)
            ?: YouTubeTarget.Playlist(YouTubePlaylistService.DEFAULT_PLAYLIST_ID)
        return syncPlaylistByTarget(target, sourceUrl)
    }

    suspend fun loadCustomYouTubeUrl(urlInput: String): Result<String> {
        val target = youtubeService.parseYouTubeTarget(urlInput)
            ?: return Result.failure(
                IllegalArgumentException("الرابط غير صحيح. الصقي رابط قائمة تشغيل أو حلقة يوتيوب صالحة.")
            )
        return syncPlaylistByTarget(target, urlInput.trim())
    }

    suspend fun resetToDefaultPlaylist(): Result<String> {
        return syncPlaylistByTarget(
            target = YouTubeTarget.Playlist(YouTubePlaylistService.DEFAULT_PLAYLIST_ID),
            sourceUrl = YouTubePlaylistService.DEFAULT_PLAYLIST_URL
        )
    }

    private suspend fun syncPlaylistByTarget(
        target: YouTubeTarget,
        sourceUrl: String
    ): Result<String> {
        return try {
            val remote = youtubeService.fetchTarget(target)
            val existingById = dao.getEpisodesForPlaylistOnce(remote.playlistId)
                .associateBy { it.videoId }

            val mergedEntities = remote.episodes.map { ep ->
                val prev = existingById[ep.videoId]
                EpisodeEntity(
                    videoId = ep.videoId,
                    playlistId = remote.playlistId,
                    episodeNumber = ep.episodeNumber,
                    title = ep.title,
                    channelName = ep.channelName.ifBlank { prev?.channelName.orEmpty() },
                    durationText = ep.durationText.ifBlank { prev?.durationText.orEmpty() },
                    thumbnailUrl = ep.thumbnailUrl,
                    description = ep.description.ifBlank { prev?.description.orEmpty() },
                    summaryJson = prev?.summaryJson,
                    gamePackJson = prev?.gamePackJson,
                    quizBestStars = prev?.quizBestStars ?: 0,
                    tfBestStars = prev?.tfBestStars ?: 0,
                    matchBestStars = prev?.matchBestStars ?: 0,
                    customNotes = prev?.customNotes.orEmpty(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            dao.saveActivePlaylist(
                PlaylistMetaEntity(
                    id = 1,
                    playlistId = remote.playlistId,
                    playlistTitle = remote.playlistTitle,
                    sourceUrl = sourceUrl,
                    lastSyncedAt = System.currentTimeMillis()
                )
            )
            dao.upsertEpisodes(mergedEntities)
            Result.success(remote.playlistTitle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateSummaryAndGames(videoId: String): Result<Unit> {
        return try {
            val entity = dao.getEpisodeById(videoId)
                ?: return Result.failure(IllegalStateException("لم يتم العثور على الحلقة."))
            val meta = dao.getActivePlaylist()
            val playlistTitle = meta?.playlistTitle ?: "قائمة الحلقات"

            val transcriptContext = youtubeService.fetchVideoContext(videoId)
            val generated = aiService.generateEpisodeSummaryAndGames(
                episode = entity.toDomain(),
                playlistTitle = playlistTitle,
                transcriptContext = transcriptContext
            )

            dao.updateGeneratedContent(
                videoId = videoId,
                summaryJson = EpisodeJsonCodec.encodeSummary(generated.summary),
                gamePackJson = EpisodeJsonCodec.encodeGamePack(generated.gamePack),
                description = transcriptContext.description.ifBlank { entity.description }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCustomNotes(videoId: String, notes: String) {
        dao.updateCustomNotes(videoId, notes.trim())
    }

    suspend fun recordGameStars(videoId: String, gameType: GameType, stars: Int) {
        val clamped = stars.coerceIn(0, 3)
        when (gameType) {
            GameType.QUIZ -> dao.updateQuizStars(videoId, clamped)
            GameType.TRUE_FALSE -> dao.updateTfStars(videoId, clamped)
            GameType.MATCH -> dao.updateMatchStars(videoId, clamped)
        }
    }
}
