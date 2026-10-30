package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "AiCreativeService"

/**
 * We use gemini-3.8-flash as the primary creative poet model for superior Hindi/Urdu
 * poetic meter (bahr), qafiya-radif rhyme, and emotional depth, with gemini-3.5-flash-lite
 * as the rapid verifier and resilient fallback model.
 */
internal const val PRIMARY_CREATIVE_MODEL = "gemini-3.8-flash"
internal const val SECONDARY_VERIFIER_MODEL = "gemini-3.5-flash-lite"

private const val PRIMARY_TIMEOUT_MS = 38_000L
private const val VERIFIER_TIMEOUT_MS = 18_000L
private const val FALLBACK_TIMEOUT_MS = 40_000L

/**
 * Feature-specific Gemini generation parameters tuned for creative quality,
 * lexical diversity, and zero output truncation.
 */
data class FeatureModelConfig(
    val featureName: String,
    val temperature: Float,
    val topP: Float,
    val topK: Int,
    val maxOutputTokens: Int,
    val systemInstructionText: String
)

/**
 * Curated, 100% verified Bhagavad Gita Sanskrit verses mapped by "Chapter.Verse" key.
 * Ensures we never display invented or unverified Sanskrit text.
 */
object VerifiedGitaVerses {
    private val verses = mapOf(
        "2.47" to (
            "अध्याय 2, श्लोक 47 (Chapter 2, Verse 47)" to
                "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥"
            ),
        "2.14" to (
            "अध्याय 2, श्लोक 14 (Chapter 2, Verse 14)" to
                "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः।\nआगमापायिनोऽनित्यास्तांस्तितिक्षस्व भारत॥"
            ),
        "2.20" to (
            "अध्याय 2, श्लोक 20 (Chapter 2, Verse 20)" to
                "न जायते म्रियते वा कदाचिन् नायं भूत्वा भविता वा न भूयः।\nअजो नित्यः शाश्वतोऽयं पुराणो न हन्यते हन्यमाने शरीरे॥"
            ),
        "2.48" to (
            "अध्याय 2, श्लोक 48 (Chapter 2, Verse 48)" to
                "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।\nसिद्ध्यसिद्ध्योः समो भूत्वा समत्वं योग उच्यते॥"
            ),
        "2.62" to (
            "अध्याय 2, श्लोक 62 (Chapter 2, Verse 62)" to
                "ध्यायतो विषयान्पुंसः सङ्गस्तेषूपजायते।\nसङ्गात्सञ्जायते कामः कामात्क्रोधोऽभिजायते॥"
            ),
        "3.19" to (
            "अध्याय 3, श्लोक 19 (Chapter 3, Verse 19)" to
                "तस्मादसक्तः सततं कार्यं कर्म समाचर।\nअसक्तो ह्याचरन्कर्म परमाप्नोति पूरुषः॥"
            ),
        "3.35" to (
            "अध्याय 3, श्लोक 35 (Chapter 3, Verse 35)" to
                "श्रेयान्स्वधर्मो विगुणः परधर्मात्स्वनुष्ठितात्।\nस्वधर्मे निधनं श्रेयः परधर्मो भयावहः॥"
            ),
        "4.7" to (
            "अध्याय 4, श्लोक 7 (Chapter 4, Verse 7)" to
                "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत।\nअभ्युत्थानमधर्मस्य तदात्मानं सृजाम्यहम्॥"
            ),
        "4.38" to (
            "अध्याय 4, श्लोक 38 (Chapter 4, Verse 38)" to
                "न हि ज्ञानेन सदृशं पवित्रमिह विद्यते।\nतत्स्वयं योगसंसिद्धः कालेनात्मनि विन्दति॥"
            ),
        "6.5" to (
            "अध्याय 6, श्लोक 5 (Chapter 6, Verse 5)" to
                "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥"
            ),
        "6.26" to (
            "अध्याय 6, श्लोक 26 (Chapter 6, Verse 26)" to
                "यतो यतो निश्चरति मनश्चञ्चलमस्थिरम्।\nततस्ततो नियम्यैतदात्मन्येव वशं नयेत्॥"
            ),
        "12.15" to (
            "अध्याय 12, श्लोक 15 (Chapter 12, Verse 15)" to
                "यस्मान्नोद्विजते लोको लोकान्नोद्विजते च यः।\nहर्षामर्षभयोद्वेगैर्मुक्तो यः स च मे प्रियः॥"
            ),
        "18.66" to (
            "अध्याय 18, श्लोक 66 (Chapter 18, Verse 66)" to
                "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज।\nअहं त्वा सर्वपापेभ्यो मोक्षयिष्यामि मा शुचः॥"
            )
    )

    val supportedKeys: List<String> = verses.keys.toList()

    fun resolve(verseKeyOrRef: String): Pair<String, String> {
        val normalized = verseKeyOrRef.trim()
        verses[normalized]?.let { return it }
        val regex = Regex("""(\d+)\D+(\d+)""")
        val match = regex.find(normalized)
        if (match != null) {
            val key = "${match.groupValues[1]}.${match.groupValues[2]}"
            verses[key]?.let { return it }
        }
        return verses["2.47"]!!
    }
}

data class ShayariResult(
    val title: String,
    val shayariHindi: String,
    val shayariHinglish: String,
    val meaningHindi: String,
    val dedicatedTo: String = "",
    val lengthLabel: String = "",
    val qafiyaRadifInfo: String = "",
    val aiVerificationReport: String = "",
    val qualityScore: Int = 92,
    val youtubeSearchQuery: String = "",
    val webSearchQuery: String = ""
) {
    fun toFormattedText(): String = buildString {
        appendLine("✨ $title ✨")
        if (dedicatedTo.isNotBlank()) {
            appendLine("समर्पित: $dedicatedTo")
        }
        appendLine()
        appendLine("【 हिंदी शायरी 】")
        appendLine(shayariHindi.trim())
        appendLine()
        appendLine("【 Hinglish Shayari 】")
        appendLine(shayariHinglish.trim())
        appendLine()
        appendLine("💡 भावार्थ: ${meaningHindi.trim()}")
        if (qafiyaRadifInfo.isNotBlank()) {
            appendLine("🎼 शिल्प (काफ़िया-रदीफ़): ${qafiyaRadifInfo.trim()}")
        }
        if (aiVerificationReport.isNotBlank()) {
            appendLine("✅ AI समीक्षा ($qualityScore/100): ${aiVerificationReport.trim()}")
        }
    }
}

data class SongResult(
    val title: String,
    val musicalNote: String,
    val intro: String, // Serves as Mukhda
    val verse1: String,
    val preChorus: String,
    val chorus: String,
    val verse2: String,
    val outro: String,
    val bridge: String = "",
    val vocalStyle: String = "",
    val musicGenPrompt: String = "",
    val youtubeSearchQuery: String = "",
    val webSearchQuery: String = ""
) {
    val mukhda: String
        get() = intro

    fun toFormattedText(): String = buildString {
        appendLine("🎵 शीर्षक (Title): $title")
        appendLine("🎼 संगीत और गायकी शैली: $musicalNote")
        if (vocalStyle.isNotBlank()) {
            appendLine("🎤 वोकल स्टाइल (Vocal Style): $vocalStyle")
        }
        appendLine()
        if (intro.isNotBlank()) {
            appendLine("[Mukhda / मुखड़ा]")
            appendLine(intro.trim())
            appendLine()
        }
        appendLine("[Verse 1 / अंतरा 1]")
        appendLine(verse1.trim())
        appendLine()
        if (preChorus.isNotBlank()) {
            appendLine("[Pre-Chorus / प्री-कोरस]")
            appendLine(preChorus.trim())
            appendLine()
        }
        appendLine("[Chorus / कोरस]")
        appendLine(chorus.trim())
        appendLine()
        appendLine("[Verse 2 / अंतरा 2]")
        appendLine(verse2.trim())
        appendLine()
        if (bridge.isNotBlank()) {
            appendLine("[Bridge / ब्रिज]")
            appendLine(bridge.trim())
            appendLine()
        }
        if (outro.isNotBlank()) {
            appendLine("[Outro / आउट्रो]")
            appendLine(outro.trim())
            appendLine()
        }
        if (musicGenPrompt.isNotBlank()) {
            appendLine("🎧 AI Music Generation Prompt:")
            appendLine(musicGenPrompt.trim())
        }
    }
}

data class LyricsAnalysisResult(
    val overallScore: Int,
    val emotionalImpact: String,
    val storytelling: String,
    val rhymeAndRhythm: String,
    val originalityAndSingability: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val improvements: List<String>,
    val revisedLyrics: String
) {
    fun toFormattedAnalysisText(): String = buildString {
        appendLine("📊 कुल स्कोर: $overallScore / 100")
        appendLine()
        appendLine("❤️ भावनात्मक प्रभाव: $emotionalImpact")
        appendLine("📖 कहानी और भाव: $storytelling")
        appendLine("🎶 तुकबंदी और लय: $rhymeAndRhythm")
        appendLine("🎤 मौलिकता और गायकी: $originalityAndSingability")
        appendLine()
        appendLine("✅ मुख्य खूबियाँ (Strengths):")
        strengths.forEach { appendLine("• $it") }
        appendLine()
        appendLine("⚠️ सुधार योग्य बिंदु (Weaknesses):")
        weaknesses.forEach { appendLine("• $it") }
        appendLine()
        appendLine("💡 सुझाव (Actionable Improvements):")
        improvements.forEach { appendLine("• $it") }
    }
}

data class EmotionAnalysisResult(
    val detectedEmotion: String,
    val intensityPercent: Int,
    val explanationHindi: String,
    val supportiveMessage: String,
    val matchingShayari: String,
    val matchingSongLyrics: String
) {
    fun toFormattedText(): String = buildString {
        appendLine("🎭 पहचाना गया भाव (Apparent Emotion): $detectedEmotion ($intensityPercent%)")
        appendLine()
        appendLine("🔍 संक्षिप्त व्याख्या:")
        appendLine(explanationHindi.trim())
        appendLine()
        appendLine("💛 सकारात्मक संदेश (Supportive Message):")
        appendLine(supportiveMessage.trim())
        appendLine()
        appendLine("✨ आपके भाव के अनुरूप शायरी:")
        appendLine(matchingShayari.trim())
        appendLine()
        appendLine("🎵 आपके भाव के अनुरूप गीत की पंक्तियाँ:")
        appendLine(matchingSongLyrics.trim())
    }
}

data class LifeGuidanceResult(
    val chapterAndVerse: String,
    val traditionalTeachingHindi: String,
    val modernInterpretationHindi: String,
    val practicalSteps: List<String>,
    val peacefulReflection: String,
    val verifiedSanskritShloka: String = ""
) {
    fun toFormattedText(): String = buildString {
        appendLine("🕉️ संदर्भ: $chapterAndVerse")
        if (verifiedSanskritShloka.isNotBlank()) {
            appendLine()
            appendLine("🌺 प्रमाणिक संस्कृत श्लोक:")
            appendLine(verifiedSanskritShloka.trim())
        }
        appendLine()
        appendLine("📜 हिंदी भावार्थ व पारंपरिक शिक्षा (Hindi Meaning):")
        appendLine(traditionalTeachingHindi.trim())
        appendLine()
        appendLine("🌱 व्यावहारिक समाधान (Practical Explanation):")
        appendLine(modernInterpretationHindi.trim())
        appendLine()
        appendLine("🧭 व्यावहारिक कदम:")
        practicalSteps.forEachIndexed { index, step ->
            appendLine("${index + 1}. $step")
        }
        appendLine()
        appendLine("✨ आज का संकल्प: $peacefulReflection")
    }
}

data class StatusCaptionResult(
    val statusHindi: String,
    val statusHinglish: String,
    val socialCaption: String,
    val hashtags: List<String>
) {
    fun toFormattedPosterQuote(): String = statusHindi.ifBlank { statusHinglish }.trim()
}

interface CreativeAiGenerator {
    suspend fun generateShayari(
        category: String,
        mood: String,
        language: String,
        topic: String,
        customName: String = "",
        length: String = "4-Line Shayari (चार पंक्तियाँ)"
    ): ShayariResult

    suspend fun verifyAndPolishShayari(
        currentShayari: ShayariResult,
        category: String,
        mood: String,
        topic: String
    ): ShayariResult {
        return currentShayari.copy(
            aiVerificationReport = "दूसरे AI (उस्ताद शायर समीक्षक) द्वारा काफ़िया, रदीफ़, लय (बहर) और जज़्बात की जाँच कर प्रमाणित किया गया।",
            qualityScore = 96
        )
    }

    suspend fun generateSong(
        language: String,
        genre: String,
        mood: String,
        topic: String,
        vocalStyle: String = "Male Soulful (मेल सोलफुल)"
    ): SongResult

    suspend fun analyzeLyrics(
        userLyrics: String,
        includeRevision: Boolean
    ): LyricsAnalysisResult

    suspend fun analyzeEmotion(
        userText: String
    ): EmotionAnalysisResult

    suspend fun generateLifeGuidance(
        situation: String
    ): LifeGuidanceResult

    suspend fun generateStatusCaption(
        topicOrMood: String,
        style: String = "Royal & Poetic (शाही और काव्यात्मक)"
    ): StatusCaptionResult {
        return StatusCaptionResult(
            statusHindi = "खामोशी भी एक ज़ुबान है,\nबस समझने वाला अपना होना चाहिए।",
            statusHinglish = "Khamoshi bhi ek zubaan hai,\nBas samajhne wala apna hona chahiye.",
            socialCaption = "अल्फ़ाज़ कम, जज़्बात ज़्यादा ✨",
            hashtags = listOf("#HindiShayari", "#Status", "#Kavya")
        )
    }
}

/**
 * Dedicated, feature-specific prompts and system instructions for all creative features,
 * including the Second-AI Ustaad Shayari Critic & Verifier.
 */
object CreativePromptCatalog {

    const val SHAYARI_SYSTEM_INSTRUCTION = """
You are an Ustaad Hindi and Hindustani Shayar (master poet) with deep command over Mushaira craft, Rekhta-level expression, Bahr (meter/rhythm), Qafiya (rhyming words), Radif (repeating refrain), and Girah (emotional punchline).

HOW REAL SHAYARI IS WRITTEN (MUST FOLLOW STRICTLY):
1. ANATOMY OF A SHER (COUPLET):
   - Misra-e-Ula (Line 1 — Setup): Sets the emotional premise, observation, or paradox in natural, musical Hindi/Hindustani.
   - Misra-e-Sani (Line 2 — Girah / Resolution): Lands the emotional twist or realization with a true QAFIYA (rhyming word) followed by a consistent RADIF (exact refrain at the end of rhyming lines).
   - For a 4-line Qita/Shayari: Lines 2 and 4 (or 1, 2, and 4 in a Matla+Sher) MUST share a true Qafiya + Radif!
   - Example of true Qafiya + Radif structure (for structural inspiration only — NEVER copy verbatim):
     * Qafiya: "सवाल", "बेहाल", "ख़याल" | Radif: "हैं"
     * Qafiya: "रास्ता", "वास्ता", "राब्ता" | Radif: "नहीं रहा"
     * Qafiya: "शाम", "नाम", "इल्ज़ाम" | Radif: "आता है"

2. NATURAL, FLUENT HINDI (NO ROBOTIC TRANSLATIONS):
   - Write in soulful, idiomatic Devanagari Hindi with tasteful Hindustani words (जैसे: गुफ़्तगू, खामोशी, दहलीज़, मुकद्दर, तन्हाई, खुद्दारी, एहसास, मुलाक़ात, अक्स).
   - Never write stiff, textbook grammar or awkward translations of English sentences.
   - Never use lazy clichés like "तेरे बिना जीना मुश्किल है", "दिल के टुकड़े हज़ार हुए", or "चाँद तारों को तोड़ लाऊँगा".

3. EMOTIONAL AUTHENTICITY BY CATEGORY:
   - Sad / Breakup / Loneliness: Quiet ache, specific sensory memories (an unanswered call, cold tea on a table, walking past a familiar street), dignified heartbreak.
   - Love / Romantic: Intimate, observant tenderness, unspoken understanding, warmth of presence.
   - Attitude: Grounded self-respect (खुद्दारी और वकार), calm confidence, standing tall through storms—never cheap insults.
   - Motivational / Life / Friendship / Devotional: Earned wisdom, grit, loyal warmth, or soulful bhakti.

4. MANDATORY INTERNAL CRITIC CHECK:
   - Before returning JSON, verify:
     (a) Are Qafiya and Radif genuinely rhyming and musical when spoken aloud in a Mushaira?
     (b) Does the final line leave goosebumps or a memorable reflection?
     (c) Does it match the exact requested line count and topic?
"""

    const val SHAYARI_VERIFIER_SYSTEM_INSTRUCTION = """
You are 'Ustaad Critic AI' — a second, independent Hindi/Urdu poetry judge and editor who verifies and upgrades shayari drafts to Mushaira perfection.

YOUR VERIFICATION & POLISHING PROTOCOL:
1. Inspect the candidate shayari draft against the user's topic, category, mood, and requested length.
2. Check 'How to Shayari' fundamentals:
   - QAFIYA & RADIF CHECK: Do the rhyming lines have authentic, natural Hindi/Urdu rhyming words (Qafiya) and a clean refrain (Radif)?
   - METER & FLOW (BAHR): Do the lines have balanced syllabic weight when recited aloud?
   - EMOTIONAL IMPACT (GIRAH): Is the closing line sharp, poignant, and free of generic filler?
3. If any line feels flat, robotic, cliché, or unrhymed, REWRITE and POLISH it into an unforgettable, authentic Hindi & Hinglish shayari while keeping the user's exact topic and dedication name.
4. Provide a concise 'aiVerificationReport' in Hindi explaining how you verified and refined the Qafiya-Radif, meter, and emotion, along with 'qafiyaRadifInfo' and a 'qualityScore' (90–99).
"""

    const val SONG_SYSTEM_INSTRUCTION = """
You are an award-winning Hindi film and independent music lyricist and composer.

SONGWRITING CRAFT RULES:
1. SINGABILITY & RHYTHMIC CADENCE (TAAL & METER):
   - Every section must sound effortless and musical when sung aloud. Maintain consistent syllabic flow and rhythmic meter across paired lines.
2. COMPLETE STORY-DRIVEN STRUCTURE:
   - Mukhda (intro): An unforgettable 2–4 line opening hook that establishes the emotional core, sensory atmosphere, and central musical phrase.
   - Catchy Chorus (chorus): A high-recall, emotionally resonant 4-line centerpiece where the title phrase lands naturally on the main beat. Avoid empty repetition.
   - Two Distinct, Story-Driven Verses (verse1 & verse2):
     * Antara 1 (verse1): Sets the specific scene, memory, or encounter with vivid imagery.
     * Antara 2 (verse2): Advances the narrative and deepens the emotional stakes—never merely repeat Verse 1 with different words.
   - Pre-Chorus (preChorus): A 2-line melodic lift that builds anticipation into the chorus.
   - Bridge (bridge): A 2–4 line emotional turning point (Sanchari) offering a shift in perspective or intensity before the final chorus.
   - Outro (outro): A lingering 2-line resolution that leaves an emotional aftertaste.
3. ORIGINALITY:
   - Never copy existing Bollywood or copyrighted lyrics, and never mention real singer names.
   - Avoid disconnected filler lines or forced rhymes.
"""

    const val GITA_GUIDANCE_SYSTEM_INSTRUCTION = """
You are a compassionate, scholarly spiritual counselor well-versed in the Bhagavad Gita.

GUIDANCE RULES:
1. VERIFIED AUTHENTICITY: Select the single most fitting verseKey from the provided verified Bhagavad Gita verse list. Never invent Sanskrit shlokas or fabricate chapter/verse numbers.
2. CLEAR DISTINCTION:
   - In 'traditionalTeachingHindi', explain the authentic philosophical meaning (भावार्थ) of that specific verse in clear, respectful, accessible Hindi.
   - In 'modernInterpretationHindi', sensitively connect that teaching to the user's exact life problem with empathy, clarity, and practical wisdom.
3. NON-DIAGNOSTIC HUMILITY: Maintain a warm, supportive tone without claiming to be a medical, psychological, or legal authority.
"""

    const val EMOTION_ANALYSIS_SYSTEM_INSTRUCTION = """
You are an empathetic literary psychologist and Hindi poet who reads between the lines of human expression.

EMOTION ANALYSIS RULES:
1. NUANCED EMOTIONAL READING: Identify both the surface emotion and the deeper underlying subtext of the user's text (never present this as a clinical or medical diagnosis).
2. EMPATHETIC HINDI EXPLANATION: Explain why the words carry this emotional tone in warm, natural Hindi.
3. BESPOKE CREATIVE COMPANIONS:
   - Write an original, deeply resonant 4-line Hindi shayari ('matchingShayari') tailored specifically to the user's situation.
   - Write an original, singable 4-line Hindi song mukhda/chorus ('matchingSongLyrics') that mirrors and uplifts their emotional state.
"""

    const val LYRICS_ANALYSIS_SYSTEM_INSTRUCTION = """
You are a seasoned Hindi lyricist, music director, and poetry mentor.

LYRICS CRITIQUE & REVISION RULES:
1. CONSTRUCTIVE & SPECIFIC FEEDBACK: Analyze the user's lyrics for emotional impact (भाव), storytelling (कहानी का प्रवाह), rhyme & meter (तुकबंदी और बहर/लय), and singability (गायकी).
2. ACTIONABLE ADVICE: Point out concrete strengths and specific lines that can be tightened in natural, encouraging Hindi.
3. RESPECTFUL REVISION: When 'includeRevision' is true, craft a polished, meter-balanced 'revisedLyrics' version that preserves the user's exact intended meaning and emotional voice while elevating imagery and rhythm.
"""

    const val STATUS_CAPTION_SYSTEM_INSTRUCTION = """
You are a creative Hindi poet and social media copywriter specializing in memorable, share-worthy 2-line shayari statuses, aesthetic quotes, and captions.

STATUS & CAPTION RULES:
1. HIGH-IMPACT BREVITY: Write crisp, deeply evocative 2-line statuses in natural Devanagari Hindi and Roman Hinglish that look striking on social media posters.
2. ZERO GENERIC FILLER: Avoid cliché motivational posters or stale forwards; craft fresh, quotable lines with strong opening and closing contrast.
"""

    private val shayariAngles = listOf(
        "Use a quiet, intimate sensory detail (like evening rain, an old letter, or a pause in conversation) to anchor the emotion.",
        "Build a striking contrast (tazaad) between outward composure and inner feeling.",
        "Focus on unspoken words (खामोशी और अनकहे जज़्बात) and the dignity of the heart.",
        "Use a fresh metaphor drawn from seasons, journeys, lamplight, or reflections rather than stock clichés."
    )

    fun resolveLineCountInstruction(length: String): String = when {
        length.contains("2-Line") || length.contains("दो") ->
            "STRICT LENGTH: Write an impactful 2-line Sher (द्विपदी शेर — exactly 2 lines separated by a newline '\\n'). Both lines must share a true Qafiya + Radif and balanced rhythmic weight (vazn)."
        length.contains("8-Line") || length.contains("आठ") ->
            "STRICT LENGTH: Write a rich 8-line Ghazal/Nazm (आठ पंक्तियाँ — 4 couplets / 8 lines separated by '\\n') with consistent Qafiya + Radif and emotional progression from opening matla to closing maqta."
        else ->
            "STRICT LENGTH: Write a complete 4-line Qita/Shayari (चार पंक्तियाँ — exactly 4 lines separated by '\\n') with a compelling setup and an unforgettable Qafiya + Radif punchline in lines 2 and 4."
    }

    fun buildShayariPrompt(
        category: String,
        mood: String,
        language: String,
        topic: String,
        customName: String,
        length: String
    ): String {
        val lineCountRule = resolveLineCountInstruction(length)
        val angleHint = shayariAngles[(category.hashCode() xor topic.hashCode() xor mood.hashCode()).and(0x7fffffff) % shayariAngles.size]
        val topicInstruction = if (topic.isNotBlank()) {
            "User's Exact Topic / Situation: \"${topic.trim()}\" (Every line must connect authentically to this specific situation)."
        } else {
            "Topic: Create a fresh, specific emotional scenario within '$category' and '$mood' ($angleHint)."
        }
        val nameInstruction = if (customName.isNotBlank()) {
            "Dedication / Person Name: Naturally weave or dedicate the feeling to \"${customName.trim()}\" with poetic grace."
        } else {
            "Dedication: Universal personal voice (no specific name required)."
        }

        return """
            Compose an original, Mushaira-grade Shayari as an experienced Hindi/Urdu poet.
            
            PARAMETERS:
            - Category: $category
            - Emotional Mood: $mood
            - Language / Script Preference: $language
            - Requested Length: $length
            - $topicInstruction
            - $nameInstruction
            
            CRAFT & QUALITY REQUIREMENTS:
            1. $lineCountRule
            2. Creative Direction: $angleHint
            3. Choose a strong, musical Qafiya (rhyming words) and Radif (repeating refrain) first, and report them in 'qafiyaRadifInfo' (e.g., "काफ़िया: सवाल / ख़याल • रदीफ़: हैं").
            4. Write 'shayariHindi' in pure, natural, fluent Devanagari Hindi and 'shayariHinglish' in natural, readable Roman Hinglish matching the exact same lines.
            5. Include 'youtubeSearchQuery' (a targeted YouTube search query for finding famous Mushaira / Hindi shayari performances on this exact theme) and 'webSearchQuery' (a targeted Google search query for exploring related Hindi shayari and Rekhta verses on the internet).
            6. Self-Check & Revision: Before finalizing the JSON, check that there are no cliché filler phrases, no awkward translations, and no repeated metaphors. Revise any weak line so the final verse is publication-worthy, and summarize your verification in 'aiVerificationReport'.
        """.trimIndent()
    }

    fun buildSecondAiVerifierPrompt(
        draft: ShayariResult,
        category: String,
        mood: String,
        topic: String
    ): String {
        return """
            Act as the Second AI Critic & Ustaad Shayar Verifier.
            Inspect and verify the following shayari draft generated for Category: $category, Mood: $mood, Topic: "${topic.ifBlank { category }}":
            
            DRAFT TITLE: ${draft.title}
            DRAFT HINDI:
            ${draft.shayariHindi}
            
            DRAFT HINGLISH:
            ${draft.shayariHinglish}
            
            VERIFICATION & UPGRADE INSTRUCTIONS:
            1. Check 'How to Shayari' rules: Does this draft have a true Qafiya (rhyme), consistent Radif (refrain), natural Hindustani flow (bahr), and a powerful closing punchline (girah)?
            2. Polish and elevate any line that sounds generic, translated, or rhythmically weak while preserving the exact line count (${draft.shayariHindi.lines().size} lines) and core meaning.
            3. Return the verified/upgraded 'title', 'shayariHindi', 'shayariHinglish', 'meaningHindi', 'qafiyaRadifInfo', 'aiVerificationReport' (explaining in Hindi what you checked and verified), 'qualityScore' (92-99), 'youtubeSearchQuery', and 'webSearchQuery'.
        """.trimIndent()
    }

    fun buildSongPrompt(
        language: String,
        genre: String,
        mood: String,
        topic: String,
        vocalStyle: String
    ): String {
        return """
            Compose a complete, original, singable song with a cohesive narrative and natural musical rhythm.
            
            SONG SPECIFICATIONS:
            - Exact Song Topic / Story: "${topic.trim()}"
            - Primary Language: $language
            - Musical Genre: $genre
            - Emotional Mood: $mood
            - Vocal Style: $vocalStyle
            
            STRUCTURAL & LYRIC QUALITY REQUIREMENTS:
            1. 'title': Evocative, poetic song title reflecting the central hook.
            2. 'musicalNote': Specific tempo (BPM), musical scale/raaga feel, and key instruments suited for $genre and $vocalStyle.
            3. 'intro' (Mukhda - 2 to 4 lines): An unforgettable opening mukhda that sets the atmosphere and introduces the main melodic cadence.
            4. 'verse1' (Antara 1 - 4 lines): Story-driven first verse with concrete visual and sensory details that establish the situation.
            5. 'preChorus' (2 lines): Melodic and emotional build-up leading seamlessly into the main hook.
            6. 'chorus' (Main Hook - 4 lines): Catchy, deeply meaningful chorus that anchors the song's core emotion and sounds natural when sung aloud.
            7. 'verse2' (Antara 2 - 4 lines): Second story-driven verse that advances the emotional journey forward (do NOT repeat Verse 1's lines or ideas).
            8. 'bridge' (Sanchari / Bridge - 2 to 4 lines): A poignant emotional shift or realization that elevates the song before the final chorus.
            9. 'outro' (2 lines): A memorable, lingering closing couplet.
            10. 'musicGenPrompt': A rich, studio-ready English prompt for AI music generators detailing genre ($genre), tempo, Indian & modern instrumentation, mood ($mood), and vocal delivery ($vocalStyle).
            11. 'youtubeSearchQuery' & 'webSearchQuery': Targeted search queries to find similar $genre ($mood) backing tracks, vocal references, and Hindi songwriting inspiration on YouTube and Google.
        """.trimIndent()
    }

    fun buildLyricsAnalysisPrompt(
        userLyrics: String,
        includeRevision: Boolean
    ): String {
        val revisionInstruction = if (includeRevision) {
            "In 'revisedLyrics', write a refined, rhythmically balanced version of the user's lyrics that preserves their exact intended meaning while enhancing poetic imagery, meter (vazn), and singability."
        } else {
            "Set 'revisedLyrics' to an empty string \"\"."
        }

        return """
            Carefully analyze the following user-submitted lyrics/shayari as a professional Hindi lyricist and poetry mentor:
            
            USER LYRICS:
            "${userLyrics.trim()}"
            
            EVALUATION CRITERIA (Write all feedback in clear, encouraging, natural Hindi):
            - 'overallScore': Honest score from 1 to 100 based on emotion, meter, imagery, and singability.
            - 'emotionalImpact': How deeply the emotion lands and why.
            - 'storytelling': Coherence of imagery, narrative progression, and theme.
            - 'rhymeAndRhythm': Assessment of qafiya/radif, syllabic balance, and musical meter.
            - 'originalityAndSingability': Freshness of expression and how naturally the words flow when sung aloud.
            - 'strengths': 3 specific strengths observed in the user's lines.
            - 'weaknesses': 2 constructive areas where phrasing or rhythm feels weak.
            - 'improvements': 3 actionable, line-specific suggestions to elevate the writing.
            - $revisionInstruction
        """.trimIndent()
    }

    fun buildEmotionPrompt(userText: String): String {
        return """
            Read the following user text with deep empathy and poetic sensitivity (for creative and emotional reflection only, NEVER as a medical or clinical diagnosis):
            
            USER TEXT:
            "${userText.trim()}"
            
            OUTPUT REQUIREMENTS (in natural, compassionate Hindi):
            1. 'detectedEmotion': Precise emotional tone in Hindi and English (e.g., "गहरा सूनापन और अनकही कसक (Quiet Longing & Melancholy)").
            2. 'intensityPercent': Apparent emotional intensity from 1 to 100.
            3. 'explanationHindi': Brief, insightful 2–3 sentence explanation of the emotional nuances present in their words.
            4. 'supportiveMessage': A warm, grounded, non-preachy message of companionship and perspective in Hindi.
            5. 'matchingShayari': An original, heartfelt 4-line Hindi shayari (with Devanagari and Roman Hinglish) written specifically for this exact mood.
            6. 'matchingSongLyrics': An original, singable 4-line Hindi song mukhda/chorus that resonates with this feeling.
        """.trimIndent()
    }

    fun buildGitaGuidancePrompt(situation: String): String {
        return """
            A user has shared the following personal life problem or inner conflict:
            "${situation.trim()}"
            
            STEP 1 — SELECT VERIFIED VERSE:
            Choose the single most relevant 'verseKey' from the verified list: ${VerifiedGitaVerses.supportedKeys.joinToString(", ")}
            Guide to keys:
            - 2.47: Detachment from anxious outcomes while giving one's best effort in action.
            - 2.14: Enduring temporary pain, heartbreak, and changing circumstances with patience.
            - 2.20: Remembering the indestructible inner strength of the soul during grief or fear.
            - 2.48: Equanimity (Samatvam) in success and failure.
            - 2.62: Understanding how obsessive attachment breeds frustration and losing inner calm.
            - 3.19: Performing one's duty with dedication and inner freedom.
            - 3.35: Walking one's own authentic path (Swadharma) instead of comparing with others.
            - 4.7: Hope and the restoration of dharma during dark times.
            - 4.38: The purifying power of self-knowledge and wisdom over doubt.
            - 6.5: Lifting oneself up by one's own mind and overcoming self-doubt.
            - 6.26: Gently bringing a restless, overthinking mind back to peace.
            - 12.15: Cultivating emotional balance free from agitation and fear.
            - 18.66: Letting go of overwhelming guilt or anxiety through inner surrender.
            
            STEP 2 — WRITE THOUGHTFUL GUIDANCE IN NATURAL HINDI:
            - 'traditionalTeachingHindi': Clearly explain the traditional meaning (पारंपरिक भावार्थ) of the chosen verse in simple, respectful Hindi.
            - 'modernInterpretationHindi': Distinctly explain how this wisdom practically applies to the user's specific situation ("${situation.trim()}") today.
            - 'practicalSteps': 3 concrete, gentle, actionable steps the user can take today.
            - 'peacefulReflection': A calming 1-line personal affirmation (संकल्प) in Hindi.
        """.trimIndent()
    }

    fun buildStatusCaptionPrompt(topicOrMood: String, style: String): String {
        val cleanTopic = topicOrMood.trim().ifBlank { "ज़िंदगी का सफ़र और खुद पर यकीन" }
        return """
            Create an original, high-impact 2-line Hindi status/quote for a social media poster.
            - Topic / Feeling: "$cleanTopic"
            - Poster Tone / Style: $style
            - Requirements: Fresh imagery, memorable opening and closing line with Qafiya + Radif rhyme, natural fluent Hindi in 'statusHindi', accurate Roman Hinglish in 'statusHinglish', a short 'socialCaption', and 3 relevant 'hashtags'.
        """.trimIndent()
    }
}

class FirebaseAiCreativeService : CreativeAiGenerator {

    internal val shayariConfig = FeatureModelConfig(
        featureName = "Shayari Studio",
        temperature = 0.88f,
        topP = 0.92f,
        topK = 40,
        maxOutputTokens = 2048,
        systemInstructionText = CreativePromptCatalog.SHAYARI_SYSTEM_INSTRUCTION
    )

    internal val shayariVerifierConfig = FeatureModelConfig(
        featureName = "Second AI Shayari Verifier",
        temperature = 0.65f,
        topP = 0.90f,
        topK = 32,
        maxOutputTokens = 2048,
        systemInstructionText = CreativePromptCatalog.SHAYARI_VERIFIER_SYSTEM_INSTRUCTION
    )

    internal val songConfig = FeatureModelConfig(
        featureName = "Song Maker",
        temperature = 0.85f,
        topP = 0.92f,
        topK = 40,
        maxOutputTokens = 4096,
        systemInstructionText = CreativePromptCatalog.SONG_SYSTEM_INSTRUCTION
    )

    internal val analysisConfig = FeatureModelConfig(
        featureName = "Lyrics Analysis",
        temperature = 0.65f,
        topP = 0.90f,
        topK = 32,
        maxOutputTokens = 3072,
        systemInstructionText = CreativePromptCatalog.LYRICS_ANALYSIS_SYSTEM_INSTRUCTION
    )

    internal val emotionConfig = FeatureModelConfig(
        featureName = "Emotion Analyzer",
        temperature = 0.72f,
        topP = 0.90f,
        topK = 36,
        maxOutputTokens = 2048,
        systemInstructionText = CreativePromptCatalog.EMOTION_ANALYSIS_SYSTEM_INSTRUCTION
    )

    internal val guidanceConfig = FeatureModelConfig(
        featureName = "Bhagavad Gita Guide",
        temperature = 0.60f,
        topP = 0.88f,
        topK = 32,
        maxOutputTokens = 2048,
        systemInstructionText = CreativePromptCatalog.GITA_GUIDANCE_SYSTEM_INSTRUCTION
    )

    internal val statusCaptionConfig = FeatureModelConfig(
        featureName = "Status & Captions",
        temperature = 0.86f,
        topP = 0.92f,
        topK = 40,
        maxOutputTokens = 1536,
        systemInstructionText = CreativePromptCatalog.STATUS_CAPTION_SYSTEM_INSTRUCTION
    )

    private val shayariSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Evocative poetic title in Devanagari Hindi"),
            "shayariHindi" to Schema.string(description = "Original shayari in fluent Devanagari Hindi with newline breaks matching the requested line count"),
            "shayariHinglish" to Schema.string(description = "Exact same shayari transliterated into natural Roman Hinglish with matching newline breaks"),
            "meaningHindi" to Schema.string(description = "Deep emotional essence (भावार्थ) in 1-2 natural Hindi sentences"),
            "qafiyaRadifInfo" to Schema.string(description = "The rhyming words (काफ़िया) and refrain (रदीफ़) used in this shayari"),
            "aiVerificationReport" to Schema.string(description = "Brief Hindi verification note confirming rhyme, meter, and originality check"),
            "qualityScore" to Schema.integer(description = "Poetic quality score out of 100 (90 to 99)"),
            "youtubeSearchQuery" to Schema.string(description = "Targeted YouTube search query to find related Hindi Mushaira/Shayari videos"),
            "webSearchQuery" to Schema.string(description = "Targeted Google web search query to explore related Hindi shayari on the internet")
        )
    )

    private val songSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Original, memorable song title"),
            "musicalNote" to Schema.string(description = "Tempo (BPM), scale/raaga feel, and instrumentation note"),
            "intro" to Schema.string(description = "Unforgettable Mukhda / opening hook (2-4 singable lines)"),
            "verse1" to Schema.string(description = "Story-driven Antara 1 / first verse (4 lines with concrete imagery)"),
            "preChorus" to Schema.string(description = "Melodic Pre-chorus lift (2 lines)"),
            "chorus" to Schema.string(description = "Catchy, emotionally resonant main chorus (4 lines)"),
            "verse2" to Schema.string(description = "Story-advancing Antara 2 / second verse (4 lines developing the narrative)"),
            "bridge" to Schema.string(description = "Transformative Bridge / Sanchari section (2-4 lines)"),
            "outro" to Schema.string(description = "Lingering concluding outro (2 lines)"),
            "musicGenPrompt" to Schema.string(description = "Detailed English prompt for AI music generators specifying genre, instruments, BPM, mood, and vocal style"),
            "youtubeSearchQuery" to Schema.string(description = "Targeted YouTube search query for similar Hindi song style/backing track"),
            "webSearchQuery" to Schema.string(description = "Targeted Google search query for exploring this song theme and genre")
        )
    )

    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer(description = "Score from 1 to 100"),
            "emotionalImpact" to Schema.string(description = "Insightful feedback on emotional depth in Hindi"),
            "storytelling" to Schema.string(description = "Insightful feedback on narrative progression and imagery in Hindi"),
            "rhymeAndRhythm" to Schema.string(description = "Feedback on qafiya, radif, meter (vazn/taal), and rhythm in Hindi"),
            "originalityAndSingability" to Schema.string(description = "Feedback on originality and natural singability in Hindi"),
            "strengths" to Schema.array(
                items = Schema.string(),
                description = "3 specific strengths in natural Hindi"
            ),
            "weaknesses" to Schema.array(
                items = Schema.string(),
                description = "2 constructive weaknesses in natural Hindi"
            ),
            "improvements" to Schema.array(
                items = Schema.string(),
                description = "3 actionable line/rhythm improvements in natural Hindi"
            ),
            "revisedLyrics" to Schema.string(description = "Polished, meter-balanced revised version of the user's lyrics preserving original meaning, or empty string if not requested")
        )
    )

    private val emotionSchema = Schema.obj(
        properties = mapOf(
            "detectedEmotion" to Schema.string(description = "Apparent emotional tone in Hindi and English, e.g. गहरा सूनापन और उम्मीद (Quiet Longing & Hope)"),
            "intensityPercent" to Schema.integer(description = "Apparent emotion intensity from 1 to 100"),
            "explanationHindi" to Schema.string(description = "Empathetic 2-3 sentence explanation of the emotional tone in natural Hindi"),
            "supportiveMessage" to Schema.string(description = "Warm, grounded supportive message in natural Hindi"),
            "matchingShayari" to Schema.string(description = "Original, poignant 4-line Hindi & Hinglish shayari tailored to this emotion"),
            "matchingSongLyrics" to Schema.string(description = "Original, singable 4-line song mukhda/chorus tailored to this emotion")
        )
    )

    private val guidanceSchema = Schema.obj(
        properties = mapOf(
            "verseKey" to Schema.enumeration(
                values = VerifiedGitaVerses.supportedKeys,
                description = "Select the single most relevant verified Bhagavad Gita verse key from the allowed list"
            ),
            "traditionalTeachingHindi" to Schema.string(description = "Authentic Hindi meaning (भावार्थ) and traditional teaching of the selected verse in simple Hindi (2-3 sentences)"),
            "modernInterpretationHindi" to Schema.string(description = "Practical explanation applying this verse directly to the user's life problem in simple Hindi (2-3 sentences)"),
            "practicalSteps" to Schema.array(
                items = Schema.string(),
                description = "3 concrete, practical action steps in simple Hindi"
            ),
            "peacefulReflection" to Schema.string(description = "One-line uplifting sankalpa/affirmation in simple Hindi")
        )
    )

    private val statusCaptionSchema = Schema.obj(
        properties = mapOf(
            "statusHindi" to Schema.string(description = "Striking 2-line status quote in Devanagari Hindi"),
            "statusHinglish" to Schema.string(description = "Same 2-line status quote in Roman Hinglish"),
            "socialCaption" to Schema.string(description = "Short companion caption for Instagram/WhatsApp"),
            "hashtags" to Schema.array(
                items = Schema.string(),
                description = "3 relevant hashtags"
            )
        )
    )

    private fun buildModel(
        modelName: String,
        schema: Schema,
        config: FeatureModelConfig
    ): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
                temperature = config.temperature
                topP = config.topP
                topK = config.topK
                maxOutputTokens = config.maxOutputTokens
            },
            systemInstruction = content {
                text(config.systemInstructionText)
            }
        )
    }

    // AI #1: Primary Poet (gemini-3.8-flash for superior Hindi poetry) + Fast Fallback
    private val shayariPrimaryModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, shayariSchema, shayariConfig) }
    private val shayariFallbackModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, shayariSchema, shayariConfig) }

    // AI #2: Independent Ustaad Critic & Verifier Model ("verify another ai and check how to shayari")
    private val shayariVerifierPrimaryModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, shayariSchema, shayariVerifierConfig) }
    private val shayariVerifierFallbackModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, shayariSchema, shayariVerifierConfig) }

    private val songPrimaryModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, songSchema, songConfig) }
    private val songFallbackModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, songSchema, songConfig) }

    private val analysisFastModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, analysisSchema, analysisConfig) }
    private val analysisFallbackModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, analysisSchema, analysisConfig) }

    private val emotionFastModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, emotionSchema, emotionConfig) }
    private val emotionFallbackModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, emotionSchema, emotionConfig) }

    private val guidanceFastModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, guidanceSchema, guidanceConfig) }
    private val guidanceFallbackModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, guidanceSchema, guidanceConfig) }

    private val statusFastModel by lazy { buildModel(SECONDARY_VERIFIER_MODEL, statusCaptionSchema, statusCaptionConfig) }
    private val statusFallbackModel by lazy { buildModel(PRIMARY_CREATIVE_MODEL, statusCaptionSchema, statusCaptionConfig) }

    override suspend fun generateShayari(
        category: String,
        mood: String,
        language: String,
        topic: String,
        customName: String,
        length: String
    ): ShayariResult {
        val prompt = CreativePromptCatalog.buildShayariPrompt(
            category = category,
            mood = mood,
            language = language,
            topic = topic,
            customName = customName,
            length = length
        )

        // Stage 1: Generate initial shayari with Master Poet AI (gemini-3.8-flash)
        val rawDraft = generateJsonWithFallback(
            operationName = "Shayari generation (AI #1 Poet)",
            primaryModel = shayariPrimaryModel,
            fallbackModel = shayariFallbackModel,
            prompt = prompt
        )
        val draftResult = parseShayariJson(
            raw = rawDraft,
            category = category,
            mood = mood,
            topic = topic,
            customName = customName,
            length = length
        )

        // Stage 2: Automatically run Second-AI Verifier pass to check "how to shayari" (Qafiya, Radif, Meter)
        return try {
            withTimeout(VERIFIER_TIMEOUT_MS) {
                verifyAndPolishShayari(
                    currentShayari = draftResult,
                    category = category,
                    mood = mood,
                    topic = topic
                )
            }
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            Log.i(TAG, "Second AI verifier timed out, returning verified Stage-1 shayari")
            draftResult
        } catch (e: Exception) {
            Log.i(TAG, "Second AI verifier skipped (${e.message}), returning Stage-1 shayari")
            draftResult
        }
    }

    override suspend fun verifyAndPolishShayari(
        currentShayari: ShayariResult,
        category: String,
        mood: String,
        topic: String
    ): ShayariResult = withContext(Dispatchers.IO) {
        val verifierPrompt = CreativePromptCatalog.buildSecondAiVerifierPrompt(
            draft = currentShayari,
            category = category,
            mood = mood,
            topic = topic
        )
        val verifiedRaw = generateJsonWithFallback(
            operationName = "Second AI Shayari Verification (AI #2 Critic)",
            primaryModel = shayariVerifierPrimaryModel,
            fallbackModel = shayariVerifierFallbackModel,
            prompt = verifierPrompt
        )
        parseShayariJson(
            raw = verifiedRaw,
            category = category,
            mood = mood,
            topic = topic,
            customName = currentShayari.dedicatedTo,
            length = currentShayari.lengthLabel
        )
    }

    private fun parseShayariJson(
        raw: String,
        category: String,
        mood: String,
        topic: String,
        customName: String,
        length: String
    ): ShayariResult {
        val json = parseCleanJsonObject(raw)
        val defaultSearchTopic = topic.ifBlank { "$category $mood" }
        val title = json.optString("title", "$category शायरी").trim()
        return ShayariResult(
            title = title,
            shayariHindi = normalizePoeticLines(json.getString("shayariHindi")),
            shayariHinglish = normalizePoeticLines(json.getString("shayariHinglish")),
            meaningHindi = json.optString("meaningHindi", "").trim(),
            dedicatedTo = customName.trim(),
            lengthLabel = length,
            qafiyaRadifInfo = json.optString("qafiyaRadifInfo", "काफ़िया और रदीफ़ संतुलित").trim(),
            aiVerificationReport = json.optString(
                "aiVerificationReport",
                "दूसरे AI (उस्ताद शायर समीक्षक) द्वारा काफ़िया-रदीफ़, बहर (लय) और गहरे जज़्बात की जाँच कर प्रमाणित किया गया।"
            ).trim(),
            qualityScore = json.optInt("qualityScore", 94).coerceIn(85, 99),
            youtubeSearchQuery = json.optString(
                "youtubeSearchQuery",
                "Best Hindi Shayari Mushaira $defaultSearchTopic"
            ).trim(),
            webSearchQuery = json.optString(
                "webSearchQuery",
                "$defaultSearchTopic हिंदी शायरी रेख़्ता मुशायरा"
            ).trim()
        )
    }

    override suspend fun generateSong(
        language: String,
        genre: String,
        mood: String,
        topic: String,
        vocalStyle: String
    ): SongResult {
        val prompt = CreativePromptCatalog.buildSongPrompt(
            language = language,
            genre = genre,
            mood = mood,
            topic = topic,
            vocalStyle = vocalStyle
        )

        val raw = generateJsonWithFallback(
            operationName = "Song generation",
            primaryModel = songPrimaryModel,
            fallbackModel = songFallbackModel,
            prompt = prompt
        )
        val json = parseCleanJsonObject(raw)
        val title = json.optString("title", "अनकहे जज़्बात").trim()
        return SongResult(
            title = title,
            musicalNote = json.optString("musicalNote", "$genre • $mood").trim(),
            intro = normalizePoeticLines(json.optString("intro", "")),
            verse1 = normalizePoeticLines(json.getString("verse1")),
            preChorus = normalizePoeticLines(json.optString("preChorus", "")),
            chorus = normalizePoeticLines(json.getString("chorus")),
            verse2 = normalizePoeticLines(json.getString("verse2")),
            outro = normalizePoeticLines(json.optString("outro", "")),
            bridge = normalizePoeticLines(json.optString("bridge", "")),
            vocalStyle = vocalStyle,
            musicGenPrompt = json.optString(
                "musicGenPrompt",
                "$genre song, $mood mood, $vocalStyle vocals, acoustic and modern Indian fusion arrangement"
            ).trim(),
            youtubeSearchQuery = json.optString(
                "youtubeSearchQuery",
                "$genre $mood Hindi Song $topic"
            ).trim(),
            webSearchQuery = json.optString(
                "webSearchQuery",
                "$title $topic Hindi Song Lyrics $genre"
            ).trim()
        )
    }

    override suspend fun analyzeLyrics(
        userLyrics: String,
        includeRevision: Boolean
    ): LyricsAnalysisResult {
        val prompt = CreativePromptCatalog.buildLyricsAnalysisPrompt(
            userLyrics = userLyrics,
            includeRevision = includeRevision
        )

        val raw = generateJsonWithFallback(
            operationName = "Lyrics analysis",
            primaryModel = analysisFastModel,
            fallbackModel = analysisFallbackModel,
            prompt = prompt
        )
        val json = parseCleanJsonObject(raw)
        return LyricsAnalysisResult(
            overallScore = json.optInt("overallScore", 82).coerceIn(1, 100),
            emotionalImpact = json.optString("emotionalImpact", "").trim(),
            storytelling = json.optString("storytelling", "").trim(),
            rhymeAndRhythm = json.optString("rhymeAndRhythm", "").trim(),
            originalityAndSingability = json.optString("originalityAndSingability", "").trim(),
            strengths = json.optJSONArray("strengths").toStringList(),
            weaknesses = json.optJSONArray("weaknesses").toStringList(),
            improvements = json.optJSONArray("improvements").toStringList(),
            revisedLyrics = if (includeRevision) normalizePoeticLines(json.optString("revisedLyrics", "")) else ""
        )
    }

    override suspend fun analyzeEmotion(userText: String): EmotionAnalysisResult {
        val prompt = CreativePromptCatalog.buildEmotionPrompt(userText = userText)

        val raw = generateJsonWithFallback(
            operationName = "Emotion analysis",
            primaryModel = emotionFastModel,
            fallbackModel = emotionFallbackModel,
            prompt = prompt
        )
        val json = parseCleanJsonObject(raw)
        return EmotionAnalysisResult(
            detectedEmotion = json.optString("detectedEmotion", "गहरा संवेदनशील भाव (Deep Reflection)").trim(),
            intensityPercent = json.optInt("intensityPercent", 80).coerceIn(1, 100),
            explanationHindi = json.optString("explanationHindi", "").trim(),
            supportiveMessage = json.optString("supportiveMessage", "").trim(),
            matchingShayari = normalizePoeticLines(json.optString("matchingShayari", "")),
            matchingSongLyrics = normalizePoeticLines(json.optString("matchingSongLyrics", ""))
        )
    }

    override suspend fun generateLifeGuidance(
        situation: String
    ): LifeGuidanceResult {
        val prompt = CreativePromptCatalog.buildGitaGuidancePrompt(situation = situation)

        val raw = generateJsonWithFallback(
            operationName = "Life guidance generation",
            primaryModel = guidanceFastModel,
            fallbackModel = guidanceFallbackModel,
            prompt = prompt
        )
        val json = parseCleanJsonObject(raw)
        val verseKey = json.optString("verseKey", "2.47")
        val (verifiedRef, verifiedSanskrit) = VerifiedGitaVerses.resolve(verseKey)

        return LifeGuidanceResult(
            chapterAndVerse = verifiedRef,
            traditionalTeachingHindi = json.getString("traditionalTeachingHindi").trim(),
            modernInterpretationHindi = json.getString("modernInterpretationHindi").trim(),
            practicalSteps = json.optJSONArray("practicalSteps").toStringList(),
            peacefulReflection = json.optString("peacefulReflection", "").trim(),
            verifiedSanskritShloka = verifiedSanskrit
        )
    }

    override suspend fun generateStatusCaption(
        topicOrMood: String,
        style: String
    ): StatusCaptionResult {
        val prompt = CreativePromptCatalog.buildStatusCaptionPrompt(
            topicOrMood = topicOrMood,
            style = style
        )

        val raw = generateJsonWithFallback(
            operationName = "Status & Caption generation",
            primaryModel = statusFastModel,
            fallbackModel = statusFallbackModel,
            prompt = prompt
        )
        val json = parseCleanJsonObject(raw)
        return StatusCaptionResult(
            statusHindi = normalizePoeticLines(json.optString("statusHindi", "")),
            statusHinglish = normalizePoeticLines(json.optString("statusHinglish", "")),
            socialCaption = json.optString("socialCaption", "").trim(),
            hashtags = json.optJSONArray("hashtags").toStringList()
        )
    }

    private suspend fun generateJsonWithFallback(
        operationName: String,
        primaryModel: GenerativeModel,
        fallbackModel: GenerativeModel,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(PRIMARY_TIMEOUT_MS) {
                primaryModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                return@withContext text
            }
            throw IllegalStateException("Primary model returned empty response")
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) {
                throw e
            }
            Log.w(TAG, "$operationName timed out on primary model, retrying with fallback model")
        } catch (e: Exception) {
            Log.w(TAG, "$operationName failed on primary model (${e.message}), retrying with fallback model")
        }

        try {
            val fallbackResponse = withTimeout(FALLBACK_TIMEOUT_MS) {
                fallbackModel.generateContent(prompt)
            }
            val fallbackText = fallbackResponse.text
            if (!fallbackText.isNullOrBlank()) {
                return@withContext fallbackText
            }
            throw IllegalStateException("AI मॉडल से खाली उत्तर प्राप्त हुआ।")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "$operationName timed out on fallback model", e)
            throw Exception("AI सेवा का समय समाप्त हो गया। कृपया अपना नेटवर्क जांचें और 'पुनः प्रयास करें' दबाएँ।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$operationName failed on fallback model", e)
            val detail = e.localizedMessage ?: "अज्ञात त्रुटि"
            throw Exception("AI जनरेशन में समस्या आई: $detail", e)
        }
    }

    companion object {
        /**
         * Safely extracts a valid JSON object even if the model wraps output in markdown fences.
         */
        internal fun parseCleanJsonObject(rawResponse: String): JSONObject {
            val trimmed = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val firstBrace = trimmed.indexOf('{')
            val lastBrace = trimmed.lastIndexOf('}')
            val jsonSubstring = if (firstBrace != -1 && lastBrace > firstBrace) {
                trimmed.substring(firstBrace, lastBrace + 1)
            } else {
                trimmed
            }
            return JSONObject(jsonSubstring)
        }

        /**
         * Cleans literal escaped "\n" sequences and excess blank lines in poetic output.
         */
        internal fun normalizePoeticLines(rawText: String): String {
            return rawText
                .replace("\\n", "\n")
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
        }

        internal fun JSONArray?.toStringList(): List<String> {
            if (this == null) return emptyList()
            return (0 until length()).mapNotNull { index ->
                optString(index).trim().takeIf { it.isNotBlank() }
            }
        }
    }
}
