package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplicationwmsystem.Components.HomeScreenItem
import com.example.myapplicationwmsystem.Components.MyTopAppBar
import com.example.myapplicationwmsystem.db.Bin

@Composable
fun SearchScreen(
    bins: List<Bin>,
    onBinClick: (Bin) -> Unit,
    navController: NavController
) {
    var showEditDialog by remember { mutableStateOf<Bin?>(null) }
    var binToDelete by remember { mutableStateOf<Bin?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter bins based on the search query
    val filteredBins = bins.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        MyTopAppBar(
            title = "Search Bins",
            onNavigationClick = {
                navController.navigate("home_screen") {
                    popUpTo("home_screen") { inclusive = false }
                }
            },
            onNotificationClick = {
                navController.navigate("notifications_screen")
            }
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Bins") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        LazyColumn {
            items(filteredBins) { bin ->  // Use filteredBins here
                HomeScreenItem(
                    imageRes = bin.imageRes,
                    binId = bin.id,
                    binName = bin.name,
                    onClick = { navController.navigate("bin_detail_screen/${bin.id}") },
                    onEdit = { showEditDialog = bin },
                    onDelete = { binToDelete = bin }
                )
            }
        }
    }
}
