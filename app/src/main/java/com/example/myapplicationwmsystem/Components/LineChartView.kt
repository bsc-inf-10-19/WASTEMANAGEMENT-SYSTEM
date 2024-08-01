package com.example.myapplicationwmsystem.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplicationwmsystem.ChartEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun LineChartView(entries: List<ChartEntry>) {
    val context = LocalContext.current

    // Calculate the time range
    val minTimestamp = entries.minOfOrNull { it.timestamp } ?: 0L
    val maxTimestamp = entries.maxOfOrNull { it.timestamp } ?: 0L
    val range = maxTimestamp - minTimestamp

    // Determine the format based on the time range
    val dateFormat = when {
        range < 24 * 60 * 60 * 1000L -> SimpleDateFormat("HH:mm", Locale.getDefault()) // less than 24 hours
        range < 7 * 24 * 60 * 60 * 1000L -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) // less than a week
        range < 30 * 24 * 60 * 60 * 1000L -> SimpleDateFormat("MM-dd", Locale.getDefault()) // less than a month
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // longer than a month
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color.White
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                LineChart(ctx).apply {
                    setDrawGridBackground(false)
                    isDragEnabled = true
                    isScaleXEnabled = true
                    isScaleYEnabled = true
                    setPinchZoom(true)
                    description.isEnabled = false

                    legend.apply {
                        isEnabled = true
                        form = Legend.LegendForm.LINE
                        textColor = Color.Black.toArgb()
                        textSize = 12f
                        verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        orientation = Legend.LegendOrientation.HORIZONTAL
                        setDrawInside(false)
                        yOffset = 10f
                        xOffset = 0f
                        yEntrySpace = 10f
                        xEntrySpace = 10f
                    }

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(true)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        granularity = 1f
                        labelRotationAngle = -45f
                        textColor = Color.Black.toArgb()
                        gridColor = Color.LightGray.toArgb()
                        gridLineWidth = 0.5f
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return dateFormat.format(Date(value.toLong()))
                            }
                        }
                    }

                    axisLeft.apply {
                        setDrawGridLines(true)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        axisMinimum = 0f
                        textColor = Color.Black.toArgb()
                        gridColor = Color.LightGray.toArgb()
                        gridLineWidth = 0.5f
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}"
                            }
                        }
                    }
                    axisRight.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                }
            },
            update = { chart ->
                if (entries.isNotEmpty()) {
                    val lineEntries = entries.map { Entry(it.timestamp.toFloat(), it.value) }
                    val lineDataSet = LineDataSet(lineEntries, "Garbage Level").apply {
                        setDrawValues(false)
                        setDrawCircles(true)
                        lineWidth = 2f
                        setDrawFilled(true)
                        fillColor = Color(0xFFBBDEFB).toArgb()
                        color = Color(0xFF1A73E8).toArgb()
                        setCircleColor(Color(0xFF1A73E8).toArgb())
                        circleHoleColor = Color(0xFF1A73E8).toArgb()
                    }
                    chart.data = LineData(lineDataSet)
                    chart.invalidate()
                    chart.animateY(500)
                } else {
                    chart.clear()
                }
            }
        )
    }
}
