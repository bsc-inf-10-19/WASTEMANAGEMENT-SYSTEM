package com.example.myapplicationwmsystem.Screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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

    // Predefined location
    val predefinedLocation = GeoPoint(-15.387608019953023, 35.33678641198779)

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
                        title = bin.name
                        setOnMarkerClickListener { _, _ ->
                            controller.animateTo(position, 20.0, 1000L)
                            selectedBinName.value = bin.name
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
            Button(onClick = {
                val distances = bins.map { bin ->
                    val binLocation = GeoPoint(bin.latitude, bin.longitude)
                    val distance = predefinedLocation.distanceToAsDouble(binLocation)
                    Triple(bin.name, distance, binLocation)
                }
                val closestBin = distances.minByOrNull { it.second }
                closestBin?.let { (name, distance, location) ->
                    distanceInfo.value = "Closest bin: $name (${distance.roundToInt()} meters)"

                    // Draw the route
                    GlobalScope.launch(Dispatchers.IO) {
                        val roadManager = OSRMRoadManager(context, "OsmAndroidDemo")
                        val waypoints = ArrayList<GeoPoint>()
                        waypoints.add(predefinedLocation)
                        waypoints.add(location)
                        val road = roadManager.getRoad(waypoints)
                        launch(Dispatchers.Main) {
                            mapViewState.value?.overlays?.removeAll { it is Polyline }
                            val roadOverlay = RoadManager.buildRoadOverlay(road)
                            mapViewState.value?.overlays?.add(roadOverlay)
                            mapViewState.value?.invalidate()
                        }
                    }
                }
            }) {
                Text("Calculate Route")
            }
        }
    }
}
