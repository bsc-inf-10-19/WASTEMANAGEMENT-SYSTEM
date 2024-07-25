package com.example.myapplicationwmsystem

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Notifications
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
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.myapplicationwmsystem.Screens.SearchScreen
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.myapplicationwmsystem.Components.MyTopAppBar
import com.example.myapplicationwmsystem.Screens.NotificationsScreen

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
        Bin("1", "Bin 1\nChikanda", R.drawable.bin_profile, -15.39628, 35.33640),
        Bin("2", "Bin 2\nMatawale", R.drawable.bin_profile, -15.38628, 35.32640),
        Bin("3", "Bin 3\nMpondabwino", R.drawable.bin_profile, -15.37628, 35.31640)
    )}

    val notifications = remember { mutableStateListOf<CustomNotification>() }

    NotificationsHandler(notifications = notifications, binId = bins.firstOrNull()?.id ?: "")


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
        composable("notifications_screen") {
            NotificationsScreen(notifications = notifications, navController)
        }
        composable("search_screen") {
            SearchScreen(
                bins = bins,
                onBinClick = { bin ->
                    navController.navigate("bin_detail_screen/${bin.id}")
                },
                navController = navController
            )
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
fun MyTopAppBar(context: Context, garbageLevel: Int, navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = "Waste Management",
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = {
                navController.navigate("notifications_screen")
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
    val rippleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val modifier = Modifier
        .fillMaxWidth()
        .clickable(
            onClick = onClick,
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple()
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
@SuppressLint("ClickableViewAccessibility")
@Composable
fun AddBinDialog(onDismiss: () -> Unit, onAddBinSuccess: (Bin) -> Unit) {
    var binName by remember { mutableStateOf("") }
    var binLocation by remember { mutableStateOf("") }
    var binLatitude by remember { mutableStateOf(-15.39628) }
    var binLongitude by remember { mutableStateOf(35.33640) }
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New Bin",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Details") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Location") }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
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
                                label = { Text("Bin Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    1 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(top = 16.dp)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                                    MapView(ctx).apply {
                                        setTileSource(TileSourceFactory.MAPNIK)
                                        controller.setZoom(15.0)
                                        controller.setCenter(GeoPoint(binLatitude, binLongitude))
                                        setOnTouchListener { v, event ->
                                            if (event.action == MotionEvent.ACTION_UP) {
                                                val tappedPoint = projection.fromPixels(event.x.toInt(), event.y.toInt())
                                                binLatitude = tappedPoint.latitude
                                                binLongitude = tappedPoint.longitude
                                                overlays.clear()
                                                overlays.add(Marker(this).apply {
                                                    position = tappedPoint as GeoPoint?
                                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                })
                                                invalidate()
                                            }
                                            false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

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
                                imageRes = R.drawable.bin_profile,
                                latitude = binLatitude,
                                longitude = binLongitude
                            )
                            onAddBinSuccess(newBin)
                            onDismiss()
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

    val builder = when {
        level > 70 -> {
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Garbage Level Alert")
                .setContentText("Garbage level is critically high at $level%. Please take action.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
        }
        level in 30..70 -> {
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Garbage Level Advisory")
                .setContentText("Garbage level is at $level%. Consider taking action soon.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }
        else -> null
    }

    builder?.let {
        notificationManager.notify(level, it.build())
    }
}

data class CustomNotification(
    val id: Int,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun NotificationsHandler(
    notifications: MutableList<CustomNotification>,
    binId: String
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            val level = fetchGarbageLevelFromThingSpeak(binId)
            if (level >= 30) {
                val notification = when {
                    level > 70 -> {
                        CustomNotification(
                            id = 4,
                            title = "Garbage Level Alert",
                            description = "Garbage level is critically high at $level%. Please take action.",
                            color = Color(0xFFf8d7da)
                        )
                    }
                    level in 30..70 -> {
                        CustomNotification(
                            id = 5,
                            title = "Garbage Level Advisory",
                            description = "Garbage level is at $level%. Consider taking action soon.",
                            color = Color(0xFFfff3cd)
                        )
                    }
                    else -> {
                        CustomNotification(
                            id = 6,
                            title = "Garbage Level Normal",
                            description = "Garbage level is normal at $level%.",
                            color = Color(0xFFd4edda)
                        )
                    }
                }

                if (notifications.none { it.description == notification.description }) {
                    notifications.add(notification)
                    showNotification(context, level)
                }
            }
            delay(3000) // Update every 3 seconds
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
