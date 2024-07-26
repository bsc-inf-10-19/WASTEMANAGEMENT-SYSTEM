package com.example.myapplicationwmsystem.Screens

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import kotlin.math.roundToInt
import kotlin.math.min
import kotlin.math.sqrt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplicationwmsystem.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.graphics.toArgb
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToInt
@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val zombaLatitude = -15.3833
    val zombaLongitude = 35.3333
    val selectedBinName = remember { mutableStateOf<String?>(null) }
    val distanceInfo = remember { mutableStateOf<String?>(null) }
    val predefinedLocation = GeoPoint(-15.387608019953023, 35.33678641198779)
    val truckMarker = remember { mutableStateOf<Marker?>(null) }
    val isAnimating = remember { mutableStateOf(false) }
    val currentBin = remember { mutableStateOf<String?>(null) }


    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(18.0)
                controller.setCenter(GeoPoint(zombaLatitude, zombaLongitude))


                bins.forEach { bin ->
                    val marker = Marker(this).apply {
                        position = GeoPoint(bin.latitude, bin.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = bin.name.toString()
                        setOnMarkerClickListener { _, _ ->
                            controller.animateTo(position, 20.0, 1000L)
                            selectedBinName.value = bin.name.toString()
                            true
                        }
                    }
                    overlays.add(marker)
                }
                mapViewState.value = this
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            mapViewState.value = mapView
        }

    )

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            selectedBinName.value?.let { binName ->
                Text(
                    text = binName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            distanceInfo.value?.let { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            currentBin.value?.let { binName ->
                Text(
                    text = "Collecting garbage from $binName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(onClick = {
                mapViewState.value?.controller?.animateTo(GeoPoint(zombaLatitude, zombaLongitude), 14.0, 1000L)
            }) {
                Text("Reset View")
            }
            Button(onClick = {
                mapViewState.value?.controller?.zoomIn()
            }) {
                Text("Zoom In")
            }
            Button(onClick = {
                mapViewState.value?.controller?.zoomOut()
            }) {
                Text("Zoom Out")
            }
            Button(
                onClick = {
                    val binLocations = bins.map { GeoPoint(it.latitude, it.longitude) }
                    val route = findOptimalRoute(binLocations)
                    val waypoints = ArrayList<GeoPoint>().apply {
                        add(predefinedLocation)
                        addAll(route)
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        val roadManager = OSRMRoadManager(context, "OsmAndroidDemo")
                        val road = roadManager.getRoad(waypoints)
                        launch(Dispatchers.Main) {
                            mapViewState.value?.overlays?.removeAll { it is Polyline }
                            val roadOverlay = RoadManager.buildRoadOverlay(road).apply {
                                outlinePaint.color = Color.BLUE // Use Android's Color.BLUE
                                paint.color = Color.BLUE // Use Android's Color.BLUE
                            }

                            mapViewState.value?.overlays?.add(roadOverlay)

                            // Remove existing truck marker if any
                            truckMarker.value?.let { mapViewState.value?.overlays?.remove(it) }

                            // Create new truck marker
                            truckMarker.value = Marker(mapViewState.value).apply {
                                position = predefinedLocation
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
                            }
                            mapViewState.value?.overlays?.add(truckMarker.value)

                            mapViewState.value?.invalidate()
                            isAnimating.value = true
                        }
                    }
                },
                enabled = !isAnimating.value
            ) {
                Text("Calculate Route")
            }
        }
    }

    LaunchedEffect(isAnimating.value) {
        if (isAnimating.value && truckMarker.value != null) {
            val road = (mapViewState.value?.overlays?.find { it is Polyline } as? Polyline)?.actualPoints ?: return@LaunchedEffect
            for (point in road) {
                truckMarker.value?.position = point
                mapViewState.value?.invalidate()

                // Check if this point is a bin location
                val bin = bins.find { bin ->
                    val binPoint = GeoPoint(bin.latitude, bin.longitude)
                    binPoint.distanceToAsDouble(point) < 10 // 10 meters threshold
                }

                if (bin != null) {
                    // Simulate garbage collection at bin
                    currentBin.value = bin.name.toString()
                    delay(2000) // Wait for 2 seconds at each bin
                    currentBin.value = null
                } else {
                    delay(100) // Move every 0.1 second between bins
                }
            }
            isAnimating.value = false
            currentBin.value = null
        }
    }

    fun findOptimalRoute(locations: List<GeoPoint>): List<GeoPoint> {
    // Simple heuristic for TSP - Nearest Neighbor
    val visited = mutableSetOf<GeoPoint>()
    val route = mutableListOf<GeoPoint>()
    var currentLocation = locations.first()

    while (visited.size < locations.size) {
        visited.add(currentLocation)
        route.add(currentLocation)
        val nextLocation = locations.filterNot { it in visited }
            .minByOrNull { currentLocation.distanceToAsDouble(it) }
        currentLocation = nextLocation ?: break
    }

    return route
}}

fun findOptimalRoute(locations: List<GeoPoint>): List<GeoPoint> {
    // Simple heuristic for TSP - Nearest Neighbor
    val visited = mutableSetOf<GeoPoint>()
    val route = mutableListOf<GeoPoint>()
    var currentLocation = locations.first()

    while (visited.size < locations.size) {
        visited.add(currentLocation)
        route.add(currentLocation)
        val nextLocation = locations.filterNot { it in visited }
            .minByOrNull { currentLocation.distanceToAsDouble(it) }
        currentLocation = nextLocation ?: break
    }

    return route
}