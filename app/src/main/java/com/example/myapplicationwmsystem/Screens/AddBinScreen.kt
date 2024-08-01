package com.example.myapplicationwmsystem.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplicationwmsystem.db.GarbageLevelEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.myapplicationwmsystem.R
import com.example.myapplicationwmsystem.db.Bin
import com.example.myapplicationwmsystem.db.DatabaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBinScreen(
    databaseHelper: DatabaseHelper,
    onAddBinSuccess: (Bin) -> Unit
) {
    var binName by remember { mutableStateOf("") }
    var binLatitude by remember { mutableStateOf("") }
    var binLongitude by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var thingSpeakLevels by remember { mutableStateOf(Pair(0, 0)) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        thingSpeakLevels = fetchGarbageLevelFromThingSpeak()
    }

    fun validateInputs(): Boolean {
        return when {
            binName.isBlank() -> {
                errorMessage = "Bin name cannot be empty"
                showError = true
                false
            }
            binLatitude.isBlank() -> {
                errorMessage = "Bin latitude cannot be empty"
                showError = true
                false
            }
            binLongitude.isBlank() -> {
                errorMessage = "Bin longitude cannot be empty"
                showError = true
                false
            }
            else -> {
                true
            }
        }
    }

    fun addBin() {
        if (validateInputs()) {
            try {
                val latitude = binLatitude.toDouble()
                val longitude = binLongitude.toDouble()
                val garbageLevel = when (binName) {
                    "Bin 1" -> thingSpeakLevels.first
                    "Bin 2" -> thingSpeakLevels.second
                    else -> 0
                }
                val newBin = Bin(
                    id = binName.hashCode().toString(),
                    name = binName,
                    imageRes = R.drawable.bin_profile,
                    latitude = latitude,
                    longitude = longitude,
                    garbageLevel = 0 // initial garbage level
                )
                val newRowId = databaseHelper.insertBin(newBin)
                if (newRowId != -1L) {
                    // Insert initial garbage level entry
                    val garbageLevelEntry = GarbageLevelEntry(
                        binId = newBin.id,
                        garbageLevel = garbageLevel,
                        timestamp = System.currentTimeMillis()
                    )
                    databaseHelper.insertGarbageLevel(garbageLevelEntry)
                    onAddBinSuccess(newBin)
                    Toast.makeText(context, "Bin added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add bin", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = binName,
            onValueChange = { binName = it },
            label = { Text("Bin Name") }
        )

        TextField(
            value = binLatitude,
            onValueChange = { binLatitude = it },
            label = { Text("Bin Latitude") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = binLongitude,
            onValueChange = { binLongitude = it },
            label = { Text("Bin Longitude") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = { addBin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Bin")
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

suspend fun fetchGarbageLevelFromThingSpeak(): Pair<Int, Int> {
    return withContext(Dispatchers.IO) {
        val apiKey = "H8M68IKI5A3QYVET"
        val channelId = "2595920"
        val url = URL("https://api.thingspeak.com/channels/$channelId/feeds.json?api_key=$apiKey&results=1")

        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val json = JSONObject(response)
                val feeds = json.getJSONArray("feeds")
                if (feeds.length() > 0) {
                    val lastEntry = feeds.getJSONObject(0)
                    val field1 = lastEntry.optString("field1").toIntOrNull() ?: 0
                    val field2 = lastEntry.optString("field2").toIntOrNull() ?: 0
                    Pair(field1, field2)
                } else {
                    Pair(0, 0)
                }
            }
        } ?: Pair(0, 0)
    }
}

