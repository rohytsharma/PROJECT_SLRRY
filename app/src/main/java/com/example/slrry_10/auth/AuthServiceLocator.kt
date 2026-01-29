package com.example.slrry_10.auth

/**
 * Very small service locator so Activities can use Firebase in production,
 * while instrumented tests can swap in a fake.
 */
object AuthServiceLocator {
    @Volatile
    var authManager: AuthManager = FirebaseAuthManager()
}

