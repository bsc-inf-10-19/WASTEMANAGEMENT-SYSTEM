package com.example.myapplicationwmsystem.Screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.example.myapplicationwmsystem.R

@Composable
fun BinLocationScreen(binId: String) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val binLocation = Point.fromLngLat(35.33640, -15.39628) // Longitude first, then Latitude

    Box(modifier = Modifier.fillMaxSize()) {
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

                    // Convert drawable to Bitmap and resize it
                    val binIcon = ContextCompat.getDrawable(context, R.drawable.bin_profile)
                    val bitmap = binIcon?.let { drawableToBitmap(it) }
                    val resizedBitmap = bitmap?.let { resizeBitmap(it, 64, 64) } // Resize to 64x64 pixels
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(binLocation)
                        .withTextField("Bin Location - Bin $binId")
                        .withIconImage(resizedBitmap!!)
                        .withIconSize(1.0) // You can adjust this value to fine-tune the size
                    pointAnnotationManager.create(pointAnnotationOptions)

                    mapViewState.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
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

// Helper function to convert Drawable to Bitmap
fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

// Helper function to resize Bitmap
fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}
