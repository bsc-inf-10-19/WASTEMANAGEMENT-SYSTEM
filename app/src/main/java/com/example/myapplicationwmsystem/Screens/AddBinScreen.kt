package com.example.myapplicationwmsystem.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplicationwmsystem.R
import com.example.myapplicationwmsystem.db.Bin
import com.example.myapplicationwmsystem.db.DatabaseHelper

@Composable
fun AddBinScreen(databaseHelper: DatabaseHelper, onAddBinSuccess: (Bin) -> Unit) {
    var binId by remember { mutableStateOf("") }
    var binName by remember { mutableStateOf("") }
    var binLocation by remember { mutableStateOf("") }
    var binLatitude by remember { mutableStateOf("") }
    var binLongitude by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bin_profile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
            )
            OutlinedTextField(
                value = binId,
                onValueChange = { binId = it },
                label = { Text("Bin ID") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binName,
                onValueChange = { binName = it },
                label = { Text("Bin Name") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLocation,
                onValueChange = { binLocation = it },
                label = { Text("Bin Location") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLatitude,
                onValueChange = { binLatitude = it },
                label = { Text("Latitude") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLongitude,
                onValueChange = { binLongitude = it },
                label = { Text("Longitude") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (binId.isNotEmpty() && binName.isNotEmpty() && binLocation.isNotEmpty() && binLatitude.isNotEmpty() && binLongitude.isNotEmpty()) {
                    val latitude = binLatitude.toDoubleOrNull()
                    val longitude = binLongitude.toDoubleOrNull()
                    if (latitude != null && longitude != null) {
                        val newBin = Bin(
                            id = binId,
                            name = "$binName\n$binLocation",
                            imageRes = R.drawable.bin_profile,
                            latitude = latitude,
                            longitude = longitude
                        )
                        val newRowId = databaseHelper.insertBin(newBin)
                        if (newRowId != -1L) {
                            Toast.makeText(context, "Bin added successfully", Toast.LENGTH_SHORT).show()
                            onAddBinSuccess(newBin)
                        } else {
                            Toast.makeText(context, "Failed to add bin to database", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Add Bin")
            }
        }
    }
}
