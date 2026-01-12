package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class StartScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    StartScreen(
                        onSignIn = {
                            // For now: login -> dashboard
                            startActivity(Intent(this, DashboardActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        },
                        onSignUp = {
                            // TODO: wire to real auth flow
                            startActivity(Intent(this, PasswordActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        },
                        onGoogleSignUp = {
                            // TODO: wire Google sign-in
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StartScreen(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignUp: () -> Unit
) {
    val accentTop = Color(0xFFC8FF2F)
    val accentBottom = Color(0xFFB8FF3A)
    val accentBrush = Brush.verticalGradient(listOf(accentTop, accentBottom))
    val text = Color(0xFF0E1112)
    val muted = Color(0xFF6E757A)
    val divider = Color(0xFF111416).copy(alpha = 0.55f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        // Logo
        Text(
            text = "SLRRY",
            fontSize = 54.sp,
            fontWeight = FontWeight.Black,
            color = text,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.42f)
                .height(6.dp)
                .background(accentBottom, shape = RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.height(72.dp))

        NeonButton(
            text = "Sign In",
            brush = accentBrush,
            textColor = text,
            onClick = onSignIn
        )
        Spacer(modifier = Modifier.height(18.dp))
        NeonButton(
            text = "SIGN Up",
            brush = accentBrush,
            textColor = text,
            onClick = onSignUp
        )

        Spacer(modifier = Modifier.height(70.dp))

        // Divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(divider)
        )
        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = "sign up with",
            fontSize = 18.sp,
            color = muted
        )
        Spacer(modifier = Modifier.height(18.dp))

        GoogleButton(onClick = onGoogleSignUp)
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun NeonButton(
    text: String,
    brush: Brush,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(elevation = 18.dp, shape = RoundedCornerShape(18.dp), ambientColor = Color(0x66000000))
            .background(brush = brush, shape = RoundedCornerShape(18.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(14.dp, CircleShape, ambientColor = Color(0x33000000))
            .background(Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Minimal Google mark approximation (keeps repo asset-free).
        Text(
            text = "G",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4285F4) // Google blue
        )
    }
}


