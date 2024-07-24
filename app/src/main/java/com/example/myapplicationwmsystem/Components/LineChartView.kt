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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
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

                    legend.isEnabled = true
                    legend.form = Legend.LegendForm.LINE
                    legend.textColor = Color.Black.toArgb()
                    legend.textSize = 12f

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(true)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        granularity = 1f
                        labelRotationAngle = -45f
                        textColor = Color.Black.toArgb()
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val chartEntry = entries.find { it.index.toInt() == value.toInt() }
                                return if (chartEntry != null) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chartEntry.timestamp))
                                } else {
                                    value.toString()
                                }
                            }
                        }
                    }

                    axisLeft.apply {
                        setDrawGridLines(true)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        axisMinimum = 0f
                        textColor = Color.Black.toArgb()
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
                    val lineEntries = entries.map { Entry(it.index.toFloat(), it.value) }
                    val lineDataSet = LineDataSet(lineEntries, "Garbage Level").apply {
                        setDrawValues(false)
                        setDrawCircles(true)
                        lineWidth = 2f
                        setDrawFilled(true)
                        fillColor = Color.Red.toArgb()
                        color = Color.Red.toArgb()
                        setCircleColor(Color.Red.toArgb())
                        circleHoleColor = Color.Red.toArgb()
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
