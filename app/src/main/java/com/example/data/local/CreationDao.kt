package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {
    @Query("SELECT * FROM creations ORDER BY timestamp DESC")
    fun getAllCreations(): Flow<List<CreationEntity>>

    @Query("SELECT * FROM creations WHERE featureType = :featureType ORDER BY timestamp DESC")
    fun getCreationsByType(featureType: String): Flow<List<CreationEntity>>

    @Query(
        """
        SELECT * FROM creations 
        WHERE title LIKE '%' || :query || '%' 
           OR primaryContent LIKE '%' || :query || '%' 
           OR categoryOrGenre LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        """
    )
    fun searchCreations(query: String): Flow<List<CreationEntity>>

    @Query("SELECT * FROM creations WHERE isFavorite = 1 ORDER BY COALESCE(favoritedAt, timestamp) DESC")
    fun getFavoriteCreations(): Flow<List<CreationEntity>>

    @Query(
        """
        SELECT * FROM creations 
        WHERE isFavorite = 1 AND featureType = :featureType 
        ORDER BY COALESCE(favoritedAt, timestamp) DESC
        """
    )
    fun getFavoriteCreationsByType(featureType: String): Flow<List<CreationEntity>>

    @Query("SELECT * FROM favorites ORDER BY favoritedAt DESC")
    fun getAllFavoriteEntities(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE contentType = :contentType ORDER BY favoritedAt DESC")
    fun getFavoriteEntitiesByType(contentType: FeatureType): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE contentType IN ('SHAYARI', 'SONG') ORDER BY favoritedAt DESC")
    fun getFavoriteShayariAndSongs(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM creations WHERE id = :id LIMIT 1")
    suspend fun getCreationById(id: Long): CreationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: CreationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteEntity(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE creationId = :creationId")
    suspend fun deleteFavoriteByCreationId(creationId: Long)

    @Query("UPDATE creations SET isFavorite = :isFavorite, favoritedAt = :favoritedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(
        id: Long,
        isFavorite: Boolean,
        favoritedAt: Long? = if (isFavorite) System.currentTimeMillis() else null
    )

    @Query("UPDATE creations SET primaryContent = :primaryContent, extraDetailsJson = :extraDetailsJson WHERE id = :id")
    suspend fun updateCreationContent(
        id: Long,
        primaryContent: String,
        extraDetailsJson: String
    )

    @Transaction
    suspend fun syncFavoriteState(id: Long, isFavorite: Boolean) {
        val now = if (isFavorite) System.currentTimeMillis() else null
        updateFavoriteStatus(id, isFavorite, now)
        if (isFavorite) {
            val item = getCreationById(id)
            if (item != null) {
                insertFavoriteEntity(
                    FavoriteEntity(
                        creationId = item.id,
                        contentType = item.contentType,
                        title = item.title,
                        categoryOrGenre = item.categoryOrGenre,
                        moodOrLanguage = item.moodOrLanguage,
                        userPrompt = item.userPrompt,
                        resultText = item.resultText,
                        secondaryText = item.secondaryContent,
                        youtubeSearchQuery = item.youtubeSearchQuery,
                        webSearchQuery = item.webSearchQuery,
                        createdAt = item.timestamp,
                        favoritedAt = now ?: System.currentTimeMillis()
                    )
                )
            }
        } else {
            deleteFavoriteByCreationId(id)
        }
    }

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteCreationRowById(id: Long)

    @Transaction
    suspend fun deleteCreationById(id: Long) {
        deleteFavoriteByCreationId(id)
        deleteCreationRowById(id)
    }

    @Query("DELETE FROM creations")
    suspend fun deleteAllCreationRows()

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavoriteRows()

    @Transaction
    suspend fun clearAllCreations() {
        deleteAllFavoriteRows()
        deleteAllCreationRows()
    }
}
