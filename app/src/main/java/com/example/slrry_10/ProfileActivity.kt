package com.example.slrry_10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activity = this
        setContent {
            SLRRY_10Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProfileScreen(onBack = { activity.finish() })
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit = {}) {
    val background = Color(0xFFF5F1EB)
    val header = Color(0xFF111416)
    val textGray = Color(0xFF6E757A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        ProfileHeader(onBack = onBack, headerColor = header)

        ProgressCard(
            title = "Weekly Distance",
            current = 103,
            goal = 150,
            unit = "km",
            textGray = textGray
        )

        ProgressCard(
            title = "Active Time",
            current = 17,
            goal = 25,
            unit = "hrs",
            textGray = textGray
        )

        AchievementCard()
        StreaksCard()
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit, headerColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(headerColor)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder avatar (no drawable dependency)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Andrew", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Beginner", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }

        Text(
            "Profile",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProgressCard(
    title: String,
    current: Int,
    goal: Int,
    unit: String,
    textGray: Color
) {
    val progress = current / goal.toFloat()

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$current / $goal $unit", color = textGray)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun AchievementCard() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("600m² is yours – congratulations")
        }
    }
}

@Composable
private fun StreaksCard() {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("STREAKS", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(140.dp)
            ) {
                items(21) { index ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    0, 1, 2, 3 -> Color(0xFF4CAF50)
                                    4, 5 -> Color(0xFFE53935)
                                    else -> Color.LightGray
                                }
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    MaterialTheme { ProfileScreen() }
}

