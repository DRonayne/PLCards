package com.darach.plcards.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.darach.plcards.domain.model.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val IS_WC2002_MODE = booleanPreferencesKey("is_wc2002_mode")
        val DEFAULT_SORT_ORDER = stringPreferencesKey("default_sort_order")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: false,
            useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true,
            isWc2002Mode = preferences[PreferencesKeys.IS_WC2002_MODE] ?: false,
            defaultSortOrder = SortOrder.fromSettingsKey(
                preferences[PreferencesKeys.DEFAULT_SORT_ORDER] ?: "player_name_asc"
            ),
        )
    }

    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDarkTheme
        }
    }

    suspend fun setDynamicColor(useDynamicColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = useDynamicColor
        }
    }

    suspend fun setWc2002Mode(isWc2002Mode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_WC2002_MODE] = isWc2002Mode
        }
    }

    suspend fun setDefaultSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SORT_ORDER] = SortOrder.toSettingsKey(sortOrder)
        }
    }

}

data class AppSettings(
    val isDarkTheme: Boolean,
    val useDynamicColor: Boolean,
    val isWc2002Mode: Boolean? = false,
    val defaultSortOrder: SortOrder = SortOrder.PLAYER_NAME_ASC,
)