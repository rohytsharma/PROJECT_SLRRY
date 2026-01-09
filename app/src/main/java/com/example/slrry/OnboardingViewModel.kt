package com.example.slrry

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

/**
 * Shared ViewModel for the onboarding flow.
 * Stores answers and simple step tracking.
 */
class OnboardingViewModel : ViewModel() {

    // Step tracking (1-based)
    val currentStep = mutableStateOf(1)
    val totalSteps: Int = 7

    // Answers
    val fullName = mutableStateOf("")
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")

    // Additional onboarding data
    val gender = mutableStateOf<String?>(null)
    val heightCm = mutableStateOf<Int?>(null)
    val experience = mutableStateOf<String?>(null)
    val weeklyDistanceKm = mutableStateOf<Int?>(null)
    val runningGoal = mutableStateOf<String?>(null)
    val specificEvent = mutableStateOf<String?>(null)
}



