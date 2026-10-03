package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SchoolAccentGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * SpringProgressRing
 *
 * Physics-based progress ring utilizing Jetpack Compose spring animations
 * (Spring.DampingRatioMediumBouncy, Spring.StiffnessLow) for fluid motion transitions.
 * Features a circular track, rounded stroke caps, glowing endpoint bead,
 * and customizable central composable.
 */
@Composable
fun SpringProgressRing(
  progress: Float, // 0.0f .. 1.0f
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 8.dp,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
  progressColor: Color = SchoolAccentGreen,
  progressGradient: Brush? = null,
  showGlowTip: Boolean = true,
  content: @Composable (BoxScope.(animatedProgress: Float) -> Unit)? = null
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "spring_progress_ring"
  )

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidth.toPx()
      val diameter = size.minDimension - strokePx
      val radius = diameter / 2f
      val topLeft = Offset(
        (size.width - diameter) / 2f,
        (size.height - diameter) / 2f
      )
      val arcSize = Size(diameter, diameter)

      // 1. Background Track
      drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )

      // 2. Active Spring-Animated Progress Arc
      val sweepAngle = animatedProgress * 360f
      if (sweepAngle > 0.5f) {
        val brush = progressGradient ?: SolidColor(progressColor)
        drawArc(
          brush = brush,
          startAngle = -90f,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // 3. Glowing Endpoint Tip Bead
        if (showGlowTip && animatedProgress > 0.03f) {
          val currentAngleRad = Math.toRadians((-90f + sweepAngle).toDouble())
          val tipCenter = Offset(
            x = center.x + radius * cos(currentAngleRad).toFloat(),
            y = center.y + radius * sin(currentAngleRad).toFloat()
          )

          // Subtle outer glow
          drawCircle(
            color = progressColor.copy(alpha = 0.35f),
            radius = strokePx * 0.9f,
            center = tipCenter
          )
          // Solid white inner bead
          drawCircle(
            color = Color.White,
            radius = strokePx * 0.42f,
            center = tipCenter
          )
        }
      }
    }

    // Centered Content (e.g. Percentage & Status Label)
    if (content != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(strokeWidth + 4.dp),
        contentAlignment = Alignment.Center
      ) {
        content(animatedProgress)
      }
    }
  }
}

/**
 * StatProgressRingBadge
 * Compact circular gauge with spring dynamics and haptic touch feedback.
 */
@Composable
fun StatProgressRingBadge(
  percentage: Float,
  label: String,
  modifier: Modifier = Modifier,
  color: Color = SchoolAccentGreen,
  size: Dp = 88.dp,
  strokeWidth: Dp = 7.dp,
  onClick: (() -> Unit)? = null
) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.93f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "stat_ring_press_scale"
  )

  Box(
    modifier = modifier
      .size(size)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .then(
        if (onClick != null) {
          Modifier
            .clip(CircleShape)
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onClick()
            }
        } else Modifier
      ),
    contentAlignment = Alignment.Center
  ) {
    SpringProgressRing(
      progress = percentage / 100f,
      strokeWidth = strokeWidth,
      progressColor = color,
      trackColor = color.copy(alpha = 0.16f),
      modifier = Modifier.fillMaxSize()
    ) { animatedProgress ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "${(animatedProgress * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (size < 80.dp) 14.sp else 18.sp
          ),
          color = color
        )
        if (label.isNotEmpty()) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = if (size < 80.dp) 9.sp else 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
