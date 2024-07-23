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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val zombaLatitude = -15.3833
    val zombaLongitude = 35.3333
    val selectedBinName = remember { mutableStateOf<String?>(null) }

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(18.0)
                controller.setCenter(GeoPoint(zombaLatitude, zombaLongitude))

                bins.forEachIndexed { index, bin ->
                    val marker = Marker(this).apply {
                        if (index == 0) {
                            position = GeoPoint(-15.7861, 35.0058)
                        } else {
                            val latOffset = (Math.random() - 0.5) * 0.05
                            val lonOffset = (Math.random() - 0.5) * 0.05
                            position = GeoPoint(zombaLatitude + latOffset, zombaLongitude + lonOffset)
                        }
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
        selectedBinName.value?.let { binName ->
            Text(
                text = binName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
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
        }
    }
}
