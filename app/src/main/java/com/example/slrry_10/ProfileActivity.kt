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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.resume

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

    val repo = remember { FirebaseUserRepoImpl() }
    var displayName by remember { mutableStateOf("—") }
    var email by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Beginner") }
    var weeklyGoalKm by remember { mutableStateOf<Int?>(null) }
    var weekDistanceKm by remember { mutableStateOf(0) }
    var weekActiveHours by remember { mutableStateOf(0) }
    var totalDistanceKm by remember { mutableStateOf(0.0) }
    var totalHours by remember { mutableStateOf(0.0) }
    var totalKcal by remember { mutableStateOf(0.0) }
    var streakDays by remember { mutableStateOf<List<StreakDay>>(emptyList()) }

    LaunchedEffect(Unit) {
        val (nameFromDb, emailFromDb) = fetchUserProfile()
        val authUser = FirebaseAuth.getInstance().currentUser

        displayName = nameFromDb
            .takeIf { !it.isNullOrBlank() }
            ?: authUser?.displayName
            ?: "Runner"
        email = emailFromDb
            .takeIf { !it.isNullOrBlank() }
            ?: authUser?.email
            ?: ""

        weeklyGoalKm = fetchWeeklyGoalKm()

        val runs = repo.getRunSessions()
        val now = System.currentTimeMillis()
        val weekAgo = now - 7L * 24L * 60L * 60L * 1000L
        val weekRuns = runs.filter { it.startTime >= weekAgo }

        val weekMeters = weekRuns.sumOf { it.distance }
        weekDistanceKm = (weekMeters / 1000.0).toInt()
        weekActiveHours = (weekRuns.sumOf { it.duration }.toDouble() / 3600.0).toInt()

        totalDistanceKm = runs.sumOf { it.distance } / 1000.0
        totalHours = runs.sumOf { it.duration }.toDouble() / 3600.0
        // Simple estimate: ~60 kcal per km (common rough estimate).
        totalKcal = totalDistanceKm * 60.0

        level = when {
            totalDistanceKm >= 200.0 -> "Advanced"
            totalDistanceKm >= 50.0 -> "Intermediate"
            else -> "Beginner"
        }

        streakDays = buildStreakDays(runs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        ProfileHeader(
            onBack = onBack,
            headerColor = header,
            displayName = displayName,
            subtitle = level,
            email = email
        )

        ProgressCard(
            title = "Weekly Distance",
            current = weekDistanceKm,
            goal = (weeklyGoalKm ?: 0).coerceAtLeast(1),
            unit = "km",
            textGray = textGray
        )

        ProgressCard(
            title = "Active Time",
            current = weekActiveHours,
            goal = 25,
            unit = "hrs",
            textGray = textGray
        )

        AchievementCard(totalDistanceKm = totalDistanceKm, totalHours = totalHours, totalKcal = totalKcal)
        StreaksCard(days = streakDays)
    }
}

@Composable
private fun ProfileHeader(
    onBack: () -> Unit,
    headerColor: Color,
    displayName: String,
    subtitle: String,
    email: String
) {
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
                val initial = displayName.trim().firstOrNull()?.uppercase() ?: "R"
                Text(initial, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            if (email.isNotBlank()) {
                Text(email, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
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
private fun AchievementCard(
    totalDistanceKm: Double,
    totalHours: Double,
    totalKcal: Double
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total progress", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressStat(icon = "🏃", value = String.format("%.1f", totalDistanceKm), label = "km")
                ProgressStat(icon = "⏱️", value = String.format("%.1f", totalHours), label = "hr")
                ProgressStat(icon = "🔥", value = String.format("%.0f", totalKcal), label = "kcal")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Keep going — your streak is building up!")
            }
        }
    }
}

@Composable
private fun ProgressStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

private enum class StreakStatus { DONE, MISSED, FUTURE }

private data class StreakDay(val date: LocalDate, val status: StreakStatus)

@Composable
private fun StreaksCard(days: List<StreakDay>) {
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
                items(days) { day ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when (day.status) {
                                    StreakStatus.DONE -> Color(0xFF4CAF50)
                                    StreakStatus.MISSED -> Color(0xFFE53935)
                                    StreakStatus.FUTURE -> Color.LightGray
                                }
                            )
                    )
                }
            }
        }
    }
}

private fun buildStreakDays(runs: List<RunSession>): List<StreakDay> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    // Align grid to weeks and include future days of the current week (grey), like the screenshot.
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val gridStart = startOfWeek.minusDays(21) // 4 weeks total (28 days)

    val runDates = runs.asSequence()
        .map { Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate() }
        .toSet()

    return (0 until 28).map { offset ->
        val d = gridStart.plusDays(offset.toLong())
        val status = when {
            d.isAfter(today) -> StreakStatus.FUTURE
            d in runDates -> StreakStatus.DONE
            else -> StreakStatus.MISSED
        }
        StreakDay(d, status)
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

private suspend fun fetchUserProfile(): Pair<String?, String?> {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Pair(null, null)
    val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    return suspendCancellableCoroutine { cont ->
        ref.get()
            .addOnSuccessListener { snap ->
                val name = snap.child("displayName").getValue(String::class.java)
                    ?: snap.child("name").getValue(String::class.java)
                val email = snap.child("email").getValue(String::class.java)
                cont.resume(Pair(name, email))
            }
            .addOnFailureListener {
                cont.resume(Pair(null, null))
            }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    MaterialTheme { ProfileScreen() }
}

