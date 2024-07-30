import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplicationwmsystem.R
import com.example.myapplicationwmsystem.db.Bin
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.turf.TurfMeasurement
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.LineString
import com.example.myapplicationwmsystem.Components.MapboxAccessToken


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun MapScreen(bins: List<Bin>) {
    val context = LocalContext.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val zombaLatitude = -15.3833
    val zombaLongitude = 35.3333
    val truckLatitude = -15.384507010964562
    val truckLongitude = 35.31856127816891
    val selectedBinName = remember { mutableStateOf<String?>(null) }
    val distanceInfo = remember { mutableStateOf<String?>(null) }
    val predefinedLocation = Point.fromLngLat(truckLongitude, truckLatitude)
    val initialZoom = 14.0
    val mapStyles = listOf(
        Style.MAPBOX_STREETS,
        Style.SATELLITE,
        Style.SATELLITE_STREETS,
        Style.OUTDOORS,
        Style.LIGHT,
        Style.DARK
    )
    val currentStyleIndex = remember { mutableStateOf(0) }
    val selectedBin = remember { mutableStateOf<Bin?>(null) }
    val routeCoordinates = remember { mutableStateOf<List<Point>?>(null) }

    // Create bitmaps for bin and truck icons
    val binIconBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bin_profile)
        val bitmap = (drawable as BitmapDrawable).bitmap
        Bitmap.createScaledBitmap(bitmap, 50, 50, true)
    }
    val truckIconBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.garbage_truck)
        val bitmap = (drawable as BitmapDrawable).bitmap
        Bitmap.createScaledBitmap(bitmap, 70, 70, true)
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapboxMap.loadStyleUri(mapStyles[currentStyleIndex.value])
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
                        selectedBin.value = it
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
                currentStyleIndex.value = (currentStyleIndex.value + 1) % mapStyles.size
                mapViewState.value?.mapboxMap?.loadStyleUri(mapStyles[currentStyleIndex.value])
            }) {
                Text("Change Style")
            }
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
                selectedBin.value = null
                routeCoordinates.value = null
                mapViewState.value?.let { mapView ->
                    mapView.annotations.cleanup()
                }
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
                val binToRoute = selectedBin.value ?: bins.minByOrNull { bin ->
                    TurfMeasurement.distance(
                        predefinedLocation,
                        Point.fromLngLat(bin.longitude, bin.latitude)
                    )
                }
                binToRoute?.let { bin ->
                    calculateRoute(
                        predefinedLocation,
                        Point.fromLngLat(bin.longitude, bin.latitude)
                    ) { coordinates ->
                        routeCoordinates.value = coordinates
                        mapViewState.value?.let { mapView ->
                            displayRoute(mapView, coordinates)
                        }
                        val distance = TurfMeasurement.distance(
                            predefinedLocation,
                            Point.fromLngLat(bin.longitude, bin.latitude)
                        )
                        distanceInfo.value = "Distance to ${bin.name}: ${distance.toInt()} meters"
                    }
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
private fun calculateRoute(start: Point, end: Point, onRouteReady: (List<Point>) -> Unit) {
    val client = MapboxDirections.builder()
        .origin(start)
        .destination(end)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .accessToken(MapboxAccessToken.VALUE)
        .build()

    client.enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
            val body = response.body()
            if (body == null || body.routes().isEmpty()) {
                // Handle error
                return
            }
            val route = body.routes()[0]
            val geometry = route.geometry()
            if (geometry != null) {
                val coordinates = LineString.fromPolyline(geometry, 6).coordinates()
                onRouteReady(coordinates)
            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            // Handle network errors
        }
    })
}
private fun displayRoute(mapView: MapView, coordinates: List<Point>) {
    mapView.getMapboxMap().getStyle { style ->
        val polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager()
        val polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(coordinates)
            .withLineColor("#3887be")
            .withLineWidth(5.0)
        polylineAnnotationManager.create(polylineAnnotationOptions)

        // Adjust camera to show the entire route
        val cameraPosition = mapView.getMapboxMap().cameraForCoordinates(
            coordinates,
            EdgeInsets(50.0, 50.0, 50.0, 50.0),
            null,
            null
        )

        mapView.getMapboxMap().flyTo(
            CameraOptions.Builder()
                .center(cameraPosition.center)
                .zoom(cameraPosition.zoom)
                .bearing(cameraPosition.bearing)
                .pitch(cameraPosition.pitch)
                .padding(cameraPosition.padding)
                .build()
        )
    }
}
fun List<Point>.boundingBox(): Pair<Point, Point> {
    if (isEmpty()) throw IllegalArgumentException("List of points is empty")

    var minLat = Double.MAX_VALUE
    var minLon = Double.MAX_VALUE
    var maxLat = -Double.MAX_VALUE
    var maxLon = -Double.MAX_VALUE

    forEach { point ->
        minLat = minOf(minLat, point.latitude())
        minLon = minOf(minLon, point.longitude())
        maxLat = maxOf(maxLat, point.latitude())
        maxLon = maxOf(maxLon, point.longitude())
    }

    return Pair(
        Point.fromLngLat(minLon, minLat),
        Point.fromLngLat(maxLon, maxLat)
    )
}