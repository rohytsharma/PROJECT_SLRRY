package com.example.slrry_10

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            com.example.slrry_10.ui.theme.SLRRY_10Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar() }
                ) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/* ---------------- HOME SCREEN ---------------- */

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { QuickOverviewCard() }
        item { PlaceholderCard("Monthly goal (coming soon)", 80.dp) }
        item { RecentActivityCard() }
        item { SuggestedWorkoutsSection() }
        item { ChallengesSection() }
    }
}

/* ---------------- QUICK OVERVIEW ---------------- */

@Composable
fun QuickOverviewCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            Text(
                "Quick overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111416)
            )
            Text(
                "This week",
                fontSize = 12.sp,
                color = Color(0xFF7E868C)
            )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewItem("12.4 KM", "This week")
                OverviewItem("4 Runs", "Total")
                OverviewItem("600 M²", "Area")
            }
        }
    }
}

@Composable
fun OverviewItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111416)
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color(0xFF7E868C)
        )
    }
}

/* ---------------- PLACEHOLDER ---------------- */

@Composable
fun PlaceholderCard(title: String, height: Dp) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color(0xFFEDEDED)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/* ---------------- RECENT ACTIVITY ---------------- */

@Composable
fun RecentActivityCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {

            Text(
                "Morning run",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111416)
            )
            Text(
                "Today, 7:30 AM",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5 KM", fontWeight = FontWeight.SemiBold, color = Color(0xFF111416))
                Text("33:11 / KM", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("30 M", fontWeight = FontWeight.Medium, color = Color(0xFF111416))
            }

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Map (coming soon)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/* ---------------- SUGGESTED WORKOUT ---------------- */

@Composable
fun SuggestedWorkoutsSection() {
    Column {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Suggested workouts",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111416)
            )
            Text(
                "See all",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {

                Text(
                    "First Half Marathon",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF111416)
                )
                Text(
                    "21.1 km workout designed to build endurance",
                    fontSize = 12.sp,
                    color = Color.Red
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB6FF00)
                    )
                ) {
                    Text("Start now", color = Color.Black)
                }
            }
        }
    }
}

/* ---------------- CHALLENGES (FIXED) ---------------- */

@Composable
fun ChallengesSection() {
    Column {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Challenges",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111416)
            )
            Text(
                "See all",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChallengeCard(
                "Run 50 km in 10 days",
                Modifier.weight(1f)
            )
            ChallengeCard(
                "Run every day for 5 days",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChallengeCard(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111416)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB6FF00)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Start now",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/* ---------------- BOTTOM NAV ---------------- */

@Composable
fun BottomNavigationBar() {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp)
                .background(Color(0xFFDFFF8C), RoundedCornerShape(32.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BottomNavItem(Icons.Filled.Home)
                BottomNavItem(
                    icon = Icons.Filled.Map,
                    onClick = {
                        context.startActivity(Intent(context, MapsHubActivity::class.java))
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BottomNavItem(Icons.Filled.DirectionsRun)
                BottomNavItem(Icons.Filled.Person)
            }
        }

        Box(
            Modifier
                .size(88.dp)
                .offset(y = (-20).dp)
                .background(Color(0xFFB6FF00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "START",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, StartRunActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, onClick: (() -> Unit)? = null) {
    Icon(
        icon,
        null,
        tint = Color.Black,
        modifier = Modifier
            .size(26.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    )
}

/* ---------------- PREVIEW ---------------- */

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    MaterialTheme {
        HomeScreen()
    }
}
