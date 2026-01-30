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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.repository.CapturedAreasRepository
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.MapViewComponent
import com.example.slrry_10.viewmodel.StartRunUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

private val DashAccentGreen = Color(0xFFB5FF00)

@Composable
fun DashboardRoot() {
    val selectedIndex = remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex.intValue,
                onSelect = { selectedIndex.intValue = it }
            )
        }
    ) { padding ->
        when (selectedIndex.intValue) {
            0 -> HomeScreen(modifier = Modifier.padding(padding))
            else -> HomeScreen(modifier = Modifier.padding(padding))
        }
    }
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
        item { GoalProgressCard() }
        item { RecentActivityCard() }
        item { SuggestedWorkoutsSection() }
        item { ChallengesSection() }
    }
}

/* ---------------- QUICK OVERVIEW ---------------- */

@Composable
fun QuickOverviewCard() {
    val repo = remember { FirebaseUserRepoImpl() }
    val areasRepo = remember { CapturedAreasRepository() }
    var weekKm by remember { mutableStateOf(0.0) }
    var runsCount by remember { mutableStateOf(0) }
    var totalArea by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        val weekAgo = now - 7L * 24L * 60L * 60L * 1000L
        val runs = repo.getRunSessions()
        val weekRuns = runs.filter { it.startTime >= weekAgo }
        weekKm = weekRuns.sumOf { it.distance } / 1000.0
        runsCount = weekRuns.size
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        totalArea = if (uid.isNullOrBlank()) null else areasRepo.getAreasForUser(uid).sumOf { it.area }
    }

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
                OverviewItem(String.format("%.1f KM", weekKm), "This week")
                OverviewItem("$runsCount Runs", "This week")
                OverviewItem(
                    value = totalArea?.let { String.format("%.0f m²", it) } ?: "—",
                    label = "Area"
                )
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

/* ---------------- GOAL PROGRESS ---------------- */

@Composable
fun GoalProgressCard() {
    val repo = remember { FirebaseUserRepoImpl() }
    var weeklyGoalKm by remember { mutableStateOf<Int?>(null) }
    var weeklyDoneKm by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        weeklyGoalKm = fetchWeeklyGoalKm()
        val now = System.currentTimeMillis()
        val weekAgo = now - 7L * 24L * 60L * 60L * 1000L
        val runs = repo.getRunSessions()
        weeklyDoneKm = runs.filter { it.startTime >= weekAgo }.sumOf { it.distance } / 1000.0
    }

    val goal = weeklyGoalKm
    val fraction = if (goal != null && goal > 0) (weeklyDoneKm / goal.toDouble()).coerceIn(0.0, 1.0) else 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly goal", fontWeight = FontWeight.Bold)
                Text(
                    text = if (goal == null) "Set goal" else "${goal}km",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (goal == null) "Complete onboarding to set your goal." else {
                    "${String.format("%.1f", weeklyDoneKm)} / $goal KM"
                },
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.toFloat())
                        .height(10.dp)
                        .background(DashAccentGreen, RoundedCornerShape(50))
                )
            }
        }
    }
}

/* ---------------- RECENT ACTIVITY ---------------- */

@Composable
fun RecentActivityCard() {
    val repo = remember { FirebaseUserRepoImpl() }
    val areasRepo = remember { CapturedAreasRepository() }
    var latest by remember { mutableStateOf<RunSession?>(null) }
    var totalCapturedArea by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        latest = repo.getRunSessions().firstOrNull()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        totalCapturedArea = if (uid.isNullOrBlank()) null else areasRepo.getAreasForUser(uid).sumOf { it.area }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Recent run", fontWeight = FontWeight.Bold)
            Text(
                if (latest == null) "Tap Start to record a run" else "Your latest recorded run",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val km = latest?.distance?.div(1000.0) ?: 0.0
                Text(String.format("%.1f KM", km))
                Text(latest?.averagePace ?: "—")
                Text(latest?.duration?.let { "${it / 60} M" } ?: "—")
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Area captured: " + (totalCapturedArea?.let { String.format("%.0f m²", it) } ?: "—"),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val session = latest
                if (session != null && session.path.size >= 2) {
                    MapViewComponent(
                        mapView = null,
                        mapLibreMap = null,
                        uiState = StartRunUiState(
                            currentSession = session,
                            runPath = session.path
                        ),
                        onMapReady = { },
                        showMap = true
                    )
                } else {
                    Text(
                        "No run recorded yet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private suspend fun fetchWeeklyGoalKm(): Int? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val ref = FirebaseDatabase.getInstance().reference
        .child("users")
        .child(uid)
        .child("goals")
        .child("weeklyDistanceKm")

    return suspendCancellableCoroutine { cont ->
        ref.get()
            .addOnSuccessListener { snap -> cont.resume(snap.getValue(Int::class.java)) }
            .addOnFailureListener { cont.resume(null) }
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

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DashAccentGreen
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
                    onClick = {
                        context.startActivity(Intent(context, RecentActivity::class.java))
                    }
                )
                BottomNavItem(
                    icon = Icons.Filled.Person,
                    onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
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
            containerColor = DashAccentGreen,
            contentColor = Color.Black
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
