package com.dabi.partypoker.featureCore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.dabi.partypoker.featureCore.data.PlayerSettingsState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class PlayerSettingsRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val VIBRATION = booleanPreferencesKey("vibration")
    }

    val currentSettings = dataStore.data.map { preferences ->
        PlayerSettingsState(
            vibration = preferences[VIBRATION] ?: true
        )
    }

    suspend fun toggleVibration() {
        dataStore.edit { preferences ->
            preferences[VIBRATION] = !currentSettings.first().vibration
        }
    }
}