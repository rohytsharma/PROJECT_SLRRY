package com.example.slrry_10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class FriendsLeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                // Backwards-compatible entry point (Maps Hub now opens LeaderBoardActivity directly).
                LeaderBoardScreen(mode = LeaderboardMode.FRIENDS, onBack = { finish() })
            }
        }
    }
}
