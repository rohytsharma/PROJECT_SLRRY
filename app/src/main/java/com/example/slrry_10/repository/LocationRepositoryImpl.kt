package com.example.slrry_10.repository

import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.model.RouteModel
import com.example.slrry_10.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class LocationRepositoryImpl : LocationRepository {
    
    private val nominatimBaseUrl = "https://nominatim.openstreetmap.org"
    private val osrmBaseUrl = "https://router.project-osrm.org"
    
    override suspend fun searchLocation(query: String): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$nominatimBaseUrl/search?q=$encodedQuery&format=json&limit=5"
            
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "SLRRY_10/1.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                results.add(
                    SearchResult(
                        displayName = item.getString("display_name"),
                        latitude = item.getDouble("lat"),
                        longitude = item.getDouble("lon"),
                        address = item.optString("display_name", null) ?: null
                    )
                )
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reverseGeocode(lat: Double, lon: Double): Result<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "$nominatimBaseUrl/reverse?format=json&lat=$lat&lon=$lon"
            
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "SLRRY_10/1.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)
            
            val address = jsonObject.getJSONObject("address")
            val displayName = jsonObject.getString("display_name")
            
            val lat = jsonObject.getDouble("lat")
            val lon = jsonObject.getDouble("lon")
            
            val result = SearchResult(
                displayName = displayName,
                latitude = lat,
                longitude = lon,
                address = displayName
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Result<RouteModel> = withContext(Dispatchers.IO) {
        try {
            val url = "$osrmBaseUrl/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=full&geometries=geojson"
            
            val connection = java.net.URL(url).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)
            
            if (jsonObject.getString("code") != "Ok") {
                return@withContext Result.failure(Exception("Route not found"))
            }
            
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) {
                return@withContext Result.failure(Exception("No route found"))
            }
            
            val route = routes.getJSONObject(0)
            val geometry = route.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")
            
            val locationList = mutableListOf<LocationModel>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                locationList.add(
                    LocationModel(
                        latitude = coord.getDouble(1),
                        longitude = coord.getDouble(0)
                    )
                )
            }
            
            val distance = route.getDouble("distance")
            val duration = route.getDouble("duration")
            
            Result.success(
                RouteModel(
                    coordinates = locationList,
                    distance = distance,
                    duration = duration
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun calculateArea(polygon: List<LocationModel>): Double {
        if (polygon.size < 3) return 0.0
        
        // Shoelace formula for calculating polygon area
        var area = 0.0
        val n = polygon.size
        
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += polygon[i].longitude * polygon[j].latitude
            area -= polygon[j].longitude * polygon[i].latitude
        }
        
        // Convert to square meters (approximate, assuming Earth is a sphere)
        // 1 degree latitude ≈ 111,000 meters
        // 1 degree longitude ≈ 111,000 * cos(latitude) meters
        val avgLat = polygon.map { it.latitude }.average()
        val latMeters = 111000.0
        val lonMeters = 111000.0 * kotlin.math.cos(Math.toRadians(avgLat))
        
        return kotlin.math.abs(area) * 0.5 * latMeters * lonMeters
    }
}

