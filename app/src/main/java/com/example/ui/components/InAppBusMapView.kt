package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

enum class MapLayerType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  STREET("Street", Icons.Default.Map),
  SATELLITE("Satellite", Icons.Default.SatelliteAlt),
  DARK("Dark Nav", Icons.Default.DarkMode)
}

/**
 * In-App Interactive Google Maps & Route Tracking Component.
 * Embeds a real-time interactive mapping engine inside the app with live bus tracking,
 * waypoints, school destination crest, layer toggles, and seamless Google Maps app integration.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBusMapView(
  route: BusRoute,
  onStopClick: (BusStop) -> Unit,
  onOpenGoogleMapsApp: () -> Unit,
  onShareLiveLocation: () -> Unit,
  modifier: Modifier = Modifier,
  isFollowBusEnabled: Boolean = true,
  onToggleFollowBus: (Boolean) -> Unit = {}
) {
  val context = LocalContext.current
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var selectedLayer by remember { mutableStateOf(MapLayerType.STREET) }
  var isMapReady by remember { mutableStateOf(false) }
  var selectedStopForPopup by remember { mutableStateOf<BusStop?>(null) }

  val stopsJson = remember(route.stops) {
    route.stops.joinToString(prefix = "[", postfix = "]") { stop ->
      """
      {
        "id": "${stop.id}",
        "name": "${stop.name.replace("\"", "\\\"")}",
        "time": "${stop.scheduledTime}",
        "isCompleted": ${stop.isCompleted},
        "isCurrent": ${stop.isCurrent},
        "students": ${stop.studentCount},
        "lat": ${stop.latitude},
        "lng": ${stop.longitude}
      }
      """.trimIndent()
    }
  }

  // Generate self-contained Leaflet / OpenStreetMap / Google Map cartography HTML
  val mapHtml = remember(route.id) {
    buildMapHtml(
      routeNumber = route.routeNumber,
      busReg = route.busRegistration,
      driver = route.driverName,
      schoolLat = route.schoolLatitude,
      schoolLng = route.schoolLongitude,
      stopsJson = stopsJson,
      initLat = route.currentLatitude,
      initLng = route.currentLongitude,
      heading = route.currentHeadingDegrees,
      speed = route.currentSpeedKmH
    )
  }

  // Update live bus marker position smoothly when GPS coordinates change
  LaunchedEffect(route.currentLatitude, route.currentLongitude, route.currentHeadingDegrees, route.currentSpeedKmH, isMapReady) {
    if (isMapReady && webViewRef != null) {
      val jsCode = """
        if (window.updateBus) {
          window.updateBus(${route.currentLatitude}, ${route.currentLongitude}, ${route.currentHeadingDegrees}, ${route.currentSpeedKmH}, '${route.currentLocationName.replace("'", "\\'")}');
        }
      """.trimIndent()
      webViewRef?.evaluateJavascript(jsCode, null)
    }
  }

  // Update layer when layer toggle changes
  LaunchedEffect(selectedLayer, isMapReady) {
    if (isMapReady && webViewRef != null) {
      val layerKey = when (selectedLayer) {
        MapLayerType.STREET -> "street"
        MapLayerType.SATELLITE -> "satellite"
        MapLayerType.DARK -> "dark"
      }
      webViewRef?.evaluateJavascript("if (window.setMapLayer) { window.setMapLayer('$layerKey'); }", null)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
      .testTag("in_app_bus_map_container")
  ) {
    // 1. Interactive Android WebView Map View
    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
          }
          webChromeClient = WebChromeClient()
          webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              isMapReady = true
            }
          }
          addJavascriptInterface(
            WebAppInterface { stopId ->
              val foundStop = route.stops.find { it.id == stopId }
              if (foundStop != null) {
                selectedStopForPopup = foundStop
                onStopClick(foundStop)
              }
            },
            "AndroidApp"
          )
          loadDataWithBaseURL("https://maps.stjosephs.edu", mapHtml, "text/html", "UTF-8", null)
          webViewRef = this
        }
      },
      update = { view ->
        webViewRef = view
      },
      modifier = Modifier.fillMaxSize().testTag("leaflet_webview_map")
    )

    // 2. Top-Left Map Layer & Telemetry Pills
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Layer Mode Pill
      Surface(
        color = SchoolNavyDark.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          MapLayerType.entries.forEach { layer ->
            val isSelected = selectedLayer == layer
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) SchoolGold else Color.Transparent)
                .clickable { selectedLayer = layer }
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = layer.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                  fontSize = 10.sp
                ),
                color = if (isSelected) SchoolNavyDark else Color.White
              )
            }
          }
        }
      }
    }

    // 3. Top-Right Google Maps Actions
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Google Maps App Button
      FilledTonalButton(
        onClick = onOpenGoogleMapsApp,
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = Color(0xFF1E88E5),
          contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(32.dp).testTag("open_gmaps_app_pill")
      ) {
        Icon(Icons.Default.Directions, contentDescription = "Open Google Maps", modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Google Maps",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
        )
      }

      // Share GPS Location
      Surface(
        color = SchoolNavyDark.copy(alpha = 0.88f),
        shape = CircleShape,
        shadowElevation = 4.dp,
        modifier = Modifier.size(32.dp).clickable { onShareLiveLocation() }
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share Location",
            tint = SchoolGold,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // 4. Bottom Map Controls (Zoom +, Zoom -, Recenter, Center Campus)
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Follow Bus Toggle
      FloatingMapControl(
        icon = if (isFollowBusEnabled) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
        tint = if (isFollowBusEnabled) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
        bgColor = if (isFollowBusEnabled) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface,
        contentDesc = "Follow Bus",
        onClick = {
          onToggleFollowBus(!isFollowBusEnabled)
          webViewRef?.evaluateJavascript("if (window.centerOnBus) { window.centerOnBus(); }", null)
        }
      )

      // Center on Campus
      FloatingMapControl(
        icon = Icons.Default.School,
        tint = MaterialTheme.colorScheme.primary,
        bgColor = MaterialTheme.colorScheme.surface,
        contentDesc = "Center on Campus",
        onClick = {
          webViewRef?.evaluateJavascript("if (window.centerOnCampus) { window.centerOnCampus(); }", null)
        }
      )

      // Fit All Stops Bounds
      FloatingMapControl(
        icon = Icons.Default.ZoomOutMap,
        tint = MaterialTheme.colorScheme.primary,
        bgColor = MaterialTheme.colorScheme.surface,
        contentDesc = "Fit Route",
        onClick = {
          webViewRef?.evaluateJavascript("if (window.fitRouteBounds) { window.fitRouteBounds(); }", null)
        }
      )

      // Zoom In
      FloatingMapControl(
        icon = Icons.Default.Add,
        tint = SchoolNavyDark,
        contentDesc = "Zoom In",
        onClick = {
          webViewRef?.evaluateJavascript("if (window.map) { window.map.zoomIn(); }", null)
        }
      )

      // Zoom Out
      FloatingMapControl(
        icon = Icons.Default.Remove,
        tint = SchoolNavyDark,
        contentDesc = "Zoom Out",
        onClick = {
          webViewRef?.evaluateJavascript("if (window.map) { window.map.zoomOut(); }", null)
        }
      )
    }

    // 5. Bottom Floating GPS Telemetry Badge
    Surface(
      color = SchoolNavyDark.copy(alpha = 0.92f),
      shape = RoundedCornerShape(12.dp),
      shadowElevation = 6.dp,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF22C55E))
        )
        Column {
          Text(
            text = "GPS LIVE: ${String.format("%.4f", route.currentLatitude)}, ${String.format("%.4f", route.currentLongitude)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              letterSpacing = 0.5.sp
            ),
            color = Color.White
          )
          Text(
            text = "Next: ${route.nextStopName} • ${route.estimatedArrivalMins}m ETA",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = SchoolGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    // 6. Selected Stop Popup Dialog
    if (selectedStopForPopup != null) {
      val stop = selectedStopForPopup!!
      AlertDialog(
        onDismissRequest = { selectedStopForPopup = null },
        icon = {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (stop.isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
          )
        },
        title = {
          Text(
            text = stop.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Scheduled Time:", style = MaterialTheme.typography.bodyMedium)
              Text(
                text = stop.scheduledTime,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Boarding Students:", style = MaterialTheme.typography.bodyMedium)
              Text(
                text = "${stop.studentCount} students",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Stop Status:", style = MaterialTheme.typography.bodyMedium)
              Text(
                text = when {
                  stop.isCompleted -> "Passed / Completed"
                  stop.isCurrent -> "Approaching Now"
                  else -> "Upcoming Stop"
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (stop.isCompleted) Color(0xFF059669) else Color(0xFFD97706)
              )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
              text = "Coordinates: ${String.format("%.4f", stop.latitude)}, ${String.format("%.4f", stop.longitude)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              selectedStopForPopup = null
              launchGoogleMapsLocation(context, stop.latitude, stop.longitude, stop.name)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open in Google Maps")
          }
        },
        dismissButton = {
          TextButton(onClick = { selectedStopForPopup = null }) {
            Text("Close")
          }
        }
      )
    }
  }
}

@Composable
private fun FloatingMapControl(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  contentDesc: String,
  onClick: () -> Unit,
  bgColor: Color = Color.White
) {
  Surface(
    color = bgColor,
    shape = CircleShape,
    shadowElevation = 4.dp,
    modifier = Modifier
      .size(36.dp)
      .clickable { onClick() }
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDesc,
        tint = tint,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

class WebAppInterface(private val onStopClicked: (String) -> Unit) {
  @JavascriptInterface
  fun onStopClick(stopId: String) {
    onStopClicked(stopId)
  }
}

/**
 * Launch native Google Maps app intent with fallback to browser
 */
fun launchGoogleMapsNavigation(
  context: Context,
  originLat: Double,
  originLng: Double,
  destLat: Double,
  destLng: Double,
  waypoints: List<BusStop> = emptyList()
) {
  val waypointsParam = if (waypoints.isNotEmpty()) {
    "&waypoints=" + waypoints.joinToString("%7C") { "${it.latitude},${it.longitude}" }
  } else ""

  val webUrl = "https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng$waypointsParam&travelmode=driving"
  
  val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
  val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
    setPackage("com.google.android.apps.maps")
  }

  try {
    if (mapIntent.resolveActivity(context.packageManager) != null) {
      context.startActivity(mapIntent)
    } else {
      val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
      context.startActivity(browserIntent)
    }
  } catch (e: Exception) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
    context.startActivity(browserIntent)
  }
}

/**
 * Launch a single location in Google Maps
 */
fun launchGoogleMapsLocation(context: Context, lat: Double, lng: Double, label: String) {
  val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
  val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
    setPackage("com.google.android.apps.maps")
  }
  try {
    if (mapIntent.resolveActivity(context.packageManager) != null) {
      context.startActivity(mapIntent)
    } else {
      val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
      context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
  } catch (e: Exception) {
    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
  }
}

/**
 * Share live bus location with parents / school community
 */
fun shareBusLiveLocation(context: Context, route: BusRoute) {
  val mapsLink = "https://maps.google.com/?q=${route.currentLatitude},${route.currentLongitude}"
  val message = """
    📍 Live GPS Tracker - St. Joseph's Academy
    🚌 Bus: ${route.routeNumber} (${route.busRegistration})
    🗺️ Route: ${route.routeName}
    📍 Current Location: ${route.currentLocationName}
    ⚡ Speed: ${route.currentSpeedKmH} km/h
    ⏱️ Next Stop: ${route.nextStopName} (ETA: ${route.estimatedArrivalMins} mins)
    
    🌐 Track Live on Google Maps:
    $mapsLink
    
    📞 Driver: ${route.driverName} (${route.driverPhone})
  """.trimIndent()

  val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, message)
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "Share Bus GPS Location")
  context.startActivity(shareIntent)
}

/**
 * Self-contained HTML with Leaflet, CartoDB/OSM tiles & custom styled GPS bus animations.
 */
private fun buildMapHtml(
  routeNumber: String,
  busReg: String,
  driver: String,
  schoolLat: Double,
  schoolLng: Double,
  stopsJson: String,
  initLat: Double,
  initLng: Double,
  heading: Float,
  speed: Int
): String {
  return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    html, body, #map {
      height: 100%;
      width: 100%;
      margin: 0;
      padding: 0;
      background: #f1f5f9;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }
    .leaflet-control-attribution {
      font-size: 8px !important;
      background: rgba(255,255,255,0.7) !important;
    }
    .bus-marker-container {
      position: relative;
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
    }
    .bus-pulse-ring {
      position: absolute;
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: rgba(234, 88, 12, 0.28);
      animation: pulse 1.8s infinite ease-out;
    }
    .bus-icon-circle {
      position: relative;
      width: 34px;
      height: 34px;
      border-radius: 50%;
      background: #0F3875;
      border: 2.5px solid #F59E0B;
      box-shadow: 0 4px 10px rgba(0,0,0,0.35);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #FFFFFF;
      font-size: 16px;
    }
    .stop-marker-badge {
      background: #FFFFFF;
      border: 2px solid #0F3875;
      color: #0F3875;
      border-radius: 50%;
      width: 24px;
      height: 24px;
      font-weight: 800;
      font-size: 11px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(0,0,0,0.25);
      cursor: pointer;
    }
    .stop-marker-badge.completed {
      background: #059669;
      border-color: #059669;
      color: #FFFFFF;
    }
    .stop-marker-badge.current {
      background: #EA580C;
      border-color: #FFFFFF;
      color: #FFFFFF;
      box-shadow: 0 0 0 3px #EA580C, 0 3px 8px rgba(0,0,0,0.4);
    }
    .campus-marker-badge {
      background: #F59E0B;
      border: 2.5px solid #0F3875;
      color: #0F3875;
      border-radius: 50%;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(0,0,0,0.35);
      font-size: 16px;
    }
    @keyframes pulse {
      0% { transform: scale(0.6); opacity: 0.9; }
      100% { transform: scale(1.6); opacity: 0; }
    }
  </style>
</head>
<body>
  <div id="map"></div>

  <script>
    var stops = $stopsJson;
    var initLat = $initLat;
    var initLng = $initLng;
    var schoolLat = $schoolLat;
    var schoolLng = $schoolLng;

    // Tile layers (CartoDB / OpenStreetMap)
    var streetLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; CARTO &copy; OSM'
    });

    var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
      maxZoom: 19,
      attribution: '&copy; Esri &copy; USGS'
    });

    var darkLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; CARTO &copy; OSM'
    });

    var map = L.map('map', {
      center: [initLat, initLng],
      zoom: 13,
      zoomControl: false,
      layers: [streetLayer]
    });

    window.map = map;

    // Polyline Coordinates
    var routeCoords = stops.map(function(s) { return [s.lat, s.lng]; });
    var polyline = L.polyline(routeCoords, {
      color: '#0F3875',
      weight: 5,
      opacity: 0.85,
      dashArray: '8, 8',
      lineCap: 'round'
    }).addTo(map);

    // Stop Markers
    var stopMarkers = [];
    stops.forEach(function(s, idx) {
      var isLast = (idx === stops.length - 1);
      var className = 'stop-marker-badge';
      if (s.isCompleted) className += ' completed';
      if (s.isCurrent) className += ' current';

      var markerIcon;
      if (isLast) {
        markerIcon = L.divIcon({
          className: 'custom-div-icon',
          html: '<div class="campus-marker-badge">🏫</div>',
          iconSize: [32, 32],
          iconAnchor: [16, 16]
        });
      } else {
        markerIcon = L.divIcon({
          className: 'custom-div-icon',
          html: '<div class="' + className + '">' + (idx + 1) + '</div>',
          iconSize: [24, 24],
          iconAnchor: [12, 12]
        });
      }

      var marker = L.marker([s.lat, s.lng], { icon: markerIcon }).addTo(map);
      marker.on('click', function() {
        if (window.AndroidApp && window.AndroidApp.onStopClick) {
          window.AndroidApp.onStopClick(s.id);
        }
      });
      stopMarkers.push(marker);
    });

    // Live Bus Marker
    var busIcon = L.divIcon({
      className: 'custom-bus-icon',
      html: '<div class="bus-marker-container"><div class="bus-pulse-ring"></div><div class="bus-icon-circle">🚌</div></div>',
      iconSize: [48, 48],
      iconAnchor: [24, 24]
    });

    var busMarker = L.marker([initLat, initLng], { icon: busIcon, zIndexOffset: 1000 }).addTo(map);

    // Methods for Android Interaction
    window.updateBus = function(lat, lng, heading, speed, locName) {
      if (busMarker) {
        busMarker.setLatLng([lat, lng]);
      }
    };

    window.centerOnBus = function() {
      if (busMarker) {
        map.flyTo(busMarker.getLatLng(), 15, { animate: true, duration: 1.0 });
      }
    };

    window.centerOnCampus = function() {
      map.flyTo([schoolLat, schoolLng], 15, { animate: true, duration: 1.0 });
    };

    window.fitRouteBounds = function() {
      if (polyline) {
        map.fitBounds(polyline.getBounds(), { padding: [30, 30] });
      }
    };

    window.setMapLayer = function(layerKey) {
      map.removeLayer(streetLayer);
      map.removeLayer(satelliteLayer);
      map.removeLayer(darkLayer);
      if (layerKey === 'satellite') {
        map.addLayer(satelliteLayer);
      } else if (layerKey === 'dark') {
        map.addLayer(darkLayer);
      } else {
        map.addLayer(streetLayer);
      }
    };

    // Auto-fit initial bounds
    setTimeout(function() {
      map.fitBounds(polyline.getBounds(), { padding: [30, 30] });
    }, 400);
  </script>
</body>
</html>
  """.trimIndent()
}
