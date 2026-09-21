package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

/**
 * School Campus Points of Interest for St. Joseph's
 */
data class CampusLocation(
  val id: String,
  val name: String,
  val category: String,
  val description: String,
  val latitude: Double,
  val longitude: Double,
  val hue: Float = BitmapDescriptorFactory.HUE_RED
)

val DEFAULT_CAMPUS_LOCATIONS = listOf(
  CampusLocation(
    id = "main_block",
    name = "St. Joseph's Main Academic Block",
    category = "Academics",
    description = "Administrative offices, Principal's chamber, High School classrooms",
    latitude = 12.74632,
    longitude = 77.80728,
    hue = BitmapDescriptorFactory.HUE_AZURE
  ),
  CampusLocation(
    id = "science_block",
    name = "Science & Computer Labs",
    category = "Laboratories",
    description = "Physics, Chemistry, Biology and Advanced Robotics lab suites",
    latitude = 12.74675,
    longitude = 77.80790,
    hue = BitmapDescriptorFactory.HUE_BLUE
  ),
  CampusLocation(
    id = "sports_complex",
    name = "Sports Arena & Football Turf",
    category = "Athletics",
    description = "Full-size football turf, basketball courts, and indoor sports hall",
    latitude = 12.74580,
    longitude = 77.80810,
    hue = BitmapDescriptorFactory.HUE_GREEN
  ),
  CampusLocation(
    id = "bus_depot",
    name = "Campus Bus Fleet Bay & Gate 2",
    category = "Transport",
    description = "Designated boarding bay for all school buses (Routes #1 to #24)",
    latitude = 12.74540,
    longitude = 77.80660,
    hue = BitmapDescriptorFactory.HUE_ORANGE
  ),
  CampusLocation(
    id = "auditorium",
    name = "St. Joseph's Jubilee Auditorium",
    category = "Events",
    description = "1,500-capacity auditorium for morning assemblies, exams, and annual day",
    latitude = 12.74690,
    longitude = 77.80690,
    hue = BitmapDescriptorFactory.HUE_VIOLET
  )
)

/**
 * CampusMapView
 *
 * Implements the Google Maps SDK MapView in Jetpack Compose to display
 * the school campus locations, points of interest, live bus tracking routes,
 * and waypoints using the configured Google Maps API key in AndroidManifest.xml.
 */
@Composable
fun CampusMapView(
  route: BusRoute? = null,
  campusLocations: List<CampusLocation> = DEFAULT_CAMPUS_LOCATIONS,
  onLocationClick: ((CampusLocation) -> Unit)? = null,
  onStopClick: ((BusStop) -> Unit)? = null,
  showTopCampusBar: Boolean = true,
  showFloatingZoomControls: Boolean = true,
  showBottomLocationCard: Boolean = true,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle

  var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
  var selectedLocation by remember { mutableStateOf<CampusLocation?>(campusLocations.firstOrNull()) }
  var activeMapType by remember { mutableStateOf(GoogleMap.MAP_TYPE_NORMAL) }
  var isTrafficEnabled by remember { mutableStateOf(false) }

  val defaultSchoolLatLng = remember { LatLng(12.74632, 77.80728) }

  // MapView lifecycle management
  val mapView = remember {
    MapView(context).apply {
      onCreate(Bundle())
    }
  }

  DisposableEffect(lifecycle, mapView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
        else -> Unit
      }
    }
    lifecycle.addObserver(observer)
    onDispose {
      lifecycle.removeObserver(observer)
      mapView.onDestroy()
    }
  }

  // Update map annotations whenever map instance, route, or locations change
  LaunchedEffect(googleMapInstance, route, campusLocations, activeMapType, isTrafficEnabled) {
    val map = googleMapInstance ?: return@LaunchedEffect
    map.clear()
    map.mapType = activeMapType
    map.isTrafficEnabled = isTrafficEnabled
    map.uiSettings.isZoomControlsEnabled = false
    map.uiSettings.isCompassEnabled = true
    map.uiSettings.isMyLocationButtonEnabled = false

    val boundsBuilder = LatLngBounds.Builder()

    if (route != null && !showTopCampusBar) {
      // In dedicated bus tracking mode, focus camera exclusively on the bus, stops, and school
      val busLatLng = LatLng(route.currentLatitude, route.currentLongitude)
      val schoolLatLng = LatLng(route.schoolLatitude, route.schoolLongitude)
      boundsBuilder.include(busLatLng)
      boundsBuilder.include(schoolLatLng)

      // Live Bus Marker
      map.addMarker(
        MarkerOptions()
          .position(busLatLng)
          .title("School Bus ${route.routeNumber}")
          .snippet("${route.studentsOnboard} students onboard • ${route.currentSpeedKmH} km/h")
          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
      )

      // Stops markers and polyline
      val polylineOpts = PolylineOptions()
        .color(android.graphics.Color.parseColor("#0F3875"))
        .width(12f)
        .geodesic(true)

      polylineOpts.add(busLatLng)

      route.stops.forEach { stop ->
        val stopLatLng = LatLng(stop.latitude, stop.longitude)
        boundsBuilder.include(stopLatLng)
        polylineOpts.add(stopLatLng)

        val hue = if (stop.isCompleted) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_CYAN
        map.addMarker(
          MarkerOptions()
            .position(stopLatLng)
            .title(stop.name)
            .snippet("Scheduled: ${stop.scheduledTime} • ${stop.studentCount} Students")
            .icon(BitmapDescriptorFactory.defaultMarker(hue))
        )?.tag = stop
      }

      polylineOpts.add(schoolLatLng)
      map.addPolyline(polylineOpts)
    } else {
      // In Campus POI mode or general view, plot campus buildings
      campusLocations.forEach { loc ->
        val latLng = LatLng(loc.latitude, loc.longitude)
        boundsBuilder.include(latLng)
        val marker = map.addMarker(
          MarkerOptions()
            .position(latLng)
            .title(loc.name)
            .snippet(loc.description)
            .icon(BitmapDescriptorFactory.defaultMarker(loc.hue))
        )
        marker?.tag = loc
      }

      if (route != null) {
        val busLatLng = LatLng(route.currentLatitude, route.currentLongitude)
        val schoolLatLng = LatLng(route.schoolLatitude, route.schoolLongitude)
        boundsBuilder.include(busLatLng)
        boundsBuilder.include(schoolLatLng)

        map.addMarker(
          MarkerOptions()
            .position(busLatLng)
            .title("School Bus ${route.routeNumber}")
            .snippet("${route.studentsOnboard} students onboard • ${route.currentSpeedKmH} km/h")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        )
      }
    }

    // Move camera smoothly
    try {
      val bounds = boundsBuilder.build()
      map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
    } catch (e: Exception) {
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultSchoolLatLng, 16.5f))
    }

    // Marker click listener
    map.setOnMarkerClickListener { marker ->
      when (val tag = marker.tag) {
        is CampusLocation -> {
          selectedLocation = tag
          onLocationClick?.invoke(tag)
        }
        is BusStop -> {
          onStopClick?.invoke(tag)
        }
      }
      false
    }
  }

  Box(modifier = modifier.fillMaxSize().testTag("campus_map_view_container")) {
    // 1. Real Google Maps View
    AndroidView(
      factory = {
        mapView.apply {
          getMapAsync { map ->
            googleMapInstance = map
            map.moveCamera(
              CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                  .target(defaultSchoolLatLng)
                  .zoom(16.5f)
                  .bearing(25f)
                  .tilt(30f)
                  .build()
              )
            )
          }
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // 2. Top Campus Filter Bar & Controls
    if (showTopCampusBar) {
      Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Map Type Selector Chips
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White.copy(alpha = 0.94f),
          shadowElevation = 4.dp,
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              FilterChip(
                selected = activeMapType == GoogleMap.MAP_TYPE_NORMAL,
                onClick = { activeMapType = GoogleMap.MAP_TYPE_NORMAL },
                label = { Text("Standard", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.height(32.dp)
              )
              FilterChip(
                selected = activeMapType == GoogleMap.MAP_TYPE_SATELLITE,
                onClick = { activeMapType = GoogleMap.MAP_TYPE_SATELLITE },
                label = { Text("Satellite", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                leadingIcon = { Icon(Icons.Default.SatelliteAlt, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.height(32.dp)
              )
              FilterChip(
                selected = activeMapType == GoogleMap.MAP_TYPE_HYBRID,
                onClick = { activeMapType = GoogleMap.MAP_TYPE_HYBRID },
                label = { Text("Hybrid", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.height(32.dp)
              )
            }

            IconButton(
              onClick = { isTrafficEnabled = !isTrafficEnabled },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Traffic,
                contentDescription = "Toggle Traffic",
                tint = if (isTrafficEnabled) Color(0xFF10B981) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        // Horizontal list of quick Campus Locations
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(campusLocations) { loc ->
            val isSelected = loc.id == selectedLocation?.id
            Surface(
              onClick = {
                selectedLocation = loc
                googleMapInstance?.animateCamera(
                  CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 18f)
                )
                onLocationClick?.invoke(loc)
              },
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) SchoolNavyPrimary else Color.White.copy(alpha = 0.95f),
              shadowElevation = if (isSelected) 4.dp else 2.dp,
              border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = when (loc.category) {
                    "Academics" -> Icons.Default.School
                    "Laboratories" -> Icons.Default.Science
                    "Athletics" -> Icons.Default.SportsSoccer
                    "Transport" -> Icons.Default.DirectionsBus
                    else -> Icons.Default.Place
                  },
                  contentDescription = null,
                  tint = if (isSelected) SchoolGold else SchoolNavyPrimary,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = loc.name,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                  ),
                  color = if (isSelected) Color.White else SchoolNavyDark,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    // 3. Floating Right-Side Controls (Recenter Campus, Zoom In/Out, Google Maps External App)
    if (showFloatingZoomControls) {
      Column(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FloatingActionButton(
          onClick = {
            googleMapInstance?.animateCamera(
              CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                  .target(defaultSchoolLatLng)
                  .zoom(17f)
                  .tilt(35f)
                  .bearing(30f)
                  .build()
              )
            )
          },
          containerColor = Color.White,
          contentColor = SchoolNavyPrimary,
          shape = CircleShape,
          modifier = Modifier.size(44.dp).testTag("campus_map_recenter_btn")
        ) {
          Icon(Icons.Default.School, contentDescription = "Focus Campus", modifier = Modifier.size(20.dp))
        }

        FloatingActionButton(
          onClick = {
            googleMapInstance?.animateCamera(CameraUpdateFactory.zoomIn())
          },
          containerColor = Color.White,
          contentColor = SchoolNavyPrimary,
          shape = CircleShape,
          modifier = Modifier.size(44.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
        }

        FloatingActionButton(
          onClick = {
            googleMapInstance?.animateCamera(CameraUpdateFactory.zoomOut())
          },
          containerColor = Color.White,
          contentColor = SchoolNavyPrimary,
          shape = CircleShape,
          modifier = Modifier.size(44.dp)
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
        }

        FloatingActionButton(
          onClick = {
            val uri = Uri.parse("geo:12.74632,77.80728?q=12.74632,77.80728(St.+Joseph+Matriculation+School+Hosur)")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
              setPackage("com.google.android.apps.maps")
            }
            try {
              context.startActivity(intent)
            } catch (e: Exception) {
              val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=12.74632,77.80728")
              context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
          },
          containerColor = SchoolNavyPrimary,
          contentColor = Color.White,
          shape = CircleShape,
          modifier = Modifier.size(44.dp).testTag("campus_map_open_external_btn")
        ) {
          Icon(Icons.Default.Directions, contentDescription = "Navigate to Campus", modifier = Modifier.size(20.dp))
        }
      }
    }

    // 4. Bottom Selected Location Details Card
    if (showBottomLocationCard) {
      selectedLocation?.let { loc ->
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          shadowElevation = 8.dp,
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("campus_location_details_card")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SchoolNavyPrimary.copy(alpha = 0.1f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = SchoolNavyPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Column {
                  Text(
                    text = loc.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SchoolNavyDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = loc.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = SchoolGold
                    )
                  )
                }
              }

              IconButton(
                onClick = {
                  val uri = Uri.parse("geo:${loc.latitude},${loc.longitude}?q=${loc.latitude},${loc.longitude}(${Uri.encode(loc.name)})")
                  context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Directions,
                  contentDescription = "Navigate",
                  tint = SchoolNavyPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            Text(
              text = loc.description,
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}
