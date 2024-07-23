package com.example.myapplicationwmsystem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationwmsystem.Screens.AddBinScreen
import com.example.myapplicationwmsystem.Screens.Bin
import com.example.myapplicationwmsystem.Screens.BinDetailScreen
import com.example.myapplicationwmsystem.Screens.HomeScreen
import com.example.myapplicationwmsystem.Screens.LoginScreen
import com.example.myapplicationwmsystem.Screens.SignUpScreen
import com.example.myapplicationwmsystem.Screens.SplashScreen
import com.example.myapplicationwmsystem.ui.theme.MyApplicationWMsystemTheme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper

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
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bins = remember { mutableStateListOf(
        Bin("1", "Bin 1\nChikanda", R.drawable.bin_profile),
        Bin("2", "Bin 2\nMatawale", R.drawable.bin_profile),
        Bin("3", "Bin 3\nMpondabwino", R.drawable.bin_profile)
    )}

    NavHost(navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(onTimeout = { navController.navigate("login_screen") })
        }
        composable("login_screen") {
            LoginScreen(onLoginSuccess = { navController.navigate("home_screen") }, onSignUp = { navController.navigate("sign_up_screen") })
        }
        composable("sign_up_screen") {
            SignUpScreen(onSignUpSuccess = { navController.navigate("login_screen") })
        }
        composable("home_screen") {
            HomeScreen(navController, bins)
        }
        composable("bin_detail_screen/{binId}") { backStackEntry ->
            BinDetailScreen(
                binId = backStackEntry.arguments?.getString("binId") ?: "",
                navController = navController
            )
        }
        composable("add_bin_screen") {
            AddBinScreen(onAddBinSuccess = { newBin ->
                bins.add(newBin)
                navController.navigate("home_screen")
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(context: Context, garbageLevel: Int) {
    TopAppBar(
        title = {
            Text(
                text = "Waste Management",
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = {
                handleNotificationClick(context, garbageLevel)
            }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1A73E8)
        )
    )
}

private fun handleNotificationClick(context: Context, garbageLevel: Int) {
    showNotification(context, garbageLevel)
}
@Composable
fun DeleteDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = (-16).dp, y = 0.dp)
    ) {
        DeleteDropdownMenuItem(onDelete)
    }
}

@Composable
fun ColumnScope.DeleteDropdownMenuItem(onDelete: () -> Unit) {
    DropdownMenuItem(onClick = {
        onDelete()
    }) {
        Text("Delete")
    }
}

@Composable
fun DropdownMenuItem(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val modifier = Modifier
        .fillMaxWidth()
        .clickable(
            onClick = onClick,
            interactionSource = interactionSource,
            indication = rememberRipple(bounded = true)
        )
        .padding(16.dp)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

@Composable
fun DeleteBinDialog(bin: Bin, onDismiss: () -> Unit, onDeleteBinSuccess: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Delete Bin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Are you sure you want to delete ${bin.name}?",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        onDeleteBinSuccess()
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun AddBinDialog(onDismiss: () -> Unit, onAddBinSuccess: (Bin) -> Unit) {
    var binName by remember { mutableStateOf("") }
    var binLocation by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New Bin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = binName,
                    onValueChange = { binName = it },
                    label = { Text("Bin ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = binLocation,
                    onValueChange = { binLocation = it },
                    label = { Text("Bin Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (binName.isNotEmpty() && binLocation.isNotEmpty()) {
                            val newBin = Bin(
                                id = binName.hashCode().toString(),
                                name = "$binName\n$binLocation",
                                imageRes = R.drawable.bin_profile
                            )
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
    }
}

data class ChartEntry(val index: Float, val value: Float, val timestamp: Long)


class TimeAxisFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return sdf.format(Date(value.toLong()))
    }
}


fun showNotification(context: Context, level: Int) {
    val channelId = "GARBAGE_ALERT_CHANNEL"
    val notificationManager = NotificationManagerCompat.from(context)

    when {
        level > 70 -> {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Garbage Level Alert")
                .setContentText("Garbage level is critically high at $level%. Please take action.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationId = 1
            notificationManager.notify(notificationId, builder.build())

            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(notificationId)
            }, 5000)
        }
    }
}


suspend fun fetchGarbageLevelFromThingSpeak(binId: String): Int {
    val apiKey = "H8M68IKI5A3QYVET"
    val channelId = "2595920" // Update this with the respective bin's channel ID
    val url = URL("https://api.thingspeak.com/channels/$channelId/feeds.json?api_key=$apiKey&results=1")
    return withContext(Dispatchers.IO) {
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val json = JSONObject(response)
                val feeds = json.getJSONArray("feeds")
                if (feeds.length() > 0) {
                    val lastEntry = feeds.getJSONObject(0)
                    val field1 = lastEntry.optString("field1")
                    field1.toIntOrNull() ?: 0
                } else {
                    0
                }
            }
        } ?: 0
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationWMsystemTheme {
        AppNavigation()
    }
}
