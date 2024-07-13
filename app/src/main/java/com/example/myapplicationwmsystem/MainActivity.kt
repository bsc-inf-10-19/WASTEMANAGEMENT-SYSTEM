// file: MainActivity.kt
package com.example.myapplicationwmsystem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.myapplicationwmsystem.ui.theme.MyApplicationWMsystemTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()
        setContent {
            MyApplicationWMsystemTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Garbage Level Alert"
            val descriptionText = "Notifications for garbage level"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("GARBAGE_ALERT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(onTimeout = { navController.navigate("login_screen") })
        }
        composable("login_screen") {
            LoginScreen(onLoginSuccess = { navController.navigate("garbage_level_screen") })
        }
        composable("garbage_level_screen") {
            GarbageLevelScreen()
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // Display splash screen for 2 seconds
        onTimeout()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = "Smart Waste Management",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // Simulate a login process
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onLoginSuccess() }) {
                Text(text = "Login")
            }
        }
    }
}

@Composable
fun GarbageLevelScreen() {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            coroutineScope.launch {
                garbageLevel = fetchGarbageLevelFromThingSpeak()
                if (garbageLevel > 70) {
                    showNotification(context, garbageLevel)
                }
            }
            delay(3000) // Update every 3 seconds
        }
    }

    val progressBarColor = if (garbageLevel > 80) {
        androidx.compose.ui.graphics.Color.Red
    } else {
        MaterialTheme.colorScheme.secondary
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
            color = progressBarColor,
            strokeWidth = 12.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$garbageLevel%",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

private suspend fun fetchGarbageLevelFromThingSpeak(): Int {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = "H8M68IKI5A3QYVET"
            val channelId = "2595920"
            val url = URL("https://api.thingspeak.com/channels/$channelId/fields/1.json?api_key=$apiKey&results=1")

            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                inputStream.bufferedReader().use {
                    val response = it.readText()
                    val json = JSONObject(response)
                    val feeds = json.getJSONArray("feeds")
                    if (feeds.length() > 0) {
                        val lastEntry = feeds.getJSONObject(0)
                        return@withContext lastEntry.getInt("field1")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext 0
    }
}

private fun showNotification(context: Context, garbageLevel: Int) {
    val builder = NotificationCompat.Builder(context, "GARBAGE_ALERT_CHANNEL")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Garbage Level Alert")
        .setContentText("Garbage level is at $garbageLevel% full. Please consider emptying it soon.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Ensure the notification pops up

    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MyApplicationWMsystemTheme {
        SplashScreen(onTimeout = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationWMsystemTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun GarbageLevelScreenPreview() {
    MyApplicationWMsystemTheme {
        GarbageLevelScreen()
    }
}
