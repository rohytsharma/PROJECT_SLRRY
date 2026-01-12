package com.example.slrry_10.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Compatibility layer for the existing onboarding screens that were authored against
 * a different theme package. We keep these names so the screens remain intact.
 */
val Mint = Color(0xFF7FE3C0)
val NeonAccent = Color(0xFFB8FF3A)
val FieldGrey = Color(0xFFEDEDED)

@Composable
fun SLRRYTheme(content: @Composable () -> Unit) {
    SLRRY_10Theme(content = content)
}


