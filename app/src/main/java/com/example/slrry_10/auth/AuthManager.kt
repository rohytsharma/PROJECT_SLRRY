package com.example.slrry_10.auth

import com.google.firebase.auth.FirebaseUser

interface AuthManager {
    fun registerWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    )

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    )

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    )

    fun ensureUserDoc(
        user: FirebaseUser,
        displayName: String? = null,
        onResult: (Result<Unit>) -> Unit
    )
}

