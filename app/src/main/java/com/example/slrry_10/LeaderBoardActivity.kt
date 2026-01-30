package com.example.slrry_10

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.repository.CapturedAreasRepository
import com.example.slrry_10.repository.FriendsRepository
import com.example.slrry_10.repository.TerritoryRepository
import com.example.slrry_10.repository.UserSummary
import com.example.slrry_10.ui.theme.SLRRYTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

enum class LeaderboardMode { WORLD, FRIENDS, PERSONAL }

private const val EXTRA_LEADERBOARD_MODE = "leaderboard_mode"

fun leaderboardIntent(context: android.content.Context, mode: LeaderboardMode): Intent {
    return Intent(context, LeaderBoardActivity::class.java).putExtra(EXTRA_LEADERBOARD_MODE, mode.name)
}

private data class LeaderboardRow(
    val rank: Int,
    val user: UserSummary,
    val points: Int,
    val isYou: Boolean
)

class LeaderBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mode = (intent.getStringExtra(EXTRA_LEADERBOARD_MODE) ?: LeaderboardMode.FRIENDS.name)
            .let { raw ->
                LeaderboardMode.entries.firstOrNull { it.name == raw } ?: LeaderboardMode.FRIENDS
            }
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LeaderBoardScreen(
                        mode = mode,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderBoardScreen(
    mode: LeaderboardMode,
    onBack: () -> Unit
) {
    val authUid = FirebaseAuth.getInstance().currentUser?.uid
    val friendsRepo = remember { FriendsRepository() }
    val territoryRepo = remember { TerritoryRepository() }

    var rows by remember { mutableStateOf<List<LeaderboardRow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(mode) {
        isLoading = true
        val baseUsers: List<UserSummary> = when (mode) {
            LeaderboardMode.WORLD -> {
                val all = friendsRepo.listAllUsers(limit = 200, includeSelf = true)
                val me = friendsRepo.getCurrentUserSummary()
                if (me != null && all.none { it.uid == me.uid }) (all + me) else all
            }
            LeaderboardMode.FRIENDS -> {
                val friends = friendsRepo.listFriends()
                val me = friendsRepo.getCurrentUserSummary()
                if (me != null) (listOf(me) + friends).distinctBy { it.uid } else friends
            }
            LeaderboardMode.PERSONAL -> {
                val me = friendsRepo.getCurrentUserSummary()
                if (me != null) listOf(me) else emptyList()
            }
        }

        val computed = coroutineScope {
            baseUsers.map { u ->
                async(Dispatchers.IO) {
                    val totalArea = territoryRepo.getTotalAreaForUser(u.uid)
                    Pair(u, totalArea)
                }
            }.awaitAll()
        }
            .sortedByDescending { it.second }
            .mapIndexed { idx, pair ->
                val (u, total) = pair
                LeaderboardRow(
                    rank = idx + 1,
                    user = u,
                    points = total.toInt(),
                    isYou = (authUid != null && u.uid == authUid)
                )
            }

        rows = computed
        isLoading = false
    }

    val top3ForPodium: List<LeaderboardRow?> = remember(rows) {
        val first = rows.getOrNull(0)
        val second = rows.getOrNull(1)
        val third = rows.getOrNull(2)
        // UI expects: left=2nd, center=1st, right=3rd
        listOf(second, first, third)
    }
    val others = remember(rows) { rows.drop(3) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFC08A),
            Color(0xFFFFDFA6),
            Color(0xFFC3E7E6)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Header(
            title = when (mode) {
                LeaderboardMode.WORLD -> "Leaderboard"
                LeaderboardMode.FRIENDS -> "Leaderboard"
                LeaderboardMode.PERSONAL -> "Your Stats"
            },
            onBack = onBack
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when (mode) {
                LeaderboardMode.WORLD -> "World"
                LeaderboardMode.FRIENDS -> "Friends"
                LeaderboardMode.PERSONAL -> "Personal"
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2F2F2F)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Text("Loading…", color = Color(0xFF2F2F2F))
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Podium(top3ForPodium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom sheet-like list
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "Runner", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "m²", fontSize = 12.sp, color = Color.Gray)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = rows, key = { it.user.uid }) { row ->
                        FriendRow(row)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ctx = LocalContext.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF2F2F2F))
            }
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F2F2F)
            )
        }
        IconButton(
            onClick = {
                // Quick path to add friends (Profile screen contains the Add Friend dialog).
                try {
                    ctx.startActivity(Intent(ctx, ProfileActivity::class.java))
                } catch (_: Exception) {}
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2F2F2F))
        }
    }
}

@Composable
private fun Podium(top3: List<LeaderboardRow?>) {
    // Expecting list order: left (2nd), center (1st), right (3rd)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumBlock(
            friend = top3.getOrNull(0),
            height = 120.dp,
            color = Color(0xFFFFC04D),
            avatarColor = Color.Black
        )
        PodiumBlock(
            friend = top3.getOrNull(1),
            height = 170.dp,
            color = Color(0xFFFF6B51),
            avatarColor = Color.Black
        )
        PodiumBlock(
            friend = top3.getOrNull(2),
            height = 140.dp,
            color = Color(0xFF75BFC9),
            avatarColor = Color.Black
        )
    }
}

@Composable
private fun PodiumBlock(friend: LeaderboardRow?, height: Dp, color: Color, avatarColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(avatarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // simple person placeholder as an outline
            Text(text = "\uD83D\uDC64", fontSize = 28.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(height)
                .background(color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = friend?.user?.displayName ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = (friend?.points ?: 0).toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = (friend?.rank ?: 0).toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FriendRow(row: LeaderboardRow) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isYou) Color(0xFFEFFBE6) else Color(0xFFF7F7F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: rank
            Text(
                text = row.rank.toString(),
                modifier = Modifier.width(28.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF2F2F2F)
            )

            // Center: avatar + name (center-aligned)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83D\uDC64", fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (row.isYou) "${row.user.displayName} (You)" else row.user.displayName,
                    modifier = Modifier.fillMaxWidth(0.82f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2F2F2F),
                    maxLines = 1
                )
            }

            // Points
            Text(
                text = row.points.toString(),
                modifier = Modifier.width(56.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                color = Color(0xFF2F2F2F)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LeaderboardPreview() {
    SLRRYTheme {
        LeaderBoardScreen(mode = LeaderboardMode.FRIENDS, onBack = {})
    }
}