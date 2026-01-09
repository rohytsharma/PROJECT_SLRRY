package com.example.slrry_10.repository

import com.example.slrry_10.model.RunSession
import com.example.slrry_10.model.UserModel
import kotlinx.coroutines.delay

class UserRepoImpl : UserRepo {
    private var currentUser: UserModel? = null
    private val runSessions = mutableListOf<RunSession>()
    
    override suspend fun getUser(): UserModel? {
        delay(100) // Simulate network call
        return currentUser ?: UserModel(
            id = "user_1",
            name = "User",
            email = "user@example.com"
        ).also { currentUser = it }
    }
    
    override suspend fun saveRunSession(session: RunSession) {
        delay(100) // Simulate network call
        val index = runSessions.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            runSessions[index] = session
        } else {
            runSessions.add(session)
        }
    }
    
    override suspend fun getRunSessions(): List<RunSession> {
        delay(100) // Simulate network call
        return runSessions.toList()
    }
}
