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
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.myapplicationwmsystem.Screens.NotificationsScreen
import com.example.myapplicationwmsystem.db.Bin
import com.example.myapplicationwmsystem.db.DatabaseHelper

class MainActivity : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()


        databaseHelper = DatabaseHelper(this)

        setContent {
            MyApplicationWMsystemTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(databaseHelper = databaseHelper)
                }
            }
        }
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
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
fun AppNavigation(databaseHelper: DatabaseHelper) {
    val navController = rememberNavController()
    val bins = remember { mutableStateListOf<Bin>() }
    val notifications = remember { mutableStateListOf<CustomNotification>() }

    // Remove the binId parameter
    NotificationsHandler(notifications = notifications, bins = bins, databaseHelper = databaseHelper)

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
            HomeScreen(navController, bins, databaseHelper)
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
            AddBinScreen(databaseHelper = databaseHelper) { newBin ->
                bins.add(newBin)
                navController.navigate("home_screen")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(navController: NavController) {
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
            containerColor = Color(0xFF33691E)
        )
    )
}

@Composable
fun EditDeleteDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = (-16).dp, y = 0.dp),
        modifier = Modifier.padding(8.dp).width(130.dp)
    ) {
        EditDropdownMenuItem(onEdit)
        Divider()
        DeleteDropdownMenuItem(onDelete)
    }
}

@Composable
fun EditDropdownMenuItem(onEdit: () -> Unit) {
    DropdownMenuItem(onClick = {
        onEdit()
    }) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Edit")
    }
}

@Composable
fun DeleteDropdownMenuItem(onDelete: () -> Unit) {
    DropdownMenuItem(onClick = {
        onDelete()
    }) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Delete")
    }
}

@Composable
fun DropdownMenuItem(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

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
fun AddBinDialog(
    onDismiss: () -> Unit,
    onAddBinSuccess: (Bin) -> Unit,
    databaseHelper: DatabaseHelper
) {
    var binId by remember { mutableStateOf("") }
    var binName by remember { mutableStateOf("") }
    var binLatitude by remember { mutableStateOf(-15.39628) }
    var binLongitude by remember { mutableStateOf(35.33640) }
    var selectedTab by remember { mutableStateOf(0) }
    var flashMessage by remember { mutableStateOf(FlashMessage(FlashMessageType.INFO, "", false)) }
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
                                value = binId,
                                onValueChange = { binId = it },
                                label = { Text("Bin ID") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = binName,
                                onValueChange = { binName = it },
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
                FlashMessageComposable(flashMessage = flashMessage)

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (binId.isNotBlank() && binName.isNotBlank()) {
                            val existingBin = databaseHelper.getBinById(binId) ?: databaseHelper.getBinByName(binName)
                            if (existingBin != null) {
                                flashMessage = FlashMessage(FlashMessageType.ERROR, "Bin already exists", true)
                            } else {
                                val newBin = Bin(binId, binName, R.drawable.bin_profile, binLatitude, binLongitude)
                                onAddBinSuccess(newBin)
                                onDismiss()
                                flashMessage = FlashMessage(FlashMessageType.SUCCESS, "Bin added successfully", true)
                            }
                        } else {
                            flashMessage = FlashMessage(FlashMessageType.WARNING, "Please fill all details", true)
                        }
                    }) {
                        Text("Add Bin")
                    }
                }
            }
        }
    }
}

data class FlashMessage(val type: FlashMessageType, val message: String, val isVisible: Boolean)

enum class FlashMessageType {
    SUCCESS, ERROR, WARNING, INFO
}

@Composable
fun FlashMessageComposable(flashMessage: FlashMessage) {
    if (flashMessage.isVisible) {
        val backgroundColor = when (flashMessage.type) {
            FlashMessageType.SUCCESS -> Color(0xFFDFF2BF)
            FlashMessageType.ERROR -> Color(0xFFFFBABA)
            FlashMessageType.WARNING -> Color(0xFFFFF2B7)
            FlashMessageType.INFO -> Color(0xFFBDE5F8)
        }
        val textColor = when (flashMessage.type) {
            FlashMessageType.SUCCESS -> Color(0xFF4F8A10)
            FlashMessageType.ERROR -> Color(0xFFD8000C)
            FlashMessageType.WARNING -> Color(0xFF9F6000)
            FlashMessageType.INFO -> Color(0xFF00529B)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Text(text = flashMessage.message, color = textColor)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun EditBinDialog(
    bin: Bin,
    onDismiss: () -> Unit,
    onEditBinSuccess: (Bin) -> Unit
) {
    var binId by remember { mutableStateOf(bin.id) }
    var binName by remember { mutableStateOf(bin.name) }
    var binLatitude by remember { mutableStateOf(bin.latitude) }
    var binLongitude by remember { mutableStateOf(bin.longitude) }
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
                    text = "Edit Bin",
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
                                value = binId,
                                onValueChange = { binId = it },
                                label = { Text("Bin ID") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = binName,
                                onValueChange = { binName = it },
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
                        if (binName.isNotBlank()) {
                            val updatedBin = bin.copy(
                                name = binName,
                                latitude = binLatitude,
                                longitude = binLongitude
                            )
                            onEditBinSuccess(updatedBin)
                            onDismiss()
                            Toast.makeText(context, "Bin updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}




data class ChartEntry(val index: Float, val value: Float, val timestamp: Long)

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
    bins: List<Bin>,
    databaseHelper: DatabaseHelper
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            updateBinLevels(bins.toMutableList(), databaseHelper)
            bins.forEach { bin ->
                if (bin.garbageLevel >= 30) {
                    val notification = when {
                        bin.garbageLevel > 70 -> {
                            CustomNotification(
                                id = notifications.size + 1,
                                title = "Garbage Level Alert for ${bin.name}",
                                description = "Garbage level is critically high at ${bin.garbageLevel}%. Please take action.",
                                color = Color(0xFFf8d7da)
                            )
                        }
                        bin.garbageLevel in 30..70 -> {
                            CustomNotification(
                                id = notifications.size + 1,
                                title = "Garbage Level Advisory for ${bin.name}",
                                description = "Garbage level is at ${bin.garbageLevel}%. Consider taking action soon.",
                                color = Color(0xFFfff3cd)
                            )
                        }
                        else -> null
                    }

                    notification?.let {
                        if (notifications.none { n -> n.description == it.description }) {
                            notifications.add(it)
                            showNotification(context, bin.garbageLevel)
                        }
                    }
                }
            }
            delay(3000) // Check every 3 seconds
        }
    }
}

suspend fun fetchGarbageLevelFromThingSpeak(binId: String): Int {
    val apiKey = "H8M68IKI5A3QYVET"
    val channelId = "2595920"
    val url = URL("https://api.thingspeak.com/channels/$channelId/feeds.json?api_key=$apiKey&results=1")
    return withContext(Dispatchers.IO) {
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                println("Raw ThingSpeak response: $response") // Add this line
                val json = JSONObject(response)
                val feeds = json.getJSONArray("feeds")
                if (feeds.length() > 0) {
                    val lastEntry = feeds.getJSONObject(0)
                    val field1 = lastEntry.optString("field1")
                    val field2 = lastEntry.optString("field2")
                    println("Field1: $field1, Field2: $field2") // Add this line
                    when (binId) {
                        "Bin 1" -> field1.toIntOrNull() ?: 0
                        "Bin 2" -> field2.toIntOrNull() ?: 0
                        else -> 0
                    }
                } else {
                    0
                }
            }
        } ?: 0
    }
}

suspend fun updateBinLevels(bins: MutableList<Bin>, databaseHelper: DatabaseHelper) {
    val level1 = fetchGarbageLevelFromThingSpeak("Bin 1")
    val level2 = fetchGarbageLevelFromThingSpeak("Bin 2")
    bins.find { it.id == "Bin 2" }?.let { bin ->
        bin.garbageLevel = level2
        databaseHelper.updateBin(bin)
    }

    bins.find { it.id == "Bin 1" }?.let { bin ->
        bin.garbageLevel = level1
        databaseHelper.updateBin(bin)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    val databaseHelper = DatabaseHelper(context)
    MyApplicationWMsystemTheme {
        AppNavigation(databaseHelper = databaseHelper)
    }
}
