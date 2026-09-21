package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * OpenStreetMapMapView
 *
 * Real, 100% Free OpenStreetMap rendering engine powered by osmdroid.
 * Requires ZERO Google Maps API keys and ZERO billing/credit cards!
 * Provides crisp vector/raster tile streaming from OpenStreetMap Foundation.
 */
@Composable
fun OpenStreetMapMapView(
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

  var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
  var selectedLocation by remember { mutableStateOf<CampusLocation?>(campusLocations.firstOrNull()) }
  var activeTileSource by remember { mutableStateOf(TileSourceFactory.MAPNIK) }

  val defaultSchoolGeoPoint = remember { GeoPoint(12.74632, 77.80728) }

  // MapView instance lifecycle management
  val mapView = remember {
    MapView(context).apply {
      setTileSource(TileSourceFactory.MAPNIK)
      zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
      setMultiTouchControls(true)
      isTilesScaledToDpi = true
      controller.setZoom(17.0)
      controller.setCenter(defaultSchoolGeoPoint)
    }
  }

  DisposableEffect(lifecycle, mapView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
        else -> Unit
      }
    }
    lifecycle.addObserver(observer)
    onDispose {
      lifecycle.removeObserver(observer)
      mapView.onDetach()
    }
  }

  // Update Overlays whenever route, locations or map instance change
  LaunchedEffect(mapViewInstance, route, campusLocations, activeTileSource, showTopCampusBar) {
    val map = mapViewInstance ?: return@LaunchedEffect
    map.setTileSource(activeTileSource)
    map.overlays.clear()

    val geoPoints = mutableListOf<GeoPoint>()

    if (route != null && !showTopCampusBar) {
      // DEDICATED BUS TRACKING MODE
      val busGeoPoint = GeoPoint(route.currentLatitude, route.currentLongitude)
      val schoolGeoPoint = GeoPoint(route.schoolLatitude, route.schoolLongitude)
      geoPoints.add(busGeoPoint)
      geoPoints.add(schoolGeoPoint)

      // Polyline for bus route path
      val polyline = Polyline().apply {
        outlinePaint.color = android.graphics.Color.parseColor("#0F3875")
        outlinePaint.strokeWidth = 14f
        outlinePaint.strokeCap = Paint.Cap.ROUND
        outlinePaint.strokeJoin = Paint.Join.ROUND
        addPoint(busGeoPoint)
      }

      // Bus Stops
      route.stops.forEach { stop ->
        val stopGeo = GeoPoint(stop.latitude, stop.longitude)
        geoPoints.add(stopGeo)
        polyline.addPoint(stopGeo)

        val stopMarker = Marker(map).apply {
          position = stopGeo
          title = stop.name
          snippet = "Scheduled: ${stop.scheduledTime} • ${stop.studentCount} Students"
          setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
          setOnMarkerClickListener { _, _ ->
            onStopClick?.invoke(stop)
            true
          }
        }
        map.overlays.add(stopMarker)
      }

      polyline.addPoint(schoolGeoPoint)
      map.overlays.add(polyline)

      // School Destination Marker
      val schoolMarker = Marker(map).apply {
        position = schoolGeoPoint
        title = "St. Joseph's Matriculation School"
        snippet = "Campus Destination"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
      }
      map.overlays.add(schoolMarker)

      // Live Bus Marker
      val busMarker = Marker(map).apply {
        position = busGeoPoint
        title = "School Bus ${route.routeNumber}"
        snippet = "${route.studentsOnboard} students onboard • ${route.currentSpeedKmH} km/h"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
      }
      map.overlays.add(busMarker)

      // Center around bus and destination
      try {
        map.controller.animateTo(busGeoPoint)
        map.controller.setZoom(16.2)
      } catch (_: Exception) {}

    } else {
      // CAMPUS GUIDE & POI MODE
      campusLocations.forEach { loc ->
        val locGeo = GeoPoint(loc.latitude, loc.longitude)
        geoPoints.add(locGeo)

        val poiMarker = Marker(map).apply {
          position = locGeo
          title = loc.name
          snippet = loc.description
          setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
          setOnMarkerClickListener { _, _ ->
            selectedLocation = loc
            onLocationClick?.invoke(loc)
            map.controller.animateTo(locGeo)
            true
          }
        }
        map.overlays.add(poiMarker)
      }

      if (route != null) {
        val busGeo = GeoPoint(route.currentLatitude, route.currentLongitude)
        val busMarker = Marker(map).apply {
          position = busGeo
          title = "School Bus ${route.routeNumber}"
          snippet = "${route.studentsOnboard} students onboard • ${route.currentSpeedKmH} km/h"
          setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
        map.overlays.add(busMarker)
      }

      map.controller.setCenter(defaultSchoolGeoPoint)
      map.controller.setZoom(17.5)
    }

    map.invalidate()
  }

  Box(modifier = modifier.fillMaxSize().testTag("osmdroid_map_view_container")) {
    // 1. OSMDROID Real Native Map View
    AndroidView(
      factory = {
        mapView.also {
          mapViewInstance = it
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // OSM Brand Attribution Watermark Chip (OpenStreetMap is 100% Free & Open)
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color.White.copy(alpha = 0.88f),
      border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = if (showBottomLocationCard && selectedLocation != null) 120.dp else 16.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Public,
          contentDescription = null,
          tint = SchoolNavyPrimary,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = "© OpenStreetMap • No API Key Needed",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
          color = SchoolNavyDark
        )
      }
    }

    // 2. Top Campus Filter Bar & Controls
    if (showTopCampusBar) {
      Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Map Tile Layer Selector Chips
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
                selected = activeTileSource == TileSourceFactory.MAPNIK,
                onClick = { activeTileSource = TileSourceFactory.MAPNIK },
                label = { Text("Standard OSM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.height(32.dp)
              )
              FilterChip(
                selected = activeTileSource == TileSourceFactory.OpenTopo,
                onClick = { activeTileSource = TileSourceFactory.OpenTopo },
                label = { Text("Topography", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                leadingIcon = { Icon(Icons.Default.Terrain, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.height(32.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF10B981).copy(alpha = 0.12f),
              border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                )
                Text(
                  text = "Free / Active",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                  color = Color(0xFF047857)
                )
              }
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
                mapViewInstance?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                mapViewInstance?.controller?.setZoom(18.0)
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
            mapViewInstance?.controller?.animateTo(defaultSchoolGeoPoint)
            mapViewInstance?.controller?.setZoom(17.5)
          },
          containerColor = Color.White,
          contentColor = SchoolNavyPrimary,
          shape = CircleShape,
          modifier = Modifier.size(44.dp).testTag("osm_campus_recenter_btn")
        ) {
          Icon(Icons.Default.School, contentDescription = "Focus Campus", modifier = Modifier.size(20.dp))
        }

        FloatingActionButton(
          onClick = {
            mapViewInstance?.controller?.zoomIn()
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
            mapViewInstance?.controller?.zoomOut()
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
          modifier = Modifier.size(44.dp).testTag("osm_campus_open_external_btn")
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
            .testTag("osm_campus_location_details_card")
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
