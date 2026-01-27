package com.example.slrry_10

import android.os.Bundle
import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
                DashboardRoot()
            }
        }
    }
}

@Composable
private fun DashboardRoot() {
    val selectedIndex = remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DashboardTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex.intValue,
                onSelect = { selectedIndex.intValue = it }
            )
        }
    ) { innerPadding ->
        DashboardContent(
            selectedIndex = selectedIndex.intValue,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun DashboardContent(selectedIndex: Int, modifier: Modifier = Modifier) {
    when (selectedIndex) {
        0 -> HomeScreen(modifier = modifier)
        1 -> PlaceholderTabScreen(title = "Map", modifier = modifier)
        2 -> PlaceholderTabScreen(title = "Run", modifier = modifier)
        3 -> PlaceholderTabScreen(title = "Profile", modifier = modifier)
        else -> HomeScreen(modifier = modifier)
    }
}

@Composable
private fun PlaceholderTabScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text("$title tab (coming soon)", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Dashboard", fontWeight = FontWeight.SemiBold) }
    )
}

/* ---------------- HOME SCREEN ---------------- */

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(16.dp, bottom = 120.dp),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quick overview", fontWeight = FontWeight.SemiBold)
                Text("This week", color = Color.Gray)
            }

            Divider(Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

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
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

/* ---------------- PLACEHOLDER ---------------- */

@Composable
fun PlaceholderCard(title: String, height: Dp) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ---------------- RECENT ACTIVITY ---------------- */

@Composable
fun RecentActivityCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Morning run", fontWeight = FontWeight.Bold)
            Text("Today, 7:30 AM", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5 KM")
                Text("6:38 min/km")
                Text("30 M")
            }

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Map (coming soon)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ---------------- SUGGESTED WORKOUT ---------------- */

@Composable
fun SuggestedWorkoutsSection() {
    val context = LocalContext.current
    Column {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Suggested workouts", fontWeight = FontWeight.Bold)
            Text(
                "See all",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Suggested workouts (coming soon)", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("First Half Marathon", fontWeight = FontWeight.Bold)
                Text(
                    "21.1 km workout designed to build endurance",
                    fontSize = 12.sp,
                    color = Color.Red
                )

                Spacer(Modifier.height(12.dp))

                DashboardPrimaryButton(
                    text = "Start now",
                    onClick = {
                        Toast.makeText(context, "Workout start (coming soon)", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/* ---------------- CHALLENGES (FIXED) ---------------- */

@Composable
fun ChallengesSection() {
    val context = LocalContext.current
    Column {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Challenges", fontWeight = FontWeight.Bold)
            Text(
                "See all",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Challenges (coming soon)", Toast.LENGTH_SHORT).show()
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp)

            Spacer(Modifier.height(12.dp))

            DashboardPrimaryButton(
                text = "Start now",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DashboardPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB6FF00)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------------- BOTTOM NAV ---------------- */

@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val lastStartClickMs = remember { mutableLongStateOf(0L) }
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
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    selected = selectedIndex == 0,
                    onClick = { onSelect(0) }
                )
                BottomNavItem(
                    icon = Icons.Filled.Map,
                    selected = selectedIndex == 1,
                    onClick = {
                        onSelect(1)
                        context.startActivity(Intent(context, MapsHubActivity::class.java))
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BottomNavItem(
                    icon = Icons.Filled.DirectionsRun,
                    selected = selectedIndex == 2,
                    onClick = { onSelect(2) }
                )
                BottomNavItem(
                    icon = Icons.Filled.Person,
                    selected = selectedIndex == 3,
                    onClick = { onSelect(3) }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                val now = SystemClock.elapsedRealtime()
                if (now - lastStartClickMs.longValue < 750) return@FloatingActionButton
                lastStartClickMs.longValue = now
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                context.startActivity(Intent(context, StartRunActivity::class.java))
            },
            modifier = Modifier
                .size(88.dp)
                .offset(y = (-20).dp),
            shape = CircleShape,
            containerColor = Color(0xFFB6FF00),
            contentColor = Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Text("START", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    IconButton(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(Color(0xFFB6FF00), CircleShape)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/* ---------------- PREVIEW ---------------- */

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    MaterialTheme {
        HomeScreen()
    }
}
