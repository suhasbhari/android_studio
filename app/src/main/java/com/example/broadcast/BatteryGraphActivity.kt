package com.example.broadcast

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class BatteryGraphActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var batteryTracker: BatteryTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_graph)

        chart = findViewById(R.id.lineChart)
        batteryTracker = BatteryTracker(this)

        setupChart()
        loadBatteryData()
    }

    private fun setupChart() {
        chart.apply {
            description = Description().apply { text = "Battery Level Over Time" }
            setTouchEnabled(true)
            setPinchZoom(true)
        }
    }

    private fun loadBatteryData() {
        lifecycleScope.launch {
            batteryTracker.batteryLogs.collect { logs ->
                val entries = logs.mapIndexed { index, log ->
                    Entry(index.toFloat(), log.level.toFloat())
                }

                val dataSet = LineDataSet(entries, "Battery %").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    lineWidth = 2f
                    setCircleColor(Color.BLUE)
                }

                val lineData = LineData(dataSet)
                chart.data = lineData
                chart.invalidate() // Refresh
            }
        }
    }
}