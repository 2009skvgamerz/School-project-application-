package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BusRoute
import com.example.model.BusStatus
import com.example.model.BusStop
import com.example.model.StudentProfile
import com.example.model.UserRole
import com.example.ui.components.CampusMapView
import com.example.ui.components.InAppBusMapView
import com.example.ui.components.OpenStreetMapMapView
import com.example.ui.components.launchGoogleMapsLocation
import com.example.ui.components.launchGoogleMapsNavigation
import com.example.ui.components.shareBusLiveLocation
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolGoldContainer
import com.example.ui.theme.SchoolGoldLight
import com.example.ui.theme.SchoolNavyContainer
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

private fun cleanRouteDisplay(raw: String): String {
  val t = raw.trim()
  return when {
    t.startsWith("Route #") -> t
    t.startsWith("Route ") -> "Route #" + t.removePrefix("Route ").trim()
    t.startsWith("#") -> "Route $t"
    else -> "Route #$t"
  }
}

enum class BusTrackingViewMode(val label: String, val icon: ImageVector) {
  IN_APP_MAP("Google Maps Live", Icons.Default.Map),
  EMBED_MAP("Google Direct", Icons.Default.Public),
  TACTICAL_RADAR("Tactical Radar", Icons.Default.Sensors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusTrackingScreen(
  routes: List<BusRoute>,
  selectedRouteId: String,
  studentProfile: StudentProfile?,
  userRole: UserRole,
  onSelectRoute: (String) -> Unit,
  onSimulateMovement: (String) -> Unit,
  modifier: Modifier = Modifier,
  onNavigateBack: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val activeRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()

  var isAutoSimulating by remember { mutableStateOf(false) }
  var selectedStopForModal by remember { mutableStateOf<BusStop?>(null) }
  var isFollowBusEnabled by remember { mutableStateOf(true) }
  var showRouteBottomSheet by remember { mutableStateOf(false) }
  var showOptionsMenu by remember { mutableStateOf(false) }
  var showTacticalRadarModal by remember { mutableStateOf(false) }
  // Map Engine: "osm" (OpenStreetMap - Free, No API key/billing needed), "google" (Google Maps SDK), "canvas" (In-App Carto Engine)
  var mapEngineType by remember { mutableStateOf("osm") }
  var isCampusGuideMode by remember { mutableStateOf(false) }

  // Auto-simulation ticker
  LaunchedEffect(isAutoSimulating, selectedRouteId) {
    while (isAutoSimulating) {
      kotlinx.coroutines.delay(2500)
      onSimulateMovement(selectedRouteId)
    }
  }

  if (activeRoute == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No bus routes configured")
    }
    return
  }

  val userAssignedStop = studentProfile?.busRoute?.substringAfter("(")?.substringBefore(")")?.takeIf { it.isNotBlank() }
    ?: activeRoute.stops.firstOrNull()?.name
    ?: "Campus Gate"

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("bus_tracking_screen")
  ) {
    // 1. IMMERSIVE FULL-SCREEN LIVE MAP (OpenStreetMap - Free, No Billing/API Key Required)
    when {
      isCampusGuideMode -> {
        OpenStreetMapMapView(
          route = activeRoute,
          showTopCampusBar = true,
          showFloatingZoomControls = true,
          showBottomLocationCard = true,
          modifier = Modifier.fillMaxSize()
        )
      }
      mapEngineType == "osm" -> {
        OpenStreetMapMapView(
          route = activeRoute,
          onStopClick = { selectedStopForModal = it },
          showTopCampusBar = false,
          showFloatingZoomControls = false,
          showBottomLocationCard = false,
          modifier = Modifier.fillMaxSize()
        )
      }
      mapEngineType == "google" -> {
        CampusMapView(
          route = activeRoute,
          onStopClick = { selectedStopForModal = it },
          showTopCampusBar = false,
          showFloatingZoomControls = false,
          showBottomLocationCard = false,
          modifier = Modifier.fillMaxSize()
        )
      }
      else -> {
        InAppBusMapView(
          route = activeRoute,
          onStopClick = { selectedStopForModal = it },
          onOpenGoogleMapsApp = {
            launchGoogleMapsNavigation(
              context = context,
              originLat = activeRoute.currentLatitude,
              originLng = activeRoute.currentLongitude,
              destLat = activeRoute.schoolLatitude,
              destLng = activeRoute.schoolLongitude,
              waypoints = activeRoute.stops
            )
          },
          onShareLiveLocation = { shareBusLiveLocation(context, activeRoute) },
          isFollowBusEnabled = isFollowBusEnabled,
          onToggleFollowBus = { isFollowBusEnabled = it },
          minimalMode = true,
          userStopName = userAssignedStop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    // 2. TOP FLOATING CONTROLS & ROUTE SELECTOR
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top Navigation & Actions Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Circle Back Button
        Surface(
          onClick = { onNavigateBack?.invoke() },
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.95f),
          shadowElevation = 4.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier
            .size(40.dp)
            .testTag("bus_back_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Center Switcher: Bus Route vs Campus Map Guide
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White.copy(alpha = 0.96f),
          shadowElevation = 4.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.padding(horizontal = 4.dp).testTag("bus_map_engine_badge")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Bus Mode Chip
            Surface(
              onClick = { isCampusGuideMode = false },
              shape = RoundedCornerShape(16.dp),
              color = if (!isCampusGuideMode) SchoolNavyPrimary else Color.Transparent,
              modifier = Modifier.height(30.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (!isCampusGuideMode) Color(0xFF10B981) else Color(0xFF94A3B8))
                )
                Text(
                  text = cleanRouteDisplay(activeRoute.routeNumber),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                  ),
                  color = if (!isCampusGuideMode) Color.White else SchoolNavyDark
                )
              }
            }

            // Campus POI Mode Chip
            Surface(
              onClick = { isCampusGuideMode = true },
              shape = RoundedCornerShape(16.dp),
              color = if (isCampusGuideMode) SchoolNavyPrimary else Color.Transparent,
              modifier = Modifier.height(30.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = null,
                  tint = if (isCampusGuideMode) SchoolGold else Color(0xFF64748B),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "Campus Map",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                  ),
                  color = if (isCampusGuideMode) Color.White else SchoolNavyDark
                )
              }
            }
          }
        }

        // Circle Options 3-Dots Button
        Box {
          Surface(
            onClick = { showOptionsMenu = true },
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
              .size(40.dp)
              .testTag("bus_options_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          DropdownMenu(
            expanded = showOptionsMenu,
            onDismissRequest = { showOptionsMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("OpenStreetMap (Free, No Key)") },
              leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = if (mapEngineType == "osm") SchoolEmerald else SchoolNavyPrimary) },
              trailingIcon = { if (mapEngineType == "osm") Icon(Icons.Default.Check, contentDescription = null, tint = SchoolEmerald, modifier = Modifier.size(16.dp)) },
              onClick = {
                showOptionsMenu = false
                mapEngineType = "osm"
              }
            )
            DropdownMenuItem(
              text = { Text("In-App Vector Canvas") },
              leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = if (mapEngineType == "canvas") SchoolEmerald else SchoolNavyPrimary) },
              trailingIcon = { if (mapEngineType == "canvas") Icon(Icons.Default.Check, contentDescription = null, tint = SchoolEmerald, modifier = Modifier.size(16.dp)) },
              onClick = {
                showOptionsMenu = false
                mapEngineType = "canvas"
              }
            )
            DropdownMenuItem(
              text = { Text("Google Maps SDK (Key Required)") },
              leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = if (mapEngineType == "google") SchoolEmerald else Color(0xFF64748B)) },
              trailingIcon = { if (mapEngineType == "google") Icon(Icons.Default.Check, contentDescription = null, tint = SchoolEmerald, modifier = Modifier.size(16.dp)) },
              onClick = {
                showOptionsMenu = false
                mapEngineType = "google"
              }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
              text = { Text("Open in Google Maps App") },
              leadingIcon = { Icon(Icons.Default.Directions, contentDescription = null) },
              onClick = {
                showOptionsMenu = false
                launchGoogleMapsNavigation(
                  context = context,
                  originLat = activeRoute.currentLatitude,
                  originLng = activeRoute.currentLongitude,
                  destLat = activeRoute.schoolLatitude,
                  destLng = activeRoute.schoolLongitude,
                  waypoints = activeRoute.stops
                )
              }
            )
            DropdownMenuItem(
              text = { Text("Share Live GPS Link") },
              leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
              onClick = {
                showOptionsMenu = false
                shareBusLiveLocation(context, activeRoute)
              }
            )
            DropdownMenuItem(
              text = { Text("Tactical Radar Canvas") },
              leadingIcon = { Icon(Icons.Default.Sensors, contentDescription = null) },
              onClick = {
                showOptionsMenu = false
                showTacticalRadarModal = true
              }
            )
            DropdownMenuItem(
              text = { Text(if (isAutoSimulating) "Stop Auto GPS" else "Start Auto GPS") },
              leadingIcon = { Icon(if (isAutoSimulating) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null) },
              onClick = {
                showOptionsMenu = false
                isAutoSimulating = !isAutoSimulating
              }
            )
          }
        }
      }

      // Quick Horizontal Route Selector Chips (Only in Bus Mode)
      if (!isCampusGuideMode) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("route_pills_row")
        ) {
          items(routes) { route ->
            val isSelected = route.id == selectedRouteId
            Surface(
              onClick = { onSelectRoute(route.id) },
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) SchoolNavyPrimary else Color.White.copy(alpha = 0.94f),
              shadowElevation = if (isSelected) 4.dp else 1.dp,
              border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsBus,
                  contentDescription = null,
                  tint = if (isSelected) SchoolGold else Color(0xFF64748B),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = cleanRouteDisplay(route.routeNumber),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 12.sp
                  ),
                  color = if (isSelected) Color.White else SchoolNavyDark
                )
              }
            }
          }
        }
      }
    }

    // 3. FLOATING BOTTOM OVERLAY CARDS (St. Joseph School Transport Experience - Bus Mode only)
    if (!isCampusGuideMode) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
      // MAIN FLOATING CARD (Real-time Bus Telemetry & Driver Contact)
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("swiggy_bus_status_card")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Top Row: Headline & Subtitle + School Navy & Gold ETA Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left Column: Status info
            Column(
              modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                )
                Text(
                  text = "En route to your stop",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 16.5.sp
                  ),
                  color = SchoolNavyDark
                )
              }
              Text(
                text = "Traffic smooth • Driver ${activeRoute.driverName} • ${activeRoute.currentSpeedKmH} km/h",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.5.sp,
                  lineHeight = 15.sp
                ),
                color = Color(0xFF64748B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Right: St. Joseph Academic Navy & Gold Gradient ETA Badge
            Box(
              modifier = Modifier
                .width(64.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.verticalGradient(
                    listOf(
                      SchoolNavyPrimary,
                      SchoolNavyDark
                    )
                  )
                )
                .padding(5.dp)
                .testTag("swiggy_eta_pill")
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = SchoolGoldLight,
                modifier = Modifier
                  .size(11.dp)
                  .align(Alignment.TopEnd)
              )

              // Number and "mins"
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "${activeRoute.estimatedArrivalMins}",
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                  ),
                  color = Color.White
                )
                Text(
                  text = "mins",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  color = SchoolGoldContainer
                )
              }
            }
          }

          // Subtle divider line
          HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

          // Bottom Action Row: "Route info & stops >" and Call/Chat/Avatar pill
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Clickable Route Info & Instructions
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showRouteBottomSheet = true }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Route stops & timeline",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp
                ),
                color = SchoolNavyPrimary
              )
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View stops",
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(16.dp)
              )
            }

            // Driver Call & Chat Actions Pill
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = SchoolNavyContainer.copy(alpha = 0.5f),
              modifier = Modifier.testTag("driver_actions_pill")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                // Call Icon Button
                IconButton(
                  onClick = { launchDialer(context, activeRoute.driverPhone) },
                  modifier = Modifier.size(30.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Driver",
                    tint = SchoolEmerald,
                    modifier = Modifier.size(16.dp)
                  )
                }

                // Chat / Message Icon Button
                IconButton(
                  onClick = { launchSms(context, activeRoute.driverPhone, "Hello Driver, tracking bus #${activeRoute.routeNumber}") },
                  modifier = Modifier.size(30.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Message Driver",
                    tint = SchoolNavyPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }

                // Driver Avatar
                Surface(
                  shape = CircleShape,
                  color = SchoolNavyPrimary,
                  modifier = Modifier.size(28.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = activeRoute.driverName.take(1),
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = SchoolGold
                    )
                  }
                }
              }
            }
          }
        }
      }

      // SECONDARY FLOATING PILL CARD (School Safety Verified Badge)
      Surface(
        onClick = { showRouteBottomSheet = true },
        shape = RoundedCornerShape(14.dp),
        color = SchoolNavyContainer.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SchoolNavyPrimary.copy(alpha = 0.2f)),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("secondary_safety_pill")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              shape = CircleShape,
              color = SchoolNavyPrimary,
              modifier = Modifier.size(18.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp)
                )
              }
            }
            Text(
              text = "Live GPS Verified • Safe Speed ${activeRoute.currentSpeedKmH} km/h • Next: ${activeRoute.nextStopName}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              ),
              color = SchoolNavyDark,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SchoolNavyPrimary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }

    // 4. EXPANDABLE ROUTE STOPS BOTTOM SHEET
    if (showRouteBottomSheet) {
      ModalBottomSheet(
        onDismissRequest = { showRouteBottomSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color.White
      ) {
        RouteStopsBottomSheetContent(
          route = activeRoute,
          studentStopName = userAssignedStop,
          isAutoSimulating = isAutoSimulating,
          onToggleAutoSimulating = { isAutoSimulating = !isAutoSimulating },
          onStepSimulation = { onSimulateMovement(activeRoute.id) },
          onStopClick = { selectedStopForModal = it },
          onOpenGoogleMaps = {
            launchGoogleMapsNavigation(
              context = context,
              originLat = activeRoute.currentLatitude,
              originLng = activeRoute.currentLongitude,
              destLat = activeRoute.schoolLatitude,
              destLng = activeRoute.schoolLongitude,
              waypoints = activeRoute.stops
            )
          },
          onClose = { showRouteBottomSheet = false }
        )
      }
    }

    // 5. TACTICAL RADAR DIALOG MODAL (Optional Secondary Visualization)
    if (showTacticalRadarModal) {
      Dialog(
        onDismissRequest = { showTacticalRadarModal = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .wrapContentHeight(),
          shape = RoundedCornerShape(20.dp),
          color = SchoolNavyDark
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Tactical Radar Visualization",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
              IconButton(onClick = { showTacticalRadarModal = false }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
              }
            }

            val currentIndex = activeRoute.stops.indexOfFirst { it.isCurrent }.let { if (it == -1) 0 else it }
            val progressPercent = if (activeRoute.stops.isNotEmpty()) {
              (currentIndex.toFloat() / (activeRoute.stops.size - 1).coerceAtLeast(1))
            } else 0f

            LiveRouteRadarCanvas(
              progressPercent = progressPercent,
              currentSpeed = activeRoute.currentSpeedKmH,
              status = activeRoute.status,
              stopsCount = activeRoute.stops.size,
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
            )

            Text(
              text = "Route #${activeRoute.routeNumber} • Speed ${activeRoute.currentSpeedKmH} km/h • Next: ${activeRoute.nextStopName}",
              style = MaterialTheme.typography.bodySmall,
              color = SchoolGold
            )
          }
        }
      }
    }

    // 6. STOP DETAIL MODAL (STUDENT / TEACHER QUICK TRACKER FOR SPECIFIC STOP)
    selectedStopForModal?.let { stop ->
      val stopIndex = activeRoute.stops.indexOfFirst { it.id == stop.id }
      val currentIndex = activeRoute.stops.indexOfFirst { it.isCurrent }.let { if (it == -1) 0 else it }
      val isPastStop = stopIndex < currentIndex || stop.isCompleted
      val isCurrentStop = stop.isCurrent
      val stopsAway = (stopIndex - currentIndex).coerceAtLeast(0)
      val calculatedETA = if (isPastStop) "Bus has already passed this stop" else if (isCurrentStop) "Bus is currently here!" else "~${stopsAway * 4 + activeRoute.delayMinutes} mins"

      AlertDialog(
        onDismissRequest = { selectedStopForModal = null },
        icon = {
          Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isCurrentStop) Color(0xFFFEF3C7) else SchoolNavyPrimary.copy(alpha = 0.12f)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isCurrentStop) Icons.Default.DirectionsBus else Icons.Default.Place,
                contentDescription = null,
                tint = if (isCurrentStop) Color(0xFFEA580C) else SchoolNavyPrimary,
                modifier = Modifier.size(26.dp)
              )
            }
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = stop.name,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
              text = "${activeRoute.routeNumber} (${activeRoute.busRegistration})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        text = {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Live Status Card
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = when {
                stop.isSkipped -> Color(0xFFFEE2E2)
                isCurrentStop -> Color(0xFFFEF3C7)
                isPastStop -> Color(0xFFD1FAE5)
                else -> Color(0xFFEFF6FF)
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "ESTIMATED ARRIVAL (ETA)",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = calculatedETA,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                  color = when {
                    stop.isSkipped -> Color(0xFFDC2626)
                    isCurrentStop -> Color(0xFFC2410C)
                    isPastStop -> Color(0xFF047857)
                    else -> Color(0xFF1E40AF)
                  }
                )
                Text(
                  text = "Scheduled Time: ${stop.scheduledTime} • Current Bus Speed: ${activeRoute.currentSpeedKmH} km/h",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Driver & Crew Quick Contact Card
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Pilot / Driver Contact",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = activeRoute.driverName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = activeRoute.driverPhone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                IconButton(
                  onClick = { launchDialer(context, activeRoute.driverPhone) },
                  modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFD1FAE5), CircleShape)
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Driver",
                    tint = Color(0xFF065F46)
                  )
                }
              }
            }

            // Google Maps Navigation button
            OutlinedButton(
              onClick = {
                launchGoogleMapsLocation(context, stop.latitude, stop.longitude, stop.name)
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Open Stop in Google Maps")
            }
          }
        },
        confirmButton = {
          Button(
            onClick = { selectedStopForModal = null },
            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
          ) {
            Text("Close")
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteStopsBottomSheetContent(
  route: BusRoute,
  studentStopName: String,
  isAutoSimulating: Boolean,
  onToggleAutoSimulating: () -> Unit,
  onStepSimulation: () -> Unit,
  onStopClick: (BusStop) -> Unit,
  onOpenGoogleMaps: () -> Unit,
  onClose: () -> Unit
) {
  val context = LocalContext.current
  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .navigationBarsPadding(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Route #${route.routeNumber}: ${route.routeName}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Vehicle: ${route.busRegistration} • ${route.stops.size} Total Stops",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = "Close Sheet")
        }
      }
    }

    // Quick Telemetry Row
    item {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          BusTelemetryMetricDark(label = "Speed", value = "${route.currentSpeedKmH} km/h", icon = Icons.Default.Speed)
          VerticalDivider(modifier = Modifier.height(28.dp))
          BusTelemetryMetricDark(label = "ETA", value = "${route.estimatedArrivalMins} mins", icon = Icons.Default.Timer)
          VerticalDivider(modifier = Modifier.height(28.dp))
          BusTelemetryMetricDark(label = "Onboard", value = "${route.studentsOnboard}/${route.capacity}", icon = Icons.Default.Groups)
        }
      }
    }

    // GPS Simulation & Testing Toolbar
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SchoolNavyPrimary.copy(alpha = 0.07f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Live GPS Simulator",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
            Text(
              text = if (isAutoSimulating) "Auto-ticking every 2.5s" else "Manual or auto advance",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
              onClick = onStepSimulation,
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = SchoolNavyPrimary, contentColor = Color.White),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("Step GPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }

            FilterChip(
              selected = isAutoSimulating,
              onClick = onToggleAutoSimulating,
              label = { Text(if (isAutoSimulating) "Auto ON" else "Auto OFF", style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.height(34.dp)
            )
          }
        }
      }
    }

    // Stops Timeline Header
    item {
      Text(
        text = "STOP PROGRESSION & SCHEDULE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // List of stops
    items(route.stops.size) { index ->
      val stop = route.stops[index]
      val isLast = index == route.stops.size - 1
      BusStopTimelineItem(
        stop = stop,
        isLast = isLast,
        onOpenInMaps = {
          launchGoogleMapsLocation(context, stop.latitude, stop.longitude, stop.name)
        }
      )
    }

    // Crew Contacts Header
    item {
      Text(
        text = "DRIVER & BUS CREW",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    item {
      BusCrewContactCard(
        title = "Lead Driver",
        name = route.driverName,
        phoneNumber = route.driverPhone,
        icon = Icons.Default.DirectionsBus,
        onCall = { launchDialer(context, route.driverPhone) },
        testTag = "call_driver_sheet_btn"
      )
    }

    item {
      BusCrewContactCard(
        title = "Bus Attendant",
        name = route.attendantName,
        phoneNumber = route.attendantPhone,
        icon = Icons.Default.Person,
        onCall = { launchDialer(context, route.attendantPhone) },
        testTag = "call_attendant_sheet_btn"
      )
    }

    // Emergency Helpline
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Emergency, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
            Column {
              Text(
                text = "Transport Helpdesk",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF991B1B)
              )
              Text(
                text = "+91 98450 99001 • 24x7 Emergency Line",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7F1D1D)
              )
            }
          }
          IconButton(
            onClick = { launchDialer(context, "+919845099001") },
            modifier = Modifier
              .size(36.dp)
              .background(Color(0xFFDC2626), CircleShape)
          ) {
            Icon(Icons.Default.Phone, contentDescription = "Emergency Call", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    // Google Maps App Button
    item {
      Button(
        onClick = onOpenGoogleMaps,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Open Route in Google Maps App", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
      }
    }

    item {
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun LiveRouteRadarCanvas(
  progressPercent: Float,
  currentSpeed: Int,
  status: BusStatus,
  stopsCount: Int,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 12f,
    targetValue = 28f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_radius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_alpha"
  )

  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val startPoint = Offset(30f, canvasHeight * 0.7f)
    val endPoint = Offset(canvasWidth - 30f, canvasHeight * 0.3f)
    val control1 = Offset(canvasWidth * 0.35f, canvasHeight * 0.15f)
    val control2 = Offset(canvasWidth * 0.65f, canvasHeight * 0.85f)

    val routePath = Path().apply {
      moveTo(startPoint.x, startPoint.y)
      cubicTo(control1.x, control1.y, control2.x, control2.y, endPoint.x, endPoint.y)
    }

    // Draw background track
    drawPath(
      path = routePath,
      color = Color.White.copy(alpha = 0.2f),
      style = Stroke(
        width = 6.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
      )
    )

    // Calculate approximate current position along Bezier
    val t = progressPercent.coerceIn(0.05f, 0.95f)
    val oneMinusT = 1.0f - t
    val currentX = (oneMinusT * oneMinusT * oneMinusT * startPoint.x) +
      (3 * oneMinusT * oneMinusT * t * control1.x) +
      (3 * oneMinusT * t * t * control2.x) +
      (t * t * t * endPoint.x)
    val currentY = (oneMinusT * oneMinusT * oneMinusT * startPoint.y) +
      (3 * oneMinusT * oneMinusT * t * control1.y) +
      (3 * oneMinusT * t * t * control2.y) +
      (t * t * t * endPoint.y)

    // Draw stop waypoint dots
    for (i in 0 until stopsCount) {
      val stopT = i.toFloat() / (stopsCount - 1).coerceAtLeast(1)
      val stopOneMinusT = 1.0f - stopT
      val stopX = (stopOneMinusT * stopOneMinusT * stopOneMinusT * startPoint.x) +
        (3 * stopOneMinusT * stopOneMinusT * stopT * control1.x) +
        (3 * stopOneMinusT * stopT * stopT * control2.x) +
        (stopT * stopT * stopT * endPoint.x)
      val stopY = (stopOneMinusT * stopOneMinusT * stopOneMinusT * startPoint.y) +
        (3 * stopOneMinusT * stopOneMinusT * stopT * control1.y) +
        (3 * stopOneMinusT * stopT * stopT * control2.y) +
        (stopT * stopT * stopT * endPoint.y)

      val isCompleted = stopT <= t
      drawCircle(
        color = if (isCompleted) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f),
        radius = if (isCompleted) 6.dp.toPx() else 4.dp.toPx(),
        center = Offset(stopX, stopY)
      )
    }

    // Pulsing radar ripple
    drawCircle(
      color = Color(0xFFFBBF24).copy(alpha = pulseAlpha),
      radius = pulseRadius.dp.toPx(),
      center = Offset(currentX, currentY)
    )

    // Outer glow
    drawCircle(
      color = Color(0xFFF59E0B),
      radius = 12.dp.toPx(),
      center = Offset(currentX, currentY)
    )

    // Bus Center Pin
    drawCircle(
      color = Color.White,
      radius = 7.dp.toPx(),
      center = Offset(currentX, currentY)
    )
  }
}

@Composable
private fun BusTelemetryMetricDark(
  label: String,
  value: String,
  icon: ImageVector
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(14.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun BusCrewContactCard(
  title: String,
  name: String,
  phoneNumber: String,
  icon: ImageVector,
  onCall: () -> Unit,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(22.dp))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = name,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = phoneNumber,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = onCall,
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag(testTag)
      ) {
        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Call")
      }
    }
  }
}

@Composable
private fun BusStopTimelineItem(
  stop: BusStop,
  isLast: Boolean,
  onOpenInMaps: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Timeline Node & Line
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(28.dp)
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(
            when {
              stop.isCurrent -> SchoolGold
              stop.isCompleted -> Color(0xFF059669)
              else -> MaterialTheme.colorScheme.surfaceVariant
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when {
            stop.isCurrent -> Icons.Default.DirectionsBus
            stop.isCompleted -> Icons.Default.Check
            else -> Icons.Default.RadioButtonUnchecked
          },
          contentDescription = null,
          tint = if (stop.isCompleted || stop.isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
      }

      if (!isLast) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(44.dp)
            .background(
              if (stop.isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant
            )
        )
      }
    }

    // Stop Details
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (stop.isCurrent) SchoolNavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
      ),
      modifier = Modifier
        .weight(1f)
        .padding(bottom = if (isLast) 0.dp else 8.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stop.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = if (stop.isCurrent) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (stop.isCurrent) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurface
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (stop.isCurrent) {
              Text(
                text = "• Current Location",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SchoolGold)
              )
            }
            Text(
              text = "${String.format("%.4f", stop.latitude)}, ${String.format("%.4f", stop.longitude)}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = stop.scheduledTime,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (stop.studentCount > 0) {
              Text(
                text = "+${stop.studentCount} students",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onOpenInMaps,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open in Google Maps",
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

private fun launchDialer(context: Context, phoneNumber: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$phoneNumber")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun launchSms(context: Context, phoneNumber: String, message: String) {
  try {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse("sms:$phoneNumber")
      putExtra("sms_body", message)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}
