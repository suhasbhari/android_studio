package com.example.broadcast

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var chargerReceiver: ChargerReceiver

    private lateinit var tvStatus: TextView
    private lateinit var tvAlarmName: TextView
    private lateinit var btnEnable: Button
    private lateinit var btnChange: Button

    private var isAlarmEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PreferencesManager(this)
        chargerReceiver = ChargerReceiver()

        // Initialize views
        tvStatus = findViewById(R.id.tvStatus)
        tvAlarmName = findViewById(R.id.tvAlarmName)
        btnEnable = findViewById(R.id.btnEnable)
        btnChange = findViewById(R.id.btnChange)

        // Setup observers
        setupObservers()

        // Setup button listeners
        setupButtons()
    }

    private fun setupObservers() {
        // Observe alarm enabled status
        lifecycleScope.launch {
            prefsManager.isAlarmEnabled.collect { enabled ->
                isAlarmEnabled = enabled
                updateUI()
            }
        }

        // Observe alarm name
        lifecycleScope.launch {
            prefsManager.alarmAudioName.collect { name ->
                tvAlarmName.text = "Current Alarm: $name"
            }
        }
    }

    private fun setupButtons() {
        btnEnable.setOnClickListener {
            lifecycleScope.launch {
                if (isAlarmEnabled) {
                    // Disable alarm and stop if ringing
                    prefsManager.setAlarmEnabled(false)
                    AlarmPlayer.stopAlarm(this@MainActivity)
                } else {
                    // Enable alarm
                    prefsManager.setAlarmEnabled(true)
                }
            }
        }

        btnChange.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateUI() {
        if (isAlarmEnabled) {
            tvStatus.text = "Alarm Status: Enabled ✓"
            btnEnable.text = "Disable Alarm / Stop Ringing"
        } else {
            tvStatus.text = "Alarm Status: Disabled"
            btnEnable.text = "Enable Alarm"
        }
    }

    override fun onResume() {
        super.onResume()
        // Register receiver dynamically
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(chargerReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Unregister receiver
        try {
            unregisterReceiver(chargerReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}