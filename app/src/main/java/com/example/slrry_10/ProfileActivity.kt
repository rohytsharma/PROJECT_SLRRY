package com.example.slrry_10

// Core Compose
import androidx.compose.foundation.Image
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import com.example.slrry.R
import com.example.slrry.ui.theme.Background
import com.example.slrry.ui.theme.RedMain
import com.example.slrry.ui.theme.TextGray

// App colors


@Composable
fun SLRRYProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ProfileHeader()

        ProgressCard(
            title = "Weekly Distance",
            current = 103,
            goal = 150,
            unit = "km"
        )

        ProgressCard(
            title = "Active Time",
            current = 17,
            goal = 25,
            unit = "hrs"
        )

        AchievementCard()
        StreaksCard()

    }
}
@Composable
fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(RedMain)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.profile_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            Spacer(modifier = Modifier.height(8.dp))


            Text("Andrew", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Beginner", color = Color.White.copy(0.8f), fontSize = 14.sp)
        }


        Text(
            "Profile",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun ProgressCard(
    title: String,
    current: Int,
    goal: Int,
    unit: String
) {
    val progress = current / goal.toFloat()

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Text("$current / $goal $unit", color = TextGray)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StatsCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .offset(y = (-40).dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("103.2", "km")
            StatItem("16.9", "hr")
            StatItem("1.5k", "kcal")
        }
    }
}


@Composable
fun StatItem(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(unit, color = TextGray, fontSize = 12.sp)
    }
}
@Composable
fun AchievementCard() {
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
fun StreaksCard() {
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
                                    0 -> Color.Green
                                    1,2,3 -> Color.Green
                                    4,5 -> Color.Red
                                    else -> Color.LightGray
                                }
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        SLRRYProfileScreen()
    }
}
