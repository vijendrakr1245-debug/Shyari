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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.LifeGuidanceResult
import com.example.ui.FeatureUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.components.LoadingCreationCard
import com.example.ui.components.ResultActionButtonsRow
import com.example.ui.components.VoiceInputButton
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent

@Composable
fun LifeGuidanceScreen(
    situationInput: String,
    onSituationChanged: (String) -> Unit,
    uiState: FeatureUiState<LifeGuidanceResult>,
    onGenerateGuidance: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenInPoster: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val commonSituations = listOf(
        "कड़ी मेहनत के बाद भी मनचाहा परिणाम न मिलना और निराशा",
        "करियर और भविष्य के फैसलों को लेकर भारी असमंजस",
        "रिश्तों में उम्मीदें टूटने पर मानसिक शांति कैसे बनाए रखें",
        "असफलता का डर और आत्मविश्वास की कमी से कैसे उबरें"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guidance_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Respectful Disclaimer Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guidance_disclaimer_banner"),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.gita_disclaimer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // 2. Input Card with Voice Input
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
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "भगवद गीता AI गाइड (Gita AI Guide)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "अपनी जीवन समस्या साझा करें — प्रमाणिक श्लोक संदर्भ, संस्कृत श्लोक, हिंदी अर्थ और व्यावहारिक समाधान पाएं",
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
                        Text(
                            text = "अपनी परिस्थिति लिखें या बोलें (Voice):",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        VoiceInputButton(
                            promptHint = "अपनी समस्या या प्रश्न हिंदी में बोलें...",
                            onTextRecognized = { spoken ->
                                val updated = if (situationInput.isBlank()) spoken else "$situationInput $spoken"
                                onSituationChanged(updated)
                            },
                            onFeedback = onFeedback,
                            testTag = "guidance_voice_input_btn"
                        )
                    }

                    OutlinedTextField(
                        value = situationInput,
                        onValueChange = onSituationChanged,
                        placeholder = {
                            Text("उदाहरण: मैं अपने लक्ष्य के लिए बहुत प्रयास कर रहा हूँ, लेकिन बार-बार चिंता और डर सताता है...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guidance_situation_input"),
                        minLines = 3,
                        maxLines = 6
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonSituations.forEachIndexed { idx, prompt ->
                            AssistChip(
                                onClick = { onSituationChanged(prompt) },
                                label = { Text(prompt.take(32) + "...") },
                                modifier = Modifier.testTag("guidance_sample_chip_$idx")
                            )
                        }
                    }

                    Button(
                        onClick = onGenerateGuidance,
                        enabled = uiState !is FeatureUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_guidance_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState is FeatureUiState.Loading) "मार्गदर्शन तैयार हो रहा है..." else "मार्गदर्शन प्राप्त करें (Get Gita Guidance)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // 3. Result Card displaying Verified Verse Reference, Verified Sanskrit Text, Hindi Meaning, and Practical Explanation
        item {
            when (uiState) {
                is FeatureUiState.Idle -> {
                    EmptyStateCard(
                        icon = Icons.Default.MenuBook,
                        title = "गीता के आलोक में शांति व समाधान",
                        subtitle = "अपनी समस्या ऊपर लिखें या माइक से बोलें। आपको प्रमाणिक अध्याय/श्लोक संदर्भ, प्रमाणिक संस्कृत श्लोक, हिंदी अर्थ और व्यावहारिक कदम मिलेंगे।",
                        testTag = "guidance_empty_state"
                    )
                }

                is FeatureUiState.Loading -> {
                    LoadingCreationCard(
                        messageHindi = "गीता के अनमोल विचारों का चिंतन हो रहा है...",
                        subtitleHindi = "प्रमाणिक श्लोक, हिंदी भावार्थ और आधुनिक व्यावहारिक समाधान तैयार किए जा रहे हैं"
                    )
                }

                is FeatureUiState.Error -> {
                    ErrorBannerCard(
                        errorMessage = uiState.message,
                        onRetry = onGenerateGuidance,
                        tagPrefix = "guidance"
                    )
                }

                is FeatureUiState.Success -> {
                    val guidance = uiState.data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guidance_result_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Verified Chapter & Verse Reference Badge
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(
                                    text = "🕉️ प्रमाणिक संदर्भ: ${guidance.chapterAndVerse}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("guidance_reference_text")
                                )
                            }

                            // Verified Sanskrit Text Box (Only displayed when verified)
                            if (guidance.verifiedSanskritShloka.isNotBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verified_sanskrit_box"),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "🌺 प्रमाणिक संस्कृत श्लोक (Verified Sanskrit Shloka)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = guidance.verifiedSanskritShloka,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Section 1: Hindi Meaning & Traditional Teaching
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("traditional_teaching_box"),
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "📜 हिंदी भावार्थ व पारंपरिक शिक्षा (Hindi Meaning)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = guidance.traditionalTeachingHindi,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Section 2: Practical Explanation
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("modern_interpretation_box"),
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "🌱 व्यावहारिक समाधान (Practical Explanation)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = guidance.modernInterpretationHindi,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Section 3: Practical Action Steps
                            if (guidance.practicalSteps.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "🧭 आज से अपनाने योग्य व्यावहारिक कदम:",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        guidance.practicalSteps.forEachIndexed { index, step ->
                                            Text(
                                                text = "${index + 1}. $step",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (guidance.peacefulReflection.isNotBlank()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Text(
                                    text = "✨ सकारात्मक संकल्प: \"${guidance.peacefulReflection}\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            ResultActionButtonsRow(
                                isFavorite = uiState.isFavorite,
                                onCopy = {
                                    copyTextToClipboard(
                                        context = context,
                                        label = guidance.chapterAndVerse,
                                        text = guidance.toFormattedText(),
                                        onCopied = { onFeedback("गीता मार्गदर्शन कॉपी हो गया! 🕉️") }
                                    )
                                },
                                onRegenerate = onGenerateGuidance,
                                onToggleFavorite = onToggleFavorite,
                                onShare = {
                                    shareTextContent(
                                        context = context,
                                        title = guidance.chapterAndVerse,
                                        text = guidance.toFormattedText()
                                    )
                                },
                                tagPrefix = "guidance",
                                onOpenInPoster = {
                                    val quoteForPoster = guidance.peacefulReflection.ifBlank {
                                        guidance.traditionalTeachingHindi
                                    }
                                    onOpenInPoster(quoteForPoster)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
