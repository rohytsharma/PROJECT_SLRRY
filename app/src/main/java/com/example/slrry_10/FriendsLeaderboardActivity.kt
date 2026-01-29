package com.example.slrry_10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.slrry_10.repository.CapturedAreasRepository
import com.example.slrry_10.repository.FriendsRepository
import com.example.slrry_10.repository.UserSummary
import com.example.slrry_10.ui.theme.SLRRY_10Theme

private data class FriendAreaRow(
    val user: UserSummary,
    val totalArea: Double
)

@Composable
fun FriendsLeaderboardUI(
    onBack: () -> Unit = {}
) {

    val friendsRepo = remember { FriendsRepository() }
    val areasRepo = remember { CapturedAreasRepository() }
    var rows by remember { mutableStateOf<List<FriendAreaRow>>(emptyList()) }

    LaunchedEffect(Unit) {
        val friends = friendsRepo.listFriends()
        val computed = friends.map { f ->
            val total = areasRepo.getAreasForUser(f.uid).sumOf { it.area }
            FriendAreaRow(user = f, totalArea = total)
        }.sortedByDescending { it.totalArea }
        rows = computed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFC371), Color(0xFF00B4DB))
                )
            )
    ) {

        // 🔹 Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable { onBack() }
            )
            Text("Leaderboard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(24.dp))
        }

        // 🔹 Leaderboard List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items = rows, key = { it.user.uid }) { row ->
                LeaderboardItem(
                    name = row.user.displayName,
                    points = String.format("%.0f", row.totalArea)
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(name: String, points: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text("$points m²", fontWeight = FontWeight.Bold)
        }
    }
}



@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun FriendsLeaderboardPreview() {
    SLRRY_10Theme { FriendsLeaderboardUI() }
}

class FriendsLeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                FriendsLeaderboardUI(onBack = { finish() })
            }
        }
    }
}
