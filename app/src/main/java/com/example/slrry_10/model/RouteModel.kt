package com.example.slrry_10.model

data class RouteModel(
    val coordinates: List<LocationModel>,
    val distance: Double = 0.0, // in meters
    val duration: Double = 0.0 // in seconds
)

