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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.ShayariResult
import com.example.ui.FeatureUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.components.LoadingCreationCard
import com.example.ui.components.ResultActionButtonsRow
import com.example.ui.components.SaveToFavoritesToggleRow
import com.example.ui.components.SecondAiVerificationCard
import com.example.ui.components.SelectableChipRow
import com.example.ui.components.VoiceInputButton
import com.example.ui.components.YouTubeAndWebSearchCard
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent

@Composable
fun ShayariScreen(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    moods: List<String>,
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    lengths: List<String>,
    selectedLength: String,
    onLengthSelected: (String) -> Unit,
    customNameInput: String,
    onCustomNameChanged: (String) -> Unit,
    topicInput: String,
    onTopicChanged: (String) -> Unit,
    uiState: FeatureUiState<ShayariResult>,
    isVerifyingSecondAi: Boolean = false,
    onGenerate: () -> Unit,
    onVerifySecondAi: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onOpenInPoster: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val sampleTopics = listOf(
        "बारिश और अधूरी मुलाकात",
        "सच्चे दोस्त की अहमियत",
        "मुश्किल वक्त में खुद पर भरोसा",
        "महादेव की भक्ति और मन की शांति"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shayari_screen_list"),
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
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI शायरी स्टूडियो (Shayari Studio)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "श्रेणी, मूड, लंबाई, कस्टम नाम और वॉइस इनपुट के साथ मौलिक हिंदी व हिंग्लिश शायरी बनाएँ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SelectableChipRow(
                        label = "1. श्रेणी चुनें (Category - Romantic, Sad, Attitude, Friendship, Motivational, Devotional):",
                        options = categories,
                        selectedOption = selectedCategory,
                        onOptionSelected = onCategorySelected,
                        tagPrefix = "shayari_category"
                    )

                    SelectableChipRow(
                        label = "2. मूड चुनें (Mood):",
                        options = moods,
                        selectedOption = selectedMood,
                        onOptionSelected = onMoodSelected,
                        tagPrefix = "shayari_mood"
                    )

                    SelectableChipRow(
                        label = "3. शायरी की लंबाई (Adjustable Length):",
                        options = lengths,
                        selectedOption = selectedLength,
                        onOptionSelected = onLengthSelected,
                        tagPrefix = "shayari_length"
                    )

                    SelectableChipRow(
                        label = "4. भाषा प्राथमिकता (Script Focus):",
                        options = languages,
                        selectedOption = selectedLanguage,
                        onOptionSelected = onLanguageSelected,
                        tagPrefix = "shayari_language"
                    )

                    OutlinedTextField(
                        value = customNameInput,
                        onValueChange = onCustomNameChanged,
                        label = { Text("5. विशेष नाम / समर्पण (Custom Name - वैकल्पिक)") },
                        placeholder = { Text("उदाहरण: प्रिया, आरव, मेरे दोस्त...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shayari_custom_name_input")
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "6. विषय या स्थिति (वॉइस या टेक्स्ट - वैकल्पिक):",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            VoiceInputButton(
                                promptHint = "शायरी का विषय हिंदी या हिंग्लिश में बोलें...",
                                onTextRecognized = { spoken ->
                                    val updated = if (topicInput.isBlank()) spoken else "$topicInput $spoken"
                                    onTopicChanged(updated)
                                },
                                onFeedback = onFeedback,
                                testTag = "shayari_voice_input_btn"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = onTopicChanged,
                            placeholder = {
                                Text("उदाहरण: कॉलेज के दोस्तों की याद, या पहली नज़र का प्यार...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shayari_topic_input"),
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
                            sampleTopics.forEachIndexed { idx, sample ->
                                AssistChip(
                                    onClick = { onTopicChanged(sample) },
                                    label = { Text(sample) },
                                    modifier = Modifier.testTag("shayari_sample_topic_$idx")
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
                            .testTag("generate_shayari_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState is FeatureUiState.Loading) "शायरी लिखी जा रही है..." else "शायरी तैयार करें (Generate Shayari)",
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
                        icon = Icons.Default.EditNote,
                        title = "आपकी शायरी यहाँ दिखाई देगी",
                        subtitle = "अपनी पसंद की श्रेणी (Romantic, Sad, Attitude, Friendship, Motivational, Devotional), लंबाई और नाम चुनकर 'शायरी तैयार करें' दबाएँ।",
                        testTag = "shayari_empty_state"
                    )
                }

                is FeatureUiState.Loading -> {
                    LoadingCreationCard(
                        messageHindi = "अल्फ़ाज़ पिरोए जा रहे हैं...",
                        subtitleHindi = "$selectedCategory ($selectedLength) में मौलिक शायरी तैयार हो रही है"
                    )
                }

                is FeatureUiState.Error -> {
                    ErrorBannerCard(
                        errorMessage = uiState.message,
                        onRetry = onGenerate,
                        tagPrefix = "shayari"
                    )
                }

                is FeatureUiState.Success -> {
                    val result = uiState.data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shayari_result_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "$selectedCategory • $selectedLength",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )

                            if (result.dedicatedTo.isNotBlank()) {
                                Text(
                                    text = "✨ समर्पित: ${result.dedicatedTo}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "हिंदी (Devanagari)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = result.shayariHindi,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("shayari_hindi_text")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Hinglish",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = result.shayariHinglish,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("shayari_hinglish_text")
                                    )
                                }
                            }

                            if (result.meaningHindi.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "💡 भावार्थ: ${result.meaningHindi}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Save to Favorites Toggle Bar (Room Database)
                            SaveToFavoritesToggleRow(
                                isFavorite = uiState.isFavorite,
                                onToggleFavorite = onToggleFavorite,
                                tagPrefix = "shayari"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Second-AI Ustaad Critic Verification & 'How to Shayari' Craft Check
                            SecondAiVerificationCard(
                                qafiyaRadifInfo = result.qafiyaRadifInfo,
                                aiVerificationReport = result.aiVerificationReport,
                                qualityScore = result.qualityScore,
                                isVerifying = isVerifyingSecondAi,
                                onVerifyWithSecondAi = onVerifySecondAi,
                                tagPrefix = "shayari"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // YouTube & Internet Search Links
                            YouTubeAndWebSearchCard(
                                youtubeQuery = result.youtubeSearchQuery.ifBlank {
                                    "Best Hindi Shayari Mushaira $selectedCategory $topicInput"
                                },
                                webQuery = result.webSearchQuery.ifBlank {
                                    "${result.title} $selectedCategory हिंदी शायरी रेख़्ता"
                                },
                                context = context,
                                onFeedback = onFeedback,
                                tagPrefix = "shayari"
                            )

                            ResultActionButtonsRow(
                                isFavorite = uiState.isFavorite,
                                onCopy = {
                                    copyTextToClipboard(
                                        context = context,
                                        label = result.title,
                                        text = result.toFormattedText(),
                                        onCopied = { onFeedback("शायरी क्लिपबोर्ड पर कॉपी हो गई! 📋") }
                                    )
                                },
                                onRegenerate = onGenerate,
                                onToggleFavorite = onToggleFavorite,
                                onShare = {
                                    shareTextContent(
                                        context = context,
                                        title = result.title,
                                        text = result.toFormattedText()
                                    )
                                },
                                tagPrefix = "shayari",
                                onOpenInPoster = {
                                    onOpenInPoster(result.shayariHindi)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
