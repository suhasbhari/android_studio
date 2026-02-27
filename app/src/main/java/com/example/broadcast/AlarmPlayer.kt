package com.example.broadcast

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.media.RingtoneManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object AlarmPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private const val TAG = "AlarmPlayer"

    suspend fun playAlarm(context: Context) {
        withContext(Dispatchers.Main) {
            try {
                stopAlarm(context)

                val prefsManager = PreferencesManager(context)
                val uriString = prefsManager.alarmAudioUri.first()

                val uri = if (!uriString.isNullOrEmpty()) {
                    Uri.parse(uriString)
                } else {
                    // Use default alarm sound
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, uri!!)
                    isLooping = true
                    prepare()
                    start()
                }

                Log.d(TAG, "Alarm started playing")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing alarm: ${e.message}", e)
            }
        }
    }

    fun stopAlarm(context: Context) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            Log.d(TAG, "Alarm stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm: ${e.message}", e)
        }
    }
}