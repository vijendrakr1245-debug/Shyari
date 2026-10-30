package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SelectableChipRow
import com.example.ui.components.VoiceInputButton
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareTextContent
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PoppinsFontFamily
import java.io.File
import java.io.FileOutputStream

data class PosterBackgroundStyle(
    val name: String,
    val startColor: Color,
    val endColor: Color,
    val accentColor: Color
)

data class PosterAspectRatioOption(
    val label: String,
    val ratio: Float,
    val bitmapWidth: Int,
    val bitmapHeight: Int
)

val PosterBackgroundOptions = listOf(
    PosterBackgroundStyle(
        name = "Royal Purple & Gold (शाही पर्पल)",
        startColor = Color(0xFF0A0514),
        endColor = Color(0xFF34135E),
        accentColor = Color(0xFFFBBF24)
    ),
    PosterBackgroundStyle(
        name = "Obsidian Gold (ब्लैक गोल्ड)",
        startColor = Color(0xFF050508),
        endColor = Color(0xFF1D192B),
        accentColor = Color(0xFFF59E0B)
    ),
    PosterBackgroundStyle(
        name = "Crimson Velvet (मखमली सुर्ख)",
        startColor = Color(0xFF1A050D),
        endColor = Color(0xFF540D22),
        accentColor = Color(0xFFFDE68A)
    ),
    PosterBackgroundStyle(
        name = "Sufi Emerald (सूफ़ी पन्ना)",
        startColor = Color(0xFF041712),
        endColor = Color(0xFF064E3B),
        accentColor = Color(0xFFFCD34D)
    ),
    PosterBackgroundStyle(
        name = "Cosmic Indigo (नीला आकाश)",
        startColor = Color(0xFF060E24),
        endColor = Color(0xFF1E3A8A),
        accentColor = Color(0xFF93C5FD)
    )
)

val PosterAspectRatios = listOf(
    PosterAspectRatioOption("1:1 Square (Instagram)", 1f, 1080, 1080),
    PosterAspectRatioOption("4:5 Portrait (Feed)", 0.8f, 1080, 1350),
    PosterAspectRatioOption("9:16 Status (WhatsApp/Reels)", 0.65f, 1080, 1920),
    PosterAspectRatioOption("16:9 Banner (YouTube)", 1.6f, 1600, 900)
)

val PosterTypographyOptions = listOf(
    "Royal Serif (शाही सेरिफ़)",
    "Modern Clean (आधुनिक)",
    "Bold Impact (प्रभावशाली)",
    "Poetic Italic (काव्यात्मक)"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PosterMakerScreen(
    posterText: String,
    onPosterTextChanged: (String) -> Unit,
    posterAuthor: String,
    onPosterAuthorChanged: (String) -> Unit,
    selectedBackgroundName: String,
    onBackgroundSelected: (String) -> Unit,
    selectedTypography: String,
    onTypographySelected: (String) -> Unit,
    selectedAspectRatioLabel: String,
    onAspectRatioSelected: (String) -> Unit,
    posterFontSizeSp: Float,
    onFontSizeChanged: (Float) -> Unit,
    onSavePosterToCreations: () -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val currentBg = PosterBackgroundOptions.firstOrNull { it.name == selectedBackgroundName }
        ?: PosterBackgroundOptions.first()
    val currentRatio = PosterAspectRatios.firstOrNull { it.label == selectedAspectRatioLabel }
        ?: PosterAspectRatios.first()

    val chosenFontFamily: FontFamily = when {
        selectedTypography.startsWith("Royal Serif") -> PlayfairDisplayFontFamily
        else -> PoppinsFontFamily
    }
    val chosenFontWeight: FontWeight = when {
        selectedTypography.startsWith("Bold") -> FontWeight.Bold
        selectedTypography.startsWith("Royal") -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }
    val chosenFontStyle: FontStyle = if (selectedTypography.startsWith("Poetic")) {
        FontStyle.Italic
    } else {
        FontStyle.Normal
    }

    val sampleQuotes = listOf(
        "खामोश निगाहों में हज़ारों सवाल हैं,\nतेरे बिना ये लम्हे भी बेहाल हैं।",
        "मंज़िलें उन्हीं को मिलती हैं,\nजिनके सपनों में जान होती है।",
        "कर्म पर विश्वास रखो,\nवक्त खुद तुम्हारी कहानी लिखेगा।"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("poster_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Poster Canvas Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI स्टेटस और पोस्टर मेकर",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = currentRatio.label.substringBefore(" "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Poster Canvas Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (currentRatio.ratio < 0.85f) 0.82f else 1f)
                            .aspectRatio(currentRatio.ratio)
                            .heightIn(min = 200.dp, max = 380.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(currentBg.startColor, currentBg.endColor)
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = currentBg.accentColor.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(24.dp)
                            .testTag("poster_preview_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✦  ❈  ✦",
                                color = currentBg.accentColor,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = posterText.ifBlank {
                                    "यहाँ अपनी शायरी या स्टेटस लिखें...\nआपका पोस्टर लाइव तैयार हो जाएगा।"
                                },
                                color = Color.White,
                                fontFamily = chosenFontFamily,
                                fontWeight = chosenFontWeight,
                                fontStyle = chosenFontStyle,
                                fontSize = posterFontSizeSp.sp,
                                lineHeight = (posterFontSizeSp * 1.45f).sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("poster_canvas_quote_text")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (posterAuthor.isNotBlank()) {
                                Text(
                                    text = "— $posterAuthor",
                                    color = currentBg.accentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSavePosterToCreations,
                            modifier = Modifier.testTag("save_poster_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdded,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("मेरी रचनाओं में सहेजें")
                        }

                        OutlinedButton(
                            onClick = {
                                exportAndSharePoster(
                                    context = context,
                                    quoteText = posterText.ifBlank { "AI Song & Shayari Generator" },
                                    authorText = posterAuthor,
                                    bgStyle = currentBg,
                                    ratioOption = currentRatio,
                                    onDone = { msg -> onFeedback(msg) }
                                )
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.testTag("share_poster_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("पोस्टर शेयर करें")
                        }

                        OutlinedButton(
                            onClick = {
                                val fullCaption = buildString {
                                    append(posterText.ifBlank { "AI Song & Shayari Studio" })
                                    if (posterAuthor.isNotBlank()) {
                                        append("\n— $posterAuthor")
                                    }
                                }
                                copyTextToClipboard(
                                    context = context,
                                    label = "Poster Shayari",
                                    text = fullCaption,
                                    onCopied = { onFeedback("पोस्टर टेक्स्ट कॉपी हो गया! 📋") }
                                )
                            },
                            modifier = Modifier.testTag("copy_poster_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("टेक्स्ट कॉपी करें")
                        }
                    }
                }
            }
        }

        // 2. Poster Controls & Customization Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. शायरी / कोट टेक्स्ट (Editable Text + Voice):",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        VoiceInputButton(
                            promptHint = "पोस्टर के लिए शायरी या कोट बोलें...",
                            onTextRecognized = { spoken ->
                                val updated = if (posterText.isBlank()) spoken else "$posterText\n$spoken"
                                onPosterTextChanged(updated)
                            },
                            onFeedback = onFeedback,
                            testTag = "poster_voice_input_btn"
                        )
                    }

                    OutlinedTextField(
                        value = posterText,
                        onValueChange = onPosterTextChanged,
                        placeholder = { Text("यहाँ अपनी शायरी या स्टेटस लिखें...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("poster_text_input"),
                        minLines = 3,
                        maxLines = 6
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleQuotes.forEachIndexed { idx, sample ->
                            AssistChip(
                                onClick = { onPosterTextChanged(sample) },
                                label = { Text("उदाहरण ${idx + 1}") },
                                modifier = Modifier.testTag("poster_sample_quote_$idx")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = posterAuthor,
                        onValueChange = onPosterAuthorChanged,
                        label = { Text("हस्ताक्षर / नाम (Author Signature - वैकल्पिक)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("poster_author_input")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    SelectableChipRow(
                        label = "2. सोशल मीडिया आकार (Aspect Ratio):",
                        options = PosterAspectRatios.map { it.label },
                        selectedOption = selectedAspectRatioLabel,
                        onOptionSelected = onAspectRatioSelected,
                        tagPrefix = "poster_ratio"
                    )

                    SelectableChipRow(
                        label = "3. बैकग्राउंड थीम (Background Style):",
                        options = PosterBackgroundOptions.map { it.name },
                        selectedOption = selectedBackgroundName,
                        onOptionSelected = onBackgroundSelected,
                        tagPrefix = "poster_bg"
                    )

                    SelectableChipRow(
                        label = "4. फ़ॉन्ट शैली (Typography):",
                        options = PosterTypographyOptions,
                        selectedOption = selectedTypography,
                        onOptionSelected = onTypographySelected,
                        tagPrefix = "poster_typo"
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "5. अक्षर आकार (Text Size): ${posterFontSizeSp.toInt()} sp",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = posterFontSizeSp,
                            onValueChange = onFontSizeChanged,
                            valueRange = 14f..26f,
                            modifier = Modifier.testTag("poster_font_size_slider")
                        )
                    }
                }
            }
        }
    }
}

private fun exportAndSharePoster(
    context: Context,
    quoteText: String,
    authorText: String,
    bgStyle: PosterBackgroundStyle,
    ratioOption: PosterAspectRatioOption,
    onDone: (String) -> Unit
) {
    try {
        val width = ratioOption.bitmapWidth
        val height = ratioOption.bitmapHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Draw gradient background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                bgStyle.startColor.toArgb(),
                bgStyle.endColor.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw golden border frame
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = bgStyle.accentColor.toArgb()
        }
        val margin = 48f
        canvas.drawRoundRect(
            RectF(margin, margin, width - margin, height - margin),
            36f,
            36f,
            borderPaint
        )

        // Draw centered text
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val fullText = if (authorText.isNotBlank()) {
            "$quoteText\n\n— $authorText"
        } else {
            quoteText
        }
        val textWidth = (width - 180).coerceAtLeast(200)
        val staticLayout = StaticLayout.Builder
            .obtain(fullText, 0, fullText.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(12f, 1.25f)
            .build()

        canvas.save()
        val dy = ((height - staticLayout.height) / 2f).coerceAtLeast(80f)
        canvas.translate(90f, dy)
        staticLayout.draw(canvas)
        canvas.restore()

        val outFile = File(context.cacheDir, "kavya_poster_${System.currentTimeMillis()}.png")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
        }

        shareTextContent(
            context = context,
            title = "AI Shayari Poster",
            text = fullText
        )
        onDone("पोस्टर तैयार होकर शेयर के लिए खुल गया है! 🖼️")
    } catch (_: Exception) {
        shareTextContent(
            context = context,
            title = "AI Shayari Poster",
            text = quoteText
        )
        onDone("पोस्टर टेक्स्ट शेयर के लिए तैयार है!")
    }
}
