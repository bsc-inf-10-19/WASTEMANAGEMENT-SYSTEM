package com.example.myapplicationwmsystem.Components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplicationwmsystem.ChartEntry
import com.example.myapplicationwmsystem.TimeAxisFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun LineChartView(entries: List<ChartEntry>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                xAxis.valueFormatter = TimeAxisFormatter()
                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                legend.isEnabled = true
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val lineEntries = entries.map { Entry(it.index, it.value) }
            val lineDataSet = LineDataSet(lineEntries, "Bin Level").apply {
                setDrawValues(false)
                setDrawCircles(true)
                lineWidth = 2f
                setDrawFilled(true)
            }
            chart.data = LineData(lineDataSet)
            chart.invalidate() // Refresh the chart
        }
    )
}
