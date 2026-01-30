package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.slrry_10.model.RunSession
import com.example.slrry_10.repository.FirebaseUserRepoImpl
import com.example.slrry_10.repository.FriendsRepository
import com.example.slrry_10.repository.UserSummary
import com.example.slrry_10.ui.theme.SLRRY_10Theme
import com.example.slrry_10.ui.theme.Mint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
    val context = androidx.compose.ui.platform.LocalContext.current
    // Light green page background + neon header to match the app's premium look.
    val background = Mint.copy(alpha = 0.14f)
    val header = Mint
    val textGray = Color(0xFF6E757A)

    val repo = remember { FirebaseUserRepoImpl() }
    var displayName by remember { mutableStateOf("—") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf<Int?>(null) }
    var bio by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Beginner") }
    var weeklyGoalKm by remember { mutableStateOf<Int?>(null) }
    var weeklyGoalActiveHours by remember { mutableStateOf<Int?>(null) }
    var weekDistanceKm by remember { mutableStateOf(0) }
    var weekActiveHours by remember { mutableStateOf(0) }
    var totalDistanceKm by remember { mutableStateOf(0.0) }
    var totalHours by remember { mutableStateOf(0.0) }
    var totalKcal by remember { mutableStateOf(0.0) }
    var streakDays by remember { mutableStateOf<List<StreakDay>>(emptyList()) }
    val friendsRepo = remember { FriendsRepository() }
    var friends by remember { mutableStateOf<List<UserSummary>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<UserSummary>>(emptyList()) }
    var showAddFriend by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    suspend fun loadProfile() {
        val (dbDisplay, dbName, dbEmail) = fetchUserProfile()
        val (ageFromDb, bioFromDb) = fetchUserExtras()
        val authUser = FirebaseAuth.getInstance().currentUser

        val resolvedEmail = dbEmail
            .takeIf { !it.isNullOrBlank() }
            ?: authUser?.email
            ?: ""
        val emailPrefix = resolvedEmail.substringBefore("@").trim()

        fun looksLikeEmailishName(n: String): Boolean {
            val s = n.trim()
            if (s.isBlank()) return true
            if (s.contains("@")) return true
            if (emailPrefix.isNotBlank() && s.equals(emailPrefix, ignoreCase = true)) return true
            if (emailPrefix.isNotBlank() && s.equals(emailPrefix.replace('.', ' '), ignoreCase = true)) return true
            return false
        }

        val resolvedName: String = when {
            !dbName.isNullOrBlank() && looksLikeEmailishName(dbDisplay.orEmpty()) -> dbName.orEmpty()
            !dbDisplay.isNullOrBlank() && !looksLikeEmailishName(dbDisplay) -> dbDisplay.orEmpty()
            !dbName.isNullOrBlank() -> dbName.orEmpty()
            authUser?.displayName?.takeIf { !looksLikeEmailishName(it) } != null -> authUser.displayName.orEmpty()
            else -> "Runner"
        }.trim().ifBlank { "Runner" }

        displayName = resolvedName
        email = resolvedEmail
        age = ageFromDb
        bio = bioFromDb.orEmpty()

        val (goalKm, goalHours) = fetchGoals()
        weeklyGoalKm = goalKm
        weeklyGoalActiveHours = goalHours

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
        friends = friendsRepo.listFriends()
        incomingRequests = friendsRepo.listIncomingFriendRequests()
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    // When returning from Edit Profile, refresh automatically.
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { loadProfile() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProfileHeader(
                onBack = onBack,
                onEdit = {
                    try {
                        context.startActivity(Intent(context, EditProfileActivity::class.java))
                    } catch (_: Exception) {}
                },
                headerColor = header,
                displayName = displayName,
                subtitle = level,
                email = email
            )
        }

        item {
            AboutCard(
                age = age,
                bio = bio
            )
        }

        item {
        ProgressCard(
            title = "Weekly Distance",
                current = weekDistanceKm,
                goal = (weeklyGoalKm ?: 0).coerceAtLeast(1),
            unit = "km",
            textGray = textGray
        )
        }

        item {
        ProgressCard(
            title = "Active Time",
                current = weekActiveHours,
            goal = (weeklyGoalActiveHours ?: 25).coerceAtLeast(1),
            unit = "hrs",
            textGray = textGray
        )
        }

        item {
            AchievementCard(totalDistanceKm = totalDistanceKm, totalHours = totalHours, totalKcal = totalKcal)
        }

        item {
            StreaksCard(days = streakDays)
        }

        item {
            FriendsCard(
                friends = friends,
                onAddClick = { showAddFriend = true }
            )
        }

        item {
            FriendRequestsCard(
                requests = incomingRequests,
                onAccept = { fromUid ->
                    scope.launch {
                        friendsRepo.acceptFriendRequest(fromUid)
                        incomingRequests = friendsRepo.listIncomingFriendRequests()
                        friends = friendsRepo.listFriends()
                    }
                }
            )
        }
    }

    if (showAddFriend) {
        AddFriendDialog(
            friendsRepo = friendsRepo,
            onDismiss = { showAddFriend = false },
            onFriendAdded = {
                scope.launch {
                    friends = friendsRepo.listFriends()
                    incomingRequests = friendsRepo.listIncomingFriendRequests()
                }
            }
        )
    }
}

@Composable
private fun FriendsCard(
    friends: List<UserSummary>,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Friends", fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add friend")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (friends.isEmpty()) {
                Text("No friends yet. Tap + to add.", color = Color.Gray, fontSize = 12.sp)
            } else {
                friends.take(6).forEach { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(f.displayName, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (friends.size > 6) {
                    Text(
                        text = "and ${friends.size - 6} more…",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    friendsRepo: FriendsRepository,
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit
) {
    var results by remember { mutableStateOf<List<UserSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // "Show all users" when opening the dialog (paged).
        isLoading = true
        results = friendsRepo.listAllUsers(limit = 50)
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add friend", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Color.White)
                    ) { Text("Done") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Text("Loading users…", fontSize = 12.sp, color = Color.Gray)
                } else if (results.isEmpty()) {
                    Text(
                        "No user profiles found in Realtime Database yet.\n" +
                            "This list shows `/users` nodes (not Firebase Auth users). " +
                            "Log in once with the other accounts so their `/users/{uid}` gets created.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        items(items = results, key = { it.uid }) { u ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(u.displayName, fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                friendsRepo.sendFriendRequest(u.uid)
                                                onFriendAdded()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Mint,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Add", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRequestsCard(
    requests: List<UserSummary>,
    onAccept: (fromUid: String) -> Unit
) {
    if (requests.isEmpty()) return

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Friend requests", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            requests.take(10).forEach { u ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.displayName, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onAccept(u.uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Color.White)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    headerColor: Color,
    displayName: String,
    subtitle: String,
    email: String
) {
    val headerText = Color(0xFF111416)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(headerColor)
            .statusBarsPadding()
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = headerText)
        }

        IconButton(
            onClick = onEdit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = headerText)
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
                    .background(Color.White.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                val initial = displayName.trim().firstOrNull()?.uppercase() ?: "R"
                Text(initial, color = headerText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(displayName, color = headerText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = headerText.copy(alpha = 0.75f), fontSize = 14.sp)
            if (email.isNotBlank()) {
                Text(email, color = headerText.copy(alpha = 0.65f), fontSize = 12.sp)
            }
        }

        Text(
            "Profile",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            color = headerText,
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
                gridItems(days) { day ->
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

@Composable
private fun AboutCard(
    age: Int?,
    bio: String
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("About", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            val ageText = age?.takeIf { it > 0 }?.toString() ?: "—"
            Text("Age: $ageText", color = Color(0xFF6E757A), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bio.trim().ifBlank { "Add a bio to tell friends about you." },
                fontSize = 14.sp
            )
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

private suspend fun fetchGoals(): Pair<Int?, Int?> {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Pair(null, null)
    val ref = FirebaseDatabase.getInstance().reference
        .child("users")
        .child(uid)
        .child("goals")

    return suspendCancellableCoroutine { cont ->
        ref.get()
            .addOnSuccessListener { snap ->
                val km = snap.child("weeklyDistanceKm").getValue(Int::class.java)
                val hrs = snap.child("weeklyActiveHours").getValue(Int::class.java)
                cont.resume(Pair(km, hrs))
            }
            .addOnFailureListener { cont.resume(Pair(null, null)) }
    }
}

private suspend fun fetchUserExtras(): Pair<Int?, String?> {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Pair(null, null)
    val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    return suspendCancellableCoroutine { cont ->
        ref.get()
            .addOnSuccessListener { snap ->
                val age = snap.child("age").getValue(Int::class.java)
                val bio = snap.child("bio").getValue(String::class.java)
                cont.resume(Pair(age, bio))
            }
            .addOnFailureListener { cont.resume(Pair(null, null)) }
    }
}

private suspend fun fetchUserProfile(): Triple<String?, String?, String?> {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Triple(null, null, null)
    val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    return suspendCancellableCoroutine { cont ->
        ref.get()
            .addOnSuccessListener { snap ->
                val display = snap.child("displayName").getValue(String::class.java)
                val name = snap.child("name").getValue(String::class.java)
                val email = snap.child("email").getValue(String::class.java)
                cont.resume(Triple(display, name, email))
            }
            .addOnFailureListener {
                cont.resume(Triple(null, null, null))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    MaterialTheme { ProfileScreen() }
}

