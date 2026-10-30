package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.CreativeAiGenerator
import com.example.data.ai.EmotionAnalysisResult
import com.example.data.ai.LifeGuidanceResult
import com.example.data.ai.LyricsAnalysisResult
import com.example.data.ai.ShayariResult
import com.example.data.ai.SongResult
import com.example.data.local.CreationEntity
import com.example.data.local.FeatureType
import com.example.data.repository.CreationRepository
import com.example.data.repository.UserSettings
import com.example.ui.screens.PosterAspectRatios
import com.example.ui.screens.PosterBackgroundOptions
import com.example.ui.screens.PosterTypographyOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(
    val route: String,
    val titleHindi: String,
    val titleEnglish: String
) {
    HOME("home", "होम", "Home"),
    SHAYARI("shayari_generator", "AI शायरी स्टूडियो", "Shayari Studio"),
    SONG("song_maker", "AI सॉन्ग मेकर", "Song Maker"),
    LYRICS_ANALYSIS("lyrics_analysis", "AI भावना व लिरिक्स विश्लेषण", "Emotion & Lyrics Analyzer"),
    LIFE_GUIDANCE("life_guidance", "भगवद गीता AI गाइड", "Gita Guide"),
    POSTER_MAKER("poster_maker", "AI स्टेटस और पोस्टर मेकर", "Status & Poster Maker"),
    HISTORY("history", "मेरी रचनाएँ (My Creations)", "My Creations"),
    FAVORITES("favorites", "पसंदीदा (Favorites)", "Favorites"),
    SETTINGS("settings", "सेटिंग्स (Settings)", "Settings")
}

sealed interface FeatureUiState<out T> {
    data object Idle : FeatureUiState<Nothing>
    data object Loading : FeatureUiState<Nothing>
    data class Success<T>(
        val data: T,
        val creationId: Long,
        val isFavorite: Boolean = false
    ) : FeatureUiState<T>
    data class Error(val message: String) : FeatureUiState<Nothing>
}

class MainViewModel(
    private val repository: CreationRepository,
    private val aiGenerator: CreativeAiGenerator
) : ViewModel() {

    companion object {
        const val DAILY_GENERATION_LIMIT = 50

        fun provideFactory(
            repository: CreationRepository,
            aiGenerator: CreativeAiGenerator
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, aiGenerator) as T
            }
        }
    }

    // Navigation & Backstack
    private val backStack = ArrayDeque<AppScreen>().apply { addLast(AppScreen.HOME) }
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Session / Daily Generation Counter for Usage Limits display
    private val _sessionGenerationCount = MutableStateFlow(0)
    val sessionGenerationCount: StateFlow<Int> = _sessionGenerationCount.asStateFlow()

    // Global Feedback Message (Snackbar)
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Selected saved creation for full detail modal
    private val _selectedCreationDetail = MutableStateFlow<CreationEntity?>(null)
    val selectedCreationDetail: StateFlow<CreationEntity?> = _selectedCreationDetail.asStateFlow()

    // Settings
    val userSettings: StateFlow<UserSettings> = repository.userSettings

    // Room Persistent Streams
    val historyCreations: StateFlow<List<CreationEntity>> = repository.allCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCreations: StateFlow<List<CreationEntity>> = repository.favoriteCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My Creations Filter & Search
    private val _historyFilter = MutableStateFlow("ALL")
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // ==================== Feature 1: AI Shayari Studio State ====================
    val shayariCategories = listOf(
        "Love / Romantic (रोमांटिक)",
        "Sad (दर्द)",
        "Attitude (एटीट्यूड)",
        "Friendship (दोस्ती)",
        "Motivational (प्रेरणा)",
        "Devotional (भक्ति)",
        "Breakup (ब्रेकअप)",
        "Life (ज़िंदगी)"
    )
    val shayariMoods = listOf(
        "Romantic (रोमांटिक)",
        "Deep & Emotional (गहरा जज़्बात)",
        "Royal & Bold (शाही अंदाज़)",
        "Peaceful & Spiritual (भक्ति व सुकून)",
        "Nostalgic (यादें)",
        "Inspiring (जोश और हौसला)"
    )
    val shayariLanguages = listOf(
        "Hindi (हिंदी)",
        "Hinglish (हिंग्लिश)",
        "Both (हिंदी + Hinglish)"
    )
    val shayariLengths = listOf(
        "2-Line Sher (दो पंक्तियाँ)",
        "4-Line Shayari (चार पंक्तियाँ)",
        "8-Line Ghazal (आठ पंक्तियाँ)"
    )

    private val _shayariCategory = MutableStateFlow(shayariCategories.first())
    val shayariCategory: StateFlow<String> = _shayariCategory.asStateFlow()

    private val _shayariMood = MutableStateFlow(shayariMoods.first())
    val shayariMood: StateFlow<String> = _shayariMood.asStateFlow()

    private val _shayariLanguage = MutableStateFlow(shayariLanguages.last())
    val shayariLanguage: StateFlow<String> = _shayariLanguage.asStateFlow()

    private val _shayariLength = MutableStateFlow(shayariLengths[1])
    val shayariLength: StateFlow<String> = _shayariLength.asStateFlow()

    private val _shayariCustomName = MutableStateFlow("")
    val shayariCustomName: StateFlow<String> = _shayariCustomName.asStateFlow()

    private val _shayariTopic = MutableStateFlow("")
    val shayariTopic: StateFlow<String> = _shayariTopic.asStateFlow()

    private val _shayariUiState = MutableStateFlow<FeatureUiState<ShayariResult>>(FeatureUiState.Idle)
    val shayariUiState: StateFlow<FeatureUiState<ShayariResult>> = _shayariUiState.asStateFlow()

    private val _isVerifyingShayari = MutableStateFlow(false)
    val isVerifyingShayari: StateFlow<Boolean> = _isVerifyingShayari.asStateFlow()

    // ==================== Feature 2: AI Song Maker State ====================
    val songLanguages = listOf(
        "Hindi (हिंदी)",
        "Hinglish (हिंग्लिश)",
        "English (अंग्रेज़ी)"
    )
    val songGenres = listOf(
        "Bollywood Romantic",
        "Sufi Soul (सूफ़ी)",
        "Indie Pop (इंडी पॉप)",
        "Acoustic Ballad",
        "Desi Hip-Hop / Rap",
        "Motivational Anthem",
        "Devotional Bhajan",
        "Lo-Fi Chill"
    )
    val songMoods = listOf(
        "Romantic (रोमांटिक)",
        "Heartbreak (दिल टूटना)",
        "Happy & Upbeat (खुशनुमा)",
        "Soulful (रूहानी)",
        "Rainy Evening (बारिश की शाम)",
        "Energetic (ऊर्जावान)"
    )
    val songVocalStyles = listOf(
        "Male Soulful (मेल सोलफुल)",
        "Female Melodic (फीमेल मेलोडिक)",
        "Duet Romantic (युगल गायन)",
        "Sufi Qawwali (सूफ़ी अंदाज़)",
        "Rap / Rhythmic Flow",
        "Classical Alaap (शास्त्रीय)"
    )

    private val _songLanguage = MutableStateFlow(songLanguages.first())
    val songLanguage: StateFlow<String> = _songLanguage.asStateFlow()

    private val _songGenre = MutableStateFlow(songGenres.first())
    val songGenre: StateFlow<String> = _songGenre.asStateFlow()

    private val _songMood = MutableStateFlow(songMoods.first())
    val songMood: StateFlow<String> = _songMood.asStateFlow()

    private val _songVocalStyle = MutableStateFlow(songVocalStyles.first())
    val songVocalStyle: StateFlow<String> = _songVocalStyle.asStateFlow()

    private val _songTopic = MutableStateFlow("")
    val songTopic: StateFlow<String> = _songTopic.asStateFlow()

    private val _songUiState = MutableStateFlow<FeatureUiState<SongResult>>(FeatureUiState.Idle)
    val songUiState: StateFlow<FeatureUiState<SongResult>> = _songUiState.asStateFlow()

    // ==================== Feature 3: Bhagavad Gita AI Guide State ====================
    private val _guidanceSituation = MutableStateFlow("")
    val guidanceSituation: StateFlow<String> = _guidanceSituation.asStateFlow()

    private val _guidanceUiState = MutableStateFlow<FeatureUiState<LifeGuidanceResult>>(FeatureUiState.Idle)
    val guidanceUiState: StateFlow<FeatureUiState<LifeGuidanceResult>> = _guidanceUiState.asStateFlow()

    // ==================== Feature 4: AI Emotion Analyzer & Lyrics Analysis State ====================
    private val _analyzerModeTab = MutableStateFlow(0) // 0 = Emotion Analyzer, 1 = Lyrics Analysis
    val analyzerModeTab: StateFlow<Int> = _analyzerModeTab.asStateFlow()

    private val _emotionInput = MutableStateFlow("")
    val emotionInput: StateFlow<String> = _emotionInput.asStateFlow()

    private val _emotionUiState = MutableStateFlow<FeatureUiState<EmotionAnalysisResult>>(FeatureUiState.Idle)
    val emotionUiState: StateFlow<FeatureUiState<EmotionAnalysisResult>> = _emotionUiState.asStateFlow()

    private val _lyricsInput = MutableStateFlow("")
    val lyricsInput: StateFlow<String> = _lyricsInput.asStateFlow()

    private val _includeRevisedLyrics = MutableStateFlow(true)
    val includeRevisedLyrics: StateFlow<Boolean> = _includeRevisedLyrics.asStateFlow()

    private val _analysisUiState = MutableStateFlow<FeatureUiState<LyricsAnalysisResult>>(FeatureUiState.Idle)
    val analysisUiState: StateFlow<FeatureUiState<LyricsAnalysisResult>> = _analysisUiState.asStateFlow()

    // ==================== Feature 5: AI Status & Poster Maker State ====================
    private val _posterText = MutableStateFlow(
        "खामोश निगाहों में हज़ारों सवाल हैं,\nतेरे बिना ये लम्हे भी बेहाल हैं।"
    )
    val posterText: StateFlow<String> = _posterText.asStateFlow()

    private val _posterAuthor = MutableStateFlow("AI Song & Shayari Studio")
    val posterAuthor: StateFlow<String> = _posterAuthor.asStateFlow()

    private val _posterBackground = MutableStateFlow(PosterBackgroundOptions.first().name)
    val posterBackground: StateFlow<String> = _posterBackground.asStateFlow()

    private val _posterTypography = MutableStateFlow(PosterTypographyOptions.first())
    val posterTypography: StateFlow<String> = _posterTypography.asStateFlow()

    private val _posterAspectRatio = MutableStateFlow(PosterAspectRatios.first().label)
    val posterAspectRatio: StateFlow<String> = _posterAspectRatio.asStateFlow()

    private val _posterFontSizeSp = MutableStateFlow(18f)
    val posterFontSizeSp: StateFlow<Float> = _posterFontSizeSp.asStateFlow()

    // ==================== Navigation Actions ====================
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            backStack.addLast(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            _currentScreen.value = backStack.last()
            return true
        }
        if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun openPosterMakerWithText(quoteText: String, author: String = "AI Song & Shayari Studio") {
        _posterText.value = quoteText.trim()
        _posterAuthor.value = author
        _selectedCreationDetail.value = null
        navigateTo(AppScreen.POSTER_MAKER)
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun openCreationDetail(creation: CreationEntity) {
        _selectedCreationDetail.value = creation
    }

    fun closeCreationDetail() {
        _selectedCreationDetail.value = null
    }

    // ==================== Form Setters ====================
    fun setShayariCategory(value: String) { _shayariCategory.value = value }
    fun setShayariMood(value: String) { _shayariMood.value = value }
    fun setShayariLanguage(value: String) { _shayariLanguage.value = value }
    fun setShayariLength(value: String) { _shayariLength.value = value }
    fun setShayariCustomName(value: String) { _shayariCustomName.value = value }
    fun setShayariTopic(value: String) { _shayariTopic.value = value }

    fun setSongLanguage(value: String) { _songLanguage.value = value }
    fun setSongGenre(value: String) { _songGenre.value = value }
    fun setSongMood(value: String) { _songMood.value = value }
    fun setSongVocalStyle(value: String) { _songVocalStyle.value = value }
    fun setSongTopic(value: String) { _songTopic.value = value }

    fun setAnalyzerModeTab(tabIndex: Int) { _analyzerModeTab.value = tabIndex }
    fun setEmotionInput(value: String) { _emotionInput.value = value }
    fun setLyricsInput(value: String) { _lyricsInput.value = value }
    fun setIncludeRevisedLyrics(value: Boolean) { _includeRevisedLyrics.value = value }

    fun setGuidanceSituation(value: String) { _guidanceSituation.value = value }

    fun setPosterText(value: String) { _posterText.value = value }
    fun setPosterAuthor(value: String) { _posterAuthor.value = value }
    fun setPosterBackground(value: String) { _posterBackground.value = value }
    fun setPosterTypography(value: String) { _posterTypography.value = value }
    fun setPosterAspectRatio(value: String) { _posterAspectRatio.value = value }
    fun setPosterFontSizeSp(value: Float) { _posterFontSizeSp.value = value }

    fun setHistoryFilter(filter: String) { _historyFilter.value = filter }
    fun setHistorySearchQuery(query: String) { _historySearchQuery.value = query }

    // ==================== AI Generation Actions ====================

    fun generateShayari() {
        if (_sessionGenerationCount.value >= DAILY_GENERATION_LIMIT) {
            _shayariUiState.value = FeatureUiState.Error(
                "दैनिक उपयोग सीमा ($DAILY_GENERATION_LIMIT जनरेशन) पूरी हो गई है। कृपया कुछ समय बाद प्रयास करें।"
            )
            return
        }

        val category = _shayariCategory.value
        val mood = _shayariMood.value
        val language = _shayariLanguage.value
        val topic = _shayariTopic.value.trim()
        val customName = _shayariCustomName.value.trim()
        val length = _shayariLength.value

        viewModelScope.launch {
            _shayariUiState.value = FeatureUiState.Loading
            try {
                val result = aiGenerator.generateShayari(
                    category = category,
                    mood = mood,
                    language = language,
                    topic = topic,
                    customName = customName,
                    length = length
                )
                _sessionGenerationCount.value += 1
                val creationId = saveToHistoryIfNeeded(
                    featureType = FeatureType.SHAYARI,
                    title = result.title,
                    categoryOrGenre = category,
                    moodOrLanguage = "$mood • $length",
                    userPrompt = topic.ifBlank { "$category ($mood)" },
                    primaryContent = result.toFormattedText(),
                    secondaryContent = result.meaningHindi,
                    extraDetailsJson = result.aiVerificationReport,
                    youtubeSearchQuery = result.youtubeSearchQuery,
                    webSearchQuery = result.webSearchQuery
                )
                _shayariUiState.value = FeatureUiState.Success(
                    data = result,
                    creationId = creationId,
                    isFavorite = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _shayariUiState.value = FeatureUiState.Error(
                    e.message ?: "शायरी तैयार करने में त्रुटि हुई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun verifyCurrentShayariWithSecondAi() {
        val current = _shayariUiState.value as? FeatureUiState.Success<ShayariResult> ?: return
        if (_isVerifyingShayari.value) return
        viewModelScope.launch {
            _isVerifyingShayari.value = true
            try {
                val polished = aiGenerator.verifyAndPolishShayari(
                    currentShayari = current.data,
                    category = _shayariCategory.value,
                    mood = _shayariMood.value,
                    topic = _shayariTopic.value
                )
                if (current.creationId > 0L) {
                    repository.updateCreationContent(
                        id = current.creationId,
                        primaryContent = polished.toFormattedText(),
                        extraDetailsJson = polished.aiVerificationReport
                    )
                }
                _shayariUiState.value = current.copy(data = polished)
                showFeedback("दूसरे AI (उस्ताद शायर समीक्षक) ने शायरी को परख कर और बेहतर बना दिया! ✨")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showFeedback(e.message ?: "दूसरे AI से सत्यापन में समस्या आई।")
            } finally {
                _isVerifyingShayari.value = false
            }
        }
    }

    fun generateSong() {
        val topic = _songTopic.value.trim()
        if (topic.isBlank()) {
            _songUiState.value = FeatureUiState.Error(
                "कृपया गाने का विषय (Song Topic) लिखें या वॉइस इनपुट से बोलें, जैसे 'पहली बारिश और चाय'।"
            )
            return
        }

        val language = _songLanguage.value
        val genre = _songGenre.value
        val mood = _songMood.value
        val vocalStyle = _songVocalStyle.value

        viewModelScope.launch {
            _songUiState.value = FeatureUiState.Loading
            try {
                val result = aiGenerator.generateSong(
                    language = language,
                    genre = genre,
                    mood = mood,
                    topic = topic,
                    vocalStyle = vocalStyle
                )
                _sessionGenerationCount.value += 1
                val creationId = saveToHistoryIfNeeded(
                    featureType = FeatureType.SONG,
                    title = result.title,
                    categoryOrGenre = genre,
                    moodOrLanguage = "$language • $vocalStyle",
                    userPrompt = topic,
                    primaryContent = result.toFormattedText(),
                    secondaryContent = result.musicGenPrompt.ifBlank { result.musicalNote },
                    youtubeSearchQuery = result.youtubeSearchQuery,
                    webSearchQuery = result.webSearchQuery
                )
                _songUiState.value = FeatureUiState.Success(
                    data = result,
                    creationId = creationId,
                    isFavorite = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _songUiState.value = FeatureUiState.Error(
                    e.message ?: "गाना तैयार करने में समस्या आई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun analyzeEmotion() {
        val input = _emotionInput.value.trim()
        if (input.length < 4) {
            _emotionUiState.value = FeatureUiState.Error(
                "कृपया भावना विश्लेषण के लिए अपने मन की बात या कुछ पंक्तियाँ लिखें।"
            )
            return
        }

        viewModelScope.launch {
            _emotionUiState.value = FeatureUiState.Loading
            try {
                val result = aiGenerator.analyzeEmotion(userText = input)
                _sessionGenerationCount.value += 1
                val creationId = saveToHistoryIfNeeded(
                    featureType = FeatureType.EMOTION_ANALYZER,
                    title = "भावना विश्लेषण: ${result.detectedEmotion}",
                    categoryOrGenre = result.detectedEmotion,
                    moodOrLanguage = "तीव्रता ${result.intensityPercent}%",
                    userPrompt = input.take(120),
                    primaryContent = result.toFormattedText(),
                    secondaryContent = result.matchingShayari
                )
                _emotionUiState.value = FeatureUiState.Success(
                    data = result,
                    creationId = creationId,
                    isFavorite = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _emotionUiState.value = FeatureUiState.Error(
                    e.message ?: "भावना विश्लेषण में समस्या आई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun analyzeLyrics() {
        val input = _lyricsInput.value.trim()
        if (input.length < 10) {
            _analysisUiState.value = FeatureUiState.Error(
                "कृपया विश्लेषण के लिए कम से कम 2 पंक्तियों के लिरिक्स या शायरी पेस्ट करें।"
            )
            return
        }
        val includeRevision = _includeRevisedLyrics.value

        viewModelScope.launch {
            _analysisUiState.value = FeatureUiState.Loading
            try {
                val result = aiGenerator.analyzeLyrics(
                    userLyrics = input,
                    includeRevision = includeRevision
                )
                _sessionGenerationCount.value += 1
                val creationId = saveToHistoryIfNeeded(
                    featureType = FeatureType.LYRICS_ANALYSIS,
                    title = "लिरिक्स विश्लेषण (स्कोर: ${result.overallScore}/100)",
                    categoryOrGenre = "स्कोर ${result.overallScore}/100",
                    moodOrLanguage = if (includeRevision) "संशोधित लिरिक्स सहित" else "केवल विश्लेषण",
                    userPrompt = input.take(120),
                    primaryContent = result.toFormattedAnalysisText(),
                    secondaryContent = result.revisedLyrics
                )
                _analysisUiState.value = FeatureUiState.Success(
                    data = result,
                    creationId = creationId,
                    isFavorite = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _analysisUiState.value = FeatureUiState.Error(
                    e.message ?: "लिरिक्स विश्लेषण में समस्या आई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun generateLifeGuidance() {
        val situation = _guidanceSituation.value.trim()
        if (situation.length < 5) {
            _guidanceUiState.value = FeatureUiState.Error(
                "कृपया अपनी परिस्थिति या मन का प्रश्न संक्षेप में लिखें ताकि उचित मार्गदर्शन मिल सके।"
            )
            return
        }

        viewModelScope.launch {
            _guidanceUiState.value = FeatureUiState.Loading
            try {
                val result = aiGenerator.generateLifeGuidance(situation = situation)
                _sessionGenerationCount.value += 1
                val creationId = saveToHistoryIfNeeded(
                    featureType = FeatureType.LIFE_GUIDANCE,
                    title = result.chapterAndVerse,
                    categoryOrGenre = "भगवद गीता प्रेरणा",
                    moodOrLanguage = "आध्यात्मिक व व्यावहारिक",
                    userPrompt = situation,
                    primaryContent = result.toFormattedText(),
                    secondaryContent = result.modernInterpretationHindi
                )
                _guidanceUiState.value = FeatureUiState.Success(
                    data = result,
                    creationId = creationId,
                    isFavorite = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guidanceUiState.value = FeatureUiState.Error(
                    e.message ?: "मार्गदर्शन प्राप्त करने में समस्या आई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun generateAiStatusForPoster(topicHint: String = _posterText.value) {
        viewModelScope.launch {
            try {
                val result = aiGenerator.generateStatusCaption(
                    topicOrMood = topicHint,
                    style = _posterBackground.value
                )
                _sessionGenerationCount.value += 1
                if (result.statusHindi.isNotBlank()) {
                    _posterText.value = result.statusHindi
                }
                showFeedback("नया AI स्टेटस और कैप्शन तैयार हो गया! ✨")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showFeedback(e.message ?: "स्टेटस तैयार करने में समस्या आई।")
            }
        }
    }

    fun savePosterToCreations() {
        val quote = _posterText.value.trim()
        if (quote.isBlank()) {
            showFeedback("कृपया पोस्टर सहेजने से पहले कुछ टेक्स्ट या शायरी लिखें।")
            return
        }
        viewModelScope.launch {
            val author = _posterAuthor.value.trim()
            val formatted = if (author.isNotBlank()) "$quote\n\n— $author" else quote
            repository.insertCreation(
                CreationEntity(
                    featureType = FeatureType.POSTER.name,
                    title = quote.lineSequence().firstOrNull()?.take(36) ?: "शायरी पोस्टर",
                    categoryOrGenre = _posterAspectRatio.value.substringBefore(" "),
                    moodOrLanguage = _posterBackground.value.substringBefore(" ("),
                    userPrompt = _posterTypography.value,
                    primaryContent = formatted,
                    secondaryContent = "${_posterBackground.value} • ${_posterAspectRatio.value}",
                    isFavorite = false
                )
            )
            showFeedback("पोस्टर 'मेरी रचनाएँ (My Creations)' में सहेजा गया! 🎨")
        }
    }

    // ==================== Favorites & History Actions ====================

    private suspend fun saveToHistoryIfNeeded(
        featureType: FeatureType,
        title: String,
        categoryOrGenre: String,
        moodOrLanguage: String,
        userPrompt: String,
        primaryContent: String,
        secondaryContent: String,
        extraDetailsJson: String = "",
        youtubeSearchQuery: String = "",
        webSearchQuery: String = ""
    ): Long {
        if (!userSettings.value.autoSaveHistory) return 0L
        return repository.insertCreation(
            CreationEntity(
                featureType = featureType.name,
                title = title,
                categoryOrGenre = categoryOrGenre,
                moodOrLanguage = moodOrLanguage,
                userPrompt = userPrompt,
                primaryContent = primaryContent,
                secondaryContent = secondaryContent,
                extraDetailsJson = extraDetailsJson,
                youtubeSearchQuery = youtubeSearchQuery,
                webSearchQuery = webSearchQuery,
                isFavorite = false
            )
        )
    }

    fun toggleShayariFavorite() {
        val current = _shayariUiState.value as? FeatureUiState.Success<ShayariResult> ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            val id = ensureSavedAndToggleFav(
                existingId = current.creationId,
                newFav = newFav,
                fallbackEntity = {
                    CreationEntity(
                        featureType = FeatureType.SHAYARI.name,
                        title = current.data.title,
                        categoryOrGenre = _shayariCategory.value,
                        moodOrLanguage = _shayariMood.value,
                        userPrompt = _shayariTopic.value.ifBlank { _shayariCategory.value },
                        primaryContent = current.data.toFormattedText(),
                        secondaryContent = current.data.meaningHindi,
                        extraDetailsJson = current.data.aiVerificationReport,
                        youtubeSearchQuery = current.data.youtubeSearchQuery,
                        webSearchQuery = current.data.webSearchQuery,
                        isFavorite = newFav
                    )
                }
            )
            _shayariUiState.value = current.copy(creationId = id, isFavorite = newFav)
            showFeedback(if (newFav) "पसंदीदा (Favorites) में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    fun toggleSongFavorite() {
        val current = _songUiState.value as? FeatureUiState.Success<SongResult> ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            val id = ensureSavedAndToggleFav(
                existingId = current.creationId,
                newFav = newFav,
                fallbackEntity = {
                    CreationEntity(
                        featureType = FeatureType.SONG.name,
                        title = current.data.title,
                        categoryOrGenre = _songGenre.value,
                        moodOrLanguage = _songLanguage.value,
                        userPrompt = _songTopic.value,
                        primaryContent = current.data.toFormattedText(),
                        secondaryContent = current.data.musicGenPrompt.ifBlank { current.data.musicalNote },
                        youtubeSearchQuery = current.data.youtubeSearchQuery,
                        webSearchQuery = current.data.webSearchQuery,
                        isFavorite = newFav
                    )
                }
            )
            _songUiState.value = current.copy(creationId = id, isFavorite = newFav)
            showFeedback(if (newFav) "पसंदीदा (Favorites) में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    fun toggleEmotionFavorite() {
        val current = _emotionUiState.value as? FeatureUiState.Success<EmotionAnalysisResult> ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            val id = ensureSavedAndToggleFav(
                existingId = current.creationId,
                newFav = newFav,
                fallbackEntity = {
                    CreationEntity(
                        featureType = FeatureType.EMOTION_ANALYZER.name,
                        title = "भावना विश्लेषण: ${current.data.detectedEmotion}",
                        categoryOrGenre = current.data.detectedEmotion,
                        moodOrLanguage = "तीव्रता ${current.data.intensityPercent}%",
                        userPrompt = _emotionInput.value.take(120),
                        primaryContent = current.data.toFormattedText(),
                        secondaryContent = current.data.matchingShayari,
                        isFavorite = newFav
                    )
                }
            )
            _emotionUiState.value = current.copy(creationId = id, isFavorite = newFav)
            showFeedback(if (newFav) "पसंदीदा (Favorites) में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    fun toggleAnalysisFavorite() {
        val current = _analysisUiState.value as? FeatureUiState.Success<LyricsAnalysisResult> ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            val id = ensureSavedAndToggleFav(
                existingId = current.creationId,
                newFav = newFav,
                fallbackEntity = {
                    CreationEntity(
                        featureType = FeatureType.LYRICS_ANALYSIS.name,
                        title = "लिरिक्स विश्लेषण (${current.data.overallScore}/100)",
                        categoryOrGenre = "स्कोर ${current.data.overallScore}/100",
                        moodOrLanguage = "लिरिक्स विश्लेषण",
                        userPrompt = _lyricsInput.value.take(120),
                        primaryContent = current.data.toFormattedAnalysisText(),
                        secondaryContent = current.data.revisedLyrics,
                        isFavorite = newFav
                    )
                }
            )
            _analysisUiState.value = current.copy(creationId = id, isFavorite = newFav)
            showFeedback(if (newFav) "पसंदीदा (Favorites) में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    fun toggleGuidanceFavorite() {
        val current = _guidanceUiState.value as? FeatureUiState.Success<LifeGuidanceResult> ?: return
        viewModelScope.launch {
            val newFav = !current.isFavorite
            val id = ensureSavedAndToggleFav(
                existingId = current.creationId,
                newFav = newFav,
                fallbackEntity = {
                    CreationEntity(
                        featureType = FeatureType.LIFE_GUIDANCE.name,
                        title = current.data.chapterAndVerse,
                        categoryOrGenre = "भगवद गीता प्रेरणा",
                        moodOrLanguage = "जीवन मार्गदर्शन",
                        userPrompt = _guidanceSituation.value,
                        primaryContent = current.data.toFormattedText(),
                        secondaryContent = current.data.modernInterpretationHindi,
                        isFavorite = newFav
                    )
                }
            )
            _guidanceUiState.value = current.copy(creationId = id, isFavorite = newFav)
            showFeedback(if (newFav) "पसंदीदा (Favorites) में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    private suspend fun ensureSavedAndToggleFav(
        existingId: Long,
        newFav: Boolean,
        fallbackEntity: () -> CreationEntity
    ): Long {
        return if (existingId > 0L) {
            repository.toggleFavorite(existingId, newFav)
            existingId
        } else {
            repository.insertCreation(fallbackEntity())
        }
    }

    fun toggleCreationFavorite(creation: CreationEntity) {
        viewModelScope.launch {
            val updated = !creation.isFavorite
            repository.toggleFavorite(creation.id, updated)
            if (_selectedCreationDetail.value?.id == creation.id) {
                _selectedCreationDetail.value = creation.copy(isFavorite = updated)
            }
            showFeedback(if (updated) "पसंदीदा में जोड़ा गया ❤️" else "पसंदीदा से हटाया गया")
        }
    }

    fun deleteCreation(id: Long) {
        viewModelScope.launch {
            repository.deleteCreationById(id)
            if (_selectedCreationDetail.value?.id == id) {
                _selectedCreationDetail.value = null
            }
            showFeedback("आइटम हटा दिया गया")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllCreations()
            _selectedCreationDetail.value = null
            showFeedback("पूरा इतिहास साफ़ कर दिया गया है")
        }
    }

    // ==================== Settings Actions ====================
    fun updateDefaultLanguage(language: String) {
        repository.updateDefaultLanguage(language)
        _songLanguage.value = language
        showFeedback("डिफ़ॉल्ट भाषा अपडेट की गई: $language")
    }

    fun updateFontScale(scale: Float) {
        repository.updateFontScale(scale)
    }

    fun updateAutoSaveHistory(enabled: Boolean) {
        repository.updateAutoSaveHistory(enabled)
        showFeedback(if (enabled) "ऑटो-सेव चालू किया गया" else "ऑटो-सेव बंद किया गया")
    }

    fun updateDarkTheme(isDark: Boolean) {
        repository.updateDarkTheme(isDark)
        showFeedback(if (isDark) "प्रीमियम डार्क थीम सक्रिय 🌙" else "लाइट थीम सक्रिय ☀️")
    }
}
