package com.example.broadcast

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "charger_alarm_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val ALARM_AUDIO_URI = stringPreferencesKey("alarm_audio_uri")
        val ALARM_AUDIO_NAME = stringPreferencesKey("alarm_audio_name")
    }

    // Get alarm enabled status
    val isAlarmEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ALARM_ENABLED] ?: false
    }

    // Get alarm audio URI
    val alarmAudioUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ALARM_AUDIO_URI]
    }

    // Get alarm audio name
    val alarmAudioName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ALARM_AUDIO_NAME] ?: "Default Alarm"
    }

    // Set alarm enabled/disabled
    suspend fun setAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_ENABLED] = enabled
        }
    }

    // Set alarm audio
    suspend fun setAlarmAudio(uri: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_AUDIO_URI] = uri
            preferences[ALARM_AUDIO_NAME] = name
        }
    }
}