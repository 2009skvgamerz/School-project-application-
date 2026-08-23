package com.example.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
  driverProfile: DriverProfile,
  busRoutes: List<BusRoute>,
  selectedRouteId: String,
  onSelectRoute: (String) -> Unit,
  onAdvanceStop: (String) -> Unit,
  onUpdatePassengerStatus: (routeId: String, stopId: String, passengerId: String, status: PassengerBoardingStatus) -> Unit,
  onMarkAllBoarded: (routeId: String, stopId: String) -> Unit,
  onAddTemporaryStop: (routeId: String, stopName: String, scheduledTime: String, lat: Double, lng: Double, note: String?) -> Unit,
  onSkipStop: (routeId: String, stopId: String, reason: String) -> Unit,
  onBroadcastDelay: (routeId: String, delayMins: Int, reason: String) -> Unit,
  onUpdateVehicleAndRoute: (driverId: String, busNo: String, busReg: String, routeId: String) -> Unit,
  onOpenLiveMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeRoute = busRoutes.find { it.id == selectedRouteId } ?: busRoutes.firstOrNull()

  var showBusVehicleSelectorDialog by remember { mutableStateOf(false) }
  var showAddDetourStopDialog by remember { mutableStateOf(false) }
  var showSkipStopDialog by remember { mutableStateOf<BusStop?>(null) }
  var showBroadcastDelayDialog by remember { mutableStateOf(false) }
  var expandedStopId by remember { mutableStateOf<String?>(null) }

  // Auto-expand current active stop by default
  LaunchedEffect(activeRoute?.id) {
    expandedStopId = activeRoute?.stops?.find { it.isCurrent }?.id ?: activeRoute?.stops?.firstOrNull()?.id
  }

  val totalPassengers = activeRoute?.stops?.flatMap { it.passengers } ?: emptyList()
  val boardedCount = totalPassengers.count { it.boardingStatus == PassengerBoardingStatus.BOARDED }
  val waitingCount = totalPassengers.count { it.boardingStatus == PassengerBoardingStatus.WAITING }
  val absentCount = totalPassengers.count { it.boardingStatus == PassengerBoardingStatus.ABSENT }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("driver_dashboard_screen"),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // 1. DRIVER COCKPIT HERO BANNER (High-Visibility Deep Safety Palette)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              listOf(Color(0xFF9A3412), Color(0xFFC2410C), Color(0xFFEA580C))
            )
          )
          .statusBarsPadding()
          .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Top Bar: Driver Identity & Assigned Bus Switcher
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                )
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                  )
                }
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = driverProfile.user.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                  Surface(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "PILOT ACTIVE",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "DL: ${driverProfile.licenseNo} • ${driverProfile.shift}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f)
                )
              }
            }

            // Quick Change Bus Vehicle Button
            FilledTonalButton(
              onClick = { showBusVehicleSelectorDialog = true },
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFC2410C)
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier.testTag("driver_switch_bus_button")
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("Switch Bus", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          // Active Assigned Bus & Route Info Card
          Surface(
            color = Color.Black.copy(alpha = 0.25f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "TODAY'S OPERATING VEHICLE & ROUTE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = SchoolGoldLight
                  )
                )
                Text(
                  text = "${activeRoute?.routeNumber ?: driverProfile.assignedBusNo} • ${activeRoute?.busRegistration ?: driverProfile.busRegistration}",
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                  color = Color.White
                )
                Text(
                  text = activeRoute?.routeName ?: "Assigned Route",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.9f)
                )
              }

              // Live Map Shortcut
              IconButton(
                onClick = onOpenLiveMap,
                modifier = Modifier
                  .size(46.dp)
                  .background(Color.White.copy(alpha = 0.2f), CircleShape)
                  .testTag("driver_open_map_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Map,
                  contentDescription = "Open In-App Map",
                  tint = Color.White
                )
              }
            }
          }
        }
      }
    }

    // 2. QUICK COCKPIT ACTIONS (BIG TOUCH TARGETS FOR SAFETY WHILE AT STOPS)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Advance to Next Stop Button
          Button(
            onClick = {
              activeRoute?.let { onAdvanceStop(it.id) }
            },
            modifier = Modifier
              .weight(1.3f)
              .height(56.dp)
              .testTag("driver_advance_stop_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
          ) {
            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Depart Stop & Go Next", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }

          // Report Traffic Delay
          FilledTonalButton(
            onClick = { showBroadcastDelayDialog = true },
            modifier = Modifier
              .weight(0.9f)
              .height(56.dp)
              .testTag("driver_broadcast_delay_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFFFEF3C7),
              contentColor = Color(0xFFB45309)
            )
          ) {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Delay Alert", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        // Add Roadwork Detour / Temporary Stop Button
        OutlinedButton(
          onClick = { showAddDetourStopDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("driver_add_detour_stop_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C)),
          border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA580C))
          )
        ) {
          Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Text("+ Add Detour / Extra Stop (Road Work / Traffic)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        }

        // Active Detour Alert Banner
        if (!activeRoute?.activeDetourAlert.isNullOrBlank()) {
          Surface(
            color = Color(0xFFFFFBEB),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFD97706))
              Text(
                text = activeRoute?.activeDetourAlert ?: "",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF92400E)
              )
            }
          }
        }
      }
    }

    // 3. PASSENGER MANIFEST SUMMARY CHIPS
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Passenger Attendance Roster",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Total: ${totalPassengers.size} Registered",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              color = Color(0xFFD1FAE5),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("Boarded", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                Text("$boardedCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF047857))
              }
            }

            Surface(
              color = Color(0xFFFEF3C7),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("Waiting", style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E))
                Text("$waitingCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFD97706))
              }
            }

            Surface(
              color = Color(0xFFFEE2E2),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("Absent/Leave", style = MaterialTheme.typography.labelSmall, color = Color(0xFF991B1B))
                Text("$absentCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFDC2626))
              }
            }
          }
        }
      }
    }

    // 4. STOP-BY-STOP MANIFEST & PASSENGER CHECK-IN LIST
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Stops & Passenger Check-In",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = SchoolNavyPrimary
        )
        Text(
          text = "${activeRoute?.stops?.size ?: 0} Checkpoints",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    val stops = activeRoute?.stops ?: emptyList()
    items(stops) { stop ->
      val isExpanded = expandedStopId == stop.id
      val stopPassengers = stop.passengers

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("driver_stop_card_${stop.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = when {
            stop.isCurrent -> Color(0xFFFEF3C7)
            stop.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            stop.isSkipped -> Color(0xFFFEE2E2).copy(alpha = 0.6f)
            stop.isExtraDetourStop -> Color(0xFFEFF6FF)
            else -> MaterialTheme.colorScheme.surface
          }
        ),
        border = when {
          stop.isCurrent -> CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA580C))
          )
          stop.isExtraDetourStop -> CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF3B82F6))
          )
          else -> null
        },
        elevation = CardDefaults.cardElevation(if (stop.isCurrent) 4.dp else 1.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Stop Header (Click to expand/collapse manifest)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { expandedStopId = if (isExpanded) null else stop.id }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = when {
                  stop.isCurrent -> Color(0xFFEA580C)
                  stop.isCompleted -> Color(0xFF059669)
                  stop.isSkipped -> Color(0xFFDC2626)
                  stop.isExtraDetourStop -> Color(0xFF2563EB)
                  else -> MaterialTheme.colorScheme.outlineVariant
                }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  when {
                    stop.isCurrent -> Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    stop.isCompleted -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    stop.isSkipped -> Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    stop.isExtraDetourStop -> Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    else -> Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                  }
                }
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (stop.isExtraDetourStop) {
                    Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(4.dp)) {
                      Text("DETOUR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                  }
                }

                Text(
                  text = "Scheduled: ${stop.scheduledTime} • ${stopPassengers.size} Passengers",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (stop.isSkipped && !stop.skipReason.isNullOrBlank()) {
                  Text(
                    text = "Bypassed: ${stop.skipReason}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFFDC2626)
                  )
                }
              }
            }

            // Quick Stop Actions / Expand Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (stop.isCurrent && !stop.isCompleted) {
                Surface(
                  color = Color(0xFFEA580C),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = "CURRENT STOP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                  )
                }
              }

              IconButton(onClick = { expandedStopId = if (isExpanded) null else stop.id }) {
                Icon(
                  imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                  contentDescription = "Expand Manifest"
                )
              }
            }
          }

          // Expandable Passenger Roster & Stop Management Tools
          AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              // Stop Roster Batch Actions
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Passenger Manifest (${stopPassengers.size})",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = SchoolNavyPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  // Skip Stop Action
                  if (!stop.isCompleted && !stop.isSkipped) {
                    TextButton(
                      onClick = { showSkipStopDialog = stop },
                      colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626)),
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Skip Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }

                  // Mark All Boarded
                  if (stopPassengers.isNotEmpty()) {
                    FilledTonalButton(
                      onClick = { activeRoute?.let { onMarkAllBoarded(it.id, stop.id) } },
                      colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFD1FAE5),
                        contentColor = Color(0xFF065F46)
                      ),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Mark All Boarded", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              // Individual Passenger Cards with Parent Quick Dial
              if (stopPassengers.isEmpty()) {
                Text(
                  text = "No registered students/teachers assigned to this stop.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 8.dp)
                )
              } else {
                stopPassengers.forEach { passenger ->
                  Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                      brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      // Passenger Info
                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                          Text(
                            text = passenger.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Surface(
                            color = if (passenger.role == UserRole.TEACHER) Color(0xFFD1FAE5) else Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(4.dp)
                          ) {
                            Text(
                              text = if (passenger.role == UserRole.TEACHER) "FACULTY" else passenger.gradeAndSection,
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (passenger.role == UserRole.TEACHER) Color(0xFF065F46) else Color(0xFF1D4ED8),
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                          }
                        }

                        Text(
                          text = "Parent: ${passenger.parentName} (${passenger.parentPhone})",
                          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!passenger.checkInTime.isNullOrBlank()) {
                          Text(
                            text = "Checked in: ${passenger.checkInTime}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF059669))
                          )
                        }
                      }

                      // Actions: Boarded / Absent toggles + Direct Call
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        // Quick Call Parent Button
                        IconButton(
                          onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${passenger.parentPhone}"))
                            context.startActivity(intent)
                          },
                          modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0E7FF), CircleShape)
                        ) {
                          Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Parent",
                            tint = Color(0xFF4338CA),
                            modifier = Modifier.size(18.dp)
                          )
                        }

                        // Toggle Status (Waiting -> Boarded -> Absent)
                        Surface(
                          modifier = Modifier
                            .clickable {
                              val nextStatus = when (passenger.boardingStatus) {
                                PassengerBoardingStatus.WAITING -> PassengerBoardingStatus.BOARDED
                                PassengerBoardingStatus.BOARDED -> PassengerBoardingStatus.ABSENT
                                PassengerBoardingStatus.ABSENT -> PassengerBoardingStatus.WAITING
                                PassengerBoardingStatus.DROPPED_OFF -> PassengerBoardingStatus.WAITING
                              }
                              activeRoute?.let { onUpdatePassengerStatus(it.id, stop.id, passenger.id, nextStatus) }
                            },
                          shape = RoundedCornerShape(8.dp),
                          color = when (passenger.boardingStatus) {
                            PassengerBoardingStatus.BOARDED -> Color(0xFFD1FAE5)
                            PassengerBoardingStatus.WAITING -> Color(0xFFFEF3C7)
                            PassengerBoardingStatus.ABSENT -> Color(0xFFFEE2E2)
                            PassengerBoardingStatus.DROPPED_OFF -> Color(0xFFDBEAFE)
                          }
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                          ) {
                            Icon(
                              imageVector = when (passenger.boardingStatus) {
                                PassengerBoardingStatus.BOARDED -> Icons.Default.CheckCircle
                                PassengerBoardingStatus.WAITING -> Icons.Default.Schedule
                                PassengerBoardingStatus.ABSENT -> Icons.Default.Cancel
                                PassengerBoardingStatus.DROPPED_OFF -> Icons.Default.Place
                              },
                              contentDescription = null,
                              modifier = Modifier.size(14.dp),
                              tint = Color(passenger.boardingStatus.colorHex)
                            )
                            Text(
                              text = passenger.boardingStatus.label.split(" ").first(),
                              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                              color = Color(passenger.boardingStatus.colorHex)
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // ==================== DIALOGS ====================

  // 1. Bus Vehicle & Route Selector Dialog
  if (showBusVehicleSelectorDialog) {
    var selectedBusNo by remember { mutableStateOf(driverProfile.assignedBusNo) }
    var selectedBusReg by remember { mutableStateOf(driverProfile.busRegistration) }
    var selectedRoute by remember { mutableStateOf(selectedRouteId) }

    val busFleet = listOf(
      Triple("Bus #12", "KA-04-SJ-1012", "route_12"),
      Triple("Bus #05", "KA-04-SJ-1005", "route_05"),
      Triple("Bus #08", "KA-04-SJ-1008", "route_08"),
      Triple("Bus #15", "KA-04-SJ-1015", "route_15")
    )

    AlertDialog(
      onDismissRequest = { showBusVehicleSelectorDialog = false },
      icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFFEA580C)) },
      title = { Text("Select Today's Bus & Route", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Select the school bus vehicle you are piloting today. The app will sync your GPS beacon and manifest automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          busFleet.forEach { (busNo, reg, routeId) ->
            val isSelected = selectedRoute == routeId
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedBusNo = busNo
                  selectedBusReg = reg
                  selectedRoute = routeId
                },
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA580C))
              ) else null
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = "$busNo • $reg",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurface
                  )
                  val rName = busRoutes.find { it.id == routeId }?.routeName ?: "Route"
                  Text(text = rName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isSelected) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFEA580C))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateVehicleAndRoute(driverProfile.driverId, selectedBusNo, selectedBusReg, selectedRoute)
            onSelectRoute(selectedRoute)
            showBusVehicleSelectorDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
        ) {
          Text("Confirm Vehicle & Route")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBusVehicleSelectorDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 2. Add Roadwork Detour / Extra Stop Dialog
  if (showAddDetourStopDialog) {
    var stopName by remember { mutableStateOf("") }
    var stopTime by remember { mutableStateOf("07:50 AM") }
    var detourNote by remember { mutableStateOf("Road construction on main street - Temporary pickup point") }

    AlertDialog(
      onDismissRequest = { showAddDetourStopDialog = false },
      icon = { Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color(0xFF2563EB)) },
      title = { Text("Add Detour / Extra Stop", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Add a temporary stop due to roadwork, fallen tree, or special event. Parents and students will receive an instant notification.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = stopName,
            onValueChange = { stopName = it },
            label = { Text("Stop / Landmark Name") },
            placeholder = { Text("e.g. 2nd Cross Road Junction") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = stopTime,
            onValueChange = { stopTime = it },
            label = { Text("Estimated Pickup Time") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = detourNote,
            onValueChange = { detourNote = it },
            label = { Text("Reason for Detour") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (stopName.isNotBlank() && activeRoute != null) {
              onAddTemporaryStop(
                activeRoute.id,
                stopName.trim(),
                stopTime.trim(),
                activeRoute.currentLatitude + 0.005,
                activeRoute.currentLongitude + 0.005,
                detourNote.trim()
              )
              showAddDetourStopDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
          Text("Broadcast & Add Stop")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDetourStopDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 3. Skip Stop Dialog
  showSkipStopDialog?.let { stopToSkip ->
    var skipReason by remember { mutableStateOf("Road closed for metro construction") }

    AlertDialog(
      onDismissRequest = { showSkipStopDialog = null },
      icon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626)) },
      title = { Text("Skip ${stopToSkip.name}?", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Are you sure you want to bypass this stop? Parents of waiting students will be notified to proceed to the next nearest checkpoint.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = skipReason,
            onValueChange = { skipReason = it },
            label = { Text("Reason for Skipping") },
            placeholder = { Text("e.g. Waterlogging / Road Blocked") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (activeRoute != null) {
              onSkipStop(activeRoute.id, stopToSkip.id, skipReason.trim())
              showSkipStopDialog = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Confirm Skip Stop")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSkipStopDialog = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // 4. Broadcast Traffic Delay Dialog
  if (showBroadcastDelayDialog) {
    var delayMinutes by remember { mutableStateOf(10) }
    var delayReason by remember { mutableStateOf("Heavy peak hour traffic at Silk Board / Metro junction") }

    AlertDialog(
      onDismissRequest = { showBroadcastDelayDialog = false },
      icon = { Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFD97706)) },
      title = { Text("Broadcast Delay Alert", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Notify all parents and campus coordinators along this route about delays.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text("Estimated Delay Time: $delayMinutes Mins", fontWeight = FontWeight.Bold)
          Slider(
            value = delayMinutes.toFloat(),
            onValueChange = { delayMinutes = it.toInt() },
            valueRange = 5f..45f,
            steps = 7
          )

          OutlinedTextField(
            value = delayReason,
            onValueChange = { delayReason = it },
            label = { Text("Cause of Delay") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (activeRoute != null) {
              onBroadcastDelay(activeRoute.id, delayMinutes, delayReason.trim())
              showBroadcastDelayDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
        ) {
          Text("Send Alert Broadcast")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBroadcastDelayDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
