package com.example.broadcast

import android.content.Context
import android.os.BatteryManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class BatteryLog(
    val timestamp: Long,
    val level: Int,
    val isCharging: Boolean
)

private val Context.batteryDataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_logs")

class BatteryTracker(private val context: Context) {

    private val gson = Gson()
    private val BATTERY_LOGS_KEY = stringPreferencesKey("battery_logs")

    suspend fun logBatteryLevel() {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging

        val log = BatteryLog(
            timestamp = System.currentTimeMillis(),
            level = level,
            isCharging = isCharging
        )

        // Get existing logs
        context.batteryDataStore.edit { preferences ->
            val existingLogsJson = preferences[BATTERY_LOGS_KEY] ?: "[]"
            val logs = gson.fromJson<MutableList<BatteryLog>>(
                existingLogsJson,
                object : TypeToken<MutableList<BatteryLog>>() {}.type
            ) ?: mutableListOf()

            // Add new log
            logs.add(log)

            // Keep only last 100 logs
            if (logs.size > 100) {
                logs.removeAt(0)
            }

            // Save back
            preferences[BATTERY_LOGS_KEY] = gson.toJson(logs)
        }
    }

    val batteryLogs: Flow<List<BatteryLog>> = context.batteryDataStore.data.map { preferences ->
        val logsJson = preferences[BATTERY_LOGS_KEY] ?: "[]"
        gson.fromJson(logsJson, object : TypeToken<List<BatteryLog>>() {}.type) ?: emptyList()
    }
}