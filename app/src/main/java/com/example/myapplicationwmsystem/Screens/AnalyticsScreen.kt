package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Tab
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplicationwmsystem.ChartEntry
import com.example.myapplicationwmsystem.Components.LineChartView
import com.example.myapplicationwmsystem.db.DatabaseHelper
import com.example.myapplicationwmsystem.db.GarbageLevelEntry
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnalyticsScreen(binId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val databaseHelper = remember { DatabaseHelper(context) }

    var dataEntries by remember { mutableStateOf(listOf<ChartEntry>()) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    val timestamp = System.currentTimeMillis()

                    saveGarbageLevelToDatabase(databaseHelper, binId, garbageLevel, timestamp)

                    // Fetch and filter data based on the selected tab index
                    val filteredEntries = filterDataEntries(databaseHelper, binId, selectedTabIndex)
                    dataEntries = filteredEntries
                    .sortedBy { it.timestamp }
                    .mapIndexed { index, entry ->
                        ChartEntry(
                            index = index.toFloat(),
                            value = entry.garbageLevel.toFloat(),
                            timestamp = entry.timestamp
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000) // Fetch every 10 seconds
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Garbage Chart",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            backgroundColor = Color.Transparent,
            contentColor = Color(0xFF1A73E8),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .width(4.dp)
                        .height(4.dp)
                        .background(Color(0xFF1A73E8))
                )
            },
            divider = {}
        ) {
            val tabTitles = listOf("Day", "Week", "Month", "Year")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        val filteredEntries = filterDataEntries(databaseHelper, binId, selectedTabIndex)
                        dataEntries = filteredEntries.mapIndexed { index, entry ->
                            ChartEntry(
                                index = index.toFloat(),
                                value = entry.garbageLevel.toFloat(),
                                timestamp = entry.timestamp
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedTabIndex == index) Color.Transparent else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (selectedTabIndex == index) Color.Transparent else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTabIndex == index) Color(0xFF1A73E8) else Color.LightGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LineChartView(entries = dataEntries) // Ensure this composable can handle the dataEntries
    }
}

private fun saveGarbageLevelToDatabase(databaseHelper: DatabaseHelper, binId: String, garbageLevel: Int, timestamp: Long) {
    val entry = GarbageLevelEntry(binId, garbageLevel, timestamp)
    databaseHelper.insertGarbageLevel(entry)
}

fun filterDataEntries(
    databaseHelper: DatabaseHelper,
    binId: String,
    tabIndex: Int
): List<GarbageLevelEntry> {
    val currentTime = System.currentTimeMillis()
    return when (tabIndex) {
        0 -> databaseHelper.getGarbageLevelsByBinIdAndTimeRange(binId, currentTime - 1.dayInMillis, currentTime)
        1 -> databaseHelper.getGarbageLevelsByBinIdAndTimeRange(binId, currentTime - 1.weekInMillis, currentTime)
        2 -> databaseHelper.getGarbageLevelsByBinIdAndTimeRange(binId, currentTime - 1.monthInMillis, currentTime)
        3 -> databaseHelper.getGarbageLevelsByBinIdAndTimeRange(binId, currentTime - 1.yearInMillis, currentTime)
        else -> emptyList()
    }
}

private val Int.dayInMillis: Long
    get() = this * 24L * 60 * 60 * 1000

private val Int.weekInMillis: Long
    get() = this * 7 * 24L * 60 * 60 * 1000

private val Int.monthInMillis: Long
    get() = this * 30 * 24L * 60 * 60 * 1000

private val Int.yearInMillis: Long
    get() = this * 365L * 24 * 60 * 60 * 1000
