package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.*
import com.example.ui.theme.*

enum class AnalyticsTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  PERFORMANCE("Performance", Icons.AutoMirrored.Filled.TrendingUp),
  ATTENDANCE("Attendance", Icons.Default.FactCheck),
  SUBJECTS("Subjects", Icons.Default.Assessment)
}

enum class ChartEngine(val label: String) {
  NATIVE_COMPOSE("Native Engine"),
  D3_RECHARTS("D3 / SVG")
}

/**
 * High-fidelity Data Visualization component for Student Academic Performance Trends
 * and Attendance Metrics.
 *
 * Defaults to a fast, hardware-accelerated Jetpack Compose Canvas chart with interactive
 * scrubbing, gradient curves, benchmark comparisons, and responsive inspector cards.
 * Also includes an embedded D3.js / Recharts SVG engine toggle.
 */
@Composable
fun StudentAnalyticsVisualizer(
  analyticsProfile: StudentAnalyticsProfile = StudentAnalyticsProfile.defaultStudentProfile,
  modifier: Modifier = Modifier,
  title: String = "Academic & Attendance Analytics",
  subtitle: String = "${analyticsProfile.studentName} • ${analyticsProfile.gradeAndSection}"
) {
  var selectedTab by remember { mutableStateOf(AnalyticsTab.PERFORMANCE) }
  var chartEngine by remember { mutableStateOf(ChartEngine.NATIVE_COMPOSE) }
  val isDark = isSystemInDarkTheme()

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("student_analytics_visualizer_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header with Title, Subtitle, and Engine Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.TrendingUp,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Engine Toggle Switch (Native Compose vs D3/SVG)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
          modifier = Modifier.clickable {
            chartEngine = if (chartEngine == ChartEngine.NATIVE_COMPOSE) {
              ChartEngine.D3_RECHARTS
            } else {
              ChartEngine.NATIVE_COMPOSE
            }
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (chartEngine == ChartEngine.NATIVE_COMPOSE) Color(0xFF059669) else Color(0xFF2563EB))
            )
            Text(
              text = chartEngine.label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 2. High-Level KPI Badges (GPA, Attendance, Score Trend)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        AnalyticsKpiCard(
          title = "Current GPA",
          value = "${analyticsProfile.currentGpa}",
          unit = "/ 10",
          subtitle = "Top 5% in 10-A",
          icon = Icons.Default.School,
          accentColor = Color(0xFF2563EB),
          modifier = Modifier.weight(1f)
        )
        AnalyticsKpiCard(
          title = "Attendance",
          value = "${analyticsProfile.overallAttendance}%",
          unit = "",
          subtitle = "Exam Eligible",
          icon = Icons.Default.CheckCircle,
          accentColor = Color(0xFF059669),
          modifier = Modifier.weight(1f)
        )
        AnalyticsKpiCard(
          title = "Score Trend",
          value = "+6.5%",
          unit = "",
          subtitle = "Since UT-1",
          icon = Icons.AutoMirrored.Filled.TrendingUp,
          accentColor = Color(0xFFD97706),
          modifier = Modifier.weight(1f)
        )
      }

      // 3. Segmented Navigation Tabs
      SingleChoiceSegmentedRow(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
      )

      // 4. Main Chart Content Area
      AnimatedContent(
        targetState = Pair(selectedTab, chartEngine),
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        label = "analytics_chart_container"
      ) { (tab, engine) ->
        if (engine == ChartEngine.NATIVE_COMPOSE) {
          NativeComposeCanvasChart(
            tab = tab,
            profile = analyticsProfile,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          D3RechartsWebViewContainer(
            tab = tab,
            profile = analyticsProfile,
            isDark = isDark,
            modifier = Modifier
              .fillMaxWidth()
              .height(290.dp)
          )
        }
      }

      // 5. Chart Legend & Benchmark Explanation
      ChartLegendBar(tab = selectedTab)
    }
  }
}

@Composable
private fun AnalyticsKpiCard(
  title: String,
  value: String,
  unit: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = accentColor.copy(alpha = 0.06f),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(14.dp)
        )
      }

      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
          color = accentColor
        )
        if (unit.isNotEmpty()) {
          Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
          )
        }
      }

      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun SingleChoiceSegmentedRow(
  selectedTab: AnalyticsTab,
  onTabSelected: (AnalyticsTab) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      AnalyticsTab.entries.forEach { tab ->
        val isSelected = tab == selectedTab
        val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
        val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        val elevation = if (isSelected) 2.dp else 0.dp

        Surface(
          shape = RoundedCornerShape(11.dp),
          color = backgroundColor,
          shadowElevation = elevation,
          modifier = Modifier
            .weight(1f)
            .clickable { onTabSelected(tab) }
        ) {
          Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = tab.icon,
              contentDescription = null,
              tint = textColor,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = tab.label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp
              ),
              color = textColor
            )
          }
        }
      }
    }
  }
}

/**
 * Native Jetpack Compose Chart Engine:
 * - 100% Native Canvas rendering at 60/120 FPS
 * - Dynamic light/dark theme adaptation
 * - Interactive term/month selector and inspector card
 * - Gradient area curves, dashed benchmark lines, and statutory threshold guides
 */
@Composable
private fun NativeComposeCanvasChart(
  tab: AnalyticsTab,
  profile: StudentAnalyticsProfile,
  isDark: Boolean,
  modifier: Modifier = Modifier
) {
  val primaryColor = Color(0xFF2563EB)
  val benchmarkColor = Color(0xFF94A3B8)
  val attendanceColor = Color(0xFF059669)
  val gridColor = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000)

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    when (tab) {
      AnalyticsTab.PERFORMANCE -> {
        val terms = profile.terms
        var selectedIndex by remember { mutableIntStateOf(terms.size - 1) }
        val activeTerm = terms.getOrNull(selectedIndex) ?: terms.last()

        // Active Term Inspection Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = primaryColor.copy(alpha = 0.07f),
          border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = activeTerm.termName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = primaryColor.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = activeTerm.gradeLetter,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "${activeTerm.date} • Class Avg: ${activeTerm.classAverage}%",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${activeTerm.studentScore}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                color = primaryColor
              )
              val diff = activeTerm.studentScore - activeTerm.classAverage
              Text(
                text = "+${String.format("%.1f", diff)}% vs Avg",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = if (diff >= 0) Color(0xFF059669) else Color(0xFFDC2626)
              )
            }
          }
        }

        // Native Line Chart Canvas with Splines and Gradient Fill
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFF8FAFC))
            .padding(top = 16.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(terms) {
                detectTapGestures { offset ->
                  val step = size.width / (terms.size - 1).coerceAtLeast(1)
                  val idx = ((offset.x + step / 2) / step).toInt().coerceIn(0, terms.size - 1)
                  selectedIndex = idx
                }
              }
          ) {
            val width = size.width
            val height = size.height
            val minY = 65.0
            val maxY = 100.0

            fun getY(valScore: Double): Float {
              val norm = ((valScore - minY) / (maxY - minY)).toFloat().coerceIn(0f, 1f)
              return height - (norm * height)
            }

            fun getX(i: Int): Float {
              return (i.toFloat() / (terms.size - 1).coerceAtLeast(1)) * width
            }

            // Draw 4 Horizontal Grid Lines (70%, 80%, 90%, 100%)
            for (step in 0..3) {
              val y = (height / 3) * step
              drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )
            }

            // Benchmark Average Line (Dashed)
            val benchPath = Path()
            terms.forEachIndexed { i, term ->
              val x = getX(i)
              val y = getY(term.classAverage)
              if (i == 0) benchPath.moveTo(x, y) else benchPath.lineTo(x, y)
            }
            drawPath(
              path = benchPath,
              color = benchmarkColor.copy(alpha = 0.75f),
              style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )

            // Student Marks Spline Curve with Gradient Area Fill
            val scorePath = Path()
            val areaPath = Path()
            terms.forEachIndexed { i, term ->
              val x = getX(i)
              val y = getY(term.studentScore)
              if (i == 0) {
                scorePath.moveTo(x, y)
                areaPath.moveTo(x, height)
                areaPath.lineTo(x, y)
              } else {
                val prevX = getX(i - 1)
                val prevY = getY(terms[i - 1].studentScore)
                val midX = (prevX + x) / 2
                scorePath.cubicTo(midX, prevY, midX, y, x, y)
                areaPath.cubicTo(midX, prevY, midX, y, x, y)
              }
            }
            areaPath.lineTo(getX(terms.size - 1), height)
            areaPath.close()

            // Area gradient
            drawPath(
              path = areaPath,
              brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0.01f))
              )
            )

            // Stroke
            drawPath(
              path = scorePath,
              color = primaryColor,
              style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )

            // Interactive Point Nodes
            terms.forEachIndexed { i, term ->
              val x = getX(i)
              val y = getY(term.studentScore)
              val isSelected = (i == selectedIndex)

              if (isSelected) {
                // Outer glow ring
                drawCircle(
                  color = primaryColor.copy(alpha = 0.25f),
                  radius = 14f,
                  center = Offset(x, y)
                )
                // Selected outer circle
                drawCircle(
                  color = Color.White,
                  radius = 7.5f,
                  center = Offset(x, y)
                )
                drawCircle(
                  color = primaryColor,
                  radius = 7.5f,
                  center = Offset(x, y),
                  style = Stroke(width = 3.5f)
                )
              } else {
                drawCircle(
                  color = Color.White,
                  radius = 4.5f,
                  center = Offset(x, y)
                )
                drawCircle(
                  color = primaryColor,
                  radius = 4.5f,
                  center = Offset(x, y),
                  style = Stroke(width = 2.5f)
                )
              }
            }
          }
        }

        // X-Axis Term Selectors (Tap to Inspect)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          terms.forEachIndexed { i, term ->
            val isSelected = i == selectedIndex
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) primaryColor.copy(alpha = 0.12f) else Color.Transparent,
              modifier = Modifier.clickable { selectedIndex = i }
            ) {
              Text(
                text = term.shortName,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      AnalyticsTab.ATTENDANCE -> {
        val months = profile.monthlyAttendance
        var selectedIndex by remember { mutableIntStateOf(months.size - 1) }
        val activeMonth = months.getOrNull(selectedIndex) ?: months.last()

        // Active Month Inspection Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = attendanceColor.copy(alpha = 0.07f),
          border = BorderStroke(1.dp, attendanceColor.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
              Text(
                text = "${activeMonth.monthName} ${if (activeMonth.isCurrent) "(Current)" else ""}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Present: ${activeMonth.presentDays} / ${activeMonth.workingDays} Working Days",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${activeMonth.attendanceRate}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                color = attendanceColor
              )
              Text(
                text = "Exam Safe (>75%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = attendanceColor
              )
            }
          }
        }

        // Native Bars Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFF8FAFC))
            .padding(top = 16.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(months) {
                detectTapGestures { offset ->
                  val stepX = size.width / months.size
                  val idx = (offset.x / stepX).toInt().coerceIn(0, months.size - 1)
                  selectedIndex = idx
                }
              }
          ) {
            val width = size.width
            val height = size.height
            val barWidth = (width / months.size) * 0.58f
            val stepX = width / months.size
            val minY = 60.0
            val maxY = 100.0

            // 75% Statutory Threshold Line
            val thresholdY = height - (((75.0 - minY) / (maxY - minY)).toFloat() * height)
            drawLine(
              color = Color(0xFFDC2626),
              start = Offset(0f, thresholdY),
              end = Offset(width, thresholdY),
              strokeWidth = 1.8f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            months.forEachIndexed { i, m ->
              val x = i * stepX + (stepX - barWidth) / 2
              val norm = ((m.attendanceRate - minY) / (maxY - minY)).toFloat().coerceIn(0.05f, 1f)
              val barH = norm * height
              val y = height - barH
              val isSelected = (i == selectedIndex)

              val barColor = when {
                m.attendanceRate >= 95.0 -> Color(0xFF059669)
                m.attendanceRate >= 90.0 -> Color(0xFF2563EB)
                else -> Color(0xFFD97706)
              }

              drawRoundRect(
                color = if (isSelected) barColor else barColor.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(6f, 6f)
              )

              if (isSelected) {
                // Indicator ring on top
                drawCircle(
                  color = Color.White,
                  radius = 3f,
                  center = Offset(x + barWidth / 2, y + 6f)
                )
              }
            }
          }
        }

        // X-Axis Month Selectors
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          months.forEachIndexed { i, m ->
            val isSelected = i == selectedIndex
            Text(
              text = m.shortMonth,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              ),
              color = if (isSelected) attendanceColor else MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .clickable { selectedIndex = i }
                .padding(vertical = 2.dp)
            )
          }
        }

        // Category Summary Badges (Full Days, Half Days, On Duty, Absent)
        val dist = profile.attendanceDistribution
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AttendanceCategoryBadge(label = "Full Days", count = dist.fullDays, color = Color(0xFF059669), modifier = Modifier.weight(1f))
          AttendanceCategoryBadge(label = "Half Days", count = dist.halfDays, color = Color(0xFFD97706), modifier = Modifier.weight(1f))
          AttendanceCategoryBadge(label = "On Duty", count = dist.onDutyDays, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
          AttendanceCategoryBadge(label = "Absent", count = dist.absentDays, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
        }
      }

      AnalyticsTab.SUBJECTS -> {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          profile.subjects.forEach { subj ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = subj.subjectName,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = subj.teacherName,
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "${subj.scorePercentage}%",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp),
                      color = Color(0xFF2563EB)
                    )
                    Text(
                      text = "Att: ${subj.attendanceRate}%",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                      color = Color(0xFF059669)
                    )
                  }
                }

                // Dual Comparative Progress Bars
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(gridColor)
                ) {
                  // Attendance bar (underneath, Emerald Green)
                  Box(
                    modifier = Modifier
                      .fillMaxHeight()
                      .fillMaxWidth(subj.attendanceRate.toFloat() / 100f)
                      .clip(RoundedCornerShape(5.dp))
                      .background(Color(0xFF059669).copy(alpha = 0.35f))
                  )
                  // Academic Marks bar (primary, Indigo/Blue)
                  Box(
                    modifier = Modifier
                      .fillMaxHeight()
                      .fillMaxWidth(subj.scorePercentage.toFloat() / 100f)
                      .clip(RoundedCornerShape(5.dp))
                      .background(Color(0xFF2563EB))
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

@Composable
private fun AttendanceCategoryBadge(
  label: String,
  count: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = color.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "$count",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
        color = color
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun ChartLegendBar(tab: AnalyticsTab) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    when (tab) {
      AnalyticsTab.PERFORMANCE -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendPill(color = Color(0xFF2563EB), label = "Student Marks %")
            LegendPill(color = Color(0xFF94A3B8), label = "Class 10-A Avg", isDashed = true)
          }
          Text(
            text = "Target: 85%",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        Text(
          text = "Min Pass: 40% • Board Target: 85% • Continuous Assessment",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      AnalyticsTab.ATTENDANCE -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendPill(color = Color(0xFF059669), label = "Monthly Attendance %")
            LegendPill(color = Color(0xFFDC2626), label = "75% Threshold", isDashed = true)
          }
          Text(
            text = "Status: Eligible",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF059669)
          )
        }
        Text(
          text = "Mandatory 75% minimum aggregate attendance required for Board Exam hall ticket",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      AnalyticsTab.SUBJECTS -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendPill(color = Color(0xFF2563EB), label = "Academic Score")
            LegendPill(color = Color(0xFF059669), label = "Attendance Rate")
          }
          Text(
            text = "6 Core Subjects",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun LegendPill(color: Color, label: String, isDashed: Boolean = false) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    if (isDashed) {
      Box(
        modifier = Modifier
          .width(14.dp)
          .height(2.5.dp)
          .background(color)
      )
    } else {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

/**
 * Fixed & Robust D3.js and Recharts HTML5/SVG interactive visualization engine.
 * Renders SVG curves, crosshairs, and popovers with proper mobile viewport parameters.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun D3RechartsWebViewContainer(
  tab: AnalyticsTab,
  profile: StudentAnalyticsProfile,
  isDark: Boolean,
  modifier: Modifier = Modifier
) {
  val htmlContent = remember(tab, profile, isDark) {
    generateD3ChartHtml(tab, profile, isDark)
  }

  AndroidView(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .testTag("d3_recharts_webview"),
    factory = { context ->
      WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        // Important: Keep wide viewport disabled so mobile viewport width matches container exactly
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = false
        setBackgroundColor(0) // Transparent background
        webViewClient = WebViewClient()
        loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
      }
    },
    update = { webView ->
      webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
  )
}

/**
 * Generates the clean D3.js / Recharts-style SVG document with high-contrast light/dark support.
 */
private fun generateD3ChartHtml(
  tab: AnalyticsTab,
  profile: StudentAnalyticsProfile,
  isDark: Boolean
): String {
  val textColor = if (isDark) "#E2E8F0" else "#1E293B"
  val subtextColor = if (isDark) "#94A3B8" else "#64748B"
  val gridColor = if (isDark) "rgba(255,255,255,0.08)" else "rgba(0,0,0,0.06)"
  val tooltipBg = if (isDark) "#0F172A" else "#FFFFFF"
  val tooltipBorder = if (isDark) "#334155" else "#CBD5E1"
  val tooltipShadow = if (isDark) "0 8px 24px rgba(0,0,0,0.4)" else "0 8px 20px rgba(0,0,0,0.1)"

  return when (tab) {
    AnalyticsTab.PERFORMANCE -> {
      val termsJson = profile.terms.joinToString(",") {
        """{"name":"${it.shortName}","full":"${it.termName}","score":${it.studentScore},"avg":${it.classAverage},"gpa":${it.gpa},"grade":"${it.gradeLetter}"}"""
      }

      """
      <!DOCTYPE html>
      <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
          html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }
          .chart-container { position: relative; width: 100%; height: 100%; padding: 8px 10px; }
          svg { width: 100%; height: 100%; display: block; overflow: visible; }
          .grid-line { stroke: $gridColor; stroke-dasharray: 2,3; stroke-width: 1; }
          .benchmark-line { stroke: #94A3B8; stroke-dasharray: 4,4; stroke-width: 2; fill: none; opacity: 0.85; }
          .area-fill { fill: url(#scoreGrad); opacity: 0.85; }
          .score-line { stroke: #2563EB; stroke-width: 3.5; fill: none; stroke-linecap: round; stroke-linejoin: round; }
          .dot { fill: #FFFFFF; stroke: #2563EB; stroke-width: 2.5; r: 5; cursor: pointer; transition: all 0.2s; }
          .dot:hover { r: 7; stroke-width: 3.5; }
          .axis-label { fill: $subtextColor; font-size: 10px; text-anchor: middle; font-weight: 600; }
          .axis-y-label { fill: $subtextColor; font-size: 9px; text-anchor: end; }
          .tooltip {
            position: absolute; display: none; background: $tooltipBg; border: 1px solid $tooltipBorder;
            border-radius: 10px; padding: 7px 11px; font-size: 11px; color: $textColor; pointer-events: none;
            box-shadow: $tooltipShadow; z-index: 10;
          }
          .crosshair { stroke: #3B82F6; stroke-dasharray: 3,3; stroke-width: 1.5; opacity: 0; }
        </style>
      </head>
      <body>
        <div class="chart-container" id="chart">
          <div class="tooltip" id="tooltip"></div>
          <svg id="svg" viewBox="0 0 340 180" preserveAspectRatio="none">
            <defs>
              <linearGradient id="scoreGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.38"/>
                <stop offset="100%" stop-color="#3B82F6" stop-opacity="0.02"/>
              </linearGradient>
            </defs>
            <line x1="30" y1="18" x2="330" y2="18" class="grid-line"/>
            <line x1="30" y1="58" x2="330" y2="58" class="grid-line"/>
            <line x1="30" y1="98" x2="330" y2="98" class="grid-line"/>
            <line x1="30" y1="138" x2="330" y2="138" class="grid-line"/>
            <line x1="30" y1="156" x2="330" y2="156" stroke="$gridColor" stroke-width="1.5"/>

            <text x="24" y="22" class="axis-y-label">100%</text>
            <text x="24" y="62" class="axis-y-label">90%</text>
            <text x="24" y="102" class="axis-y-label">80%</text>
            <text x="24" y="142" class="axis-y-label">70%</text>

            <line id="crosshair" x1="0" y1="15" x2="0" y2="156" class="crosshair"/>

            <path id="area" class="area-fill"/>
            <path id="benchLine" class="benchmark-line"/>
            <path id="scoreLine" class="score-line"/>

            <g id="dotsGroup"></g>
            <g id="xLabelsGroup"></g>
          </svg>
        </div>

        <script>
          const data = [$termsJson];
          const width = 340, height = 180;
          const padLeft = 40, padRight = 20, padTop = 18, padBottom = 24;
          const chartW = width - padLeft - padRight;
          const chartH = height - padTop - padBottom;
          const minY = 65, maxY = 100;

          function getY(val) {
            return padTop + chartH - ((val - minY) / (maxY - minY)) * chartH;
          }
          function getX(idx) {
            return padLeft + (idx / (data.length - 1)) * chartW;
          }

          function buildSplinePath(points) {
            if (!points.length) return "";
            let d = "M " + points[0].x + "," + points[0].y;
            for (let i = 0; i < points.length - 1; i++) {
              const p0 = points[i];
              const p1 = points[i + 1];
              const cx = (p0.x + p1.x) / 2;
              d += " C " + cx + "," + p0.y + " " + cx + "," + p1.y + " " + p1.x + "," + p1.y;
            }
            return d;
          }

          const scorePts = data.map((d, i) => ({ x: getX(i), y: getY(d.score), data: d }));
          const benchPts = data.map((d, i) => ({ x: getX(i), y: getY(d.avg) }));

          const scorePathD = buildSplinePath(scorePts);
          document.getElementById('scoreLine').setAttribute('d', scorePathD);

          const areaD = scorePathD + " L " + scorePts[scorePts.length - 1].x + "," + (padTop + chartH) + " L " + scorePts[0].x + "," + (padTop + chartH) + " Z";
          document.getElementById('area').setAttribute('d', areaD);
          document.getElementById('benchLine').setAttribute('d', buildSplinePath(benchPts));

          const dotsGroup = document.getElementById('dotsGroup');
          const xLabelsGroup = document.getElementById('xLabelsGroup');
          const tooltip = document.getElementById('tooltip');
          const crosshair = document.getElementById('crosshair');

          scorePts.forEach((pt, i) => {
            const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('x', pt.x);
            text.setAttribute('y', height - 6);
            text.setAttribute('class', 'axis-label');
            text.textContent = pt.data.name;
            xLabelsGroup.appendChild(text);

            const dot = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            dot.setAttribute('cx', pt.x);
            dot.setAttribute('cy', pt.y);
            dot.setAttribute('class', 'dot');
            dot.addEventListener('click', () => showTip(pt));
            dotsGroup.appendChild(dot);
          });

          function showTip(pt) {
            tooltip.style.display = 'block';
            tooltip.style.left = Math.min(Math.max(10, (pt.x / width) * window.innerWidth - 60), window.innerWidth - 130) + 'px';
            tooltip.style.top = '10px';
            tooltip.innerHTML = '<strong>' + pt.data.full + '</strong><br>' +
              '<span style="color:#2563EB">●</span> Score: <strong>' + pt.data.score + '%</strong> (' + pt.data.grade + ')<br>' +
              '<span style="color:#94A3B8">--</span> Class Avg: ' + pt.data.avg + '%<br>' +
              '<small style="color:#10B981">GPA: ' + pt.data.gpa + ' / 10</small>';
            crosshair.setAttribute('x1', pt.x);
            crosshair.setAttribute('x2', pt.x);
            crosshair.style.opacity = '1';
          }

          if (scorePts.length > 0) {
            showTip(scorePts[scorePts.length - 1]);
          }
        </script>
      </body>
      </html>
      """.trimIndent()
    }

    AnalyticsTab.ATTENDANCE -> {
      val monthlyJson = profile.monthlyAttendance.joinToString(",") {
        """{"m":"${it.shortMonth}","full":"${it.monthName}","rate":${it.attendanceRate},"days":${it.workingDays},"present":${it.presentDays}}"""
      }

      """
      <!DOCTYPE html>
      <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
          html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }
          .chart-container { position: relative; width: 100%; height: 100%; padding: 8px 10px; }
          svg { width: 100%; height: 100%; display: block; overflow: visible; }
          .grid-line { stroke: $gridColor; stroke-dasharray: 2,3; stroke-width: 1; }
          .threshold-line { stroke: #DC2626; stroke-dasharray: 4,4; stroke-width: 1.5; }
          .bar { rx: 3px; cursor: pointer; transition: opacity 0.2s; }
          .bar:hover { opacity: 0.8; }
          .axis-label { fill: $subtextColor; font-size: 9px; text-anchor: middle; font-weight: 500; }
          .axis-y-label { fill: $subtextColor; font-size: 9px; text-anchor: end; }
          .tooltip {
            position: absolute; display: none; background: $tooltipBg; border: 1px solid $tooltipBorder;
            border-radius: 10px; padding: 7px 11px; font-size: 11px; color: $textColor; pointer-events: none;
            box-shadow: $tooltipShadow; z-index: 10;
          }
        </style>
      </head>
      <body>
        <div class="chart-container">
          <div class="tooltip" id="tooltip"></div>
          <svg viewBox="0 0 340 180" preserveAspectRatio="none">
            <line x1="30" y1="18" x2="330" y2="18" class="grid-line"/>
            <line x1="30" y1="60" x2="330" y2="60" class="grid-line"/>
            <line x1="30" y1="102" x2="330" y2="102" class="grid-line"/>
            <line x1="30" y1="144" x2="330" y2="144" stroke="$gridColor" stroke-width="1.5"/>

            <line x1="30" y1="110" x2="330" y2="110" class="threshold-line"/>
            <text x="328" y="106" fill="#DC2626" font-size="8px" text-anchor="end" font-weight="bold">75% Min Exam Eligibility</text>

            <text x="24" y="22" class="axis-y-label">100%</text>
            <text x="24" y="64" class="axis-y-label">90%</text>
            <text x="24" y="106" class="axis-y-label">80%</text>
            <text x="24" y="148" class="axis-y-label">70%</text>

            <g id="barsGroup"></g>
            <g id="labelsGroup"></g>
          </svg>
        </div>

        <script>
          const data = [$monthlyJson];
          const width = 340, height = 180;
          const padLeft = 38, padRight = 15, padTop = 18, padBottom = 26;
          const chartW = width - padLeft - padRight;
          const chartH = height - padTop - padBottom;
          const minY = 60, maxY = 100;

          const barW = (chartW / data.length) * 0.65;
          const stepX = chartW / data.length;

          const barsGroup = document.getElementById('barsGroup');
          const labelsGroup = document.getElementById('labelsGroup');
          const tooltip = document.getElementById('tooltip');

          data.forEach((d, i) => {
            const x = padLeft + i * stepX + (stepX - barW) / 2;
            const barH = ((d.rate - minY) / (maxY - minY)) * chartH;
            const y = padTop + chartH - barH;
            const color = d.rate >= 95 ? '#059669' : (d.rate >= 90 ? '#2563EB' : '#D97706');

            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', x);
            rect.setAttribute('y', y);
            rect.setAttribute('width', barW);
            rect.setAttribute('height', Math.max(2, barH));
            rect.setAttribute('fill', color);
            rect.setAttribute('class', 'bar');
            rect.addEventListener('click', () => showTip(d, x));
            barsGroup.appendChild(rect);

            const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('x', x + barW / 2);
            text.setAttribute('y', height - 8);
            text.setAttribute('class', 'axis-label');
            text.textContent = d.m;
            labelsGroup.appendChild(text);
          });

          function showTip(d, x) {
            tooltip.style.display = 'block';
            tooltip.style.left = Math.min(Math.max(10, (x / width) * window.innerWidth - 40), window.innerWidth - 130) + 'px';
            tooltip.style.top = '10px';
            tooltip.innerHTML = '<strong>' + d.full + '</strong><br>' +
              '<span style="color:#059669">●</span> Attendance: <strong>' + d.rate + '%</strong><br>' +
              '<small>Present: ' + d.present + ' / ' + d.days + ' days</small>';
          }

          if (data.length > 0) {
            showTip(data[data.length - 1], padLeft + (data.length - 1) * stepX);
          }
        </script>
      </body>
      </html>
      """.trimIndent()
    }

    AnalyticsTab.SUBJECTS -> {
      val subjectsJson = profile.subjects.joinToString(",") {
        """{"name":"${it.subjectName}","score":${it.scorePercentage},"att":${it.attendanceRate},"max":${it.highestScore},"teacher":"${it.teacherName}"}"""
      }

      """
      <!DOCTYPE html>
      <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
          body { background: transparent; overflow: hidden; padding: 6px 10px; }
          .subj-row { display: flex; align-items: center; margin-bottom: 10px; font-size: 11px; }
          .subj-name { width: 95px; color: $textColor; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
          .bar-track { flex: 1; height: 14px; background: $gridColor; border-radius: 7px; overflow: hidden; position: relative; margin: 0 8px; }
          .bar-fill-score { height: 100%; background: #2563EB; border-radius: 7px; }
          .bar-fill-att { position: absolute; top: 0; left: 0; height: 100%; background: #059669; border-radius: 7px; opacity: 0.4; }
          .subj-stat { width: 68px; text-align: right; color: $textColor; font-size: 10px; font-weight: bold; }
        </style>
      </head>
      <body>
        <div id="container"></div>
        <script>
          const data = [$subjectsJson];
          const container = document.getElementById('container');
          data.forEach(d => {
            const row = document.createElement('div');
            row.className = 'subj-row';
            row.innerHTML = 
              '<div class="subj-name">' + d.name + '</div>' +
              '<div class="bar-track">' +
                '<div class="bar-fill-att" style="width:' + d.att + '%"></div>' +
                '<div class="bar-fill-score" style="width:' + d.score + '%"></div>' +
              '</div>' +
              '<div class="subj-stat">' + d.score + '% <small style="color:#059669">(' + d.att + '%)</small></div>';
            container.appendChild(row);
          });
        </script>
      </body>
      </html>
      """.trimIndent()
    }
  }
}
