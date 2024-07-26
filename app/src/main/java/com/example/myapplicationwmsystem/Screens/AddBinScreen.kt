package com.example.myapplicationwmsystem.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplicationwmsystem.R


@Entity(tableName = "bins")
data class Bin(
    @PrimaryKey val id: String,
    val name: String,
    val imageRes: Int,
    val latitude: Double,
    val longitude: Double
)
@Composable
fun AddBinScreen(onAddBinSuccess: (Bin) -> Unit) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.bin_profile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
            )
            OutlinedTextField(
                value = binName,
                onValueChange = { binName = it },
                label = { Text("Bin Name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLocation,
                onValueChange = { binLocation = it },
                label = { Text("Bin Location") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLatitude,
                onValueChange = { binLatitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLongitude,
                onValueChange = { binLongitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (binName.isNotEmpty() && binLocation.isNotEmpty() && binLatitude.isNotEmpty() && binLongitude.isNotEmpty()) {
                    val latitude = binLatitude.toDoubleOrNull()
                    val longitude = binLongitude.toDoubleOrNull()
                    if (latitude != null && longitude != null) {
                        val newBin = Bin(
                            id = binName.hashCode().toString(),
                            name = "$binName\n$binLocation",
                            imageRes = R.drawable.bin_profile,
                            latitude = latitude,
                            longitude = longitude
                        )
                        onAddBinSuccess(newBin)
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
