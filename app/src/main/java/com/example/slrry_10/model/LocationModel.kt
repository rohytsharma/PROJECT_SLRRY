package com.example.slrry_10.model

data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toLatLng(): Pair<Double, Double> = Pair(latitude, longitude)
}

