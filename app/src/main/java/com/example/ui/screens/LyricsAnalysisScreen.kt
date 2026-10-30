package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.EmotionAnalysisResult
import com.example.data.ai.LyricsAnalysisResult
import com.example.ui.FeatureUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.components.LoadingCreationCard
import com.example.ui.components.ResultActionButtonsRow
import com.example.ui.components.VoiceInputButton
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent

@Composable
fun LyricsAnalysisScreen(
    analyzerModeTab: Int,
    onAnalyzerModeTabChanged: (Int) -> Unit,
    emotionInput: String,
    onEmotionInputChanged: (String) -> Unit,
    emotionUiState: FeatureUiState<EmotionAnalysisResult>,
    onAnalyzeEmotion: () -> Unit,
    onToggleEmotionFavorite: () -> Unit,
    lyricsInput: String,
    onLyricsInputChanged: (String) -> Unit,
    includeRevisedLyrics: Boolean,
    onIncludeRevisedChanged: (Boolean) -> Unit,
    uiState: FeatureUiState<LyricsAnalysisResult>,
    onAnalyze: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenInPoster: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val sampleLyrics = """
        रात की खामोशी में तेरा नाम लिया,
        हर टूटे तारे से तेरा पैगाम लिया,
        राहें तो बहुत थीं मंज़िल तक जाने की,
        पर दिल ने सिर्फ तेरा ही मुकाम लिया।
    """.trimIndent()

    val sampleEmotionText = "आज मन थोड़ा उदास है और पुराने दोस्तों व बीते लम्हों की बहुत याद आ रही है।"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analysis_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: AI Emotion Analyzer vs AI Lyrics Analysis
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = analyzerModeTab == 0,
                    onClick = { onAnalyzerModeTabChanged(0) },
                    label = { Text("🎭 AI भावना विश्लेषण (Emotion Analyzer)") },
                    modifier = Modifier.testTag("tab_emotion_analyzer")
                )
                FilterChip(
                    selected = analyzerModeTab == 1,
                    onClick = { onAnalyzerModeTabChanged(1) },
                    label = { Text("🎶 लिरिक्स विश्लेषण (Lyrics Analysis)") },
                    modifier = Modifier.testTag("tab_lyrics_analyzer")
                )
            }
        }

        if (analyzerModeTab == 0) {
            // ==================== TAB 0: AI EMOTION ANALYZER ====================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emotion_disclaimer_banner"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Disclaimer",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "नोट: यह सुविधा आपके शब्दों के भावनात्मक स्वर की रचनात्मक पहचान करती है। यह कोई चिकित्सीय या मनोवैज्ञानिक निदान (Medical Diagnosis) नहीं है।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI भावना विश्लेषक (Emotion Analyzer)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "अपने मन की बात लिखें या बोलें — भावनात्मक स्वर जानें और अनुरूप शायरी, गीत व सकारात्मक संदेश पाएं",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { onEmotionInputChanged(sampleEmotionText) },
                                label = { Text("उदाहरण भरें (Sample)") },
                                modifier = Modifier.testTag("fill_sample_emotion_btn")
                            )
                            VoiceInputButton(
                                promptHint = "अपने मन की भावना हिंदी या हिंग्लिश में बोलें...",
                                onTextRecognized = { spoken ->
                                    val updated = if (emotionInput.isBlank()) spoken else "$emotionInput $spoken"
                                    onEmotionInputChanged(updated)
                                },
                                onFeedback = onFeedback,
                                testTag = "emotion_voice_input_btn"
                            )
                        }

                        OutlinedTextField(
                            value = emotionInput,
                            onValueChange = onEmotionInputChanged,
                            placeholder = {
                                Text("यहाँ अपने विचार, स्थिति या कोई संदेश लिखें...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("emotion_text_input"),
                            minLines = 3,
                            maxLines = 6
                        )

                        Button(
                            onClick = onAnalyzeEmotion,
                            enabled = emotionUiState !is FeatureUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("analyze_emotion_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (emotionUiState is FeatureUiState.Loading) "भावना का विश्लेषण हो रहा है..." else "भावना पहचानें (Analyze Emotion)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item {
                when (emotionUiState) {
                    is FeatureUiState.Idle -> {
                        EmptyStateCard(
                            icon = Icons.Default.Psychology,
                            title = "अपने शब्दों का भावनात्मक स्वर जानें",
                            subtitle = "ऊपर कुछ पंक्तियाँ लिखें और जानें कि उनमें कौन-सा भाव (प्यार, उदासी, उत्साह, शांति) झलकता है—साथ ही मैचिंग शायरी और गीत पाएं।",
                            testTag = "emotion_empty_state"
                        )
                    }

                    is FeatureUiState.Loading -> {
                        LoadingCreationCard(
                            messageHindi = "भावनात्मक स्वर पहचाना जा रहा है...",
                            subtitleHindi = "आपके भाव के अनुरूप शायरी, गीत और सकारात्मक संदेश तैयार हो रहे हैं"
                        )
                    }

                    is FeatureUiState.Error -> {
                        ErrorBannerCard(
                            errorMessage = emotionUiState.message,
                            onRetry = onAnalyzeEmotion,
                            tagPrefix = "emotion"
                        )
                    }

                    is FeatureUiState.Success -> {
                        val emotion = emotionUiState.data
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("emotion_result_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🎭 पहचाना गया भाव: ${emotion.detectedEmotion}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("detected_emotion_text")
                                )

                                LinearProgressIndicator(
                                    progress = { emotion.intensityPercent / 100f },
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )

                                AnalysisDimensionItem(
                                    title = "🔍 भावनात्मक व्याख्या (Brief Explanation)",
                                    description = emotion.explanationHindi
                                )

                                AnalysisDimensionItem(
                                    title = "💛 सहयोगी संदेश (Supportive Message)",
                                    description = emotion.supportiveMessage
                                )

                                if (emotion.matchingShayari.isNotBlank()) {
                                    AnalysisDimensionItem(
                                        title = "✨ आपके भाव के अनुरूप शायरी",
                                        description = emotion.matchingShayari
                                    )
                                }

                                if (emotion.matchingSongLyrics.isNotBlank()) {
                                    AnalysisDimensionItem(
                                        title = "🎵 आपके भाव के अनुरूप गीत की पंक्तियाँ",
                                        description = emotion.matchingSongLyrics
                                    )
                                }

                                ResultActionButtonsRow(
                                    isFavorite = emotionUiState.isFavorite,
                                    onCopy = {
                                        copyTextToClipboard(
                                            context = context,
                                            label = emotion.detectedEmotion,
                                            text = emotion.toFormattedText(),
                                            onCopied = { onFeedback("भावना विश्लेषण कॉपी हो गया! 📋") }
                                        )
                                    },
                                    onRegenerate = onAnalyzeEmotion,
                                    onToggleFavorite = onToggleEmotionFavorite,
                                    onShare = {
                                        shareTextContent(
                                            context = context,
                                            title = emotion.detectedEmotion,
                                            text = emotion.toFormattedText()
                                        )
                                    },
                                    tagPrefix = "emotion",
                                    onOpenInPoster = {
                                        onOpenInPoster(
                                            emotion.matchingShayari.ifBlank { emotion.supportiveMessage }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== TAB 1: EXISTING LYRICS ANALYSIS ====================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI लिरिक्स विश्लेषण (Lyrics Analyzer)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "अपने लिखे बोल या शायरी की भावनात्मक गहराई, तुकबंदी, लय और गायकी का सरल हिंदी में विश्लेषण पाएं",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { onLyricsInputChanged(sampleLyrics) },
                                label = { Text("उदाहरण भरें (Sample)") },
                                modifier = Modifier.testTag("fill_sample_lyrics_btn")
                            )
                            VoiceInputButton(
                                promptHint = "लिरिक्स या शायरी बोलें...",
                                onTextRecognized = { spoken ->
                                    val updated = if (lyricsInput.isBlank()) spoken else "$lyricsInput\n$spoken"
                                    onLyricsInputChanged(updated)
                                },
                                onFeedback = onFeedback,
                                testTag = "lyrics_voice_input_btn"
                            )
                        }

                        OutlinedTextField(
                            value = lyricsInput,
                            onValueChange = onLyricsInputChanged,
                            placeholder = {
                                Text("यहाँ अपनी कविता, शायरी या गाने के बोल लिखें या पेस्ट करें...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lyrics_analysis_input"),
                            minLines = 5,
                            maxLines = 10
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "संशोधित और बेहतर लिरिक्स भी बनाएँ",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "आपके मूल भाव को सुरक्षित रखते हुए लय और तुकबंदी सुधारें",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = includeRevisedLyrics,
                                    onCheckedChange = onIncludeRevisedChanged,
                                    modifier = Modifier.testTag("include_revised_switch")
                                )
                            }
                        }

                        Button(
                            onClick = onAnalyze,
                            enabled = uiState !is FeatureUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("analyze_lyrics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState is FeatureUiState.Loading) "विश्लेषण किया जा रहा है..." else "लिरिक्स का विश्लेषण करें (Analyze Lyrics)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item {
                when (uiState) {
                    is FeatureUiState.Idle -> {
                        EmptyStateCard(
                            icon = Icons.Default.RateReview,
                            title = "अपने लिरिक्स की समीक्षा पाएं",
                            subtitle = "भावनात्मक प्रभाव, कहानी, तुकबंदी (Rhyme), लय (Rhythm), मौलिकता और गायकी योग्यता पर विस्तृत फीडबैक सरल हिंदी में प्राप्त करें।",
                            testTag = "analysis_empty_state"
                        )
                    }

                    is FeatureUiState.Loading -> {
                        LoadingCreationCard(
                            messageHindi = "लिरिक्स की बारीकी से जांच हो रही है...",
                            subtitleHindi = "भाव, छंद, तुकबंदी और गायकी के आधार पर विश्लेषण तैयार किया जा रहा है"
                        )
                    }

                    is FeatureUiState.Error -> {
                        ErrorBannerCard(
                            errorMessage = uiState.message,
                            onRetry = onAnalyze,
                            tagPrefix = "analysis"
                        )
                    }

                    is FeatureUiState.Success -> {
                        val analysis = uiState.data
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("analysis_result_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = BorderStroke(1.5.dp, Color(0xFF34D399))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📊 लिरिक्स विश्लेषण रिपोर्ट",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = Color(0xFF34D399).copy(alpha = 0.2f),
                                            shape = MaterialTheme.shapes.small,
                                            border = BorderStroke(1.dp, Color(0xFF34D399))
                                        ) {
                                            Text(
                                                text = "${analysis.overallScore} / 100",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFF34D399),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    LinearProgressIndicator(
                                        progress = { analysis.overallScore / 100f },
                                        color = Color(0xFF34D399),
                                        trackColor = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                    )

                                    AnalysisDimensionItem(
                                        title = "❤️ भावनात्मक प्रभाव (Emotional Impact)",
                                        description = analysis.emotionalImpact
                                    )
                                    AnalysisDimensionItem(
                                        title = "📖 कहानी और भाव (Storytelling)",
                                        description = analysis.storytelling
                                    )
                                    AnalysisDimensionItem(
                                        title = "🎶 तुकबंदी और लय (Rhyme & Rhythm)",
                                        description = analysis.rhymeAndRhythm
                                    )
                                    AnalysisDimensionItem(
                                        title = "🎤 मौलिकता और गायकी योग्यता (Originality & Singability)",
                                        description = analysis.originalityAndSingability
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                                    BulletSectionBlock(
                                        heading = "✅ मुख्य खूबियाँ (Strengths):",
                                        items = analysis.strengths,
                                        accentColor = Color(0xFF34D399)
                                    )

                                    BulletSectionBlock(
                                        heading = "⚠️ कमियाँ (Areas to Improve):",
                                        items = analysis.weaknesses,
                                        accentColor = MaterialTheme.colorScheme.tertiary
                                    )

                                    BulletSectionBlock(
                                        heading = "💡 सुधार के विशेष सुझाव (Actionable Improvements):",
                                        items = analysis.improvements,
                                        accentColor = MaterialTheme.colorScheme.primary
                                    )

                                    ResultActionButtonsRow(
                                        isFavorite = uiState.isFavorite,
                                        onCopy = {
                                            copyTextToClipboard(
                                                context = context,
                                                label = "Lyrics Analysis Report",
                                                text = analysis.toFormattedAnalysisText(),
                                                onCopied = { onFeedback("विश्लेषण रिपोर्ट कॉपी हो गई! 📋") }
                                            )
                                        },
                                        onRegenerate = onAnalyze,
                                        onToggleFavorite = onToggleFavorite,
                                        onShare = {
                                            shareTextContent(
                                                context = context,
                                                title = "AI लिरिक्स विश्लेषण रिपोर्ट",
                                                text = analysis.toFormattedAnalysisText()
                                            )
                                        },
                                        tagPrefix = "analysis"
                                    )
                                }
                            }

                            if (analysis.revisedLyrics.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("revised_lyrics_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = "✨ संशोधित और बेहतर लिरिक्स (Revised Lyrics)",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = MaterialTheme.shapes.medium,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text(
                                                text = analysis.revisedLyrics,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .testTag("revised_lyrics_text")
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                copyTextToClipboard(
                                                    context = context,
                                                    label = "Revised Lyrics",
                                                    text = analysis.revisedLyrics,
                                                    onCopied = { onFeedback("संशोधित लिरिक्स कॉपी हो गए! 🎶") }
                                                )
                                            },
                                            modifier = Modifier.testTag("copy_revised_lyrics_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Revised Lyrics",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("संशोधित लिरिक्स कॉपी करें")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisDimensionItem(
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BulletSectionBlock(
    heading: String,
    items: List<String>,
    accentColor: Color
) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
        items.forEach { point ->
            Text(
                text = "• $point",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
