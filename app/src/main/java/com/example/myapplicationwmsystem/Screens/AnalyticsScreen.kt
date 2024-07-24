package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplicationwmsystem.ChartEntry
import com.example.myapplicationwmsystem.Components.LineChartView
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak

@Composable
fun AnalyticsScreen(binId: String) {
    val dataEntries = remember { mutableStateListOf<ChartEntry>() }
    var entryIndex by remember { mutableStateOf(1f) }
    var selectedTabIndex by remember { mutableStateOf(0) }
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

    fun filterDataEntries() {
        val currentTime = System.currentTimeMillis()
        val filteredEntries = when (selectedTabIndex) {
            0 -> dataEntries.filter { it.timestamp >= currentTime - 1.dayInMillis }
            1 -> dataEntries.filter { it.timestamp >= currentTime - 1.weekInMillis }
            2 -> dataEntries.filter { it.timestamp >= currentTime - 1.monthInMillis }
            3 -> dataEntries.filter { it.timestamp >= currentTime - 1.yearInMillis }
            4 -> dataEntries // All time
            else -> emptyList()
        }
        dataEntries.clear()
        dataEntries.addAll(filteredEntries)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Garbage Level",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            backgroundColor = Color.Transparent,
            contentColor = Color.Black
        ) {
            val tabTitles = listOf("1 Day", "1 Week", "1 Month", "1 Year", "All Time")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        filterDataEntries()
                    },
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .border(
                            width = 0.dp,
                            color = Color.Transparent
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .border(
                                width = if (selectedTabIndex == index) 2.dp else 0.dp,
                                color = if (selectedTabIndex == index) Color.Black else Color.Transparent,

                            )
                    ) {
                        Text(text = title, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LineChartView(entries = dataEntries)
    }
}

//private fun <T> SnapshotStateList<T>.filter(predicate: (ChartEntry) -> Unit): List<T> {
//    return this.filter(predicate)
//}

val Int.dayInMillis: Long
    get() = this * 24L * 60 * 60 * 1000

val Int.weekInMillis: Long
    get() = this * 7 * 24L * 60 * 60 * 1000

val Int.monthInMillis: Long
    get() = this * 30 * 24L * 60 * 60 * 1000

val Int.yearInMillis: Long
    get() = this * 365L * 24 * 60 * 60 * 1000