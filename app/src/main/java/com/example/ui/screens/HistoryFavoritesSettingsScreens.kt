package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CreationEntity
import com.example.data.local.FeatureType
import com.example.data.repository.UserSettings
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SelectableChipRow
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.formatTimestamp
import com.example.ui.components.shareTextContent

@Composable
fun HistoryScreen(
    creations: List<CreationEntity>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onOpenCreation: (CreationEntity) -> Unit,
    onToggleFavorite: (CreationEntity) -> Unit,
    onDeleteCreation: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenInPoster: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        "ALL" to "सभी (All)",
        "FAVORITES" to "★ पसंदीदा",
        FeatureType.SHAYARI.name to "शायरी",
        FeatureType.SONG.name to "सॉन्ग",
        FeatureType.LIFE_GUIDANCE.name to "गीता गाइड",
        FeatureType.EMOTION_ANALYZER.name to "भावना विश्लेषण",
        FeatureType.LYRICS_ANALYSIS.name to "लिरिक्स विश्लेषण",
        FeatureType.POSTER.name to "पोस्टर"
    )

    val filteredList = remember(creations, selectedFilter, searchQuery) {
        creations.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "FAVORITES" -> item.isFavorite
                else -> item.featureType == selectedFilter
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.primaryContent.lowercase().contains(q) ||
                item.categoryOrGenre.lowercase().contains(q)
            matchesFilter && matchesSearch
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("क्या आप मेरी रचनाओं का पूरा इतिहास मिटाना चाहते हैं?") },
            text = {
                Text("यह क्रिया आपके फ़ोन में सहेजे गए सभी जनरेट किए गए परिणामों को स्थायी रूप से हटा देगी।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_clear_history_btn")
                ) {
                    Text("हाँ, सब मिटाएँ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_clear_history_btn")
                ) {
                    Text("रद्द करें")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Local storage notice card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.local_storage_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search and Clear History Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("मेरी रचनाओं में खोजें...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input")
                )

                if (creations.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("मिटाएँ")
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { onFilterSelected(key) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("history_filter_$key")
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.History,
                    title = "अभी कोई रचना उपलब्ध नहीं है",
                    subtitle = "जब आप शायरी, गाना, गीता गाइड, भावना विश्लेषण या पोस्टर बनाएंगे, तो वे अपने आप यहाँ सहेजे जाएंगे।",
                    actionLabel = "होम स्क्रीन पर जाएँ",
                    onActionClick = onNavigateHome,
                    testTag = "history_empty_state"
                )
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                SavedCreationItemCard(
                    creation = item,
                    onOpen = { onOpenCreation(item) },
                    onCopy = {
                        copyTextToClipboard(
                            context = context,
                            label = item.title,
                            text = item.primaryContent,
                            onCopied = { onFeedback("कॉपी हो गया! 📋") }
                        )
                    },
                    onShare = {
                        shareTextContent(context, item.title, item.primaryContent)
                    },
                    onToggleFavorite = { onToggleFavorite(item) },
                    onDelete = { onDeleteCreation(item.id) },
                    onOpenInPoster = { onOpenInPoster(item.primaryContent) },
                    tagPrefix = "history_item_${item.id}"
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<CreationEntity>,
    onOpenCreation: (CreationEntity) -> Unit,
    onToggleFavorite: (CreationEntity) -> Unit,
    onDeleteCreation: (Long) -> Unit,
    onNavigateHome: () -> Unit,
    onOpenInPoster: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "आपकी पसंदीदा रचनाएँ (${favorites.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (favorites.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.FavoriteBorder,
                    title = "पसंदीदा सूची खाली है",
                    subtitle = "किसी भी शायरी, गाने या मार्गदर्शन परिणाम पर 'पसंदीदा (Favorite)' बटन दबाकर उसे यहाँ सहेजें।",
                    actionLabel = "नई रचना बनाएँ",
                    onActionClick = onNavigateHome,
                    testTag = "favorites_empty_state"
                )
            }
        } else {
            items(favorites, key = { it.id }) { item ->
                SavedCreationItemCard(
                    creation = item,
                    onOpen = { onOpenCreation(item) },
                    onCopy = {
                        copyTextToClipboard(
                            context = context,
                            label = item.title,
                            text = item.primaryContent,
                            onCopied = { onFeedback("पसंदीदा रचना कॉपी हो गई! ❤️") }
                        )
                    },
                    onShare = {
                        shareTextContent(context, item.title, item.primaryContent)
                    },
                    onToggleFavorite = { onToggleFavorite(item) },
                    onDelete = { onDeleteCreation(item.id) },
                    onOpenInPoster = { onOpenInPoster(item.primaryContent) },
                    tagPrefix = "favorite_item_${item.id}"
                )
            }
        }
    }
}

@Composable
private fun SavedCreationItemCard(
    creation: CreationEntity,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenInPoster: () -> Unit,
    tagPrefix: String
) {
    val featureLabel = FeatureType.fromName(creation.featureType).labelHindi

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag(tagPrefix),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "$featureLabel • ${creation.categoryOrGenre}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = formatTimestamp(creation.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = creation.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = creation.primaryContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenInPoster,
                    modifier = Modifier.testTag("${tagPrefix}_poster_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Create Poster",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.testTag("${tagPrefix}_copy_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("${tagPrefix}_fav_btn")
                ) {
                    Icon(
                        imageVector = if (creation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (creation.isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("${tagPrefix}_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("${tagPrefix}_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    historyCount: Int,
    favoritesCount: Int,
    sessionGenerationCount: Int,
    onUpdateDefaultLanguage: (String) -> Unit,
    onUpdateFontScale: (Float) -> Unit,
    onUpdateAutoSave: (Boolean) -> Unit,
    onUpdateDarkTheme: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("सहेजा गया डेटा मिटाएँ?") },
            text = { Text("क्या आप वाकई सभी इतिहास और पसंदीदा आइटम हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        onClearAllHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("settings_confirm_clear_btn")
                ) {
                    Text("हाँ, मिटाएँ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "थीम, पठन और भाषा सेटिंग्स (Preferences)",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Dark / Light Theme Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "प्रीमियम डार्क थीम (Dark / Light Theme)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (userSettings.isDarkTheme) "ब्लैक, पर्पल और गोल्ड डार्क थीम सक्रिय है" else "लाइट थीम सक्रिय है",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings.isDarkTheme,
                            onCheckedChange = onUpdateDarkTheme,
                            modifier = Modifier.testTag("settings_dark_theme_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    SelectableChipRow(
                        label = "डिफ़ॉल्ट भाषा (Default Language):",
                        options = listOf("Hindi (हिंदी)", "Hinglish (हिंग्लिश)", "English (अंग्रेज़ी)"),
                        selectedOption = userSettings.defaultLanguage,
                        onOptionSelected = onUpdateDefaultLanguage,
                        tagPrefix = "settings_lang"
                    )

                    val fontOptions = listOf(
                        "सामान्य (Normal)" to 1.0f,
                        "बड़ा (Large)" to 1.12f,
                        "बहुत बड़ा (Extra Large)" to 1.24f
                    )
                    val currentFontLabel = fontOptions.firstOrNull {
                        kotlin.math.abs(it.second - userSettings.fontScale) < 0.05f
                    }?.first ?: "सामान्य (Normal)"

                    SelectableChipRow(
                        label = "अक्षरों का आकार (Font Size):",
                        options = fontOptions.map { it.first },
                        selectedOption = currentFontLabel,
                        onOptionSelected = { label ->
                            val scale = fontOptions.firstOrNull { it.first == label }?.second ?: 1.0f
                            onUpdateFontScale(scale)
                        },
                        tagPrefix = "settings_font"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "मेरी रचनाओं में स्वतः सहेजें (Auto-Save)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "नई जनरेट की गई शायरी और गीतों को अपने आप लोकल हिस्ट्री में सेव करें",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings.autoSaveHistory,
                            onCheckedChange = onUpdateAutoSave,
                            modifier = Modifier.testTag("settings_autosave_switch")
                        )
                    }
                }
            }
        }

        // Local Storage & Data Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "लोकल स्टोरेज और इतिहास नियंत्रण",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "सहेजे गए कुल आइटम: $historyCount  •  पसंदीदा (Favorites): $favoritesCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Text(
                        text = stringResource(id = R.string.local_storage_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        enabled = historyCount > 0,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("settings_clear_history_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("पूरा लोकल इतिहास मिटाएँ (Clear All History)")
                    }
                }
            }
        }

        // Privacy, Usage Limits & AI Security Card
        item {
            val context = LocalContext.current
            var activeDebugToken by remember {
                mutableStateOf(com.example.security.getActiveAppCheckDebugToken(context))
            }
            var customTokenInput by remember { mutableStateOf("") }
            var tokenFeedback by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "गोपनीयता, उपयोग सीमा और AI सुरक्षा (Privacy & Usage)",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "📊 सत्र उपयोग सीमा (Session Usage): $sessionGenerationCount / ${MainViewModel.DAILY_GENERATION_LIMIT} जनरेशन",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "🔒 गोपनीयता जानकारी (Privacy Info): आपकी सहेजी गई रचनाएँ और पसंदीदा आइटम केवल आपके फ़ोन के लोकल Room SQLite डेटाबेस में रहते हैं। AI जनरेशन के लिए केवल आपका प्रॉम्प्ट सुरक्षित Firebase AI Logic (Gemini API) को भेजा जाता है। कोई भी API कुंजी APK में हार्डकोड नहीं की गई है।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (activeDebugToken.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Text(
                            text = "🛡️ Firebase App Check डिबग टोकन (फ़ोन टेस्टिंग के लिए):",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Package ID: ${context.packageName}\nDebug Token: $activeDebugToken",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedButton(
                            onClick = {
                                copyTextToClipboard(
                                    context = context,
                                    label = "Firebase App Check Debug Token",
                                    text = activeDebugToken,
                                    onCopied = {
                                        tokenFeedback = "डिबग टोकन कॉपी हो गया! इसे Firebase Console → App Check → Apps → Manage debug tokens में जोड़ें।"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_copy_debug_token_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("App Check Debug Token कॉपी करें")
                        }

                        OutlinedTextField(
                            value = customTokenInput,
                            onValueChange = { customTokenInput = it },
                            placeholder = { Text("या Firebase Console से बनाया गया टोकन पेस्ट करें") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_custom_debug_token_input")
                        )
                        if (customTokenInput.isNotBlank()) {
                            Button(
                                onClick = {
                                    if (com.example.security.saveCustomAppCheckDebugToken(context, customTokenInput)) {
                                        activeDebugToken = customTokenInput.trim()
                                        customTokenInput = ""
                                        tokenFeedback = "नया App Check Debug Token सफलतापूर्वक सहेजा गया!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("नया Debug Token सहेजें (Save Token)")
                            }
                        }
                        if (tokenFeedback.isNotBlank()) {
                            Text(
                                text = tokenFeedback,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }
        }
    }
}
