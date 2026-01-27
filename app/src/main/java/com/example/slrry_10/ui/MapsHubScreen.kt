package com.example.slrry_10.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.slrry_10.FriendsLeaderboardActivity
import com.example.slrry_10.model.AreaModel
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.viewmodel.MapsTab
import com.example.slrry_10.viewmodel.StartRunUiState
import com.example.slrry_10.viewmodel.StartRunViewModel
import com.example.slrry_10.viewmodel.ZoneOwner
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng

private data class RenderedMapIds(
    val layerIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList()
)

@Composable
fun MapsHubScreen(
    uiState: StartRunUiState,
    viewModel: StartRunViewModel,
    mapLibreMap: MapLibreMap?,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val accent = Color(0xFFB8FF3A)
    val text = Color(0xFF111416)

    val selectedOwners = when (uiState.mapsTab) {
        MapsTab.WORLD -> uiState.worldOwners
        MapsTab.PERSONAL -> listOf(
            ZoneOwner(
                id = "you",
                displayName = "You",
                colorArgb = accent.toArgb(),
                areas = uiState.capturedAreas
            )
        )
        MapsTab.FRIENDS -> uiState.friendsOwners
    }

    val totalArea = selectedOwners.sumOf { owner -> owner.areas.sumOf { it.area } }

    var rendered by remember { mutableStateOf(RenderedMapIds()) }

    LaunchedEffect(mapLibreMap, uiState.mapsTab, uiState.capturedAreas, uiState.worldOwners, uiState.friendsOwners) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style ->
            // Remove previously rendered layers/sources from our maps screen.
            rendered.layerIds.forEach { id -> try { style.removeLayer(id) } catch (_: Exception) {} }
            rendered.sourceIds.forEach { id -> try { style.removeSource(id) } catch (_: Exception) {} }

            // Also remove legacy area layers added by older helpers so we fully control the view here.
            clearLegacyAreaLayers(style)

            val newLayerIds = mutableListOf<String>()
            val newSourceIds = mutableListOf<String>()

            renderOwners(
                style = style,
                owners = selectedOwners.filter { it.areas.isNotEmpty() },
                layerIdsOut = newLayerIds,
                sourceIdsOut = newSourceIds
            )

            rendered = RenderedMapIds(layerIds = newLayerIds, sourceIds = newSourceIds)
        }

        // Always snap camera to the last known location (tight zoom).
        uiState.currentLocation?.let { loc ->
            try {
                val camera = CameraPosition.Builder()
                    .target(LatLng(loc.latitude, loc.longitude))
                    .zoom(17.0)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(camera), 450)
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top app bar + tabs (overlay on map)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack?.invoke() ?: viewModel.backToSummaryFromMaps() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = text)
                }
                Text(
                    text = "MAPS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, FriendsLeaderboardActivity::class.java))
                    }
                ) {
                    Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard", tint = text)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabText(
                    label = "World",
                    selected = uiState.mapsTab == MapsTab.WORLD,
                    onClick = { viewModel.setMapsTab(MapsTab.WORLD) }
                )
                Text(" | ", color = text.copy(alpha = 0.55f), fontSize = 20.sp)
                TabText(
                    label = "personal",
                    selected = uiState.mapsTab == MapsTab.PERSONAL,
                    onClick = { viewModel.setMapsTab(MapsTab.PERSONAL) }
                )
                Text(" | ", color = text.copy(alpha = 0.55f), fontSize = 20.sp)
                TabText(
                    label = "Friends",
                    selected = uiState.mapsTab == MapsTab.FRIENDS,
                    onClick = { viewModel.setMapsTab(MapsTab.FRIENDS) }
                )
            }
        }

        // Bottom metric (overlay)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Castle, contentDescription = null, tint = text, modifier = Modifier.height(30.dp))
                Text(
                    text = String.format("%.0fm²", totalArea),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
                Text(
                    text = "area captured",
                    fontSize = 14.sp,
                    color = text.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun TabText(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier.clickable { onClick() },
        fontSize = 28.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = Color(0xFF111416)
    )
}

private fun renderOwners(
    style: Style,
    owners: List<ZoneOwner>,
    layerIdsOut: MutableList<String>,
    sourceIdsOut: MutableList<String>
) {
    owners.forEach { owner ->
        owner.areas.forEachIndexed { idx, area ->
            val poly = area.polygon
            if (poly.size < 3) return@forEachIndexed

            val sourceId = "maps-zone-source-${owner.id}-$idx"
            val fillLayerId = "maps-zone-fill-${owner.id}-$idx"
            val lineLayerId = "maps-zone-line-${owner.id}-$idx"

            val ring = poly.map { Point.fromLngLat(it.longitude, it.latitude) }.toMutableList()
            // Close the ring for GeoJSON polygons.
            if (ring.isNotEmpty() && ring.first() != ring.last()) ring.add(ring.first())
            val polygon = Polygon.fromLngLats(listOf(ring))
            val feature = Feature.fromGeometry(polygon)

            val source = GeoJsonSource(sourceId, FeatureCollection.fromFeatures(listOf(feature)))
            style.addSource(source)
            sourceIdsOut.add(sourceId)

            val fill = FillLayer(fillLayerId, sourceId).withProperties(
                PropertyFactory.fillColor(owner.colorArgb),
                PropertyFactory.fillOpacity(0.45f)
            )
            style.addLayer(fill)
            layerIdsOut.add(fillLayerId)

            val line = LineLayer(lineLayerId, sourceId).withProperties(
                PropertyFactory.lineColor(darken(owner.colorArgb)),
                PropertyFactory.lineWidth(2.5f),
                PropertyFactory.lineOpacity(0.9f)
            )
            style.addLayer(line)
            layerIdsOut.add(lineLayerId)
        }

        // Add a simple "avatar" dot for each owner at the centroid of their first area.
        owner.areas.firstOrNull()?.polygon?.let { poly ->
            val centroid = centroidOf(poly) ?: return@let
            val avatarSourceId = "maps-avatar-source-${owner.id}"
            val avatarLayerId = "maps-avatar-layer-${owner.id}"
            val pt = Feature.fromGeometry(Point.fromLngLat(centroid.longitude, centroid.latitude))
            val source = GeoJsonSource(avatarSourceId, FeatureCollection.fromFeatures(listOf(pt)))
            style.addSource(source)
            sourceIdsOut.add(avatarSourceId)

            val layer = CircleLayer(avatarLayerId, avatarSourceId).withProperties(
                PropertyFactory.circleRadius(14f),
                PropertyFactory.circleColor(Color.White.toArgb()),
                PropertyFactory.circleStrokeColor(owner.colorArgb),
                PropertyFactory.circleStrokeWidth(6f)
            )
            style.addLayer(layer)
            layerIdsOut.add(avatarLayerId)
        }
    }
}

private fun centroidOf(points: List<LocationModel>): LocationModel? {
    if (points.isEmpty()) return null
    val lat = points.sumOf { it.latitude } / points.size
    val lon = points.sumOf { it.longitude } / points.size
    return LocationModel(latitude = lat, longitude = lon)
}

private fun darken(argb: Int): Int {
    val a = (argb shr 24) and 0xFF
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    fun d(x: Int) = (x * 0.70).toInt().coerceIn(0, 255)
    return (a shl 24) or (d(r) shl 16) or (d(g) shl 8) or d(b)
}

private fun clearLegacyAreaLayers(style: Style) {
    // Old StartRunActivity helpers used these IDs. Remove if present so this screen is clean.
    try {
        style.removeLayer("current-area-line-layer")
        style.removeLayer("current-area-fill-layer")
        style.removeSource("current-area-source")
    } catch (_: Exception) {}

    // Remove up to a reasonable number of legacy layers/sources.
    for (i in 0..60) {
        try {
            style.removeLayer("area-fill-$i")
            style.removeLayer("area-line-$i")
            style.removeSource("area-source-$i")
        } catch (_: Exception) {}
    }
}


