package com.sabid.khotianv2.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sabid.khotianv2.domain.model.CommaStyle
import com.sabid.khotianv2.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val COMMA_STYLE = stringPreferencesKey("comma_style")
    }

    override fun getCommaStyle(): Flow<CommaStyle> {
        return dataStore.data.map { preferences ->
            val styleName = preferences[PreferencesKeys.COMMA_STYLE] ?: CommaStyle.BD.name
            CommaStyle.valueOf(styleName)
        }
    }

    override suspend fun setCommaStyle(style: CommaStyle) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMMA_STYLE] = style.name
        }
    }
}
