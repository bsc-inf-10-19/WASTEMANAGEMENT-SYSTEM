package com.example.myapplicationwmsystem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationwmsystem.ui.theme.MyApplicationWMsystemTheme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
            BinDetailScreen(backStackEntry.arguments?.getString("binId") ?: "")
        }
        composable("add_bin_screen") {
            AddBinScreen(onAddBinSuccess = { newBin ->
                bins.add(newBin)
                navController.navigate("home_screen")
            })
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
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    //.background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    val sharedPref = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                    val storedUsername = sharedPref.getString("username", "")
                    val storedPassword = sharedPref.getString("password", "")
                    if (username == storedUsername && password == storedPassword) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Sign In")
                }
                Button(onClick = { onSignUp() }) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else if (username.isEmpty() || password.isEmpty()) {
                    errorMessage = "Username and Password cannot be empty"
                } else {
                    val sharedPref = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("username", username)
                        putString("password", password)
                        apply()
                    }
                    errorMessage = ""
                    onSignUpSuccess()
                }
            }) {
                Text("Sign Up")
            }
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}@Composable
fun HomeScreen(navController: NavHostController, bins: SnapshotStateList<Bin>) {
    var showDialog by remember { mutableStateOf(false) }
    var binToDelete by remember { mutableStateOf<Bin?>(null) }
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedItem) {
                0 -> {
                    // Home content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Smart Waste Management",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        bins.forEach { bin ->
                            HomeScreenItem(
                                imageRes = bin.imageRes,
                                text = bin.name,
                                onClick = { navController.navigate("bin_detail_screen/${bin.id}") },
                                onDelete = { binToDelete = bin }
                            )
                        }
                    }
                }
                1 -> SearchScreen(bins = bins) { bin ->
                    navController.navigate("bin_detail_screen/${bin.id}")
                }
                2 -> MapScreen(bins = bins)
            }

            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bin")
                }
            }
        }
    }

    if (showDialog) {
        AddBinDialog(
            onDismiss = { showDialog = false },
            onAddBinSuccess = { newBin ->
                bins.add(newBin)
                showDialog = false
            }
        )
    }

    binToDelete?.let { bin ->
        DeleteBinDialog(
            bin = bin,
            onDismiss = { binToDelete = null },
            onDeleteBinSuccess = {
                bins.remove(bin)
                binToDelete = null
            }
        )
    }
}


@Composable
fun HomeScreenItem(imageRes: Int, text: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onDelete() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Bin")
        }
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
@Composable
fun AddBinScreen(onAddBinSuccess: (Bin) -> Unit) {
    var binName by remember { mutableStateOf("") }
    var binLocation by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {Image(
            painter = painterResource(id = R.drawable.bin_profile),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(150.dp)
                //.background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .padding(16.dp)
        )
            OutlinedTextField(
                value = binName,
                onValueChange = { binName = it },
                label = { Text("Bin ID") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = binLocation,
                onValueChange = { binLocation = it },
                label = { Text("Bin Location") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (binName.isNotEmpty() && binLocation.isNotEmpty()) {
                    val newBin = Bin(id = binName.hashCode().toString(), name = "$binName\n$binLocation", imageRes = R.drawable.bin_profile)
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
data class Bin(val id: String, val name: String, val imageRes: Int)

@Composable
fun BinDetailScreen(binId: String) {
    val tabs = listOf("Bin Level", "Analytics", "Location")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
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

@Composable
fun GarbageLevelScreen(binId: String) {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    if (garbageLevel > 80) {
                        showNotification(context, garbageLevel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log the exception
                }
                delay(3000) // Update every 3 seconds
            }
        }
    }

    val progressBarColor = if (garbageLevel > 80) {
        Color.Red
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

@Composable
fun AnalyticsScreen(binId: String) {
    // State to hold data entries for the chart
    var dataEntries by remember { mutableStateOf(mutableListOf<Entry>()) }
    var entryIndex by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val garbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    val timestamp = System.currentTimeMillis()
                    dataEntries.add(Entry(entryIndex, garbageLevel.toFloat(), timestamp))
                    entryIndex += 1
                    // Limit entries to a reasonable number to avoid performance issues
                    if (dataEntries.size > 30) {
                        dataEntries.removeAt(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log the exception
                }
                delay(30000)
            }
        }
    }

    LineChartView(entries = dataEntries)
}


@Composable
fun LineChartView(entries: List<Entry>) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val lineDataSet = LineDataSet(entries, "Garbage Level").apply {
                        color = Color.Green.toArgb()
                        valueTextColor = Color.Black.toArgb()
                        lineWidth = 2f
                    }
                    data = LineData(lineDataSet)
                    description.isEnabled = false
                    xAxis.isEnabled = false
                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    invalidate()
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp) // Set the desired size for the chart
        )
    }
}
@Composable
fun SearchScreen(bins: List<Bin>, onBinClick: (Bin) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredBins = bins.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Bins") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        LazyColumn {
            items(filteredBins) { bin ->
                HomeScreenItem(
                    imageRes = bin.imageRes,
                    text = bin.name,
                    onClick = { onBinClick(bin) },
                    onDelete = { /* No delete option in search */ }
                )
            }
        }
    }
}

@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(-34.0, 151.0)) // Default center, adjust as needed

                bins.forEach { bin ->
                    val marker = Marker(this).apply {
                        position = GeoPoint(-34.0 + Math.random() * 0.1, 151.0 + Math.random() * 0.1) // Random positions for demo
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = bin.name
                    }
                    overlays.add(marker)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BinLocationScreen(binId: String) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(-34.0, 151.0))
                val marker = Marker(this).apply {
                    position = GeoPoint(-34.0, 151.0)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Bin Location - Bin $binId"
                }
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun showNotification(context: Context, level: Int) {
    val builder = NotificationCompat.Builder(context, "GARBAGE_ALERT_CHANNEL")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Garbage Level Alert")
        .setContentText("Garbage level is at $level%")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
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
