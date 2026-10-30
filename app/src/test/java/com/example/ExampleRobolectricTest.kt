package com.example

import android.content.ClipboardManager
import android.content.Context
import android.os.Looper
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.CreativeAiGenerator
import com.example.data.ai.CreativePromptCatalog
import com.example.data.ai.EmotionAnalysisResult
import com.example.data.ai.FirebaseAiCreativeService
import com.example.data.ai.LifeGuidanceResult
import com.example.data.ai.LyricsAnalysisResult
import com.example.data.ai.ShayariResult
import com.example.data.ai.SongResult
import com.example.data.ai.StatusCaptionResult
import com.example.data.ai.VerifiedGitaVerses
import com.example.data.local.AppDatabase
import com.example.data.local.FeatureType
import com.example.data.repository.CreationRepository
import com.example.ui.AppScreen
import com.example.ui.FeatureUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private class FakeCreativeAiGenerator(
    var shouldFail: Boolean = false
) : CreativeAiGenerator {

    val capturedShayariPrompts = mutableListOf<String>()
    val capturedSongPrompts = mutableListOf<String>()

    override suspend fun generateShayari(
        category: String,
        mood: String,
        language: String,
        topic: String,
        customName: String,
        length: String
    ): ShayariResult {
        if (shouldFail) throw Exception("AI नेटवर्क कनेक्शन विफल रहा।")
        val prompt = CreativePromptCatalog.buildShayariPrompt(
            category = category,
            mood = mood,
            language = language,
            topic = topic,
            customName = customName,
            length = length
        )
        capturedShayariPrompts.add(prompt)

        val namePart = if (customName.isNotBlank()) " ($customName)" else ""
        val combinedKey = "$category $mood $topic".lowercase()

        return when {
            combinedKey.contains("breakup") || combinedKey.contains("ब्रेकअप") || combinedKey.contains("अधूरा") -> ShayariResult(
                title = "अधूरी मुलाक़ात की कसक$namePart",
                shayariHindi = "मेज़ पर रखी ठंडी चाय में अब भी तेरी बातों की भाप है,\nतूने पन्ना तो पलट दिया, पर मेरे हिस्से में पूरी किताब है।\nबिछड़ने का गिला किससे करें जब रास्ते खुद ही मुड़ गए,\nतेरे शहर की हवाओं में आज भी मेरे अनकहे सवालों का हिसाब है।",
                shayariHinglish = "Mez par rakhi thandi chai mein ab bhi teri baaton ki bhaap hai,\nTune panna toh palat diya, par mere hisse mein poori kitaab hai.\nBichhadne ka gila kisse karein jab raaste khud hi mud gaye,\nTere shahar ki hawaon mein aaj bhi mere ankahe sawaalon ka hisaab hai.",
                meaningHindi = "यह शायरी रिश्ते के टूटने के बाद बची हुई सूनी यादों और अनकहे सवालों के दर्द को सहज रूपकों में व्यक्त करती है।",
                dedicatedTo = customName,
                lengthLabel = length,
                qafiyaRadifInfo = "काफ़िया: भाप, किताब, हिसाब | रदीफ़: है",
                aiVerificationReport = "2nd-AI Verified: काफ़िया-रदीफ़ संतुलित हैं और ठंडी चाय व किताब का रूपक मौलिक है।",
                qualityScore = 95,
                youtubeSearchQuery = "Adhuri Mulaqat Breakup Hindi Shayari Mushaira",
                webSearchQuery = "अधूरी मुलाक़ात ब्रेकअप हिंदी शायरी"
            )

            combinedKey.contains("sad") || combinedKey.contains("तन्हाई") || combinedKey.contains("अकेलापन") || combinedKey.contains("दर्द") -> ShayariResult(
                title = "सूने कमरे की खामोशी$namePart",
                shayariHindi = "भीड़ से लौटकर जब अपने कमरे का दरवाज़ा बंद करता हूँ,\nदीवारों पर टँगी खामोशी से रोज़ थोड़ा-सा संवाद करता हूँ।\nकोई पूछता है हाल तो मुस्कुराकर टाल देता हूँ अक्सर,\nमैं अपनी तन्हाई की इज़्ज़त सरेआम नीलाम नहीं करता हूँ।",
                shayariHinglish = "Bheed se lautkar jab apne kamre ka darwaza band karta hoon,\nDeewaron par tangi khamoshi se roz thoda-sa samvaad karta hoon.\nKoi poochta hai haal toh muskurakar taal deta hoon aksar,\nMain apni tanhai ki izzat sareaam neelaam nahi karta hoon.",
                meaningHindi = "यह शायरी अकेलेपन की गहरी अनुभूति और अपने भीतर के दुख को गरिमा के साथ संभालने के भाव को दर्शाती है।",
                dedicatedTo = customName,
                lengthLabel = length,
                qafiyaRadifInfo = "काफ़िया: बंद, संवाद | रदीफ़: करता हूँ",
                aiVerificationReport = "2nd-AI Verified: तन्हाई और स्वाभिमान का गहरा व सजीव चित्रण।",
                qualityScore = 94,
                youtubeSearchQuery = "Tanhai Sad Hindi Shayari Mushaira",
                webSearchQuery = "तन्हाई दर्द भरी हिंदी शायरी"
            )

            combinedKey.contains("attitude") || combinedKey.contains("एटीट्यूड") || combinedKey.contains("खुद्दारी") -> ShayariResult(
                title = "वकार और खुद्दारी$namePart",
                shayariHindi = "हमने लहजे में नरमी रखी है, इसे हमारी कमज़ोरी मत समझना,\nतूफ़ानों में भी जो दिया जलता रहे, उस लौ की कहानी अलग होती है।\nशोहरत की धूप देखकर हम अपने साये का कद नहीं बदलते,\nज़मीन से जुड़े दरख़्तों की अपनी एक शाही निशानी होती है।",
                shayariHinglish = "Humne lehje mein narmi rakhi hai, ise hamari kamzori mat samajhna,\nToofano mein bhi jo diya jalta rahe, us lau ki kahani alag hoti hai.\nShohrat ki dhoop dekhkar hum apne saaye ka qad nahi badalte,\nZameen se jude darakhton ki apni ek shahi nishani hoti hai.",
                meaningHindi = "यह शायरी बिना किसी अहंकार या कटुता के आत्म-सम्मान, संयम और अडिग व्यक्तित्व को प्रकट करती है।",
                dedicatedTo = customName,
                lengthLabel = length,
                qafiyaRadifInfo = "काफ़िया: कहानी, निशानी | रदीफ़: होती है",
                aiVerificationReport = "2nd-AI Verified: शाही वकार और खुद्दारी का उत्कृष्ट संतुलन।",
                qualityScore = 96,
                youtubeSearchQuery = "Khuddari Royal Attitude Hindi Shayari",
                webSearchQuery = "खुद्दारी और वकार हिंदी शायरी"
            )

            combinedKey.contains("motivational") || combinedKey.contains("प्रेरणा") || combinedKey.contains("मंज़िल") || combinedKey.contains("हौसला") -> ShayariResult(
                title = "राख से उठती उड़ान$namePart",
                shayariHindi = "ठोकरों ने ही सिखाया है मुझे संभलकर कदमों को आगे बढ़ाना,\nअँधेरी रात जितनी गहरी हो, उतना ही करीब होता है नया सवेरा।\nथककर बैठ जाने से मंज़िलें कभी दरवाज़ा नहीं खटखटातीं,\nअपने पसीने की स्याही से लिखना पड़ता है मुकद्दर का हर सवेरा।",
                shayariHinglish = "Thokaron ne hi sikhaya hai mujhe sambhalkar kadmon ko aage badhana,\nAndheri raat jitni gehri ho, utna hi kareeb hota hai naya savera.\nThakkar baith jaane se manzilein kabhi darwaza nahi khatkhatatin,\nApne paseene ki syahi se likhna padta hai muqaddar ka har savera.",
                meaningHindi = "यह शायरी असफलताओं से सीखकर निरंतर परिश्रम और आत्मविश्वास के साथ आगे बढ़ने की प्रेरणा देती है।",
                dedicatedTo = customName,
                lengthLabel = length,
                qafiyaRadifInfo = "काफ़िया: नया, हर | रदीफ़: सवेरा",
                aiVerificationReport = "2nd-AI Verified: प्रेरणादायक भाव और सशक्त मिसरा-ए-सानी।",
                qualityScore = 95,
                youtubeSearchQuery = "Hausla Motivational Hindi Shayari",
                webSearchQuery = "हौसला और मंज़िल प्रेरक हिंदी शायरी"
            )

            else -> ShayariResult(
                title = "चाँदनी रात की गुफ़्तगू$namePart",
                shayariHindi = "खामोश निगाहों में हज़ारों सवाल हैं,\nतेरे पास बैठूँ तो लम्हे भी निहाल हैं।\nकोई लफ़्ज़ न भी कहूँ तो तू समझ लेता है,\nइसी सादगी से जुड़े हमारे दिल के खयाल हैं।",
                shayariHinglish = "Khamosh nigahon mein hazaron sawaal hain,\nTere paas baithoon toh lamhe bhi nihaal hain.\nKoi lafz na bhi kahoon toh tu samajh leta hai,\nIsi saadgi se jude hamare dil ke khayaal hain.",
                meaningHindi = "यह शायरी सच्चे प्रेम की आत्मीयता और बिना कहे एक-दूसरे के मन को समझ लेने के कोमल भाव को दर्शाती है।",
                dedicatedTo = customName,
                lengthLabel = length,
                qafiyaRadifInfo = "काफ़िया: सवाल, निहाल, खयाल | रदीफ़: हैं",
                aiVerificationReport = "2nd-AI Verified: काफ़िया (सवाल/निहाल/खयाल) और रदीफ़ (हैं) पूर्णतः शुद्ध हैं।",
                qualityScore = 96,
                youtubeSearchQuery = "Chandni Raat Romantic Hindi Shayari Mushaira",
                webSearchQuery = "चाँदनी रात की गुफ़्तगू रोमांटिक हिंदी शायरी"
            )
        }
    }

    override suspend fun generateSong(
        language: String,
        genre: String,
        mood: String,
        topic: String,
        vocalStyle: String
    ): SongResult {
        if (shouldFail) throw Exception("सॉन्ग जनरेशन विफल रहा।")
        val prompt = CreativePromptCatalog.buildSongPrompt(
            language = language,
            genre = genre,
            mood = mood,
            topic = topic,
            vocalStyle = vocalStyle
        )
        capturedSongPrompts.add(prompt)

        val lowerTopic = topic.lowercase()
        return when {
            lowerTopic.contains("यार") || lowerTopic.contains("दोस्त") || lowerTopic.contains("सफर") -> SongResult(
                title = "मीलों का ये कारवाँ",
                musicalNote = "Indie Pop Acoustic • 108 BPM • Upbeat Strum & Cajon • $vocalStyle",
                intro = "खुली सड़क है, हवाओं में नया तराना है,\nबचपन के यारों संग फिर से मुस्कुराना है।",
                verse1 = "कंधे पे बस्ता टाँगे निकले थे जिस गली से,\nआज फिर वही किस्से लौट आए हैं हँसी से।\nचाय की टपरी पे उधार के वो पुराने दिन,\nअधूरे लगते थे हर लम्हे तुम्हारे बिन।",
                preChorus = "शहर बदल गए पर दिल के पते वही हैं,\nअपनी यारी की रगों में हौसले वही हैं।",
                chorus = "चल पड़े हैं फिर से, मीलों का ये कारवाँ है,\nसच्चे यारों के संग हर मोड़ पे आसमाँ है।\nवक़्त की धूल भी धुँधला न पाएगी जिसे,\nअपनी दोस्ती की ये ऐसी अमिट दास्ताँ है।",
                verse2 = "कभी किसी की हार पे सबने कंधा थामा था,\nआँसुओं को भी कहकहों में हमने ढाला था।\nअब जो मिले हैं तो घड़ी की सुइयाँ रोक दो,\nआज की शाम फिर उसी बेफ़िक्री में झोंक दो।",
                outro = "मीलों का ये कारवाँ... यारों का ये आसमाँ...",
                bridge = "कल फिर अपनी-अपनी दुनिया में लौट जाएँगे,\nपर इस सफर के उजाले दिल में जगमगाएँगे।",
                vocalStyle = vocalStyle,
                musicGenPrompt = "Upbeat Hindi Indie Pop road-trip anthem, 108 BPM, strumming acoustic guitars, warm bassline, cajon percussion, $vocalStyle vocals",
                youtubeSearchQuery = "Meelon Ka Ye Karwan Friendship Hindi Indie Song",
                webSearchQuery = "मीलों का ये कारवाँ दोस्ती हिंदी गीत लिरिक्स"
            )

            lowerTopic.contains("उड़ान") || lowerTopic.contains("हौसलों") || genre.contains("Motivational", ignoreCase = true) -> SongResult(
                title = "पंखों में आगाज़ है",
                musicalNote = "Motivational Rock Anthem • 118 BPM • Dhol, Electric Guitar & Strings • $vocalStyle",
                intro = "राख के नीचे दबी चिंगारी को हवा दे,\nअपने ही कदमों से अब नया रास्ता बना दे।",
                verse1 = "कल जो गिरा था मैं उस अंधेरी ढलान पर,\nलिख आया हूँ अपना इरादा आसमान पर।\nज़ख्मों ने ही सिखाई है ज़िद मुझे चलने की,\nअब परवाह नहीं किसी तूफ़ान के मचलने की।",
                preChorus = "रुकना मेरी फ़ितरत नहीं, टूटना मेरा अंत नहीं,\nमेरे हौसलों की उड़ान अब कोई सीमा में बंद नहीं।",
                chorus = "उठ चल मुसाफ़िर, तेरे पंखों में नया आगाज़ है,\nहर कठिनाई से ऊँची अब तेरी परवाज़ है।\nजो कल तक तुझपे हँसते थे वो भी देखेंगे,\nतेरी मेहनत की गूँज ही तेरी असली आवाज़ है।",
                verse2 = "काँटों भरी पगडंडियों ने पाँव को मज़बूत किया,\nहर इम्तिहान ने मेरे सपनों को नया सबूत दिया।\nपसीने की हर बूँद अब मोती बनकर चमकेगी,\nमेरी जीत की सुबह अब पूरे क्षितिज पे दमकेगी।",
                outro = "पंखों में नया आगाज़ है... यही मेरी परवाज़ है...",
                bridge = "जब तक साँस में लोहा है, कोई मंज़िल दूर नहीं,\nमैं अपने मुकद्दर का कारीगर हूँ, मजबूर नहीं।",
                vocalStyle = vocalStyle,
                musicGenPrompt = "Inspirational Hindi rock fusion anthem, 118 BPM, cinematic orchestral strings, Indian dhol, soaring electric guitar, $vocalStyle vocals",
                youtubeSearchQuery = "Pankhon Mein Aagaz Hai Motivational Hindi Rock Song",
                webSearchQuery = "पंखों में आगाज़ है प्रेरणादायक हिंदी गीत लिरिक्स"
            )

            else -> SongResult(
                title = "पहली बारिश की बूँदें",
                musicalNote = "Acoustic Guitar & Bansuri • 92 BPM • $vocalStyle",
                intro = "हल्की सी हवा चली, भीगी ये शाम है,\nबूँदों की सरगम पे तेरा ही नाम है।",
                verse1 = "खिड़की पे बैठी यादें तुम्हारी,\nकागज़ की कश्ती और बातें हमारी।\nमिट्टी की सोंधी सी खुशबू जो आई,\nदिल के दरीचों में हलचल सी छाई।",
                preChorus = "बादल ने छुआ ज़मीं को जैसे,\nदिल ने पुकारा फिर तुझको वैसे...",
                chorus = "पहली बारिश की बूँदें कहती हैं मुझसे,\nहर इक मोड़ पे मिलना है तुझसे।\nभीगने दे आज रूह को इस सावन में,\nतेरा ही अक्स बसा है मेरे आँगन में।",
                verse2 = "छतरी के नीचे वो सिमटे से साये,\nवक़्त के पन्नों से फिर मुस्कुराए।\nचाय के प्याले में घुलती वो बातें,\nलौट आईं फिर से वो भीगी सी रातें।",
                outro = "बस यूँही बरसता रहे ये सावन... तेरा मेरा मन...",
                bridge = "वक़्त थम जाए यहीं, बस तेरा साथ रहे,\nहर धड़कन में तेरा ही एहसास रहे।",
                vocalStyle = vocalStyle,
                musicGenPrompt = "Bollywood acoustic romantic ballad, 92 BPM, warm acoustic guitar, bansuri flute, $vocalStyle, emotional studio mix",
                youtubeSearchQuery = "Pehli Baarish Ki Boondein Romantic Hindi Acoustic Song",
                webSearchQuery = "पहली बारिश की बूँदें रोमांटिक हिंदी गीत लिरिक्स"
            )
        }
    }

    override suspend fun analyzeLyrics(
        userLyrics: String,
        includeRevision: Boolean
    ): LyricsAnalysisResult {
        if (shouldFail) throw Exception("लिरिक्स विश्लेषण विफल रहा।")
        return LyricsAnalysisResult(
            overallScore = 88,
            emotionalImpact = "पंक्तियों में गहरा भाव और संवेदनशीलता स्पष्ट झलकती है।",
            storytelling = "कहानी का प्रवाह सहज और चित्रमयी है।",
            rhymeAndRhythm = "तुकबंदी अच्छी है, दूसरी पंक्ति में मात्राओं को और संतुलित किया जा सकता है।",
            originalityAndSingability = "गाने के लिए बोल बहुत मधुर और मौलिक हैं।",
            strengths = listOf("सुंदर रूपक", "सहज हिंदी शब्दावली", "आकर्षक मुखड़ा"),
            weaknesses = listOf("तीसरी पंक्ति थोड़ी लंबी है", "अंतरे में लय को और कसा जा सकता है"),
            improvements = listOf("मुखड़े को दो बार दोहराने योग्य बनाएँ", "काफ़िया को और स्पष्ट रखें", "बिंबों को और सजीव करें"),
            revisedLyrics = if (includeRevision) {
                "रात की खामोशी में तेरा ही नाम लिया,\nहर टूटे तारे से तेरा पैगाम लिया।"
            } else {
                ""
            }
        )
    }

    override suspend fun analyzeEmotion(userText: String): EmotionAnalysisResult {
        if (shouldFail) throw Exception("इमोशन विश्लेषण विफल रहा।")
        return EmotionAnalysisResult(
            detectedEmotion = "आशावादी और भावुक (Hopeful & Nostalgic)",
            intensityPercent = 82,
            explanationHindi = "आपके शब्दों में पुरानी यादों की मिठास और आने वाले कल के लिए उम्मीद साफ झलकती है।",
            supportiveMessage = "आपकी संवेदनशीलता आपकी ताकत है। अपने दिल की आवाज़ पर भरोसा रखें।",
            matchingShayari = "बीते लम्हों की कसक दिल में बसाए रखना,\nउम्मीद का दिया हर हाल में जलाए रखना।",
            matchingSongLyrics = "फिर नई सुबह आएगी, बदली ये छँट जाएगी,\nतेरे कदमों की आहट से राहें महक जाएंगी।"
        )
    }

    override suspend fun generateLifeGuidance(situation: String): LifeGuidanceResult {
        if (shouldFail) throw Exception("मार्गदर्शन सेवा अभी उपलब्ध नहीं है।")
        val (refHindi, sanskrit) = VerifiedGitaVerses.resolve("2.47")
        return LifeGuidanceResult(
            chapterAndVerse = refHindi,
            traditionalTeachingHindi = "मनुष्य का अधिकार केवल अपने कर्म करने में है, उसके फलों में कभी नहीं।",
            modernInterpretationHindi = "अपने प्रयास की गुणवत्ता पर ध्यान दें और परिणाम की चिंता से मन को बोझिल न करें।",
            practicalSteps = listOf(
                "आज के मुख्य कार्य की छोटी सूची बनाएँ",
                "परिणाम की तुलना दूसरों से न करें",
                "प्रतिदिन 10 मिनट शांत आत्म-चिंतन करें"
            ),
            peacefulReflection = "मेरा शांत मन और निरंतर प्रयास ही मेरी सबसे बड़ी शक्ति है।",
            verifiedSanskritShloka = sanskrit
        )
    }

    override suspend fun generateStatusCaption(
        topicOrMood: String,
        style: String
    ): StatusCaptionResult {
        if (shouldFail) throw Exception("स्टेटस जनरेशन विफल रहा।")
        return StatusCaptionResult(
            statusHindi = "लहजे में सादगी और इरादों में चट्टान रखते हैं,\nहम अपनी खुद्दारी से अपनी पहचान रखते हैं।",
            statusHinglish = "Lehje mein saadgi aur iraadon mein chattaan rakhte hain,\nHum apni khuddari से apni pehchaan rakhte hain.",
            socialCaption = "सादगी और खुद्दारी ✨ #RoyalVibes",
            hashtags = listOf("#HindiShayari", "#Khuddari", "#KavyaStudio")
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: CreationRepository
    private lateinit var fakeAi: FakeCreativeAiGenerator
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setTransactionExecutor { it.run() }
            .setQueryExecutor { it.run() }
            .build()
        repository = CreationRepository(database.creationDao(), context)
        fakeAi = FakeCreativeAiGenerator()
        viewModel = MainViewModel(repository, fakeAi)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun idleMain() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `read app_name from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Song & Shayari Generator", appName)
    }

    @Test
    fun `test navigation across all 9 screens including Poster Maker and My Creations`() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AiSongShayariApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // 1. Home Screen
        assertEquals(AppScreen.HOME, viewModel.currentScreen.value)
        composeTestRule.onNodeWithTag("home_hero_card").assertIsDisplayed()

        // 2. Shayari Studio Screen (Bottom Nav)
        composeTestRule.onNodeWithTag("nav_item_shayari").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.SHAYARI, viewModel.currentScreen.value)

        // 3. Song Maker Screen (Bottom Nav)
        composeTestRule.onNodeWithTag("nav_item_song").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.SONG, viewModel.currentScreen.value)

        // 4. Gita Guide Screen (Bottom Nav)
        composeTestRule.onNodeWithTag("nav_item_guidance").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.LIFE_GUIDANCE, viewModel.currentScreen.value)
        composeTestRule.onNodeWithTag("guidance_disclaimer_banner").assertIsDisplayed()

        // 5. My Creations / History Screen (Bottom Nav)
        composeTestRule.onNodeWithTag("top_nav_history").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.HISTORY, viewModel.currentScreen.value)

        // 6. Emotion & Lyrics Analyzer Screen (Top Bar)
        composeTestRule.onNodeWithTag("nav_item_analysis").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.LYRICS_ANALYSIS, viewModel.currentScreen.value)

        // 7. Poster Maker Screen (Top Bar)
        composeTestRule.onNodeWithTag("top_nav_poster").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.POSTER_MAKER, viewModel.currentScreen.value)
        composeTestRule.onNodeWithTag("poster_preview_canvas").assertIsDisplayed()

        // 8. Favorites Screen (Top Bar)
        composeTestRule.onNodeWithTag("top_nav_favorites").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.FAVORITES, viewModel.currentScreen.value)

        // 9. Settings Screen (Top Bar)
        composeTestRule.onNodeWithTag("top_nav_settings").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.SETTINGS, viewModel.currentScreen.value)
    }

    @Test
    fun `test 5 Shayari themes and 3 Song themes for creative quality, coherence, and feature-specific prompts`() = runBlocking {
        // 1. Verify feature-specific system instructions and model parameters
        val service = FirebaseAiCreativeService()
        assertTrue(service.shayariConfig.temperature in 0.8f..0.95f)
        assertTrue(service.shayariConfig.maxOutputTokens >= 2048)
        assertTrue(service.songConfig.maxOutputTokens >= 4096)
        assertTrue(service.guidanceConfig.temperature <= 0.7f)

        val systemPrompts = setOf(
            CreativePromptCatalog.SHAYARI_SYSTEM_INSTRUCTION,
            CreativePromptCatalog.SONG_SYSTEM_INSTRUCTION,
            CreativePromptCatalog.GITA_GUIDANCE_SYSTEM_INSTRUCTION,
            CreativePromptCatalog.EMOTION_ANALYSIS_SYSTEM_INSTRUCTION,
            CreativePromptCatalog.LYRICS_ANALYSIS_SYSTEM_INSTRUCTION,
            CreativePromptCatalog.STATUS_CAPTION_SYSTEM_INSTRUCTION
        )
        assertEquals("Each feature must have its own distinct system instruction", 6, systemPrompts.size)

        // 2. Test 5 distinct Shayari themes: Heartbreak, Romantic Love, Loneliness, Attitude, Motivation
        data class ShayariCase(val category: String, val mood: String, val topic: String)
        val fiveShayariThemes = listOf(
            ShayariCase("Breakup (ब्रेकअप)", "Deep & Emotional (गहरा जज़्बात)", "अधूरा वादा और ठंडी चाय"),
            ShayariCase("Love / Romantic (रोमांटिक)", "Romantic (रोमांटिक)", "चाँदनी रात में खामोश गुफ़्तगू"),
            ShayariCase("Sad (दर्द)", "Nostalgic (यादें)", "भीड़ के बाद सूने कमरे की तन्हाई"),
            ShayariCase("Attitude (एटीट्यूड)", "Royal & Bold (शाही अंदाज़)", "खुद्दारी और ज़मीन से जुड़ा वकार"),
            ShayariCase("Motivational (प्रेरणा)", "Inspiring (जोश और हौसला)", "असफलता की राख से नई उड़ान")
        )

        val generatedShayariTexts = mutableSetOf<String>()
        fiveShayariThemes.forEach { testCase ->
            viewModel.setShayariCategory(testCase.category)
            viewModel.setShayariMood(testCase.mood)
            viewModel.setShayariLength("4-Line Shayari (चार पंक्तियाँ)")
            viewModel.setShayariTopic(testCase.topic)
            viewModel.generateShayari()
            idleMain()

            val state = viewModel.shayariUiState.value as FeatureUiState.Success
            val result = state.data
            assertTrue("Shayari Hindi must not be blank for ${testCase.category}", result.shayariHindi.isNotBlank())
            assertTrue("Shayari Hinglish must not be blank for ${testCase.category}", result.shayariHinglish.isNotBlank())
            assertTrue("Shayari meaning must not be blank for ${testCase.category}", result.meaningHindi.isNotBlank())
            assertEquals("4-line shayari should have 4 lines", 4, result.shayariHindi.lines().size)
            generatedShayariTexts.add(result.shayariHindi)
        }
        assertEquals("All 5 Shayari themes must produce unique, non-template outputs", 5, generatedShayariTexts.size)
        assertEquals(5, fakeAi.capturedShayariPrompts.size)
        assertTrue(fakeAi.capturedShayariPrompts.all { it.contains("Self-Check & Revision") })

        // 3. Test 3 distinct Song themes: Romantic Rain, Friendship Road Trip, Motivational Rise
        val threeSongThemes = listOf(
            Triple("Bollywood Romantic", "Romantic (रोमांटिक)", "पहली बारिश और तुम्हारी याद"),
            Triple("Indie Pop (इंडी पॉप)", "Happy & Upbeat (खुशनुमा)", "सच्चे यारों के साथ लंबा सफर"),
            Triple("Motivational Anthem", "Energetic (ऊर्जावान)", "हार के बाद हौसलों की नई उड़ान")
        )

        val generatedSongChoruses = mutableSetOf<String>()
        threeSongThemes.forEach { (genre, mood, topic) ->
            viewModel.setSongGenre(genre)
            viewModel.setSongMood(mood)
            viewModel.setSongTopic(topic)
            viewModel.generateSong()
            idleMain()

            val songState = viewModel.songUiState.value as FeatureUiState.Success
            val song = songState.data
            assertTrue("Mukhda/Intro must be present", song.mukhda.isNotBlank())
            assertTrue("Chorus must be present", song.chorus.isNotBlank())
            assertTrue("Verse 1 must be present", song.verse1.isNotBlank())
            assertTrue("Verse 2 must be present", song.verse2.isNotBlank())
            assertNotEquals("Verse 1 and Verse 2 must advance the story without repeating", song.verse1, song.verse2)
            assertTrue("Bridge must be present", song.bridge.isNotBlank())
            assertTrue("Music generation prompt must be present", song.musicGenPrompt.isNotBlank())
            generatedSongChoruses.add(song.chorus)
        }
        assertEquals("All 3 song themes must produce distinct songs", 3, generatedSongChoruses.size)

        // 4. Verify JSON sanitization & poetic line normalization helper functions
        val fencedJson = """
            ```json
            {
              "title": "तेरी याद",
              "shayariHindi": "पहली पंक्ति\nदूसरी पंक्ति"
            }
            ```
        """.trimIndent()
        val parsed = FirebaseAiCreativeService.parseCleanJsonObject(fencedJson)
        assertEquals("तेरी याद", parsed.getString("title"))
        val normalized = FirebaseAiCreativeService.normalizePoeticLines("पहली पंक्ति\\n\\nदूसरी पंक्ति")
        assertEquals("पहली पंक्ति\nदूसरी पंक्ति", normalized)
    }

    @Test
    fun `test all upgraded AI features, Emotion Analyzer, Poster Maker, Gita Sanskrit Shloka, and Room persistence`() = runBlocking {
        // Test empty input validation on Song Maker, Emotion Analyzer, Lyrics Analysis, and Life Guidance
        viewModel.generateSong()
        idleMain()
        assertTrue(viewModel.songUiState.value is FeatureUiState.Error)

        viewModel.analyzeEmotion()
        idleMain()
        assertTrue(viewModel.emotionUiState.value is FeatureUiState.Error)

        viewModel.analyzeLyrics()
        idleMain()
        assertTrue(viewModel.analysisUiState.value is FeatureUiState.Error)

        viewModel.generateLifeGuidance()
        idleMain()
        assertTrue(viewModel.guidanceUiState.value is FeatureUiState.Error)

        // Test failed AI request error propagation
        fakeAi.shouldFail = true
        viewModel.generateShayari()
        idleMain()
        assertTrue(viewModel.shayariUiState.value is FeatureUiState.Error)

        // Recover and test all features succeeding
        fakeAi.shouldFail = false

        // Feature 1: Shayari Studio (with custom name & length)
        viewModel.setShayariCategory("Devotional (भक्ति)")
        viewModel.setShayariLength("4-Line Shayari (चार पंक्तियाँ)")
        viewModel.setShayariCustomName("आरव")
        viewModel.setShayariTopic("महादेव की कृपा")
        viewModel.generateShayari()
        idleMain()
        val shayariState = viewModel.shayariUiState.value as FeatureUiState.Success
        assertTrue(shayariState.data.title.contains("आरव"))
        viewModel.toggleShayariFavorite()
        idleMain()

        // Feature 2: Song Maker (with vocal style, bridge, and music-gen prompt)
        viewModel.setSongVocalStyle("Sufi Qawwali (सूफ़ी अंदाज़)")
        viewModel.setSongTopic("पहली बारिश की शाम")
        viewModel.generateSong()
        idleMain()
        val songState = viewModel.songUiState.value as FeatureUiState.Success
        assertTrue(songState.data.bridge.isNotBlank())
        assertTrue(songState.data.musicGenPrompt.contains("92 BPM"))

        // Feature 3: Bhagavad Gita Guide (with verified Sanskrit shloka)
        viewModel.setGuidanceSituation("मुझे अपने करियर और भविष्य की बहुत चिंता हो रही है")
        viewModel.generateLifeGuidance()
        idleMain()
        val guidanceState = viewModel.guidanceUiState.value as FeatureUiState.Success
        assertTrue(guidanceState.data.chapterAndVerse.contains("अध्याय 2"))
        assertTrue(guidanceState.data.verifiedSanskritShloka.contains("कर्मण्येवाधिकारस्ते"))

        // Feature 4A: AI Emotion Analyzer
        viewModel.setEmotionInput("आज पुरानी डायरी पढ़कर दिल भर आया, पर कल की नई शुरुआत का हौसला भी है।")
        viewModel.analyzeEmotion()
        idleMain()
        val emotionState = viewModel.emotionUiState.value as FeatureUiState.Success
        assertEquals(82, emotionState.data.intensityPercent)
        assertTrue(emotionState.data.matchingShayari.isNotBlank())

        // Feature 4B: AI Lyrics Analysis
        viewModel.setLyricsInput("रात की खामोशी में तेरा नाम लिया, हर टूटे तारे से तेरा पैगाम लिया।")
        viewModel.setIncludeRevisedLyrics(true)
        viewModel.analyzeLyrics()
        idleMain()
        val analysisState = viewModel.analysisUiState.value as FeatureUiState.Success
        assertEquals(88, analysisState.data.overallScore)
        assertTrue(analysisState.data.revisedLyrics.isNotBlank())

        // Feature 5: AI Status & Poster Maker (AI Status generation + Save to My Creations)
        viewModel.openPosterMakerWithText("खामोश निगाहों में हज़ारों सवाल हैं", "चाँदनी रात")
        assertEquals(AppScreen.POSTER_MAKER, viewModel.currentScreen.value)
        viewModel.generateAiStatusForPoster("खुद्दारी और सादगी")
        idleMain()
        assertTrue(viewModel.posterText.value.contains("खुद्दारी"))
        viewModel.savePosterToCreations()
        idleMain()

        // Feature 8: Settings Dark/Light theme toggle
        assertTrue(viewModel.userSettings.value.isDarkTheme)
        viewModel.updateDarkTheme(false)
        assertFalse(viewModel.userSettings.value.isDarkTheme)
        viewModel.updateDarkTheme(true)

        // Verify Room My Creations & Favorites persistence (6 saved items total)
        val allSaved = repository.allCreations.first()
        assertEquals(6, allSaved.size)
        assertTrue(allSaved.all { it.resultText.isNotBlank() && it.timestamp > 0L })

        val posterOnly = repository.getCreationsByType(FeatureType.POSTER).first()
        assertEquals(1, posterOnly.size)

        val emotionOnly = repository.getCreationsByType(FeatureType.EMOTION_ANALYZER).first()
        assertEquals(1, emotionOnly.size)

        val favSaved = repository.favoriteCreations.first()
        assertEquals(1, favSaved.size)
        assertTrue((favSaved.first().favoritedAt ?: 0L) > 0L)

        //Also favorite the generated song and verify favoriteShayariAndSongs Room schema
        viewModel.toggleSongFavorite()
        idleMain()
        val favShayariAndSongs = repository.favoriteShayariAndSongs.first()
        assertEquals(2, favShayariAndSongs.size)
        assertTrue(favShayariAndSongs.any { it.contentType == FeatureType.SHAYARI })
        assertTrue(favShayariAndSongs.any { it.contentType == FeatureType.SONG })
        assertTrue(favShayariAndSongs.all { it.resultText.isNotBlank() && it.favoritedAt > 0L })

        // Verify Second-AI verification & polishing pass updates Shayari with critic report
        viewModel.verifyCurrentShayariWithSecondAi()
        idleMain()
        val verifiedShayari = (viewModel.shayariUiState.value as FeatureUiState.Success).data
        assertTrue(verifiedShayari.aiVerificationReport.isNotBlank())
        assertTrue(verifiedShayari.qualityScore >= 90)
        assertTrue(verifiedShayari.youtubeSearchQuery.isNotBlank())
        assertTrue(verifiedShayari.webSearchQuery.isNotBlank())

        // Delete single item & Clear all history
        viewModel.deleteCreation(allSaved.first().id)
        idleMain()
        assertEquals(5, repository.allCreations.first().size)

        viewModel.clearAllHistory()
        idleMain()
        assertEquals(0, repository.allCreations.first().size)
        assertEquals(0, repository.favoriteEntities.first().size)
    }

    @Test
    fun `test Shayari UI generation, Save to Favorites toggle, 2nd AI verification, YouTube search, and copy to clipboard`() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AiSongShayariApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Scroll Home LazyColumn to Shayari card and open Shayari screen
        composeTestRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("feature_card_shayari"))
        composeTestRule.onNodeWithTag("feature_card_shayari")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.SHAYARI, viewModel.currentScreen.value)

        // Trigger Shayari generation
        viewModel.generateShayari()
        idleMain()
        composeTestRule.waitForIdle()

        // Scroll Shayari LazyColumn to result card
        composeTestRule.onNodeWithTag("shayari_screen_list")
            .performScrollToNode(hasTestTag("shayari_result_card"))
        composeTestRule.onNodeWithTag("shayari_result_card")
            .assertIsDisplayed()

        // Verify Save to Favorites toggle row exists and toggles favorite in Room
        composeTestRule.onNodeWithTag("shayari_save_to_favorites_row")
            .assertExists()
        composeTestRule.onNodeWithTag("shayari_save_to_favorites_row")
            .performSemanticsAction(SemanticsActions.OnClick)
        idleMain()
        composeTestRule.waitForIdle()
        assertTrue((viewModel.shayariUiState.value as FeatureUiState.Success).isFavorite)
        val favItems = runBlocking { repository.favoriteShayariAndSongs.first() }
        assertEquals(1, favItems.size)

        // Verify Second-AI verification card & YouTube/Web search buttons exist
        composeTestRule.onNodeWithTag("shayari_second_ai_verification_card")
            .assertExists()
        composeTestRule.onNodeWithTag("shayari_verify_second_ai_button")
            .assertExists()
        composeTestRule.onNodeWithTag("shayari_youtube_web_card")
            .assertExists()
        composeTestRule.onNodeWithTag("shayari_youtube_button")
            .assertExists()
        composeTestRule.onNodeWithTag("shayari_web_search_button")
            .assertExists()

        // Invoke copy button
        composeTestRule.onNodeWithTag("shayari_copy_button")
            .performSemanticsAction(SemanticsActions.OnClick)
        idleMain()
        composeTestRule.waitForIdle()

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copiedText = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
        assertTrue("Expected copied shayari in clipboard, got: $copiedText", copiedText.contains("चाँदनी रात की गुफ़्तगू"))
    }
}
