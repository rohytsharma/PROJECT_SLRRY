package com.example.slrry_10.auth

/**
 * Shared auth/onboarding rules extracted for unit testing.
 */
fun canProceedFromPassword(password: String, confirm: String): Boolean {
    return password.isNotBlank() && password == confirm
}

