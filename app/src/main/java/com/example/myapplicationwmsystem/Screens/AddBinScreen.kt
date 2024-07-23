package com.example.myapplicationwmsystem.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplicationwmsystem.R

@Composable
fun AddBinScreen(onAddBinSuccess: (Bin) -> Unit) {
    var binName by remember { mutableStateOf("") }
    var binLocation by remember { mutableStateOf("") }
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
                //.background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(16.dp)
        )
            OutlinedTextField(
                value = binName,
                onValueChange = { binName = it },
                label = { Text("Bin ID") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLocation,
                onValueChange = { binLocation = it },
                label = { Text("Bin Location") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (binName.isNotEmpty() && binLocation.isNotEmpty()) {
                    val newBin = Bin(id = binName.hashCode().toString(), name = "$binName\n$binLocation", imageRes = R.drawable.bin_profile)
                    onAddBinSuccess(newBin)
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Add Bin")
            }
        }
    }
}
data class Bin(val id: String, val name: String, val imageRes: Int)


fun navigateBack(navController: NavController) {
    navController.popBackStack()
}
