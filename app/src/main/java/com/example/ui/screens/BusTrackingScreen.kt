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
import androidx.compose.material.icons.outlined.*
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
import com.example.model.BusRoute
import com.example.model.BusStatus
import com.example.model.BusStop
import com.example.model.StudentProfile
import com.example.model.UserRole
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusTrackingScreen(
  routes: List<BusRoute>,
  selectedRouteId: String,
  studentProfile: StudentProfile?,
  userRole: UserRole,
  onSelectRoute: (String) -> Unit,
  onSimulateMovement: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()

  var isAutoSimulating by remember { mutableStateOf(false) }

  // Auto-simulation ticker
  LaunchedEffect(isAutoSimulating, selectedRouteId) {
    while (isAutoSimulating) {
      kotlinx.coroutines.delay(2500)
      onSimulateMovement(selectedRouteId)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("bus_tracking_screen")
  ) { paddingValues ->
    if (activeRoute == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No bus routes configured")
      }
      return@Scaffold
    }

    val statusColor = Color(activeRoute.status.colorHex)

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("bus_tracking_lazy_column"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. ROUTE SELECTOR CHIPS
      item(key = "route_selector_row") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "SELECT BUS ROUTE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(routes, key = { it.id }) { route ->
              val isSelected = route.id == selectedRouteId
              val isStudentAssigned = studentProfile?.busRoute?.contains(route.routeNumber) == true

              FilterChip(
                selected = isSelected,
                onClick = { onSelectRoute(route.id) },
                label = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = route.routeNumber,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isStudentAssigned) {
                      Surface(
                        color = SchoolGold.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                      ) {
                        Text(
                          text = "YOUR BUS",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                          ),
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                      }
                    }
                  }
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                },
                modifier = Modifier.testTag("chip_route_${route.id}")
              )
            }
          }
        }
      }

      // 2. LIVE GPS RADAR HERO CARD
      item(key = "live_radar_card") {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("bus_live_radar_card"),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(SchoolNavyDark, SchoolNavyPrimary)
                )
              )
              .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              // Header & Status Pill
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = activeRoute.routeNumber,
                      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                      color = Color.White
                    )
                    Surface(
                      color = Color.White.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = activeRoute.busRegistration,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                  Text(
                    text = activeRoute.routeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Surface(
                  color = statusColor,
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                    )
                    Text(
                      text = activeRoute.status.label.uppercase(),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                      ),
                      color = Color.White
                    )
                  }
                }
              }

              // Custom Animated Live Radar Canvas
              LiveRouteRadarCanvas(
                progressPercent = activeRoute.progressPercent,
                currentSpeed = activeRoute.currentSpeedKmH,
                status = activeRoute.status,
                stopsCount = activeRoute.stops.size,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
              )

              // Live Telemetry Stats Strip
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.White.copy(alpha = 0.12f))
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                BusTelemetryMetric(
                  label = "Speed",
                  value = "${activeRoute.currentSpeedKmH} km/h",
                  icon = Icons.Default.Speed
                )
                VerticalDivider(modifier = Modifier.height(28.dp), color = Color.White.copy(alpha = 0.2f))
                BusTelemetryMetric(
                  label = "Next Stop ETA",
                  value = "${activeRoute.estimatedArrivalMins} mins",
                  icon = Icons.Default.Timer
                )
                VerticalDivider(modifier = Modifier.height(28.dp), color = Color.White.copy(alpha = 0.2f))
                BusTelemetryMetric(
                  label = "Onboard",
                  value = "${activeRoute.studentsOnboard}/${activeRoute.capacity}",
                  icon = Icons.Default.Groups
                )
              }

              // Next Stop Alert Box
              Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = SchoolGold,
                    modifier = Modifier.size(20.dp)
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "APPROACHING NEXT STOP",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.sp
                      ),
                      color = SchoolGold
                    )
                    Text(
                      text = activeRoute.nextStopName,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }

              // Live Simulation Controls
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { onSimulateMovement(activeRoute.id) },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                  border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Color.White, Color.White))
                  ),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("simulate_step_btn")
                ) {
                  Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Step GPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                  onClick = { isAutoSimulating = !isAutoSimulating },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAutoSimulating) Color(0xFFDC2626) else SchoolGold,
                    contentColor = if (isAutoSimulating) Color.White else SchoolNavyDark
                  ),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("auto_simulate_toggle_btn")
                ) {
                  Icon(
                    imageVector = if (isAutoSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (isAutoSimulating) "Pause Live" else "Live Radar",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
            }
          }
        }
      }

      // 3. DRIVER & ATTENDANT CONTACT CARDS
      item(key = "driver_contacts_section") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "DESIGNATED BUS CREW",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Driver Card
          BusCrewContactCard(
            title = "Assigned Bus Driver",
            name = activeRoute.driverName,
            phoneNumber = activeRoute.driverPhone,
            icon = Icons.Default.AirlineSeatReclineNormal,
            onCall = { launchDialer(context, activeRoute.driverPhone) },
            testTag = "call_driver_btn"
          )

          // Attendant Card
          BusCrewContactCard(
            title = "Bus Safety Attendant",
            name = activeRoute.attendantName,
            phoneNumber = activeRoute.attendantPhone,
            icon = Icons.Default.PersonOutline,
            onCall = { launchDialer(context, activeRoute.attendantPhone) },
            testTag = "call_attendant_btn"
          )
        }
      }

      // 4. STOP-BY-STOP ROUTE TIMELINE
      item(key = "route_stops_header") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ROUTE STOPS & SCHEDULE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${activeRoute.stops.size} Stops Total",
            style = MaterialTheme.typography.bodySmall,
            color = SchoolNavyPrimary
          )
        }
      }

      items(activeRoute.stops, key = { it.id }) { stop ->
        BusStopTimelineItem(
          stop = stop,
          isLast = stop == activeRoute.stops.last()
        )
      }

      // 5. HELPLINE FOOTER CARD
      item(key = "transport_helpline_card") {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("transport_helpline_card")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.SupportAgent, contentDescription = null, tint = SchoolNavyPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "School Transport Helpdesk",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "For emergency route queries & lost items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            FilledTonalButton(
              onClick = { launchDialer(context, "+91 98450 99001") },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Helpdesk")
            }
          }
        }
      }
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
private fun BusTelemetryMetric(
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
      Icon(imageVector = icon, contentDescription = null, tint = SchoolGold, modifier = Modifier.size(14.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = Color.White.copy(alpha = 0.75f)
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = Color.White
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
  isLast: Boolean
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
            .height(38.dp)
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
          if (stop.isCurrent) {
            Text(
              text = "• Current Checkpoint",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SchoolGold)
            )
          }
        }

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
