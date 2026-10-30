package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.FirebaseAiCreativeService
import com.example.data.local.AppDatabase
import com.example.data.repository.CreationRepository
import com.example.security.configureAppCheck
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CreationDetailDialog
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LifeGuidanceScreen
import com.example.ui.screens.LyricsAnalysisScreen
import com.example.ui.screens.PosterMakerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShayariScreen
import com.example.ui.screens.SongMakerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        // STEP 2: Enable Edge-to-Edge display
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = CreationRepository(database.creationDao(), applicationContext)
        val aiService = FirebaseAiCreativeService()

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.provideFactory(repository, aiService)
            )
            val userSettings by mainViewModel.userSettings.collectAsStateWithLifecycle()

            MyApplicationTheme(
                darkTheme = userSettings.isDarkTheme,
                fontScale = userSettings.fontScale
            ) {
                AiSongShayariApp(viewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

private data class PrimaryNavItem(
    val screen: AppScreen,
    val labelHindi: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSongShayariApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val selectedCreationDetail by viewModel.selectedCreationDetail.collectAsStateWithLifecycle()
    val sessionGenerationCount by viewModel.sessionGenerationCount.collectAsStateWithLifecycle()

    val historyCreations by viewModel.historyCreations.collectAsStateWithLifecycle()
    val favoriteCreations by viewModel.favoriteCreations.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    // Feature 1: AI Shayari Studio states
    val shayariCategory by viewModel.shayariCategory.collectAsStateWithLifecycle()
    val shayariMood by viewModel.shayariMood.collectAsStateWithLifecycle()
    val shayariLanguage by viewModel.shayariLanguage.collectAsStateWithLifecycle()
    val shayariLength by viewModel.shayariLength.collectAsStateWithLifecycle()
    val shayariCustomName by viewModel.shayariCustomName.collectAsStateWithLifecycle()
    val shayariTopic by viewModel.shayariTopic.collectAsStateWithLifecycle()
    val shayariUiState by viewModel.shayariUiState.collectAsStateWithLifecycle()
    val isVerifyingShayari by viewModel.isVerifyingShayari.collectAsStateWithLifecycle()

    // Feature 2: AI Song Maker states
    val songLanguage by viewModel.songLanguage.collectAsStateWithLifecycle()
    val songGenre by viewModel.songGenre.collectAsStateWithLifecycle()
    val songMood by viewModel.songMood.collectAsStateWithLifecycle()
    val songVocalStyle by viewModel.songVocalStyle.collectAsStateWithLifecycle()
    val songTopic by viewModel.songTopic.collectAsStateWithLifecycle()
    val songUiState by viewModel.songUiState.collectAsStateWithLifecycle()

    // Feature 3: Bhagavad Gita AI Guide states
    val guidanceSituation by viewModel.guidanceSituation.collectAsStateWithLifecycle()
    val guidanceUiState by viewModel.guidanceUiState.collectAsStateWithLifecycle()

    // Feature 4: AI Emotion Analyzer & Lyrics Analysis states
    val analyzerModeTab by viewModel.analyzerModeTab.collectAsStateWithLifecycle()
    val emotionInput by viewModel.emotionInput.collectAsStateWithLifecycle()
    val emotionUiState by viewModel.emotionUiState.collectAsStateWithLifecycle()
    val lyricsInput by viewModel.lyricsInput.collectAsStateWithLifecycle()
    val includeRevisedLyrics by viewModel.includeRevisedLyrics.collectAsStateWithLifecycle()
    val analysisUiState by viewModel.analysisUiState.collectAsStateWithLifecycle()

    // Feature 5: AI Status & Poster Maker states
    val posterText by viewModel.posterText.collectAsStateWithLifecycle()
    val posterAuthor by viewModel.posterAuthor.collectAsStateWithLifecycle()
    val posterBackground by viewModel.posterBackground.collectAsStateWithLifecycle()
    val posterTypography by viewModel.posterTypography.collectAsStateWithLifecycle()
    val posterAspectRatio by viewModel.posterAspectRatio.collectAsStateWithLifecycle()
    val posterFontSizeSp by viewModel.posterFontSizeSp.collectAsStateWithLifecycle()

    // Feature 7: My Creations filter & search
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle system back navigation for all non-Home screens
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateBack()
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Bottom Navigation Bar for Home, Shayari, Song Maker, Gita Guide, and My Creations
    val primaryNavItems = remember {
        listOf(
            PrimaryNavItem(AppScreen.HOME, "होम", Icons.Default.Home, "nav_item_home"),
            PrimaryNavItem(AppScreen.SHAYARI, "शायरी", Icons.Default.EditNote, "nav_item_shayari"),
            PrimaryNavItem(AppScreen.SONG, "सॉन्ग मेकर", Icons.Default.MusicNote, "nav_item_song"),
            PrimaryNavItem(AppScreen.LIFE_GUIDANCE, "गीता गाइड", Icons.Default.MenuBook, "nav_item_guidance"),
            PrimaryNavItem(AppScreen.HISTORY, "मेरी रचनाएँ", Icons.Default.CollectionsBookmark, "top_nav_history")
        )
    }

    // Detail Dialog when user taps a saved item from My Creations, Favorites, or Home
    selectedCreationDetail?.let { detailItem ->
        CreationDetailDialog(
            creation = detailItem,
            onDismiss = { viewModel.closeCreationDetail() },
            onCopy = { text ->
                copyTextToClipboard(
                    context = context,
                    label = detailItem.title,
                    text = text,
                    onCopied = { viewModel.showFeedback("कॉपी हो गया! 📋") }
                )
            },
            onShare = { title, text ->
                shareTextContent(context, title, text)
            },
            onToggleFavorite = { viewModel.toggleCreationFavorite(it) },
            onDelete = { viewModel.deleteCreation(it) },
            onOpenInPoster = { creation ->
                viewModel.openPosterMakerWithText(creation.primaryContent, creation.title)
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (currentScreen == AppScreen.HOME) {
                                "AI Song & Shayari Generator"
                            } else {
                                currentScreen.titleHindi
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("top_bar_title")
                        )
                    },
                    navigationIcon = {
                        if (currentScreen != AppScreen.HOME) {
                            IconButton(
                                onClick = { viewModel.navigateBack() },
                                modifier = Modifier.testTag("top_bar_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.LYRICS_ANALYSIS) },
                            modifier = Modifier.testTag("nav_item_analysis")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Emotion & Lyrics Analyzer",
                                tint = if (currentScreen == AppScreen.LYRICS_ANALYSIS) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.POSTER_MAKER) },
                            modifier = Modifier.testTag("top_nav_poster")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "AI Poster Maker",
                                tint = if (currentScreen == AppScreen.POSTER_MAKER) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.FAVORITES) },
                            modifier = Modifier.testTag("top_nav_favorites")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites",
                                tint = if (currentScreen == AppScreen.FAVORITES) {
                                    Color(0xFFF43F5E)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("top_nav_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (currentScreen == AppScreen.SETTINGS) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        primaryNavItems.forEach { item ->
                            val selected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.labelHindi
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.labelHindi,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("side_navigation_rail")
                    ) {
                        primaryNavItems.forEach { item ->
                            NavigationRailItem(
                                selected = currentScreen == item.screen,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.labelHindi
                                    )
                                },
                                label = { Text(item.labelHindi) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.HOME -> HomeScreen(
                                recentCreations = historyCreations,
                                favoritesCount = favoriteCreations.size,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenCreation = { viewModel.openCreationDetail(it) }
                            )

                            AppScreen.SHAYARI -> ShayariScreen(
                                categories = viewModel.shayariCategories,
                                selectedCategory = shayariCategory,
                                onCategorySelected = { viewModel.setShayariCategory(it) },
                                moods = viewModel.shayariMoods,
                                selectedMood = shayariMood,
                                onMoodSelected = { viewModel.setShayariMood(it) },
                                languages = viewModel.shayariLanguages,
                                selectedLanguage = shayariLanguage,
                                onLanguageSelected = { viewModel.setShayariLanguage(it) },
                                lengths = viewModel.shayariLengths,
                                selectedLength = shayariLength,
                                onLengthSelected = { viewModel.setShayariLength(it) },
                                customNameInput = shayariCustomName,
                                onCustomNameChanged = { viewModel.setShayariCustomName(it) },
                                topicInput = shayariTopic,
                                onTopicChanged = { viewModel.setShayariTopic(it) },
                                uiState = shayariUiState,
                                isVerifyingSecondAi = isVerifyingShayari,
                                onGenerate = { viewModel.generateShayari() },
                                onVerifySecondAi = { viewModel.verifyCurrentShayariWithSecondAi() },
                                onToggleFavorite = { viewModel.toggleShayariFavorite() },
                                onOpenInPoster = { quote -> viewModel.openPosterMakerWithText(quote) },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.SONG -> SongMakerScreen(
                                languages = viewModel.songLanguages,
                                selectedLanguage = songLanguage,
                                onLanguageSelected = { viewModel.setSongLanguage(it) },
                                genres = viewModel.songGenres,
                                selectedGenre = songGenre,
                                onGenreSelected = { viewModel.setSongGenre(it) },
                                moods = viewModel.songMoods,
                                selectedMood = songMood,
                                onMoodSelected = { viewModel.setSongMood(it) },
                                vocalStyles = viewModel.songVocalStyles,
                                selectedVocalStyle = songVocalStyle,
                                onVocalStyleSelected = { viewModel.setSongVocalStyle(it) },
                                topicInput = songTopic,
                                onTopicChanged = { viewModel.setSongTopic(it) },
                                uiState = songUiState,
                                onGenerate = { viewModel.generateSong() },
                                onToggleFavorite = { viewModel.toggleSongFavorite() },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.LYRICS_ANALYSIS -> LyricsAnalysisScreen(
                                analyzerModeTab = analyzerModeTab,
                                onAnalyzerModeTabChanged = { viewModel.setAnalyzerModeTab(it) },
                                emotionInput = emotionInput,
                                onEmotionInputChanged = { viewModel.setEmotionInput(it) },
                                emotionUiState = emotionUiState,
                                onAnalyzeEmotion = { viewModel.analyzeEmotion() },
                                onToggleEmotionFavorite = { viewModel.toggleEmotionFavorite() },
                                lyricsInput = lyricsInput,
                                onLyricsInputChanged = { viewModel.setLyricsInput(it) },
                                includeRevisedLyrics = includeRevisedLyrics,
                                onIncludeRevisedChanged = { viewModel.setIncludeRevisedLyrics(it) },
                                uiState = analysisUiState,
                                onAnalyze = { viewModel.analyzeLyrics() },
                                onToggleFavorite = { viewModel.toggleAnalysisFavorite() },
                                onOpenInPoster = { quote -> viewModel.openPosterMakerWithText(quote) },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.LIFE_GUIDANCE -> LifeGuidanceScreen(
                                situationInput = guidanceSituation,
                                onSituationChanged = { viewModel.setGuidanceSituation(it) },
                                uiState = guidanceUiState,
                                onGenerateGuidance = { viewModel.generateLifeGuidance() },
                                onToggleFavorite = { viewModel.toggleGuidanceFavorite() },
                                onOpenInPoster = { quote -> viewModel.openPosterMakerWithText(quote, "भगवद गीता प्रेरणा") },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.POSTER_MAKER -> PosterMakerScreen(
                                posterText = posterText,
                                onPosterTextChanged = { viewModel.setPosterText(it) },
                                posterAuthor = posterAuthor,
                                onPosterAuthorChanged = { viewModel.setPosterAuthor(it) },
                                selectedBackgroundName = posterBackground,
                                onBackgroundSelected = { viewModel.setPosterBackground(it) },
                                selectedTypography = posterTypography,
                                onTypographySelected = { viewModel.setPosterTypography(it) },
                                selectedAspectRatioLabel = posterAspectRatio,
                                onAspectRatioSelected = { viewModel.setPosterAspectRatio(it) },
                                posterFontSizeSp = posterFontSizeSp,
                                onFontSizeChanged = { viewModel.setPosterFontSizeSp(it) },
                                onSavePosterToCreations = { viewModel.savePosterToCreations() },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.HISTORY -> HistoryScreen(
                                creations = historyCreations,
                                selectedFilter = historyFilter,
                                onFilterSelected = { viewModel.setHistoryFilter(it) },
                                searchQuery = historySearchQuery,
                                onSearchQueryChanged = { viewModel.setHistorySearchQuery(it) },
                                onOpenCreation = { viewModel.openCreationDetail(it) },
                                onToggleFavorite = { viewModel.toggleCreationFavorite(it) },
                                onDeleteCreation = { viewModel.deleteCreation(it) },
                                onClearAllHistory = { viewModel.clearAllHistory() },
                                onNavigateHome = { viewModel.navigateTo(AppScreen.HOME) },
                                onOpenInPoster = { quote -> viewModel.openPosterMakerWithText(quote) },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.FAVORITES -> FavoritesScreen(
                                favorites = favoriteCreations,
                                onOpenCreation = { viewModel.openCreationDetail(it) },
                                onToggleFavorite = { viewModel.toggleCreationFavorite(it) },
                                onDeleteCreation = { viewModel.deleteCreation(it) },
                                onNavigateHome = { viewModel.navigateTo(AppScreen.HOME) },
                                onOpenInPoster = { quote -> viewModel.openPosterMakerWithText(quote) },
                                onFeedback = { viewModel.showFeedback(it) }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                userSettings = userSettings,
                                historyCount = historyCreations.size,
                                favoritesCount = favoriteCreations.size,
                                sessionGenerationCount = sessionGenerationCount,
                                onUpdateDefaultLanguage = { viewModel.updateDefaultLanguage(it) },
                                onUpdateFontScale = { viewModel.updateFontScale(it) },
                                onUpdateAutoSave = { viewModel.updateAutoSaveHistory(it) },
                                onUpdateDarkTheme = { viewModel.updateDarkTheme(it) },
                                onClearAllHistory = { viewModel.clearAllHistory() }
                            )
                        }
                    }
                }
            }
        }
    }
}
