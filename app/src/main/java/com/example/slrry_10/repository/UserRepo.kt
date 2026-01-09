package com.example.slrry_10.repository

import com.example.slrry_10.model.RunSession
import com.example.slrry_10.model.UserModel

interface UserRepo {
    suspend fun getUser(): UserModel?
    suspend fun saveRunSession(session: RunSession)
    suspend fun getRunSessions(): List<RunSession>
}