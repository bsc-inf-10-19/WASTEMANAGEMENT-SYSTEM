import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.content.ContextCompat
import com.example.myapplicationwmsystem.R
import com.example.myapplicationwmsystem.db.Bin
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.turf.TurfMeasurement

@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val zombaLatitude = -15.3833
    val zombaLongitude = 35.3333
    val truckLatitude = -15.384507010964562  // Specific latitude for the truck within Zomba
    val truckLongitude = 35.31856127816891 // Specific longitude for the truck within Zomba
    val selectedBinName = remember { mutableStateOf<String?>(null) }
    val distanceInfo = remember { mutableStateOf<String?>(null) }
    val predefinedLocation = Point.fromLngLat(truckLongitude, truckLatitude)
    val initialZoom = 14.0

    // Create bitmaps for bin and truck icons
    val binIconBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bin_profile)
        val bitmap = (drawable as BitmapDrawable).bitmap
        Bitmap.createScaledBitmap(bitmap, 50, 50, true)
    }
    val truckIconBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.garbage_truck)
        val bitmap = (drawable as BitmapDrawable).bitmap
        Bitmap.createScaledBitmap(bitmap, 70, 70, true) // Slightly larger than bin icon
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(zombaLongitude, zombaLatitude))
                        .zoom(initialZoom)
                        .build()
                )

                val annotationApi = annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                // Add bin markers
                bins.forEach { bin ->
                    addBinMarker(pointAnnotationManager, bin, binIconBitmap)
                }
                // Add truck marker for predefined location
                addTruckMarker(pointAnnotationManager, predefinedLocation, truckIconBitmap)

                mapboxMap.addOnMapClickListener { point ->
                    val clickedBin = bins.minByOrNull { bin ->
                        TurfMeasurement.distance(
                            Point.fromLngLat(bin.longitude, bin.latitude),
                            point
                        )
                    }
                    clickedBin?.let {
                        selectedBinName.value = it.name
                        mapboxMap.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(it.longitude, it.latitude))
                                .zoom(16.0)
                                .build()
                        )
                    }
                    true
                }
                mapViewState.value = this
            }
        },
        modifier = Modifier.fillMaxSize()
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
                mapViewState.value?.mapboxMap?.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(zombaLongitude, zombaLatitude))
                        .zoom(initialZoom)
                        .bearing(0.0)
                        .pitch(0.0)
                        .build()
                )
                selectedBinName.value = null
                distanceInfo.value = null
            }) {
                Text("Reset View")
            }
            Button(onClick = {
                mapViewState.value?.mapboxMap?.cameraState?.zoom?.let { currentZoom ->
                    mapViewState.value?.mapboxMap?.flyTo(
                        CameraOptions.Builder().zoom(currentZoom + 1).build()
                    )
                }
            }) {
                Text("Zoom In")
            }
            Button(onClick = {
                mapViewState.value?.mapboxMap?.cameraState?.zoom?.let { currentZoom ->
                    mapViewState.value?.mapboxMap?.flyTo(
                        CameraOptions.Builder().zoom(currentZoom - 1).build()
                    )
                }
            }) {
                Text("Zoom Out")
            }
            Button(onClick = {
                val distances = bins.map { bin ->
                    val binLocation = Point.fromLngLat(bin.longitude, bin.latitude)
                    val distance = TurfMeasurement.distance(predefinedLocation, binLocation)
                    Triple(bin.name, distance, binLocation)
                }
                val closestBin = distances.minByOrNull { it.second }
                closestBin?.let { (name, distance, location) ->
                    distanceInfo.value = "Closest bin: $name (${distance.toInt()} meters)"
                    // Note: Mapbox Directions API requires additional setup and is not included in this example
                }
            }) {
                Text("See Route")
            }
        }
    }
}

private fun addBinMarker(manager: PointAnnotationManager, bin: Bin, icon: Bitmap) {
    val pointAnnotationOptions = PointAnnotationOptions()
        .withPoint(Point.fromLngLat(bin.longitude, bin.latitude))
        .withTextField(bin.name)
        .withIconImage(icon)
        .withIconSize(1.0)
        .withIconOffset(listOf(0.0, -25.0))

    manager.create(pointAnnotationOptions)
}

private fun addTruckMarker(manager: PointAnnotationManager, location: Point, icon: Bitmap) {
    val pointAnnotationOptions = PointAnnotationOptions()
        .withPoint(location)
        .withTextField("Zomba City Council")
        .withIconImage(icon)
        .withIconSize(1.0)
        .withIconOffset(listOf(0.0, -30.0))

    manager.create(pointAnnotationOptions)
}
