package com.example.myapplicationwmsystem.Screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationwmsystem.R

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
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
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
                .fillMaxWidth(0.9f)
        ) {
            Text(
                text = "Create an Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
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
                    focusedLabelColor = Color(0xFF33691E),
                    cursorColor = Color(0xFF33691E)
                ),
                isError = errorMessage.contains("User name")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF33691E),
                    focusedLabelColor = Color(0xFF33691E),
                    cursorColor = Color(0xFF33691E)
                ),
                isError = errorMessage.contains("Email")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF33691E),
                    focusedLabelColor = Color(0xFF33691E),
                    cursorColor = Color(0xFF33691E)
                ),
                isError = errorMessage.contains("Password")
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF33691E),
                    focusedLabelColor = Color(0xFF33691E),
                    cursorColor = Color(0xFF33691E)
                ),
                isError = errorMessage.contains("Confirm Password")
            )

            Button(
                onClick = {
                    when {
                        username.isEmpty() -> errorMessage = "User name cannot be empty"
                        email.isEmpty() -> errorMessage = "Email cannot be empty"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> {
                            val sharedPref = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("username", username)
                                putString("email", email)
                                putString("password", password)
                                apply()
                            }
                            errorMessage = ""
                            onSignUpSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E))
            ) {
                Text("Register", fontSize = 16.sp)
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Have an Account?", modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = "Login",
                color = Color(0xFF33691E),
                modifier = Modifier.clickable { onLogin() }
            )
        }
    }
}
