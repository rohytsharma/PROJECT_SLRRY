package com.example.slrry_10.model

data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val runSessions: List<RunSession> = emptyList()
)