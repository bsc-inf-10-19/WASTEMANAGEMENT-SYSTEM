package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplicationwmsystem.AddBinDialog
import com.example.myapplicationwmsystem.Components.HomeScreenItem
import com.example.myapplicationwmsystem.DeleteBinDialog
import com.example.myapplicationwmsystem.MyTopAppBar
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(
    navController: NavHostController,
    bins: SnapshotStateList<Bin>
) {
    var showDialog by remember { mutableStateOf(false) }
    var binToDelete by remember { mutableStateOf<Bin?>(null) }
    var binToEdit by remember { mutableStateOf<Bin?>(null) }
    var selectedItem by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val garbageLevel = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val binId = "your-bin-id" // Replace with the actual bin ID
        while (true) {
            try {
                garbageLevel.value = fetchGarbageLevelFromThingSpeak(binId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            MyTopAppBar(context = context, garbageLevel = garbageLevel.value)
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Bin Locations") },
                    label = { Text("Bin Locations") },
                    selected = selectedItem == 2,
                    onClick = { selectedItem = 2 }
                )
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Bin")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when (selectedItem) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        bins.forEach { bin ->
                            HomeScreenItem(
                                imageRes = bin.imageRes,
                                text = bin.name,
                                onClick = { navController.navigate("bin_detail_screen/${bin.id}") },
                                onDelete = { binToDelete = bin }
                            )
                        }
                    }
                }
                1 -> SearchScreen(bins = bins) { bin ->
                    navController.navigate("bin_detail_screen/${bin.id}")
                }
                2 -> MapScreen(bins = bins)
            }

            if (showDialog) {
                AddBinDialog(
                    onDismiss = { showDialog = false },
                    onAddBinSuccess = { newBin ->
                        bins.add(newBin)
                        showDialog = false
                    }
                )
            }

            binToDelete?.let { bin ->
                DeleteBinDialog(
                    bin = bin,
                    onDismiss = { binToDelete = null },
                    onDeleteBinSuccess = {
                        bins.remove(bin)
                        binToDelete = null
                    }
                )
            }
        }
    }
}
