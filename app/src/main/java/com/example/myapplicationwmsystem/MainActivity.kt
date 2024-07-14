package com.example.myapplicationwmsystem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
            LoginScreen(onLoginSuccess = { navController.navigate("home_screen") }, onSignUp = { navController.navigate("sign_up_screen") })
        }
        composable("sign_up_screen") {
            SignUpScreen(onSignUpSuccess = { navController.navigate("login_screen") })
        }
        composable("home_screen") {
            HomeScreen(navController)
        }
        composable("garbage_level_screen") {
            GarbageLevelScreen()
        }
        composable("analytics_screen") {
            AnalyticsScreen()
        }
        composable("bin_location_screen") {
            BinLocationScreen()
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
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Smart Waste Management",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        HomeScreenItem(
            imageRes = R.drawable.bin_profile,
            text = "Bin Level",
            onClick = { navController.navigate("garbage_level_screen") }
        )
        HomeScreenItem(
            imageRes = R.drawable.analytics_profile,
            text = "Analytics",
            onClick = { navController.navigate("analytics_screen") }
        )
        HomeScreenItem(
            imageRes = R.drawable.location_profile,
            text = "Bin Location",
            onClick = { navController.navigate("bin_location_screen") }
        )
    }
}

@Composable
fun HomeScreenItem(imageRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
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
    }
}

@Composable
fun GarbageLevelScreen() {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    garbageLevel = fetchGarbageLevelFromThingSpeak()
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

@Composable
fun AnalyticsScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Analytics Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BinLocationScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Bin Location Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
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

suspend fun fetchGarbageLevelFromThingSpeak(): Int {
    val apiKey = "H8M68IKI5A3QYVET"
    val channelId = "2595920"
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
