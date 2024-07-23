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
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import com.example.myapplicationwmsystem.showNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun GarbageLevelScreen(binId: String) {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    if (garbageLevel > 80) {
                        showNotification(context, garbageLevel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log the exception
                }
                delay(3000) // Update every 3 seconds
            }
        }
    }

    val progressBarColor = if (garbageLevel > 80) {
        Color.Red
    } else {
        MaterialTheme.colorScheme.secondary
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