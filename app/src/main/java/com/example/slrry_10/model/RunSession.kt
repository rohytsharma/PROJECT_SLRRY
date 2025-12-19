package com.example.slrry_10.model

data class RunSession(
    val id: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val path: List<LocationModel> = emptyList(),
    val distance: Double = 0.0, // in meters
    val duration: Long = 0L, // in seconds
    val averagePace: String = "0'00''", // minutes'seconds''
    val capturedAreas: List<AreaModel> = emptyList(),
    val isActive: Boolean = false
)

