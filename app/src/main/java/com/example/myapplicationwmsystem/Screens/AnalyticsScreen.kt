package com.example.myapplicationwmsystem.Screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.myapplicationwmsystem.ChartEntry
import com.example.myapplicationwmsystem.Components.LineChartView
import com.example.myapplicationwmsystem.Components.LineChartView
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AnalyticsScreen(binId: String) {
    val dataEntries = remember { mutableStateListOf<ChartEntry>() }
    var entryIndex by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    val timestamp = System.currentTimeMillis()
                    dataEntries.add(ChartEntry(entryIndex, garbageLevel.toFloat(), timestamp))
                    entryIndex += 1
                    if (dataEntries.size > 30) {
                        dataEntries.removeAt(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000)
            }
        }
    }

    LineChartView(entries = dataEntries)
}