package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgeIconType
import com.example.data.model.EpisodeItem
import com.example.data.model.GameType
import com.example.data.remote.EpisodeAiService
import com.example.data.remote.YouTubePlaylistService
import com.example.data.repository.EpisodeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab {
    EPISODES,
    GAMES,
    AWARDS
}

enum class EpisodeFilter {
    ALL,
    READY,
    NEW
}

sealed interface AppScreen {
    data object Home : AppScreen
    data class EpisodeDetail(val videoId: String, val initialTab: Int = 0) : AppScreen
    data class QuizGame(val videoId: String) : AppScreen
    data class TrueFalseGame(val videoId: String) : AppScreen
    data class MatchGame(val videoId: String) : AppScreen
}

sealed interface PlaylistLoadState {
    data object Loading : PlaylistLoadState
    data object Ready : PlaylistLoadState
    data class Error(val message: String) : PlaylistLoadState
}

sealed interface GenerationState {
    data object Idle : GenerationState
    data object Loading : GenerationState
    data class Error(val message: String) : GenerationState
}

data class AwardsSummary(
    val totalStars: Int,
    val readyEpisodesCount: Int,
    val completedGamesCount: Int,
    val rankTitle: String,
    val nextRankTitle: String,
    val progressToNextRank: Float,
    val badges: List<AchievementBadge>
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EpisodeRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = EpisodeRepository(
            dao = db.episodeDao(),
            youtubeService = YouTubePlaylistService(),
            aiService = EpisodeAiService()
        )
    }

    val episodes: StateFlow<List<EpisodeItem>> = repository.activeEpisodes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlistTitle: StateFlow<String> = repository.activePlaylistMeta
        .combine(episodes) { meta, _ ->
            meta?.playlistTitle?.takeIf { it.isNotBlank() } ?: "قائمة الحلقات"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "قائمة الحلقات"
        )

    val activeSourceUrl: StateFlow<String> = repository.activePlaylistMeta
        .combine(episodes) { meta, _ ->
            meta?.sourceUrl ?: YouTubePlaylistService.DEFAULT_PLAYLIST_URL
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = YouTubePlaylistService.DEFAULT_PLAYLIST_URL
        )

    private val _playlistLoadState = MutableStateFlow<PlaylistLoadState>(PlaylistLoadState.Loading)
    val playlistLoadState: StateFlow<PlaylistLoadState> = _playlistLoadState.asStateFlow()

    private val _generationStates = MutableStateFlow<Map<String, GenerationState>>(emptyMap())
    val generationStates: StateFlow<Map<String, GenerationState>> = _generationStates.asStateFlow()

    private val _selectedTab = MutableStateFlow(HomeTab.EPISODES)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    private val _episodeFilter = MutableStateFlow(EpisodeFilter.ALL)
    val episodeFilter: StateFlow<EpisodeFilter> = _episodeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _backStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Home))
    val currentScreen: StateFlow<AppScreen> = _backStack
        .combine(_selectedTab) { stack, _ -> stack.lastOrNull() ?: AppScreen.Home }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppScreen.Home
        )

    val filteredEpisodes: StateFlow<List<EpisodeItem>> = combine(
        episodes,
        _episodeFilter,
        _searchQuery
    ) { list, filter, query ->
        val trimmed = query.trim()
        list.filter { ep ->
            val matchesFilter = when (filter) {
                EpisodeFilter.ALL -> true
                EpisodeFilter.READY -> ep.isReady
                EpisodeFilter.NEW -> !ep.isReady
            }
            val matchesQuery = trimmed.isEmpty() ||
                ep.title.contains(trimmed, ignoreCase = true) ||
                ep.summary?.friendlyTitle?.contains(trimmed, ignoreCase = true) == true
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val awardsSummary: StateFlow<AwardsSummary> = episodes
        .combine(_selectedTab) { list, _ -> computeAwards(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = computeAwards(emptyList())
        )

    init {
        loadInitialPlaylist()
    }

    private fun loadInitialPlaylist() {
        viewModelScope.launch {
            _playlistLoadState.value = PlaylistLoadState.Loading
            val result = repository.ensureDefaultPlaylistLoaded()
            _playlistLoadState.value = result.fold(
                onSuccess = { PlaylistLoadState.Ready },
                onFailure = { PlaylistLoadState.Error(it.message ?: "تعذر تحميل قائمة التشغيل.") }
            )
        }
    }

    fun refreshPlaylist() {
        viewModelScope.launch {
            _playlistLoadState.value = PlaylistLoadState.Loading
            val result = repository.refreshActivePlaylist()
            _playlistLoadState.value = result.fold(
                onSuccess = { PlaylistLoadState.Ready },
                onFailure = { PlaylistLoadState.Error(it.message ?: "تعذر تحديث الحلقات.") }
            )
        }
    }

    fun loadCustomPlaylistUrl(url: String, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _playlistLoadState.value = PlaylistLoadState.Loading
            val result = repository.loadCustomYouTubeUrl(url)
            result.fold(
                onSuccess = {
                    _playlistLoadState.value = PlaylistLoadState.Ready
                    onFinished(true, null)
                },
                onFailure = { err ->
                    val msg = err.message ?: "تعذر تحميل الرابط."
                    _playlistLoadState.value = PlaylistLoadState.Error(msg)
                    onFinished(false, msg)
                }
            )
        }
    }

    fun resetToDefaultPlaylist() {
        viewModelScope.launch {
            _playlistLoadState.value = PlaylistLoadState.Loading
            val result = repository.resetToDefaultPlaylist()
            _playlistLoadState.value = result.fold(
                onSuccess = { PlaylistLoadState.Ready },
                onFailure = { PlaylistLoadState.Error(it.message ?: "تعذر تحميل القائمة الأساسية.") }
            )
        }
    }

    fun generateForEpisode(videoId: String) {
        if (_generationStates.value[videoId] is GenerationState.Loading) return
        viewModelScope.launch {
            _generationStates.update { it + (videoId to GenerationState.Loading) }
            try {
                val result = repository.generateSummaryAndGames(videoId)
                result.fold(
                    onSuccess = {
                        _generationStates.update { it + (videoId to GenerationState.Idle) }
                    },
                    onFailure = { err ->
                        _generationStates.update {
                            it + (videoId to GenerationState.Error(
                                err.message ?: "حدث خطأ أثناء تحضير الملخص والألعاب."
                            ))
                        }
                    }
                )
            } catch (e: CancellationException) {
                _generationStates.update { it + (videoId to GenerationState.Idle) }
                throw e
            }
        }
    }

    fun saveEpisodeNotesAndRegenerate(videoId: String, notes: String) {
        viewModelScope.launch {
            repository.saveCustomNotes(videoId, notes)
            generateForEpisode(videoId)
        }
    }

    fun recordGameScore(videoId: String, gameType: GameType, stars: Int) {
        viewModelScope.launch {
            repository.recordGameStars(videoId, gameType, stars)
        }
    }

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun setEpisodeFilter(filter: EpisodeFilter) {
        _episodeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun navigateTo(screen: AppScreen) {
        _backStack.update { stack ->
            if (stack.lastOrNull() == screen) stack else stack + screen
        }
    }

    fun navigateBack() {
        _backStack.update { stack ->
            if (stack.size > 1) stack.dropLast(1) else stack
        }
    }

    private fun computeAwards(list: List<EpisodeItem>): AwardsSummary {
        val totalStars = list.sumOf { it.totalStars }
        val readyCount = list.count { it.isReady }
        val quizDone = list.count { it.quizBestStars > 0 }
        val tfDone = list.count { it.tfBestStars > 0 }
        val matchDone = list.count { it.matchBestStars > 0 }
        val perfectGames = list.count { it.quizBestStars == 3 } +
            list.count { it.tfBestStars == 3 } +
            list.count { it.matchBestStars == 3 }
        val completedGamesCount = quizDone + tfDone + matchDone

        val (rankTitle, nextRank, progress) = when {
            totalStars < 6 -> Triple("نجمة مبتدئة", "نجمة متألقة (6 نجوم)", totalStars / 6f)
            totalStars < 15 -> Triple("نجمة متألقة", "أميرة المعرفة (15 نجمة)", (totalStars - 6) / 9f)
            totalStars < 30 -> Triple("أميرة المعرفة", "ملكة الحلقات (30 نجمة)", (totalStars - 15) / 15f)
            else -> Triple("ملكة الحلقات", "أعلى مستوى! أنتِ رائعة", 1f)
        }

        val badges = listOf(
            AchievementBadge(
                id = "first_summary",
                title = "قارئة ذكية",
                description = "جهّزي أول ملخص لحلقة من القائمة",
                isUnlocked = readyCount >= 1,
                progressLabel = "${readyCount.coerceAtMost(1)} / 1",
                iconType = BadgeIconType.FIRST_SUMMARY
            ),
            AchievementBadge(
                id = "quiz_champ",
                title = "بطلة الاختيارات",
                description = "أنهي لعبة مغامرة الاختيارات بنجاح",
                isUnlocked = quizDone >= 1,
                progressLabel = "${quizDone.coerceAtMost(1)} / 1",
                iconType = BadgeIconType.QUIZ_CHAMP
            ),
            AchievementBadge(
                id = "speed_star",
                title = "سريعة البديهة",
                description = "أكملي تحدي صح أم خطأ السريع",
                isUnlocked = tfDone >= 1,
                progressLabel = "${tfDone.coerceAtMost(1)} / 1",
                iconType = BadgeIconType.SPEED_STAR
            ),
            AchievementBadge(
                id = "match_queen",
                title = "ملكة التوصيل",
                description = "طابقي جميع البطاقات في التوصيل السحري",
                isUnlocked = matchDone >= 1,
                progressLabel = "${matchDone.coerceAtMost(1)} / 1",
                iconType = BadgeIconType.MATCH_QUEEN
            ),
            AchievementBadge(
                id = "ten_stars",
                title = "صيادة النجوم",
                description = "اجمعي 10 نجوم ذهبية من الألعاب",
                isUnlocked = totalStars >= 10,
                progressLabel = "${totalStars.coerceAtMost(10)} / 10",
                iconType = BadgeIconType.TEN_STARS
            ),
            AchievementBadge(
                id = "explorer",
                title = "مستكشفة متفوقة",
                description = "احصلي على 3 نجوم كاملة في 3 ألعاب",
                isUnlocked = perfectGames >= 3,
                progressLabel = "${perfectGames.coerceAtMost(3)} / 3",
                iconType = BadgeIconType.EXPLORER
            )
        )

        return AwardsSummary(
            totalStars = totalStars,
            readyEpisodesCount = readyCount,
            completedGamesCount = completedGamesCount,
            rankTitle = rankTitle,
            nextRankTitle = nextRank,
            progressToNextRank = progress.coerceIn(0f, 1f),
            badges = badges
        )
    }
}
