package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SLRRY_10Theme {
                LoginScreen(
                    onBack = { finish() },
                    onLogin = {
                        // For now (until Firebase): login -> dashboard
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {}
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // TOP: Welcome Text + Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Welcome\nrunners!",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFB5FF00), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // USERNAME FIELD
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFFF6F6F6),
                focusedContainerColor = Color(0xFFF6F6F6)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PASSWORD FIELD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFFF6F6F6),
                focusedContainerColor = Color(0xFFF6F6F6)
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // LOGIN BUTTON
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB5FF00)
            )
        ) {
            Text(
                text = "Log In",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SLRRY_10Theme { LoginScreen() }
}
