package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.SongResult
import com.example.ui.FeatureUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.components.LoadingCreationCard
import com.example.ui.components.ResultActionButtonsRow
import com.example.ui.components.SaveToFavoritesToggleRow
import com.example.ui.components.SelectableChipRow
import com.example.ui.components.VoiceInputButton
import com.example.ui.components.YouTubeAndWebSearchCard
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent

@Composable
fun SongMakerScreen(
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    genres: List<String>,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit,
    moods: List<String>,
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    vocalStyles: List<String>,
    selectedVocalStyle: String,
    onVocalStyleSelected: (String) -> Unit,
    topicInput: String,
    onTopicChanged: (String) -> Unit,
    uiState: FeatureUiState<SongResult>,
    onGenerate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val quickTopics = listOf(
        "पहली बारिश और तुम्हारी याद",
        "सपनों के शहर में नई शुरुआत",
        "सच्चे यारों के साथ लंबा सफर",
        "अधूरे इश्क़ की दास्तान"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("song_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI सॉन्ग मेकर (Original Songwriter)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "शीर्षक, मुखड़ा, कोरस, अंतरा, ब्रिज और अलग AI म्यूज़िक प्रॉम्प्ट के साथ गाना तैयार करें",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SelectableChipRow(
                        label = "1. भाषा चुनें (Language):",
                        options = languages,
                        selectedOption = selectedLanguage,
                        onOptionSelected = onLanguageSelected,
                        tagPrefix = "song_language"
                    )

                    SelectableChipRow(
                        label = "2. संगीत शैली (Genre):",
                        options = genres,
                        selectedOption = selectedGenre,
                        onOptionSelected = onGenreSelected,
                        tagPrefix = "song_genre"
                    )

                    SelectableChipRow(
                        label = "3. मूड चुनें (Mood):",
                        options = moods,
                        selectedOption = selectedMood,
                        onOptionSelected = onMoodSelected,
                        tagPrefix = "song_mood"
                    )

                    SelectableChipRow(
                        label = "4. गायकी / वोकल स्टाइल (Vocal Style):",
                        options = vocalStyles,
                        selectedOption = selectedVocalStyle,
                        onOptionSelected = onVocalStyleSelected,
                        tagPrefix = "song_vocal"
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "5. गाने का विषय (वॉइस या टेक्स्ट - आवश्यक):",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            VoiceInputButton(
                                promptHint = "गाने का विषय हिंदी या हिंग्लिश में बोलें...",
                                onTextRecognized = { spoken ->
                                    val updated = if (topicInput.isBlank()) spoken else "$topicInput $spoken"
                                    onTopicChanged(updated)
                                },
                                onFeedback = onFeedback,
                                testTag = "song_voice_input_btn"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = onTopicChanged,
                            placeholder = {
                                Text("उदाहरण: पहली बारिश और तुम्हारी याद, या सपनों की उड़ान...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("song_topic_input"),
                            minLines = 2,
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickTopics.forEachIndexed { idx, sample ->
                                AssistChip(
                                    onClick = { onTopicChanged(sample) },
                                    label = { Text(sample) },
                                    modifier = Modifier.testTag("song_sample_topic_$idx")
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onGenerate,
                        enabled = uiState !is FeatureUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_song_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState is FeatureUiState.Loading) "गाने के बोल तैयार हो रहे हैं..." else "गाना तैयार करें (Generate Song)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Result Section
        item {
            when (uiState) {
                is FeatureUiState.Idle -> {
                    EmptyStateCard(
                        icon = Icons.Default.MusicNote,
                        title = "आपका नया गाना यहाँ प्रदर्शित होगा",
                        subtitle = "भाषा, जॉनर, मूड, वोकल स्टाइल और गाने का विषय लिखकर 'गाना तैयार करें' दबाएँ। साथ में अलग AI Music Generation Prompt भी मिलेगा।",
                        testTag = "song_empty_state"
                    )
                }

                is FeatureUiState.Loading -> {
                    LoadingCreationCard(
                        messageHindi = "धुन, मुखड़ा, अंतरे और ब्रिज रचे जा रहे हैं...",
                        subtitleHindi = "$selectedGenre ($selectedVocalStyle) में मौलिक गीत लिखा जा रहा है"
                    )
                }

                is FeatureUiState.Error -> {
                    ErrorBannerCard(
                        errorMessage = uiState.message,
                        onRetry = onGenerate,
                        tagPrefix = "song"
                    )
                }

                is FeatureUiState.Success -> {
                    val song = uiState.data
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // CARD 1: Complete Song Lyrics Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("song_result_card"),
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
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "$selectedLanguage • $selectedGenre • $selectedVocalStyle",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "🎵 ${song.title}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("song_title_text")
                                )

                                Text(
                                    text = "🎼 संगीत व गायकी सुझाव: ${song.musicalNote}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (song.mukhda.isNotBlank()) {
                                    SongSectionBlock(
                                        label = "Mukhda / Intro (मुखड़ा)",
                                        lyrics = song.mukhda,
                                        accentColor = MaterialTheme.colorScheme.tertiary,
                                        isHighlighted = true
                                    )
                                }

                                SongSectionBlock(
                                    label = "Verse 1 (अंतरा 1)",
                                    lyrics = song.verse1,
                                    accentColor = MaterialTheme.colorScheme.primary
                                )

                                if (song.preChorus.isNotBlank()) {
                                    SongSectionBlock(
                                        label = "Pre-Chorus (प्री-कोरस)",
                                        lyrics = song.preChorus,
                                        accentColor = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                SongSectionBlock(
                                    label = "Chorus (कोरस / मुख्य हुक)",
                                    lyrics = song.chorus,
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    isHighlighted = true
                                )

                                SongSectionBlock(
                                    label = "Verse 2 (अंतरा 2)",
                                    lyrics = song.verse2,
                                    accentColor = MaterialTheme.colorScheme.primary
                                )

                                if (song.bridge.isNotBlank()) {
                                    SongSectionBlock(
                                        label = "Bridge (ब्रिज - भावनात्मक शिखर)",
                                        lyrics = song.bridge,
                                        accentColor = Color(0xFF60A5FA)
                                    )
                                }

                                if (song.outro.isNotBlank()) {
                                    SongSectionBlock(
                                        label = "Outro (आउट्रो)",
                                        lyrics = song.outro,
                                        accentColor = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                // Explicit 'Save to Favorites' Switch Toggle
                                SaveToFavoritesToggleRow(
                                    isFavorite = uiState.isFavorite,
                                    onToggleFavorite = onToggleFavorite,
                                    tagPrefix = "song"
                                )

                                // YouTube & Internet Search Card
                                YouTubeAndWebSearchCard(
                                    youtubeQuery = song.youtubeSearchQuery,
                                    webQuery = song.webSearchQuery,
                                    context = context,
                                    onFeedback = onFeedback,
                                    tagPrefix = "song"
                                )

                                ResultActionButtonsRow(
                                    isFavorite = uiState.isFavorite,
                                    onCopy = {
                                        copyTextToClipboard(
                                            context = context,
                                            label = song.title,
                                            text = song.toFormattedText(),
                                            onCopied = { onFeedback("पूरे गाने के बोल कॉपी हो गए! 🎵") }
                                        )
                                    },
                                    onRegenerate = onGenerate,
                                    onToggleFavorite = onToggleFavorite,
                                    onShare = {
                                        shareTextContent(
                                            context = context,
                                            title = song.title,
                                            text = song.toFormattedText()
                                        )
                                    },
                                    tagPrefix = "song"
                                )
                            }
                        }

                        // CARD 2: Separate AI Music-Generation Prompt Card
                        if (song.musicGenPrompt.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("song_music_prompt_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🎧 AI म्यूज़िक-जनरेशन प्रॉम्प्ट (Music-Generation Prompt)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "इस प्रॉम्प्ट को किसी भी AI Music Generator या कंपोज़र टूल में उपयोग कर अपने गीत के लिए ट्रैक तैयार करें:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.medium,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text(
                                            text = song.musicGenPrompt,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                                .testTag("song_music_prompt_text")
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            copyTextToClipboard(
                                                context = context,
                                                label = "Music Generation Prompt",
                                                text = song.musicGenPrompt,
                                                onCopied = { onFeedback("म्यूज़िक जनरेशन प्रॉम्प्ट कॉपी हो गया! 🎧") }
                                            )
                                        },
                                        modifier = Modifier.testTag("copy_music_prompt_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Music Prompt",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("म्यूज़िक प्रॉम्प्ट कॉपी करें")
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
private fun SongSectionBlock(
    label: String,
    lyrics: String,
    accentColor: Color,
    isHighlighted: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isHighlighted) {
            accentColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) accentColor else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lyrics,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
