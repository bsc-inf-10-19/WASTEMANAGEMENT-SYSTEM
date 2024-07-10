// file: MainActivity.kt
package com.example.myapplicationwmsystem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplicationwmsystem.ui.theme.MyApplicationWMsystemTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            MyApplicationWMsystemTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GarbageLevelScreen()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Garbage Level Alert"
            val descriptionText = "Notifications for garbage level"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("GARBAGE_ALERT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun GarbageLevelScreen() {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            garbageLevel = Random.nextInt(0, 101) // Simulate sensor data
            if (garbageLevel > 75) {
                showNotification(context, garbageLevel)
            }
            delay(3000) // Update every 3 seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Garbage Level",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(
            progress = garbageLevel / 100f,
            modifier = Modifier.size(150.dp),
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 12.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$garbageLevel%",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { garbageLevel = Random.nextInt(0, 101) }) {
            Text("Refresh")
        }
    }
}

private fun showNotification(context: Context, garbageLevel: Int) {
    val builder = NotificationCompat.Builder(context, "GARBAGE_ALERT_CHANNEL")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Garbage Level Alert")
        .setContentText("Garbage level is at $garbageLevel%")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
    }
}

@Preview(showBackground = true)
@Composable
fun GarbageLevelScreenPreview() {
    MyApplicationWMsystemTheme {
        GarbageLevelScreen()
    }
}
