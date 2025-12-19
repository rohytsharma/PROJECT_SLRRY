package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.slrry_10.ui.theme.SLRRY_10Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRY_10Theme {
                SLRRYScreen(
                    onStartRunClick = {
                        // Single-activity run flow (keeps MapView from resetting)
                        val intent = Intent(this@MainActivity, StartRunActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }
}

@Composable
fun SLRRYScreen(
    modifier: Modifier = Modifier,
    onStartRunClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onStartRunClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SLRRY",
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SLRRYScreenPreview() {
    SLRRY_10Theme {
        SLRRYScreen()
    }
}