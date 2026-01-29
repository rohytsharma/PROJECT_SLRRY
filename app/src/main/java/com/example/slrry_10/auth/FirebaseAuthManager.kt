package com.example.slrry_10.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
        val doc = db.collection("users").document(uid)

        val data = mutableMapOf<String, Any?>(
            "uid" to uid,
            "email" to user.email,
            "displayName" to (displayName ?: user.displayName),
            "photoUrl" to user.photoUrl?.toString(),
            "updatedAt" to Timestamp.now()
        )

        // Only set createdAt if the document doesn't exist yet.
        doc.get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    data["createdAt"] = Timestamp.now()
                }
                doc.set(data, SetOptions.merge())
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}

