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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

@Composable
fun BinLocationScreen(binId: String) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val binLocation = Point.fromLngLat(35.33640, -15.39628) // Longitude first, then Latitude

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(binLocation)
                        .zoom(15.0)
                        .build()
                )

                val annotationApi = annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(binLocation)
                    .withTextField("Bin Location - Bin $binId")
                pointAnnotationManager.create(pointAnnotationOptions)

                mapViewState.value = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    mapViewState.value?.getMapboxMap()?.flyTo(
                        CameraOptions.Builder()
                            .center(binLocation)
                            .zoom(15.0)
                            .build()
                    )
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Reset View", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = {
                    mapViewState.value?.getMapboxMap()?.cameraState?.zoom?.let { currentZoom ->
                        mapViewState.value?.getMapboxMap()?.flyTo(
                            CameraOptions.Builder().zoom(currentZoom + 1).build()
                        )
                    }
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Zoom In", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = {
                    mapViewState.value?.getMapboxMap()?.cameraState?.zoom?.let { currentZoom ->
                        mapViewState.value?.getMapboxMap()?.flyTo(
                            CameraOptions.Builder().zoom(currentZoom - 1).build()
                        )
                    }
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Zoom Out", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
