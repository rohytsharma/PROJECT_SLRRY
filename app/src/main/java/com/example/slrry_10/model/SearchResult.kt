package com.example.slrry_10.model

data class SearchResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

