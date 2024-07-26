import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplicationwmsystem.db.Bin
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val zombaLatitude = -15.3833
    val zombaLongitude = 35.3333
    val selectedBinName = remember { mutableStateOf<String?>(null) }
    val distanceInfo = remember { mutableStateOf<String?>(null) }
    val predefinedLocation = Point.fromLngLat(35.33678641198779, -15.387608019953023)

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(zombaLongitude, zombaLatitude))
                        .zoom(14.0)
                        .build()
                )

                val annotationApi = annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                bins.forEach { bin ->
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(bin.longitude, bin.latitude))
                        .withTextField(bin.name)
                    pointAnnotationManager.create(pointAnnotationOptions)
                }

                getMapboxMap().addOnMapClickListener { point ->
                    val clickedBin = bins.minByOrNull { bin ->
                        TurfMeasurement.distance(
                            Point.fromLngLat(bin.longitude, bin.latitude),
                            point
                        )
                    }
                    clickedBin?.let {
                        selectedBinName.value = it.name
                        getMapboxMap().flyTo(
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
                mapViewState.value?.getMapboxMap()?.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(zombaLongitude, zombaLatitude))
                        .zoom(14.0)
                        .build()
                )
            }) {
                Text("Reset View")
            }
            Button(onClick = {
                mapViewState.value?.getMapboxMap()?.cameraState?.zoom?.let { currentZoom ->
                    mapViewState.value?.getMapboxMap()?.flyTo(
                        CameraOptions.Builder().zoom(currentZoom + 1).build()
                    )
                }
            }) {
                Text("Zoom In")
            }
            Button(onClick = {
                mapViewState.value?.getMapboxMap()?.cameraState?.zoom?.let { currentZoom ->
                    mapViewState.value?.getMapboxMap()?.flyTo(
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
                Text("Find Closest Bin")
            }
        }
    }
}
