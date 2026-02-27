package com.example.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChargerReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "ChargerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d(TAG, "Charger connected")
                // Optional: Stop alarm if it's ringing
                AlarmPlayer.stopAlarm(context)
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d(TAG, "Charger disconnected")
                handleChargerDisconnected(context)
            }
        }
    }

    private fun handleChargerDisconnected(context: Context) {
        // Check if alarm is enabled
        val prefsManager = PreferencesManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = prefsManager.isAlarmEnabled.first()

            if (isEnabled) {
                Log.d(TAG, "Alarm is enabled, starting alarm")
                AlarmPlayer.playAlarm(context)
            } else {
                Log.d(TAG, "Alarm is disabled, ignoring event")
            }
        }
    }
}