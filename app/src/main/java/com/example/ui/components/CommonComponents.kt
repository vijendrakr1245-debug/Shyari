package com.example.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.CreationEntity
import com.example.data.local.FeatureType
import com.example.security.getActiveAppCheckDebugToken
import com.example.security.saveCustomAppCheckDebugToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun copyTextToClipboard(
    context: Context,
    label: String,
    text: String,
    onCopied: () -> Unit
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    onCopied()
}

fun shareTextContent(context: Context, title: String, text: String) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (_: Exception) {
        // Ignored in headless test environments
    }
}

fun openYouTubeSearch(
    context: Context,
    query: String,
    onFeedback: (String) -> Unit
) {
    val cleanQuery = query.trim().ifBlank { "Best Hindi Shayari Mushaira" }
    val encoded = Uri.encode(cleanQuery)
    val url = "https://www.youtube.com/results?search_query=$encoded"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        onFeedback("YouTube पर खोज रहे हैं: $cleanQuery ▶️")
    } catch (_: Exception) {
        onFeedback("YouTube सर्च लिंक: $cleanQuery")
    }
}

fun openWebSearch(
    context: Context,
    query: String,
    onFeedback: (String) -> Unit
) {
    val cleanQuery = query.trim().ifBlank { "Hindi Shayari Rekhta" }
    val encoded = Uri.encode(cleanQuery)
    val url = "https://www.google.com/search?q=$encoded"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        onFeedback("इंटरनेट (Google) पर खोज रहे हैं: $cleanQuery 🌐")
    } catch (_: Exception) {
        onFeedback("इंटरनेट सर्च लिंक: $cleanQuery")
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Voice Input button using Android's SpeechRecognizer (RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
 * for Hindi (hi-IN) and Hinglish dictation into editable text fields.
 */
@Composable
fun VoiceInputButton(
    promptHint: String = "हिंदी या Hinglish में बोलें...",
    onTextRecognized: (String) -> Unit,
    onFeedback: (String) -> Unit,
    testTag: String = "voice_input_button"
) {
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()?.trim().orEmpty()
            if (spokenText.isNotEmpty()) {
                onTextRecognized(spokenText)
                onFeedback("वॉइस इनपुट जोड़ा गया—आप इसे जनरेट करने से पहले संपादित (Edit) कर सकते हैं। 🎙️")
            }
        }
    }

    IconButton(
        onClick = {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, promptHint)
                }
                speechLauncher.launch(intent)
            } catch (_: ActivityNotFoundException) {
                onFeedback("इस डिवाइस पर Google वॉइस टाइपिंग उपलब्ध नहीं है। कृपया कीबोर्ड के माइक या टेक्स्ट का उपयोग करें।")
            } catch (e: Exception) {
                onFeedback("वॉइस इनपुट शुरू करने में समस्या: ${e.localizedMessage ?: "अज्ञात"}")
            }
        },
        modifier = Modifier.testTag(testTag)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice Input (Hindi / Hinglish)",
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * Dedicated 'Save to Favorites' toggle bar for generated Shayari, Songs, and other content.
 */
@Composable
fun SaveToFavoritesToggleRow(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    tagPrefix: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleFavorite)
            .testTag("${tagPrefix}_save_to_favorites_row"),
        color = if (isFavorite) {
            Color(0xFFF43F5E).copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save to Favorites",
                    tint = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isFavorite) {
                            "पसंदीदा में सहेजा गया (Saved to Favorites ★)"
                        } else {
                            "पसंदीदा में सहेजें (Save to Favorites)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Room डेटाबेस में ऑफ़लाइन सुरक्षित रखें",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite() },
                modifier = Modifier.testTag("${tagPrefix}_save_to_favorites_toggle")
            )
        }
    }
}

/**
 * Displays the Second-AI Critic Verification & 'How to Shayari' (Qafiya-Radif) craft check,
 * plus an action button to re-verify and polish with the Second AI.
 */
@Composable
fun SecondAiVerificationCard(
    qafiyaRadifInfo: String,
    aiVerificationReport: String,
    qualityScore: Int,
    isVerifying: Boolean,
    onVerifyWithSecondAi: () -> Unit,
    tagPrefix: String = "shayari"
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_second_ai_verification_card"),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified by Second AI",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "दूसरे AI (उस्ताद शायर समीक्षक) की जाँच",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "शिल्प स्कोर: $qualityScore/100",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (qafiyaRadifInfo.isNotBlank()) {
                Text(
                    text = "🎼 शायरी का शिल्प (How to Shayari Check): $qafiyaRadifInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (aiVerificationReport.isNotBlank()) {
                Text(
                    text = "✅ $aiVerificationReport",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onVerifyWithSecondAi,
                enabled = !isVerifying,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${tagPrefix}_verify_second_ai_button")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("दूसरा AI शायरी को परख और निखार रहा है...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Verify and Polish with Second AI",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("दूसरे AI से परखें और और बेहतर बनाएँ (Verify with 2nd AI)")
                }
            }
        }
    }
}

/**
 * Interactive YouTube & Internet (Google) search links for exploring related Mushaira,
 * Shayari recitations, and Song references on the web.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YouTubeAndWebSearchCard(
    youtubeQuery: String,
    webQuery: String,
    context: Context,
    onFeedback: (String) -> Unit,
    tagPrefix: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_youtube_web_card"),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🌐 YouTube और इंटरनेट पर संबंधित शायरी / गीत खोजें:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openYouTubeSearch(context, youtubeQuery, onFeedback) },
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.testTag("${tagPrefix}_youtube_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Search on YouTube",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("YouTube पर मुशायरा / वीडियो खोजें")
                }

                OutlinedButton(
                    onClick = { openWebSearch(context, webQuery, onFeedback) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("${tagPrefix}_web_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Search on Internet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("इंटरनेट (Google) पर खोजें")
                }
            }
        }
    }
}

@Composable
fun SelectableChipRow(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    tagPrefix: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selectedOption
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionSelected(option) },
                    label = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.testTag("${tagPrefix}_chip_$index")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultActionButtonsRow(
    isFavorite: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    tagPrefix: String,
    onOpenInPoster: (() -> Unit)? = null
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCopy,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
            modifier = Modifier.testTag("${tagPrefix}_copy_button")
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy text",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("कॉपी करें (Copy)")
        }

        OutlinedButton(
            onClick = onRegenerate,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)),
            modifier = Modifier.testTag("${tagPrefix}_regenerate_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Regenerate",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("फिर से बनाएँ (Regenerate)")
        }

        OutlinedButton(
            onClick = onToggleFavorite,
            border = BorderStroke(
                1.dp,
                if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outline
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.testTag("${tagPrefix}_favorite_button")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isFavorite) "पसंदीदा ★" else "पसंदीदा (Favorite)")
        }

        if (onOpenInPoster != null) {
            OutlinedButton(
                onClick = onOpenInPoster,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.testTag("${tagPrefix}_poster_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Create Poster",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("पोस्टर बनाएँ")
            }
        }

        OutlinedButton(
            onClick = onShare,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.testTag("${tagPrefix}_share_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("शेयर")
        }
    }
}

@Composable
fun LoadingCreationCard(
    messageHindi: String,
    subtitleHindi: String = "Gemini AI आपकी रचना तैयार कर रहा है, कृपया प्रतीक्षा करें..."
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("loading_indicator_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = messageHindi,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitleHindi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorBannerCard(
    errorMessage: String,
    onRetry: () -> Unit,
    tagPrefix: String
) {
    val context = LocalContext.current
    val isAppCheckError = remember(errorMessage) {
        errorMessage.contains("App Check", ignoreCase = true) ||
            errorMessage.contains("token is invalid", ignoreCase = true) ||
            errorMessage.contains("403", ignoreCase = true)
    }
    var activeDebugToken by remember {
        mutableStateOf(if (isAppCheckError) getActiveAppCheckDebugToken(context) else "")
    }
    var customTokenInput by remember { mutableStateOf("") }
    var tokenStatusMsg by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("${tagPrefix}_error_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ध्यान दें",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            if (isAppCheckError && activeDebugToken.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔑 आपके फ़ोन का Firebase App Check Debug Token:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeDebugToken,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "समाधान: नीचे दिए बटन से यह टोकन कॉपी करें और Firebase Console → App Check → Apps → Manage debug tokens में जोड़ें, या वहाँ से बनाया गया टोकन नीचे पेस्ट करें:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                copyTextToClipboard(
                                    context = context,
                                    label = "App Check Debug Token",
                                    text = activeDebugToken,
                                    onCopied = {
                                        tokenStatusMsg = "डिबग टोकन कॉपी हो गया! इसे Firebase Console → App Check → Manage debug tokens में पेस्ट करें।"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("${tagPrefix}_copy_debug_token_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Debug Token",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("डिबग टोकन कॉपी करें (Copy Debug Token)")
                        }

                        OutlinedTextField(
                            value = customTokenInput,
                            onValueChange = { customTokenInput = it },
                            placeholder = { Text("या Firebase Console का Debug Token यहाँ पेस्ट करें") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("${tagPrefix}_custom_debug_token_input")
                        )
                        if (customTokenInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    if (saveCustomAppCheckDebugToken(context, customTokenInput)) {
                                        activeDebugToken = customTokenInput.trim()
                                        customTokenInput = ""
                                        tokenStatusMsg = "नया Debug Token सहेजा गया! अब 'पुनः प्रयास करें' दबाएँ।"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("यह टोकन सहेजें (Save Debug Token)")
                            }
                        }
                        if (tokenStatusMsg.isNotBlank()) {
                            Text(
                                text = tokenStatusMsg,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("${tagPrefix}_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("पुनः प्रयास करें (Retry)")
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    testTag: String = "empty_state_card"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.testTag("${testTag}_action_btn")
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun CreationDetailDialog(
    creation: CreationEntity,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String, String) -> Unit,
    onToggleFavorite: (CreationEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenInPoster: ((CreationEntity) -> Unit)? = null
) {
    val featureLabel = FeatureType.fromName(creation.featureType).labelHindi

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = featureLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = creation.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${creation.categoryOrGenre} • ${formatTimestamp(creation.timestamp)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = creation.primaryContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (creation.secondaryContent.isNotBlank() &&
                    !creation.primaryContent.contains(creation.secondaryContent)
                ) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "संशोधित / अतिरिक्त विवरण:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = creation.secondaryContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onOpenInPoster != null) {
                    IconButton(
                        onClick = { onOpenInPoster(creation) },
                        modifier = Modifier.testTag("detail_dialog_poster_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Open in Poster Maker",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                IconButton(
                    onClick = { onToggleFavorite(creation) },
                    modifier = Modifier.testTag("detail_dialog_favorite_btn")
                ) {
                    Icon(
                        imageVector = if (creation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (creation.isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        val fullText = if (creation.secondaryContent.isNotBlank() &&
                            !creation.primaryContent.contains(creation.secondaryContent)
                        ) {
                            "${creation.primaryContent}\n\n${creation.secondaryContent}"
                        } else {
                            creation.primaryContent
                        }
                        onCopy(fullText)
                    },
                    modifier = Modifier.testTag("detail_dialog_copy_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        onShare(creation.title, creation.primaryContent)
                    },
                    modifier = Modifier.testTag("detail_dialog_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(
                    onClick = { onDelete(creation.id) },
                    modifier = Modifier.testTag("detail_dialog_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("detail_dialog_close_btn")
                ) {
                    Text("बंद करें")
                }
            }
        }
    )
}
