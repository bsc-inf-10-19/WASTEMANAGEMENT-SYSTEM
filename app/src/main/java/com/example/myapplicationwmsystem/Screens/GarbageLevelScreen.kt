package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationwmsystem.db.DatabaseHelper
import com.example.myapplicationwmsystem.db.GarbageLevelEntry
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import com.example.myapplicationwmsystem.showNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GarbageLevelScreen(binId: String) {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val databaseHelper = remember { DatabaseHelper(context) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    var garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    val timestamp = System.currentTimeMillis()

                    saveGarbageLevelToDatabase(databaseHelper, binId, garbageLevel, timestamp)

                    // Update UI if needed based on the latest garbage level
                    garbageLevel = garbageLevel // Update the garbage level state

                    if (garbageLevel > 80) {
                        showNotification(context, garbageLevel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000) // Fetch every 3 seconds
            }
        }
    }


    val progressBarColor = when {
        garbageLevel > 70 -> Color.Red
        garbageLevel in 0..45 -> Color.Green
        else -> Color(0xFFFFA500)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Garbage Level",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(
            progress = garbageLevel / 100f,
            modifier = Modifier.size(150.dp),
            color = progressBarColor,
            strokeWidth = 12.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$garbageLevel%",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

private fun saveGarbageLevelToDatabase(databaseHelper: DatabaseHelper, binId: String, garbageLevel: Int, timestamp: Long) {
    val entry = GarbageLevelEntry(binId, garbageLevel, timestamp)
    databaseHelper.insertGarbageLevel(entry)
}