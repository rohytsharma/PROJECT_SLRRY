package com.example.slrry_10.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : AuthManager {

    override fun registerWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { res ->
                val user = res.user
                if (user == null) onResult(Result.failure(IllegalStateException("User is null")))
                else onResult(Result.success(user))
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { res ->
                val user = res.user
                if (user == null) onResult(Result.failure(IllegalStateException("User is null")))
                else onResult(Result.success(user))
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun sendPasswordResetEmail(email: String, onResult: (Result<Unit>) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun ensureUserDoc(
        user: FirebaseUser,
        displayName: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        val uid = user.uid
        val usersRef = db.reference.child("users").child(uid)
        val email = (user.email ?: "").trim()
        val fallbackNameFromEmail = email.substringBefore("@").replace('.', ' ').trim()

        fun chooseBestDisplayName(existingSnap: DataSnapshot?): String {
            val existingDisplay = existingSnap
                ?.child("displayName")
                ?.getValue(String::class.java)
                ?.trim()
                .orEmpty()
            val existingName = existingSnap
                ?.child("name")
                ?.getValue(String::class.java)
                ?.trim()
                .orEmpty()

            // Prefer onboarding full name if present.
            if (existingName.isNotBlank()) return existingName

            // If we already have a non-fallback displayName, keep it.
            val isFallback = existingDisplay.isNotBlank() &&
                fallbackNameFromEmail.isNotBlank() &&
                existingDisplay.equals(fallbackNameFromEmail, ignoreCase = true)
            if (existingDisplay.isNotBlank() && !isFallback) return existingDisplay

            // Otherwise, prefer explicit displayName (Google), then Firebase, then email prefix.
            val fromInput = (displayName ?: user.displayName).orEmpty().trim()
            if (fromInput.isNotBlank()) return fromInput
            if (fallbackNameFromEmail.isNotBlank()) return fallbackNameFromEmail
            return "Runner"
        }

        usersRef.get()
            .addOnSuccessListener { snap ->
                val resolvedName = chooseBestDisplayName(snap)
                val updates = mutableMapOf<String, Any?>(
                    "uid" to uid,
                    "email" to email,
                    "emailLower" to email.lowercase(),
                    // Canonical name fields used throughout the app
                    "displayName" to resolvedName,
                    "displayNameLower" to resolvedName.lowercase(),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "emailVerified" to user.isEmailVerified,
                    "updatedAt" to System.currentTimeMillis()
                )
                usersRef.updateChildren(updates)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e ->
                // Best-effort: write minimal fields without blocking sign-in.
                val resolvedName = (displayName ?: user.displayName ?: fallbackNameFromEmail)
                    .orEmpty()
                    .trim()
                    .ifBlank { "Runner" }
                val updates = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "emailLower" to email.lowercase(),
                    "displayName" to resolvedName,
                    "displayNameLower" to resolvedName.lowercase(),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "emailVerified" to user.isEmailVerified,
                    "updatedAt" to System.currentTimeMillis()
                )
                usersRef.updateChildren(updates)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { _ -> onResult(Result.failure(e)) }
            }
    }
}

