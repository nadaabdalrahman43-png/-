package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM playlist_meta WHERE id = 1")
    fun observeActivePlaylist(): Flow<PlaylistMetaEntity?>

    @Query("SELECT * FROM playlist_meta WHERE id = 1")
    suspend fun getActivePlaylist(): PlaylistMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActivePlaylist(meta: PlaylistMetaEntity)

    @Query("SELECT * FROM episodes WHERE playlistId = :playlistId ORDER BY episodeNumber ASC")
    fun observeEpisodesForPlaylist(playlistId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE videoId = :videoId LIMIT 1")
    fun observeEpisodeById(videoId: String): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE videoId = :videoId LIMIT 1")
    suspend fun getEpisodeById(videoId: String): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE playlistId = :playlistId")
    suspend fun getEpisodesForPlaylistOnce(playlistId: String): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisode(episode: EpisodeEntity)

    @Query(
        """
        UPDATE episodes
        SET summaryJson = :summaryJson,
            gamePackJson = :gamePackJson,
            description = :description,
            updatedAt = :updatedAt
        WHERE videoId = :videoId
        """
    )
    suspend fun updateGeneratedContent(
        videoId: String,
        summaryJson: String,
        gamePackJson: String,
        description: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE episodes SET customNotes = :notes WHERE videoId = :videoId")
    suspend fun updateCustomNotes(videoId: String, notes: String)

    @Query("UPDATE episodes SET quizBestStars = MAX(quizBestStars, :stars) WHERE videoId = :videoId")
    suspend fun updateQuizStars(videoId: String, stars: Int)

    @Query("UPDATE episodes SET tfBestStars = MAX(tfBestStars, :stars) WHERE videoId = :videoId")
    suspend fun updateTfStars(videoId: String, stars: Int)

    @Query("UPDATE episodes SET matchBestStars = MAX(matchBestStars, :stars) WHERE videoId = :videoId")
    suspend fun updateMatchStars(videoId: String, stars: Int)
}
