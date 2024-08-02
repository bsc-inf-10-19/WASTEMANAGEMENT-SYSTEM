package com.example.myapplicationwmsystem.Screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationwmsystem.R

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login to your account",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("User name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF33691E),
                    unfocusedIndicatorColor = Color(0xFFB0BEC5), // Add unfocused indicator color
                    focusedLabelColor = Color(0xFF33691E),
                    unfocusedLabelColor = Color(0xFF757575), // Add unfocused label color
                    cursorColor = Color(0xFF33691E)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF33691E),
                    unfocusedIndicatorColor = Color(0xFFB0BEC5),
                    focusedLabelColor = Color(0xFF33691E),
                    unfocusedLabelColor = Color(0xFF757575),
                    cursorColor = Color(0xFF33691E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Forgot Password",
                color = Color(0xFF33691E),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp
            )

            Button(
                onClick = {
                    val sharedPref = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                    val storedUsername = sharedPref.getString("username", "")
                    val storedPassword = sharedPref.getString("password", "")
                    if (username == storedUsername && password == storedPassword) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
            ) {
                Text("Login", color = Color.White)
            }

            Text(
                text = "Or Login with",
                modifier = Modifier.padding(vertical = 16.dp),
                fontSize = 16.sp
            )

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.facebook_logo),
                    contentDescription = "Facebook Login",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Login",
                    modifier = Modifier.size(50.dp)
                )
            }

            Text(
                text = "Don’t have an account?",
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 16.sp
            )

            Text(
                text = "Register Now",
                color = Color(0xFF33691E),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onSignUp() },
                fontSize = 16.sp
            )
        }
    }
}
