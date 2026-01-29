package com.example.slrry_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slrry_10.repository.UserProfileStore
import com.example.slrry_10.ui.theme.Mint
import com.example.slrry_10.ui.theme.NeonAccent
import com.example.slrry_10.ui.theme.SLRRYTheme
import kotlinx.coroutines.launch

class RunningGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SLRRYTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val onboardingViewModel: OnboardingViewModel = viewModel()
                    RunningGoalScreen(
                        selectedGoal = onboardingViewModel.runningGoal.value
                            ?: "Improve endurance",
                        step = 6,
                        totalSteps = onboardingViewModel.totalSteps,
                        onGoalSelected = { onboardingViewModel.runningGoal.value = it },
                        onBack = {
                            onboardingViewModel.currentStep.value = 5
                            finish()
                        },
                        onNext = {
                            val goal = onboardingViewModel.runningGoal.value ?: "Improve endurance"
                            lifecycleScope.launch {
                                UserProfileStore.updateCurrentUserFields(
                                    mapOf(
                                        "goals/runningGoal" to goal,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                )
                            }
                            onboardingViewModel.currentStep.value = 7
                            startActivity(
                                Intent(
                                    this@RunningGoalActivity,
                                    SpecificEventActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RunningGoalScreen(
    selectedGoal: String,
    step: Int,
    totalSteps: Int,
    onGoalSelected: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val options = listOf(
        "Improve endurance",
        "Increase speed",
        "Lose weight",
        "Stay fit & healthy",
        "Just for fun"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1EB))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Progress(step = step, totalSteps = totalSteps)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "WHAT'S YOUR RUNNING GOAL?",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select an option.",
            fontSize = 14.sp,
            color = Color(0xFF3F3F3F),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        options.forEachIndexed { index, option ->
            GoalOptionRow(
                label = option,
                selected = selectedGoal == option,
                onSelect = { onGoalSelected(option) }
            )
            if (index != options.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedNavButton(
                text = "Back",
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") },
                modifier = Modifier.weight(1f),
                onClick = onBack
            )
            FilledNavButton(
                text = "Next",
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next") },
                modifier = Modifier.weight(1f),
                onClick = onNext
            )
        }
    }
}

@Composable
private fun Logo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "SLRRY",
            style = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(6.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(NeonAccent, NeonAccent.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun Progress(step: Int, totalSteps: Int) {
    val fraction = (step.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$step/$totalSteps", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFEEEAE4), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(Color(0xFF2F2F2F), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun GoalOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable { onSelect() }
            .semantics { contentDescription = "Running goal option: $label" },
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Color.Black else Color(0xFFB8B8B8)
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6C63FF),
                    uncheckedColor = Color(0xFFB8B8B8)
                )
            )
        }
    }
}

@Composable
private fun OutlinedNavButton(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black,
            containerColor = Color(0xFFF5F1EB)
        ),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FilledNavButton(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Mint,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            icon()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningGoalPreview() {
    SLRRYTheme {
        RunningGoalScreen(
            selectedGoal = "Improve endurance",
            step = 6,
            totalSteps = 7,
            onGoalSelected = {},
            onBack = {},
            onNext = {}
        )
    }
}