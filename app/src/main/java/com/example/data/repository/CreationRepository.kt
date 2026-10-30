package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.CreationDao
import com.example.data.local.CreationEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.FeatureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val defaultLanguage: String = "Hindi (हिंदी)",
    val fontScale: Float = 1.0f,
    val autoSaveHistory: Boolean = true,
    val isDarkTheme: Boolean = true
)

class CreationRepository(
    private val creationDao: CreationDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("kavya_user_settings", Context.MODE_PRIVATE)

    private val _userSettings = MutableStateFlow(loadSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    val allCreations: Flow<List<CreationEntity>> = creationDao.getAllCreations()
    val favoriteCreations: Flow<List<CreationEntity>> = creationDao.getFavoriteCreations()
    val favoriteEntities: Flow<List<FavoriteEntity>> = creationDao.getAllFavoriteEntities()
    val favoriteShayariAndSongs: Flow<List<FavoriteEntity>> = creationDao.getFavoriteShayariAndSongs()

    fun getCreationsByType(featureType: FeatureType): Flow<List<CreationEntity>> {
        return creationDao.getCreationsByType(featureType.name)
    }

    fun getFavoriteEntitiesByType(featureType: FeatureType): Flow<List<FavoriteEntity>> {
        return creationDao.getFavoriteEntitiesByType(featureType)
    }

    fun searchCreations(query: String): Flow<List<CreationEntity>> {
        return creationDao.searchCreations(query)
    }

    private fun loadSettings(): UserSettings {
        val defaultLang = prefs.getString(KEY_DEFAULT_LANG, "Hindi (हिंदी)") ?: "Hindi (हिंदी)"
        val fontScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        val autoSave = prefs.getBoolean(KEY_AUTO_SAVE, true)
        val isDark = prefs.getBoolean(KEY_DARK_THEME, true)
        return UserSettings(
            defaultLanguage = defaultLang,
            fontScale = fontScale,
            autoSaveHistory = autoSave,
            isDarkTheme = isDark
        )
    }

    fun updateDefaultLanguage(language: String) {
        prefs.edit().putString(KEY_DEFAULT_LANG, language).apply()
        _userSettings.value = _userSettings.value.copy(defaultLanguage = language)
    }

    fun updateFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
        _userSettings.value = _userSettings.value.copy(fontScale = scale)
    }

    fun updateAutoSaveHistory(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
        _userSettings.value = _userSettings.value.copy(autoSaveHistory = enabled)
    }

    fun updateDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        _userSettings.value = _userSettings.value.copy(isDarkTheme = isDark)
    }

    suspend fun insertCreation(creation: CreationEntity): Long {
        val id = creationDao.insertCreation(creation)
        if (creation.isFavorite) {
            creationDao.syncFavoriteState(id, true)
        }
        return id
    }

    suspend fun updateCreationContent(
        id: Long,
        primaryContent: String,
        extraDetailsJson: String
    ) {
        creationDao.updateCreationContent(id, primaryContent, extraDetailsJson)
        val existing = creationDao.getCreationById(id)
        if (existing?.isFavorite == true) {
            creationDao.syncFavoriteState(id, true)
        }
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        creationDao.syncFavoriteState(id, isFavorite)
    }

    suspend fun deleteCreationById(id: Long) {
        creationDao.deleteCreationById(id)
    }

    suspend fun clearAllCreations() {
        creationDao.clearAllCreations()
    }

    companion object {
        private const val KEY_DEFAULT_LANG = "default_language"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_AUTO_SAVE = "auto_save_history"
        private const val KEY_DARK_THEME = "is_dark_theme"
    }
}
