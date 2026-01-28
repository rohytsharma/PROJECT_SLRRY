package com.example.slrry_10

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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

data class FriendUI(
    val name: String,
    val distance: Int
)

@Composable
fun FriendsLeaderboardUI() {

    var searchText by remember { mutableStateOf("") }

    val friends = remember {
        mutableStateListOf(
            FriendUI("Someone", 1020),
            FriendUI("Sodul", 1010),
            FriendUI("Sameer", 990),
            FriendUI("yuvati", 958)
        )
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
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Text("Leaderboard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Add, contentDescription = null)
        }

        // 🔹 Friends Section
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF8C42))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text("Friends", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AddFriendRow("Someone")
                Spacer(modifier = Modifier.height(8.dp))
                AddFriendRow("Sodul")
            }
        }

        // 🔹 Leaderboard List
        LazyColumn {
            itemsIndexed(friends.sortedByDescending { it.distance }) { index, user ->
                LeaderboardItem(rank = index + 1, friend = user)
            }
        }
    }
}

@Composable
fun AddFriendRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.LightGray, RoundedCornerShape(30.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(name)
        }
        Icon(Icons.Default.PersonAdd, contentDescription = null)
    }
}

@Composable
fun LeaderboardItem(rank: Int, friend: FriendUI) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(rank.toString())
            Text(friend.name)
            Text(friend.distance.toString())
        }
    }
}



@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun FriendsLeaderboardPreview() {
    FriendsLeaderboardUI()
}
