package com.example.slrry_10.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        val data = mapOf(
            "uid" to uid,
            "email" to (user.email ?: ""),
            "displayName" to (displayName ?: user.displayName ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "emailVerified" to user.isEmailVerified,
            "updatedAt" to System.currentTimeMillis()
        )

        // Merge by updating children; this creates the node if missing.
        usersRef.updateChildren(data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}

