package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class FeatureType(val labelHindi: String, val labelEnglish: String) {
    SHAYARI("शायरी स्टूडियो", "Shayari Studio"),
    SONG("सॉन्ग मेकर", "Song Maker"),
    LYRICS_ANALYSIS("लिरिक्स विश्लेषण", "Lyrics Analysis"),
    EMOTION_ANALYZER("भावना विश्लेषण", "Emotion Analyzer"),
    LIFE_GUIDANCE("गीता गाइड", "Gita Guide"),
    POSTER("स्टेटस व पोस्टर", "Status & Poster");

    companion object {
        fun fromName(value: String): FeatureType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SHAYARI
        }
    }
}

typealias ContentType = FeatureType

class DatabaseConverters {
    @TypeConverter
    fun fromFeatureType(type: FeatureType): String = type.name

    @TypeConverter
    fun toFeatureType(value: String): FeatureType = FeatureType.fromName(value)
}

/**
 * Primary Room entity representing a generated AI creation stored in local history,
 * with content type, result text, AI verification details, YouTube/Web search queries,
 * favorite status, and creation/favorite timestamps.
 */
@Entity(
    tableName = "creations",
    indices = [
        Index(value = ["featureType"]),
        Index(value = ["isFavorite"]),
        Index(value = ["timestamp"])
    ]
)
data class CreationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "featureType")
    val featureType: String, // FeatureType.name

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "categoryOrGenre")
    val categoryOrGenre: String,

    @ColumnInfo(name = "moodOrLanguage")
    val moodOrLanguage: String,

    @ColumnInfo(name = "userPrompt")
    val userPrompt: String,

    @ColumnInfo(name = "primaryContent")
    val primaryContent: String, // Main generated result text

    @ColumnInfo(name = "secondaryContent")
    val secondaryContent: String = "", // Revised lyrics, music prompt, or meaning

    @ColumnInfo(name = "extraDetailsJson")
    val extraDetailsJson: String = "", // AI verification report or metadata

    @ColumnInfo(name = "youtubeSearchQuery")
    val youtubeSearchQuery: String = "",

    @ColumnInfo(name = "webSearchQuery")
    val webSearchQuery: String = "",

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "favoritedAt")
    val favoritedAt: Long? = null
) {
    val contentType: ContentType
        get() = FeatureType.fromName(featureType)

    val resultText: String
        get() = primaryContent
}

/**
 * Dedicated Room entity for storing a user's favorite generated shayari, songs,
 * and other creations with content type, result text, YouTube/web search links,
 * AI verification notes, and timestamps.
 */
@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["creationId"], unique = true),
        Index(value = ["contentType"]),
        Index(value = ["favoritedAt"])
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val favoriteId: Long = 0L,

    @ColumnInfo(name = "creationId")
    val creationId: Long,

    @ColumnInfo(name = "contentType")
    val contentType: FeatureType,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "categoryOrGenre")
    val categoryOrGenre: String,

    @ColumnInfo(name = "moodOrLanguage")
    val moodOrLanguage: String,

    @ColumnInfo(name = "userPrompt")
    val userPrompt: String,

    @ColumnInfo(name = "resultText")
    val resultText: String,

    @ColumnInfo(name = "secondaryText")
    val secondaryText: String = "",

    @ColumnInfo(name = "youtubeSearchQuery")
    val youtubeSearchQuery: String = "",

    @ColumnInfo(name = "webSearchQuery")
    val webSearchQuery: String = "",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "favoritedAt")
    val favoritedAt: Long = System.currentTimeMillis()
)
