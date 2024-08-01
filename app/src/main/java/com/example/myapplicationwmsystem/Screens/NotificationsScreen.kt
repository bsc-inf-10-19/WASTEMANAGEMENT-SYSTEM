package com.example.myapplicationwmsystem.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplicationwmsystem.Components.MyTopAppBar
import com.example.myapplicationwmsystem.CustomNotification

fun navigateBack(navController: NavController) {
    navController.popBackStack()
}

@Composable
fun NotificationsScreen(notifications: List<CustomNotification>, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        MyTopAppBar(
            title = "Notifications",
            onNavigationClick = { navigateBack(navController) },
            showNotificationIcon = false
        )

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the top app bar and the list

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(notifications.reversed()) { notification ->
                NotificationItem(notification)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: CustomNotification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = notification.color)  // Use containerColor for Material3
    ) {
        Column(
            modifier = Modifier
                .clickable { /* Handle item click */ }
                .padding(16.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                color = when (notification.color) {
                    Color(0xFFf8d7da) -> Color(0xFF721c24)  // Dark red for danger
                    Color(0xFFfff3cd) -> Color(0xFF856404)  // Dark yellow for warning
                    Color(0xFFd4edda) -> Color(0xFF155724)  // Dark green for success
                    else -> Color.Black  // Default color
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.description,
                style = MaterialTheme.typography.bodyMedium,
                color = when (notification.color) {
                    Color(0xFFf8d7da) -> Color(0xFF721c24)  // Dark red for danger
                    Color(0xFFfff3cd) -> Color(0xFF856404)  // Dark yellow for warning
                    Color(0xFFd4edda) -> Color(0xFF155724)  // Dark green for success
                    else -> Color.Black  // Default color
                }
            )
        }
    }
}
