package com.example.myapplicationwmsystem.Screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun BinLocationScreen(binId: String) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // Initialize the mapView in a LaunchedEffect to ensure it's only done once
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(-15.39628, 35.33640))
        val marker = Marker(mapView).apply {
            position = GeoPoint(-15.39628, 35.33640)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Bin Location - Bin $binId"
            setOnMarkerClickListener { marker, _ ->
                mapView.controller.animateTo(marker.position, 20.0, 1000L)
                true
            }
        }
        mapView.overlays.add(marker)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    mapView.controller.animateTo(GeoPoint(-15.39628, 35.33640), 15.0, 1000L)
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Reset View", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = { mapView.controller.zoomIn() },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Zoom In", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = { mapView.controller.zoomOut() },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Zoom Out", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
