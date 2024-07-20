package com.example.myapplicationwmsystem

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class LineChartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_chart) // Ensure this layout file exists

        val chart: LineChart =
            findViewById(R.id.linechart) // Ensure this ID matches your layout file

        val noOfEmp = ArrayList<Entry>()

        noOfEmp.add(Entry(945f, 0f))
        noOfEmp.add(Entry(1040f, 1f))
        noOfEmp.add(Entry(1133f, 2f))
        noOfEmp.add(Entry(1240f, 3f))
        noOfEmp.add(Entry(1369f, 4f))
        noOfEmp.add(Entry(1487f, 5f))
        noOfEmp.add(Entry(1501f, 6f))
        noOfEmp.add(Entry(1645f, 7f))
        noOfEmp.add(Entry(1578f, 8f))
        noOfEmp.add(Entry(1695f, 9f))

        // Line chart data needs no X-axis labels directly from data, so we won't use year ArrayList
        val lineDataSet = LineDataSet(noOfEmp, "No Of Employee")
        lineDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        lineDataSet.valueTextColor = Color.BLACK // Customize text color if needed
        lineDataSet.valueTextSize = 10f // Customize text size if needed
        lineDataSet.setDrawFilled(true) // Optional: Draw fill under the line

        val data = LineData(lineDataSet)

        chart.data = data
        chart.animateY(5000) // Animate the chart
    }
}