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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent
import com.example.slrry_10.ui.theme.SLRRYTheme

data class RunActivity(
    val id: String,
    val name: String,
    val date: String,
    val distance: String,
    val time: String,
    val pace: String,
    val progress: Float // 0.0 to 1.0 for progress bar
)

class RecentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecentActivitiesScreen()
                }
            }
        }
    }
}

@Composable
fun RecentActivitiesScreen() {
    // Sample data - in real app, this would come from ViewModel/Repository
    val activities = remember {
        listOf(
            RunActivity("1", "Monday Morning Run", "16/7/2024", "0 Km", "0", "0\"", 0.6f),
            RunActivity("2", "Sunday Morning Run", "15/7/2024", "0 Km", "0", "0\"", 0.95f),
            RunActivity("3", "Saturday Morning Run", "15/7/2024", "0 Km", "0", "0\"", 0.9f),
            RunActivity("4", "Friday Morning Run", "15/7/2024", "0 Km", "0", "0\"", 0.75f)
        )
    }
    
    val totalDistance = "0"
    val totalRuns = "0"
    val averagePace = "0"
    val totalTime = "0"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: navigate back */ }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recent Activities",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F2F2F)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Total distance
        Column {
            Text(
                text = totalDistance,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F2F2F)
            )
            Text(
                text = "Kilometer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2F2F2F)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(value = totalRuns, label = "Run")
            StatItem(value = averagePace, label = "Average Pace")
            StatItem(value = totalTime, label = "Time")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Activities list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = activities, key = { it.id }) { activity ->
                ActivityCard(activity = activity)
            }
        }
        }
    }

@Composable
private fun StatItem(value: String, label: String) {
    Column {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2F2F2F)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF2F2F2F)
        )
    }
}

@Composable
private fun ActivityCard(activity: RunActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // use white for better contrast
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and star icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = NeonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = activity.date,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2F2F2F)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Activity name
            Text(
                text = activity.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F2F2F)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time, Pace, and Distance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Time and Pace column
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = activity.time,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35) // Reddish-orange
                            )
                            Text(
                                text = "Time",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF808080)
                            )
                        }
                        Column {
                            Text(
                                text = activity.pace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35) // Reddish-orange
                            )
                            Text(
                                text = "Pace",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF808080)
                            )
                        }
                    }
                }
                
                // Distance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = activity.distance,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F2F2F)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(activity.progress)
                        .height(4.dp)
                        .background(NeonAccent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentActivitiesPreview() {
    SLRRYTheme {
        RecentActivitiesScreen()
    }
}
