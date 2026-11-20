package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GameType
import com.example.firebase.configureAppCheck
import com.example.ui.screens.AwardsScreen
import com.example.ui.screens.EpisodeDetailScreen
import com.example.ui.screens.GamesHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchGameScreen
import com.example.ui.screens.QuizGameScreen
import com.example.ui.screens.TrueFalseGameScreen
import com.example.ui.theme.NajmatAlHalaqatTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.GenerationState
import com.example.ui.viewmodel.HomeTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NajmatAlHalaqatTheme {
                    NajmatAlHalaqatApp()
                }
            }
        }
    }
}

@Composable
fun NajmatAlHalaqatApp(
    viewModel: AppViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val allEpisodes by viewModel.episodes.collectAsStateWithLifecycle()
    val filteredEpisodes by viewModel.filteredEpisodes.collectAsStateWithLifecycle()
    val playlistTitle by viewModel.playlistTitle.collectAsStateWithLifecycle()
    val sourceUrl by viewModel.activeSourceUrl.collectAsStateWithLifecycle()
    val playlistLoadState by viewModel.playlistLoadState.collectAsStateWithLifecycle()
    val generationStates by viewModel.generationStates.collectAsStateWithLifecycle()
    val episodeFilter by viewModel.episodeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val awardsSummary by viewModel.awardsSummary.collectAsStateWithLifecycle()

    when (val screen = currentScreen) {
        is AppScreen.Home -> {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                if (useRail) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                HomeTab.entries.forEach { tab ->
                                    val selected = selectedTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = { TabIcon(tab = tab, selected = selected) },
                                        label = { Text(tabLabel(tab)) },
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                HomeTabContent(
                                    selectedTab = selectedTab,
                                    playlistTitle = playlistTitle,
                                    sourceUrl = sourceUrl,
                                    allEpisodes = allEpisodes,
                                    filteredEpisodes = filteredEpisodes,
                                    awardsSummary = awardsSummary,
                                    playlistLoadState = playlistLoadState,
                                    generationStates = generationStates,
                                    episodeFilter = episodeFilter,
                                    searchQuery = searchQuery,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                HomeTab.entries.forEach { tab ->
                                    val selected = selectedTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = { TabIcon(tab = tab, selected = selected) },
                                        label = { Text(tabLabel(tab)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        HomeTabContent(
                            selectedTab = selectedTab,
                            playlistTitle = playlistTitle,
                            sourceUrl = sourceUrl,
                            allEpisodes = allEpisodes,
                            filteredEpisodes = filteredEpisodes,
                            awardsSummary = awardsSummary,
                            playlistLoadState = playlistLoadState,
                            generationStates = generationStates,
                            episodeFilter = episodeFilter,
                            searchQuery = searchQuery,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }

        is AppScreen.EpisodeDetail -> {
            val episode = allEpisodes.firstOrNull { it.videoId == screen.videoId }
            if (episode == null) {
                viewModel.navigateBack()
            } else {
                EpisodeDetailScreen(
                    episode = episode,
                    initialTab = screen.initialTab,
                    generationState = generationStates[episode.videoId] ?: GenerationState.Idle,
                    onBack = { viewModel.navigateBack() },
                    onGenerate = { viewModel.generateForEpisode(episode.videoId) },
                    onSaveNotesAndRegenerate = { notes ->
                        viewModel.saveEpisodeNotesAndRegenerate(episode.videoId, notes)
                    },
                    onLaunchGame = { gameType ->
                        val dest = when (gameType) {
                            GameType.QUIZ -> AppScreen.QuizGame(episode.videoId)
                            GameType.TRUE_FALSE -> AppScreen.TrueFalseGame(episode.videoId)
                            GameType.MATCH -> AppScreen.MatchGame(episode.videoId)
                        }
                        viewModel.navigateTo(dest)
                    }
                )
            }
        }

        is AppScreen.QuizGame -> {
            val episode = allEpisodes.firstOrNull { it.videoId == screen.videoId }
            if (episode == null) {
                viewModel.navigateBack()
            } else {
                QuizGameScreen(
                    episode = episode,
                    onBack = { viewModel.navigateBack() },
                    onGameCompleted = { stars ->
                        viewModel.recordGameScore(episode.videoId, GameType.QUIZ, stars)
                    }
                )
            }
        }

        is AppScreen.TrueFalseGame -> {
            val episode = allEpisodes.firstOrNull { it.videoId == screen.videoId }
            if (episode == null) {
                viewModel.navigateBack()
            } else {
                TrueFalseGameScreen(
                    episode = episode,
                    onBack = { viewModel.navigateBack() },
                    onGameCompleted = { stars ->
                        viewModel.recordGameScore(episode.videoId, GameType.TRUE_FALSE, stars)
                    }
                )
            }
        }

        is AppScreen.MatchGame -> {
            val episode = allEpisodes.firstOrNull { it.videoId == screen.videoId }
            if (episode == null) {
                viewModel.navigateBack()
            } else {
                MatchGameScreen(
                    episode = episode,
                    onBack = { viewModel.navigateBack() },
                    onGameCompleted = { stars ->
                        viewModel.recordGameScore(episode.videoId, GameType.MATCH, stars)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    selectedTab: HomeTab,
    playlistTitle: String,
    sourceUrl: String,
    allEpisodes: List<com.example.data.model.EpisodeItem>,
    filteredEpisodes: List<com.example.data.model.EpisodeItem>,
    awardsSummary: com.example.ui.viewmodel.AwardsSummary,
    playlistLoadState: com.example.ui.viewmodel.PlaylistLoadState,
    generationStates: Map<String, GenerationState>,
    episodeFilter: com.example.ui.viewmodel.EpisodeFilter,
    searchQuery: String,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        HomeTab.EPISODES -> HomeScreen(
            playlistTitle = playlistTitle,
            sourceUrl = sourceUrl,
            episodes = filteredEpisodes,
            allEpisodesCount = allEpisodes.size,
            totalStars = awardsSummary.totalStars,
            readyCount = awardsSummary.readyEpisodesCount,
            loadState = playlistLoadState,
            generationStates = generationStates,
            selectedFilter = episodeFilter,
            searchQuery = searchQuery,
            onFilterSelected = viewModel::setEpisodeFilter,
            onSearchQueryChange = viewModel::setSearchQuery,
            onEpisodeClick = { ep -> viewModel.navigateTo(AppScreen.EpisodeDetail(ep.videoId)) },
            onRefreshClick = viewModel::refreshPlaylist,
            onLoadCustomUrl = { url -> viewModel.loadCustomPlaylistUrl(url) },
            onResetDefaultPlaylist = viewModel::resetToDefaultPlaylist,
            modifier = modifier
        )

        HomeTab.GAMES -> GamesHubScreen(
            episodes = allEpisodes,
            onStartGame = { ep, type ->
                val dest = when (type) {
                    GameType.QUIZ -> AppScreen.QuizGame(ep.videoId)
                    GameType.TRUE_FALSE -> AppScreen.TrueFalseGame(ep.videoId)
                    GameType.MATCH -> AppScreen.MatchGame(ep.videoId)
                }
                viewModel.navigateTo(dest)
            },
            onOpenEpisodeToGenerate = { ep ->
                viewModel.navigateTo(AppScreen.EpisodeDetail(ep.videoId))
            },
            modifier = modifier
        )

        HomeTab.AWARDS -> AwardsScreen(
            awards = awardsSummary,
            modifier = modifier
        )
    }
}

@Composable
private fun TabIcon(tab: HomeTab, selected: Boolean) {
    val icon = when (tab) {
        HomeTab.EPISODES -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
        HomeTab.GAMES -> if (selected) Icons.Rounded.SportsEsports else Icons.Outlined.SportsEsports
        HomeTab.AWARDS -> if (selected) Icons.Rounded.EmojiEvents else Icons.Outlined.EmojiEvents
    }
    Icon(imageVector = icon, contentDescription = null)
}

@Composable
private fun tabLabel(tab: HomeTab): String = when (tab) {
    HomeTab.EPISODES -> stringResource(R.string.nav_episodes)
    HomeTab.GAMES -> stringResource(R.string.nav_games)
    HomeTab.AWARDS -> stringResource(R.string.nav_awards)
}
