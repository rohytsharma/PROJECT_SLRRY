package com.example.slrry_10.repository

import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RouteModel
import com.example.slrry_10.model.SearchResult

interface LocationRepository {
    suspend fun searchLocation(query: String): Result<List<SearchResult>>
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<SearchResult>
    suspend fun getRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Result<RouteModel>
    fun calculateArea(polygon: List<LocationModel>): Double
}

