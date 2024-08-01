package com.example.myapplicationwmsystem.Screens

import MapScreen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TabRowDefaults.Divider
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
import com.example.myapplicationwmsystem.EditBinDialog
import com.example.myapplicationwmsystem.MyTopAppBar
import com.example.myapplicationwmsystem.db.Bin
import com.example.myapplicationwmsystem.db.DatabaseHelper
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    bins: SnapshotStateList<Bin>,
    databaseHelper: DatabaseHelper
) {
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Bin?>(null) }
    var binToDelete by remember { mutableStateOf<Bin?>(null) }
    var selectedItem by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val fetchedBins = databaseHelper.getAllBins()
        bins.clear()
        bins.addAll(fetchedBins)
    }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val updatedBins = bins.toList().map { bin ->
                    val updatedLevel = fetchGarbageLevelFromThingSpeak(bin.id)
                    bin.copy(garbageLevel = updatedLevel)
                }
                bins.clear()
                bins.addAll(updatedBins)
                updatedBins.forEach { databaseHelper.updateBin(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            if (selectedItem == 0) {
                MyTopAppBar(navController)
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LazyColumn {
                            items(bins) { bin ->
                                HomeScreenItem(
                                    imageRes = bin.imageRes,
                                    binId = bin.id,
                                    binName = bin.name,
                                    onClick = { navController.navigate("bin_detail_screen/${bin.id}") },
                                    onEdit = { showEditDialog = bin },
                                    onDelete = { binToDelete = bin }
                                )
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                            }

                        }
                    }
                }
                1 -> SearchScreen(
                    bins = bins,
                    onBinClick = { bin ->
                        navController.navigate("bin_detail_screen/${bin.id}")
                    },
                    navController = navController
                )
                2 -> MapScreen(bins = bins)
            }

            if (showDialog) {
                AddBinDialog(
                    databaseHelper = databaseHelper,
                    onDismiss = { showDialog = false },
                    onAddBinSuccess = { newBin ->
                        if (bins.none { it.id == newBin.id }) {
                            bins.add(newBin)
                            databaseHelper.insertBin(newBin)
                        }
                        showDialog = false
                    }
                )
            }

            showEditDialog?.let { bin ->
                EditBinDialog(
                    databaseHelper = databaseHelper,
                    bin = bin,
                    onDismiss = { showEditDialog = null },
                    onEditBinSuccess = { updatedBin ->
                        val index = bins.indexOfFirst { it.id == updatedBin.id }
                        if (index != -1) {
                            bins[index] = updatedBin
                            databaseHelper.updateBin(updatedBin)
                        }
                        showEditDialog = null
                    }
                )
            }

            binToDelete?.let { bin ->
                DeleteBinDialog(
                    bin = bin,
                    onDismiss = { binToDelete = null },
                    onDeleteBinSuccess = {
                        bins.remove(bin)
                        databaseHelper.deleteBin(bin.id)
                        binToDelete = null
                    }
                )
            }
        }
    }
}
