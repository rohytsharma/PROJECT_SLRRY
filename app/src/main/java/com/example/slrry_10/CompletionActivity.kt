package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.SLRRYTheme
import kotlin.random.Random

class CompletionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompletionScreen(
                        onDone = {
                            // Navigate to StartScreen and clear the back stack (no going back to onboarding)
                            val intent = Intent(this@CompletionActivity, StartScreenActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionScreen(onDone: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1EB))
    ) {
        // Confetti particles
        ConfettiLayer()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Checkmark Icon
            SuccessIcon()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Heading
            Text(
                text = "AWESOME!",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sub-heading
            Text(
                text = "YOU'RE ALL SET.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Descriptive Message
            Text(
                text = "We've personalized your experience based on your answers. Let's hit the ground running and crush your goals!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF3F3F3F),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Done Button
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Done",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SuccessIcon() {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dark grey circular background
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF4A4A4A), CircleShape)
        )
        
        // Purple square border (offset)
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 4.dp, y = 4.dp)
                .border(2.dp, Color(0xFF6C63FF), RoundedCornerShape(4.dp))
        )
        
        // Black square
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color.Black, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            // White checkmark
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.Green,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun ConfettiLayer() {
    val confettiColors = listOf(
        Color(0xFF4A90E2), // Light blue
        Color(0xFF2E5C8A), // Medium blue
        Color(0xFF1A3A5C), // Dark blue
        Color(0xFF6CE1B3)  // Mint/teal
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Generate confetti particles scattered across the screen
        repeat(30) {
            val random = Random(it)
            val xPercent = random.nextFloat()
            val yPercent = random.nextFloat()
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (xPercent * 400 - 200).dp,
                        y = (yPercent * 800 - 400).dp
                    )
                    .width((random.nextFloat() * 8 + 4).dp)
                    .height((random.nextFloat() * 12 + 6).dp)
                    .background(
                        confettiColors[random.nextInt(confettiColors.size)],
                        RoundedCornerShape(2.dp)
                    )
                    .rotate((random.nextFloat() * 360).toFloat())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletionPreview() {
    SLRRYTheme {
        CompletionScreen()
    }
}
