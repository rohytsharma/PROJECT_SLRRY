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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRYTheme

data class FriendRank(
    val rank: Int,
    val name: String,
    val points: Int,
    val rankChange: Int // positive for up, negative for down (we’ll show arrow up/green only for now)
)

class LeaderBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LeaderBoardScreen()
                }
            }
        }
    }
}

@Composable
fun LeaderBoardScreen() {
    val top3 = listOf(
        FriendRank(rank = 2, name = "Lokeece", points = 0, rankChange = 0),
        FriendRank(rank = 1, name = "Richu", points = 0, rankChange = 0),
        FriendRank(rank = 3, name = "Rohit", points = 0, rankChange = 0),
    )
    val others = listOf(
        FriendRank(4, "Yuva", 0, 0),
        FriendRank(5, "Sameer", 0, 0),
        FriendRank(6, "yuvati", 0, 0),
    )

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
        Header()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Friends",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2F2F2F)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Podium(top3)

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text(text = "Anime", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "Pts", fontSize = 12.sp, color = Color.Gray)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(others) { friend ->
                        FriendRow(friend)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ctx = LocalContext.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { (ctx as? ComponentActivity)?.finish() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF2F2F2F))
            }
            Text(
                text = "Leaderboard",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F2F2F)
            )
        }
        IconButton(onClick = { /* TODO: add friend */ }) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2F2F2F))
        }
    }
}

@Composable
private fun Podium(top3: List<FriendRank>) {
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
private fun PodiumBlock(friend: FriendRank?, height: Dp, color: Color, avatarColor: Color) {
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
                    text = friend?.name ?: "",
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
private fun FriendRow(friend: FriendRank) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Rank + arrow
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = friend.rank.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Up",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83D\uDC64", fontSize = 18.sp, color = Color.White)
                }
                // Name
                Text(text = friend.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2F2F2F))
            }
            // Points
            Text(text = friend.points.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2F2F))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LeaderboardPreview() {
    SLRRYTheme {
        LeaderBoardScreen()
    }
}