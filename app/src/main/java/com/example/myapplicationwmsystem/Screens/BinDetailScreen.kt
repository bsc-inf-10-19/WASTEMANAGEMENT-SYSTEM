package com.example.myapplicationwmsystem.Screens

import GarbageLevelScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = Color.Red
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) Color.Black else Color.Gray
                        )
                    }
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
