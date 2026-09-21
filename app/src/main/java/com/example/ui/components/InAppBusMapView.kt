package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary
import kotlin.math.roundToInt

enum class MapLayerType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  STREET("Google Maps", Icons.Default.Map),
  SATELLITE("Satellite", Icons.Default.SatelliteAlt),
  DARK("Dark Nav", Icons.Default.DarkMode)
}

/**
 * 100% Native Jetpack Compose Interactive Bus Tracking Map.
 * Renders rich Google Maps style vector cartography, arterial highways, residential blocks,
 * green botanical parks, canal waterway, St. Joseph School campus zone, glowing animated route polyline,
 * interactive waypoint stops, and real-time top-down school bus with rotating chassis & radar pulse.
 * Eliminates WebView failures and ensures instant, robust 60 FPS rendering in all Android environments.
 */
@Composable
fun InAppBusMapView(
  route: BusRoute,
  onStopClick: (BusStop) -> Unit,
  onOpenGoogleMapsApp: () -> Unit,
  onShareLiveLocation: () -> Unit,
  modifier: Modifier = Modifier,
  isFollowBusEnabled: Boolean = true,
  onToggleFollowBus: (Boolean) -> Unit = {},
  minimalMode: Boolean = false,
  userStopName: String? = null
) {
  val context = LocalContext.current
  var selectedLayer by remember { mutableStateOf(MapLayerType.STREET) }
  var selectedStopForPopup by remember { mutableStateOf<BusStop?>(null) }

  // Map viewport transform state
  var scale by remember { mutableFloatStateOf(1.0f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }
  var hasInitializedCenter by remember { mutableStateOf(false) }

  // Animated polyline dashed effect
  val infiniteTransition = rememberInfiniteTransition(label = "map_infinite")
  val dashPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -32f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "dash_phase"
  )

  // Animated Radar Pulse on Live School Bus
  val radarPulseRadius by infiniteTransition.animateFloat(
    initialValue = 16f,
    targetValue = 64f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_radius"
  )
  val radarPulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_alpha"
  )

  // Smooth bus GPS coordinate interpolation
  val animatedBusLat by animateFloatAsState(
    targetValue = route.currentLatitude.toFloat(),
    animationSpec = tween(900, easing = LinearOutSlowInEasing),
    label = "bus_lat"
  )
  val animatedBusLng by animateFloatAsState(
    targetValue = route.currentLongitude.toFloat(),
    animationSpec = tween(900, easing = LinearOutSlowInEasing),
    label = "bus_lng"
  )
  val animatedBusHeading by animateFloatAsState(
    targetValue = route.currentHeadingDegrees,
    animationSpec = tween(600, easing = FastOutSlowInEasing),
    label = "bus_heading"
  )

  // Coordinate Projection Calculations
  val allLats = remember(route) {
    listOf(route.schoolLatitude, route.currentLatitude) + route.stops.map { it.latitude }
  }
  val allLngs = remember(route) {
    listOf(route.schoolLongitude, route.currentLongitude) + route.stops.map { it.longitude }
  }
  val minLat = remember(allLats) { allLats.minOrNull() ?: 12.74 }
  val maxLat = remember(allLats) { allLats.maxOrNull() ?: 12.75 }
  val minLng = remember(allLngs) { allLngs.minOrNull() ?: 77.80 }
  val maxLng = remember(allLngs) { allLngs.maxOrNull() ?: 77.83 }

  val latSpan = remember(minLat, maxLat) { (maxLat - minLat).coerceAtLeast(0.005) }
  val lngSpan = remember(minLng, maxLng) { (maxLng - minLng).coerceAtLeast(0.005) }
  val pMinLat = remember(minLat, latSpan) { minLat - latSpan * 0.22 }
  val pMaxLat = remember(maxLat, latSpan) { maxLat + latSpan * 0.22 }
  val pMinLng = remember(minLng, lngSpan) { minLng - lngSpan * 0.22 }
  val pMaxLng = remember(maxLng, lngSpan) { maxLng + lngSpan * 0.22 }

  fun project(lat: Double, lng: Double): Offset {
    val x = ((lng - pMinLng) / (pMaxLng - pMinLng) * 1100.0 + 150.0).toFloat()
    val y = ((pMaxLat - lat) / (pMaxLat - pMinLat) * 1100.0 + 150.0).toFloat()
    return Offset(x, y)
  }

  val busWorldPt = project(animatedBusLat.toDouble(), animatedBusLng.toDouble())
  val schoolWorldPt = project(route.schoolLatitude, route.schoolLongitude)
  val stopWorldPts = remember(route.stops, pMinLat, pMaxLat, pMinLng, pMaxLng) {
    route.stops.map { it to project(it.latitude, it.longitude) }
  }

  BoxWithConstraints(
    modifier = if (minimalMode) {
      modifier
        .testTag("in_app_bus_map_container")
        .clipToBounds()
    } else {
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
        .testTag("in_app_bus_map_container")
        .clipToBounds()
    }
  ) {
    val viewWidth = constraints.maxWidth.toFloat()
    val viewHeight = constraints.maxHeight.toFloat()

    // Initialize camera centered on live bus
    LaunchedEffect(viewWidth, viewHeight, route.id) {
      if (viewWidth > 0 && viewHeight > 0) {
        val cx = viewWidth / 2f
        val cy = viewHeight * 0.42f
        panOffset = Offset(cx - busWorldPt.x * scale, cy - busWorldPt.y * scale)
        hasInitializedCenter = true
      }
    }

    // Auto-follow bus if enabled
    LaunchedEffect(animatedBusLat, animatedBusLng, isFollowBusEnabled) {
      if (isFollowBusEnabled && hasInitializedCenter && viewWidth > 0 && viewHeight > 0) {
        val cx = viewWidth / 2f
        val cy = viewHeight * 0.42f
        panOffset = Offset(cx - busWorldPt.x * scale, cy - busWorldPt.y * scale)
      }
    }

    // Main Interactive Map Gesture Surface
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTransformGestures { centroid, pan, zoom, _ ->
            val newScale = (scale * zoom).coerceIn(0.45f, 3.5f)
            panOffset = (panOffset - centroid) * (newScale / scale) + centroid + pan
            scale = newScale
            if (pan.getDistanceSquared() > 8f) {
              onToggleFollowBus(false)
            }
          }
        }
        .pointerInput(Unit) {
          detectTapGestures(
            onDoubleTap = { tapOffset ->
              val newScale = (scale * 1.35f).coerceAtMost(3.5f)
              panOffset = (panOffset - tapOffset) * (newScale / scale) + tapOffset
              scale = newScale
            }
          )
        }
    ) {
      // 1. HIGH-FIDELITY VECTOR CARTOGRAPHY CANVAS LAYER
      Canvas(modifier = Modifier.fillMaxSize()) {
        withTransform({
          translate(panOffset.x, panOffset.y)
          scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
          val groundColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFF1F5F9)
            MapLayerType.DARK -> Color(0xFF0F172A)
            MapLayerType.SATELLITE -> Color(0xFF1E293B)
          }
          val parkColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFD1FAE5)
            MapLayerType.DARK -> Color(0xFF064E3B).copy(alpha = 0.6f)
            MapLayerType.SATELLITE -> Color(0xFF14532D)
          }
          val parkBorderColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFA7F3D0)
            MapLayerType.DARK -> Color(0xFF047857)
            MapLayerType.SATELLITE -> Color(0xFF15803D)
          }
          val waterColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFC6DAFC)
            MapLayerType.DARK -> Color(0xFF1E3A8A)
            MapLayerType.SATELLITE -> Color(0xFF0369A1)
          }
          val campusDistrictColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFEF3D6)
            MapLayerType.DARK -> Color(0xFF78350F).copy(alpha = 0.35f)
            MapLayerType.SATELLITE -> Color(0xFF451A03).copy(alpha = 0.5f)
          }
          val campusBorderColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFDE68A)
            MapLayerType.DARK -> Color(0xFFB45309)
            MapLayerType.SATELLITE -> Color(0xFFD97706)
          }
          val parcelColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFFFFFF)
            MapLayerType.DARK -> Color(0xFF1E293B)
            MapLayerType.SATELLITE -> Color(0xFF334155)
          }
          val parcelBorder = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFE2E8F0)
            MapLayerType.DARK -> Color(0xFF334155)
            MapLayerType.SATELLITE -> Color(0xFF475569)
          }
          val localRoadColor = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFFFFFF)
            MapLayerType.DARK -> Color(0xFF2E3D52)
            MapLayerType.SATELLITE -> Color(0xFF64748B)
          }
          val arterialCasing = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFCBD5E1)
            MapLayerType.DARK -> Color(0xFF475569)
            MapLayerType.SATELLITE -> Color(0xFF475569)
          }
          val arterialCore = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFFFFFF)
            MapLayerType.DARK -> Color(0xFF1E293B)
            MapLayerType.SATELLITE -> Color(0xFF94A3B8)
          }
          val highwayCasing = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFF59E0B)
            MapLayerType.DARK -> Color(0xFFD97706)
            MapLayerType.SATELLITE -> Color(0xFFD97706)
          }
          val highwayCore = when (selectedLayer) {
            MapLayerType.STREET -> Color(0xFFFEF3C7)
            MapLayerType.DARK -> Color(0xFF451A03)
            MapLayerType.SATELLITE -> Color(0xFF78350F)
          }

          // 1. Base Landmass Canvas
          drawRect(groundColor, size = Size(1400f, 1400f))

          // 2. City Blocks & Districts
          // St. Joseph Campus District Zone
          drawRoundRect(
            color = campusDistrictColor,
            topLeft = Offset(740f, 160f),
            size = Size(460f, 340f),
            cornerRadius = CornerRadius(24f, 24f)
          )
          drawRoundRect(
            color = campusBorderColor,
            topLeft = Offset(740f, 160f),
            size = Size(460f, 340f),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(3f)
          )

          // Greenwood Botanical Park
          drawRoundRect(
            color = parkColor,
            topLeft = Offset(140f, 160f),
            size = Size(320f, 240f),
            cornerRadius = CornerRadius(20f, 20f)
          )
          drawRoundRect(
            color = parkBorderColor,
            topLeft = Offset(140f, 160f),
            size = Size(320f, 240f),
            cornerRadius = CornerRadius(20f, 20f),
            style = Stroke(2f)
          )

          // Central Meadows Botanical Park
          drawRoundRect(
            color = parkColor,
            topLeft = Offset(980f, 680f),
            size = Size(320f, 260f),
            cornerRadius = CornerRadius(20f, 20f)
          )
          drawRoundRect(
            color = parkBorderColor,
            topLeft = Offset(980f, 680f),
            size = Size(320f, 260f),
            cornerRadius = CornerRadius(20f, 20f),
            style = Stroke(2f)
          )

          // Residential Neighborhood Parcel Blocks
          val parcels = listOf(
            Offset(140f, 480f) to Size(240f, 180f),
            Offset(440f, 480f) to Size(230f, 180f),
            Offset(140f, 720f) to Size(300f, 200f),
            Offset(480f, 720f) to Size(360f, 200f),
            Offset(740f, 560f) to Size(180f, 120f),
            Offset(140f, 980f) to Size(350f, 220f),
            Offset(540f, 980f) to Size(440f, 220f),
            Offset(1020f, 980f) to Size(260f, 220f)
          )
          parcels.forEach { (pos, sz) ->
            drawRoundRect(parcelColor, topLeft = pos, size = sz, cornerRadius = CornerRadius(14f, 14f))
            drawRoundRect(parcelBorder, topLeft = pos, size = sz, cornerRadius = CornerRadius(14f, 14f), style = Stroke(2f))
          }

          // 3. Blue Horizon Water Canal
          val canalPath = Path().apply {
            moveTo(-20f, 400f)
            cubicTo(300f, 360f, 480f, 460f, 740f, 430f)
            cubicTo(1000f, 400f, 1180f, 320f, 1420f, 350f)
            lineTo(1420f, 420f)
            cubicTo(1180f, 390f, 1000f, 470f, 740f, 500f)
            cubicTo(480f, 530f, 300f, 430f, -20f, 470f)
            close()
          }
          drawPath(canalPath, waterColor)

          // 4. Street Network
          // Local Grid Streets
          listOf(310f, 640f, 900f, 1160f).forEach { y ->
            drawLine(localRoadColor, start = Offset(60f, y), end = Offset(1340f, y), strokeWidth = 10f, cap = StrokeCap.Round)
          }
          listOf(310f, 540f, 840f, 1100f).forEach { x ->
            drawLine(localRoadColor, start = Offset(x, 60f), end = Offset(x, 1340f), strokeWidth = 10f, cap = StrokeCap.Round)
          }

          // Major Arterial Avenue (Cross-Island)
          val arterialPath = Path().apply {
            moveTo(60f, 810f)
            quadraticTo(440f, 760f, 720f, 620f)
            quadraticTo(1000f, 480f, 1340f, 320f)
          }
          drawPath(arterialPath, arterialCasing, style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawPath(arterialPath, arterialCore, style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round))

          // Golden Parkway Highway Corridor to School
          val highwayPath = Path().apply {
            moveTo(180f, 1280f)
            quadraticTo(420f, 880f, 640f, 590f)
            quadraticTo(860f, 300f, 1160f, 160f)
          }
          drawPath(highwayPath, highwayCasing, style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawPath(highwayPath, highwayCore, style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round))

          // 5. Dynamic Route Polyline Path
          val allRoutePoints = stopWorldPts.map { it.second } + schoolWorldPt
          if (allRoutePoints.size > 1) {
            val routePoly = Path().apply {
              moveTo(allRoutePoints.first().x, allRoutePoints.first().y)
              for (i in 1 until allRoutePoints.size) {
                val p0 = allRoutePoints[i - 1]
                val p1 = allRoutePoints[i]
                quadraticTo(p0.x, p1.y, p1.x, p1.y)
              }
            }

            // Route Ambient Glow
            drawPath(
              routePoly,
              color = SchoolNavyPrimary.copy(alpha = 0.28f),
              style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Route Main Track
            val polyColor = if (selectedLayer == MapLayerType.DARK) Color(0xFF38BDF8) else SchoolNavyPrimary
            drawPath(
              routePoly,
              color = polyColor,
              style = Stroke(width = 6.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Animated Traveling Dash Phase
            drawPath(
              routePoly,
              color = SchoolGold,
              style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), dashPhase)
              )
            )
          }
        }
      }

      // 2. INTERACTIVE COMPOSABLE STOP MARKERS LAYER
      stopWorldPts.forEachIndexed { index, (stop, worldPos) ->
        val screenX = panOffset.x + worldPos.x * scale
        val screenY = panOffset.y + worldPos.y * scale
        val isUserStop = !userStopName.isNullOrBlank() && stop.name.contains(userStopName, ignoreCase = true)

        Box(
          modifier = Modifier
            .offset { IntOffset((screenX - 40.dp.toPx()).roundToInt(), (screenY - 40.dp.toPx()).roundToInt()) }
            .width(80.dp)
            .clickable {
              selectedStopForPopup = stop
              onStopClick(stop)
            },
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            // Stop Pin Coin Badge
            Surface(
              shape = CircleShape,
              color = if (isUserStop) SchoolEmerald else if (stop.isCompleted) Color(0xFF059669) else if (stop.isCurrent) SchoolGold else Color.White,
              border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (isUserStop) Color.White else if (stop.isCompleted) Color.White else if (stop.isCurrent) Color(0xFFB45309) else SchoolNavyPrimary
              ),
              shadowElevation = 4.dp,
              modifier = Modifier.size(if (isUserStop) 26.dp else 22.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                if (isUserStop) {
                  Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                  Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Black,
                      fontSize = 11.sp
                    ),
                    color = if (stop.isCompleted) Color.White else if (stop.isCurrent) Color(0xFF78350F) else SchoolNavyDark
                  )
                }
              }
            }

            // Stop Name Label Pill
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isUserStop) SchoolEmerald else Color.White.copy(alpha = 0.94f),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isUserStop) Color.White else Color(0xFFCBD5E1)),
              shadowElevation = 3.dp,
              modifier = Modifier.padding(horizontal = 2.dp)
            ) {
              Text(
                text = if (isUserStop) "MY STOP" else stop.name.take(12),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                ),
                color = if (isUserStop) Color.White else SchoolNavyDark,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
          }
        }
      }

      // School Campus Destination Marker
      val schoolScreenX = panOffset.x + schoolWorldPt.x * scale
      val schoolScreenY = panOffset.y + schoolWorldPt.y * scale
      Box(
        modifier = Modifier
          .offset { IntOffset((schoolScreenX - 50.dp.toPx()).roundToInt(), (schoolScreenY - 50.dp.toPx()).roundToInt()) }
          .width(100.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = SchoolNavyPrimary,
            border = androidx.compose.foundation.BorderStroke(2.5.dp, Color.White),
            shadowElevation = 6.dp,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = "School Campus",
                tint = SchoolGold,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = SchoolNavyPrimary,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            shadowElevation = 4.dp
          ) {
            Text(
              text = "CAMPUS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 9.5.sp,
                letterSpacing = 0.5.sp
              ),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // 3. ANIMATED TOP-DOWN LIVE SCHOOL BUS VEHICLE LAYER
      val busScreenX = panOffset.x + busWorldPt.x * scale
      val busScreenY = panOffset.y + busWorldPt.y * scale

      Box(
        modifier = Modifier
          .offset { IntOffset((busScreenX - 40.dp.toPx()).roundToInt(), (busScreenY - 40.dp.toPx()).roundToInt()) }
          .size(80.dp),
        contentAlignment = Alignment.Center
      ) {
        // Radar Pulse Rings
        Canvas(modifier = Modifier.size(80.dp)) {
          drawCircle(
            color = Color(0xFFD97706).copy(alpha = radarPulseAlpha),
            radius = radarPulseRadius * scale.coerceIn(0.8f, 1.8f),
            center = center,
            style = Stroke(width = 2.5f)
          )
          drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = radarPulseAlpha * 0.35f),
            radius = radarPulseRadius * scale.coerceIn(0.8f, 1.8f),
            center = center
          )
        }

        // Rotator Container based on animated heading
        Box(
          modifier = Modifier
            .rotate(animatedBusHeading)
            .size(54.dp),
          contentAlignment = Alignment.Center
        ) {
          // Directional Headlights Beam
          Box(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .offset(y = (-14).dp)
              .size(width = 30.dp, height = 20.dp)
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.65f),
                    Color(0xFFFEF08A).copy(alpha = 0.05f)
                  )
                ),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
              )
          )

          // 4 Wheels
          Row(
            modifier = Modifier
              .width(34.dp)
              .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(
              modifier = Modifier.fillMaxHeight(),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Box(modifier = Modifier.size(4.dp, 8.dp).background(Color(0xFF0F172A), RoundedCornerShape(2.dp)))
              Box(modifier = Modifier.size(4.dp, 8.dp).background(Color(0xFF0F172A), RoundedCornerShape(2.dp)))
            }
            Column(
              modifier = Modifier.fillMaxHeight(),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Box(modifier = Modifier.size(4.dp, 8.dp).background(Color(0xFF0F172A), RoundedCornerShape(2.dp)))
              Box(modifier = Modifier.size(4.dp, 8.dp).background(Color(0xFF0F172A), RoundedCornerShape(2.dp)))
            }
          }

          // Yellow School Bus Chassis Body
          Surface(
            shape = RoundedCornerShape(7.dp),
            color = Color(0xFFFBBF24),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFB45309)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(width = 26.dp, height = 46.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              // Windshield front
              Box(
                modifier = Modifier
                  .width(18.dp)
                  .height(5.dp)
                  .background(Color(0xFF1E293B), RoundedCornerShape(2.dp))
              )

              // Roof Route Number Strip
              Surface(
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFFF59E0B),
                modifier = Modifier.padding(horizontal = 2.dp)
              ) {
                Text(
                  text = "#${route.routeNumber}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 7.5.sp,
                    letterSpacing = (-0.5).sp
                  ),
                  color = Color(0xFF78350F),
                  modifier = Modifier.padding(horizontal = 2.dp)
                )
              }

              // Back window
              Box(
                modifier = Modifier
                  .width(16.dp)
                  .height(3.5.dp)
                  .background(Color(0xFF1E293B), RoundedCornerShape(1.5.dp))
              )
            }
          }
        }
      }
    }

    // 4. FLOATING TOP-LEFT MAP LAYER CONTROLS (Only when not minimalMode)
    if (!minimalMode) {
      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Surface(
          color = SchoolNavyDark.copy(alpha = 0.92f),
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

      // Top-Right Actions
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Button(
          onClick = onOpenGoogleMapsApp,
          colors = ButtonDefaults.buttonColors(
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

        Surface(
          color = SchoolNavyDark.copy(alpha = 0.88f),
          shape = CircleShape,
          shadowElevation = 4.dp,
          modifier = Modifier
            .size(32.dp)
            .clickable { onShareLiveLocation() }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Live Bus Link",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 5. FLOATING MAP NAVIGATION BUTTONS (Follow, Fit Route, Zoom +, Zoom -)
    Column(
      modifier = Modifier
        .align(if (minimalMode) Alignment.CenterEnd else Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = if (minimalMode) 0.dp else 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Follow Bus Toggle
      FloatingMapControl(
        icon = if (isFollowBusEnabled) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
        tint = if (isFollowBusEnabled) Color(0xFF059669) else SchoolNavyPrimary,
        bgColor = if (isFollowBusEnabled) Color(0xFFDCFCE7) else Color.White,
        contentDesc = "Follow Bus",
        onClick = {
          val willFollow = !isFollowBusEnabled
          onToggleFollowBus(willFollow)
          if (willFollow) {
            val cx = viewWidth / 2f
            val cy = viewHeight * 0.42f
            panOffset = Offset(cx - busWorldPt.x * scale, cy - busWorldPt.y * scale)
          }
        }
      )

      // Fit Route Bounds
      FloatingMapControl(
        icon = Icons.Default.CropFree,
        tint = SchoolNavyPrimary,
        contentDesc = "Fit Route",
        onClick = {
          scale = 0.85f
          val cx = viewWidth / 2f
          val cy = viewHeight * 0.42f
          panOffset = Offset(cx - 700f * scale, cy - 700f * scale)
          onToggleFollowBus(false)
        }
      )

      // Zoom In (+)
      FloatingMapControl(
        icon = Icons.Default.Add,
        tint = SchoolNavyPrimary,
        contentDesc = "Zoom In",
        onClick = {
          val oldScale = scale
          val newScale = (scale * 1.3f).coerceAtMost(3.5f)
          val cx = viewWidth / 2f
          val cy = viewHeight / 2f
          panOffset = (panOffset - Offset(cx, cy)) * (newScale / oldScale) + Offset(cx, cy)
          scale = newScale
        }
      )

      // Zoom Out (-)
      FloatingMapControl(
        icon = Icons.Default.Remove,
        tint = SchoolNavyPrimary,
        contentDesc = "Zoom Out",
        onClick = {
          val oldScale = scale
          val newScale = (scale / 1.3f).coerceAtLeast(0.45f)
          val cx = viewWidth / 2f
          val cy = viewHeight / 2f
          panOffset = (panOffset - Offset(cx, cy)) * (newScale / oldScale) + Offset(cx, cy)
          scale = newScale
        }
      )
    }

    // 6. BOTTOM BRAND WATERMARK
    Surface(
      color = Color.White.copy(alpha = 0.82f),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = if (minimalMode) 130.dp else 12.dp)
    ) {
      Text(
        text = "ST. JOSEPH GPS LIVE • ${route.routeName.take(22)}",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          letterSpacing = 0.5.sp
        ),
        color = Color(0xFF64748B),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }

    // 7. SELECTED STOP POPUP DIALOG
    if (selectedStopForPopup != null) {
      val stop = selectedStopForPopup!!
      AlertDialog(
        onDismissRequest = { selectedStopForPopup = null },
        icon = {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = SchoolNavyPrimary,
            modifier = Modifier.size(32.dp)
          )
        },
        title = {
          Text(
            text = stop.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Scheduled Arrival: ${stop.scheduledTime}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
              text = "Status: ${if (stop.isCompleted) "Visited" else if (stop.isCurrent) "Current Stop" else "Upcoming"}",
              color = if (stop.isCompleted) Color(0xFF059669) else if (stop.isCurrent) SchoolGold else MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
            if (stop.studentCount > 0) {
              Text(
                text = "${stop.studentCount} students boarding at this stop",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "GPS Coordinates: ${String.format("%.4f", stop.latitude)}, ${String.format("%.4f", stop.longitude)}",
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
    shape = CircleShape,
    color = bgColor.copy(alpha = 0.95f),
    shadowElevation = 5.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    modifier = Modifier
      .size(42.dp)
      .clickable { onClick() }
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDesc,
        tint = tint,
        modifier = Modifier.size(20.dp)
      )
    }
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
    📍 Live GPS Tracker - St. Joseph Matriculation Hr. Sec. School
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
