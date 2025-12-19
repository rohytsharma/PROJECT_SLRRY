package com.example.slrry_10.model

data class AreaModel(
    val polygon: List<LocationModel>,
    val area: Double = 0.0 // in square meters
)

