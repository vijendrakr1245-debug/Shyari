package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CreationEntity
import com.example.data.local.FeatureType
import com.example.ui.AppScreen
import com.example.ui.components.formatTimestamp

private data class FeatureCardModel(
    val screen: AppScreen,
    val titleHindi: String,
    val titleEnglish: String,
    val descriptionHindi: String,
    val badgeText: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTag: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    recentCreations: List<CreationEntity>,
    favoritesCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onOpenCreation: (CreationEntity) -> Unit
) {
    val features = listOf(
        FeatureCardModel(
            screen = AppScreen.SHAYARI,
            titleHindi = "AI शायरी स्टूडियो",
            titleEnglish = "AI Shayari Studio",
            descriptionHindi = "रोमांटिक, दर्द, एटीट्यूड, दोस्ती, प्रेरणा और भक्ति श्रेणियों में नाम व लंबाई के साथ हिंदी-हिंग्लिश शायरी बनाएँ।",
            badgeText = "नाम समर्पण • 2/4/8 पंक्तियाँ • वॉइस इनपुट",
            icon = Icons.Default.EditNote,
            accentColor = Color(0xFFB366FF),
            testTag = "feature_card_shayari"
        ),
        FeatureCardModel(
            screen = AppScreen.SONG,
            titleHindi = "AI सॉन्ग मेकर",
            titleEnglish = "AI Song Maker",
            descriptionHindi = "शीर्षक, मुखड़ा, अंतरा, कोरस, ब्रिज और अलग AI म्यूज़िक प्रॉम्प्ट के साथ पूरा मौलिक गाना लिखें।",
            badgeText = "वोकल स्टाइल • म्यूज़िक प्रॉम्प्ट • 3 भाषाएँ",
            icon = Icons.Default.MusicNote,
            accentColor = Color(0xFFFBBF24),
            testTag = "feature_card_song"
        ),
        FeatureCardModel(
            screen = AppScreen.LIFE_GUIDANCE,
            titleHindi = "भगवद गीता AI गाइड",
            titleEnglish = "Bhagavad Gita AI Guide",
            descriptionHindi = "जीवन की उलझनों में प्रमाणिक श्लोक संदर्भ, संस्कृत श्लोक, हिंदी अर्थ और व्यावहारिक समाधान पाएं।",
            badgeText = "प्रमाणिक संस्कृत श्लोक • हिंदी अर्थ",
            icon = Icons.Default.MenuBook,
            accentColor = Color(0xFFF59E0B),
            testTag = "feature_card_guidance"
        ),
        FeatureCardModel(
            screen = AppScreen.LYRICS_ANALYSIS,
            titleHindi = "AI भावना और लिरिक्स विश्लेषण",
            titleEnglish = "AI Emotion & Lyrics Analyzer",
            descriptionHindi = "अपने शब्दों के भावनात्मक स्वर की पहचान करें और उसके अनुरूप शायरी, गीत व लिरिक्स सुधार सुझाव पाएं।",
            badgeText = "इमोशन एनालाइज़र + लिरिक्स स्कोर",
            icon = Icons.Default.RateReview,
            accentColor = Color(0xFF34D399),
            testTag = "feature_card_analysis"
        ),
        FeatureCardModel(
            screen = AppScreen.POSTER_MAKER,
            titleHindi = "AI स्टेटस और पोस्टर मेकर",
            titleEnglish = "AI Status & Poster Maker",
            descriptionHindi = "शायरी और कोट्स को आकर्षक बैकग्राउंड, फ़ॉन्ट और सोशल मीडिया अनुपात (1:1, 9:16, 4:5) में पोस्टर बनाएँ।",
            badgeText = "Instagram • WhatsApp Status • शेयर",
            icon = Icons.Default.Palette,
            accentColor = Color(0xFF60A5FA),
            testTag = "feature_card_poster"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Welcome Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card"),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1793277934883),
                        contentDescription = "AI Song and Shayari Generator Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x6606040C),
                                        Color(0xCC06040C),
                                        Color(0xF806040C)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI क्रिएटिव स्टूडियो • वॉइस और पोस्टर मेकर सहित",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI Song & Shayari Generator",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "शायरी स्टूडियो, सॉन्ग मेकर, गीता गाइड, भावना विश्लेषण और स्टेटस पोस्टर मेकर — सब एक जगह।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 2. Quick Access Row for My Creations, Favorites, Poster Maker, and Settings
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onNavigate(AppScreen.HISTORY) },
                    label = { Text("मेरी रचनाएँ (${recentCreations.size})") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "My Creations",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("home_quick_history_btn")
                )

                AssistChip(
                    onClick = { onNavigate(AppScreen.FAVORITES) },
                    label = { Text("पसंदीदा ($favoritesCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFF43F5E)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("home_quick_favorites_btn")
                )

                AssistChip(
                    onClick = { onNavigate(AppScreen.POSTER_MAKER) },
                    label = { Text("पोस्टर मेकर") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Poster Maker",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("home_quick_poster_btn")
                )

                AssistChip(
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    label = { Text("सेटिंग्स (Settings)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("home_quick_settings_btn")
                )
            }
        }

        // 3. Section Header
        item {
            Text(
                text = "क्रिएटिव AI स्टूडियो टूल्स (Creative Studio)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 4. Feature Cards
        items(features) { feature ->
            FeatureDestinationCard(
                model = feature,
                onClick = { onNavigate(feature.screen) }
            )
        }

        // 5. Recent Creations Preview
        if (recentCreations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "हाल की रचनाएँ (Recent Creations)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { onNavigate(AppScreen.HISTORY) },
                        modifier = Modifier.testTag("view_all_history_btn")
                    ) {
                        Text("सभी देखें")
                    }
                }
            }

            items(recentCreations.take(3), key = { it.id }) { item ->
                val featureName = FeatureType.fromName(item.featureType).labelHindi

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCreation(item) }
                        .testTag("recent_creation_card_${item.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$featureName • ${item.categoryOrGenre}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = formatTimestamp(item.timestamp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.primaryContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureDestinationCard(
    model: FeatureCardModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag(model.testTag),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, model.accentColor.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            model.accentColor.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = model.accentColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, model.accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = model.icon,
                    contentDescription = model.titleEnglish,
                    tint = model.accentColor,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = model.accentColor.copy(alpha = 0.18f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = model.badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = model.accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = model.titleHindi,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = model.titleEnglish,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = model.descriptionHindi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open ${model.titleEnglish}",
                tint = model.accentColor
            )
        }
    }
}
