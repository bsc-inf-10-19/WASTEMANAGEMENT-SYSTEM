package com.example.myapplicationwmsystem.Screens

import GarbageLevelScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.myapplicationwmsystem.Components.MyTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinDetailScreen(binId: String, navController: NavController) {
    val tabs = listOf("Bin Level", "Analytics", "Location")
    var selectedTabIndex by remember { mutableStateOf(0) }


    Column {
        MyTopAppBar(
            title = "Bin Detail",
            onNavigationClick = { navController.popBackStack() },
            onNotificationClick = {
                navController.navigate("notifications_screen")
            }
        )
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> GarbageLevelScreen(binId)
            1 -> AnalyticsScreen(binId)
            2 -> BinLocationScreen(binId)
        }
    }
}
