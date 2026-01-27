package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class FrontActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SLRRY_10Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    FrontScreen(
                        onLogin = { startActivity(Intent(this, LoginActivity::class.java)) },
                        onDashboard = { startActivity(Intent(this, DashboardActivity::class.java)) },
                        onSignUp = { startActivity(Intent(this, PasswordActivity::class.java)) }
                    )
                }
            }
        }
    }
}

@Composable
fun FrontScreen(
    onLogin: () -> Unit = {},
    onDashboard: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val accent = Color(0xFFB5FF00)
    val text = Color.Black
    val muted = Color(0xFF6E757A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 26.dp, vertical = 44.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SLRRY",
            fontSize = 54.sp,
            fontWeight = FontWeight.Black,
            color = text
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Welcome runners!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = muted
        )

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text("Sign In", color = text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text("Sign Up", color = text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Skip to dashboard",
            modifier = Modifier.clickable { onDashboard() },
            color = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FrontScreenPreview() {
    MaterialTheme {
        FrontScreen()
    }
}
