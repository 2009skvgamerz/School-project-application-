package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a smooth, subtle scroll-based / stagger-based entrance animation
 * to list items and dashboard cards for a polished, fluid navigation feel.
 *
 * Animates:
 * - Alpha: 0f -> 1f
 * - TranslationY: slideOffset -> 0dp
 * - Scale: 0.97f -> 1.0f
 */
fun Modifier.scrollEntrance(
  index: Int = 0,
  slideDistance: Dp = 24.dp,
  baseDelayMs: Int = 40,
  durationMs: Int = 360
): Modifier = composed {
  val density = LocalDensity.current
  val slideDistancePx = with(density) { slideDistance.toPx() }
  val progress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = durationMs,
        delayMillis = (index * baseDelayMs).coerceAtMost(360),
        easing = FastOutSlowInEasing
      )
    )
  }

  this.graphicsLayer {
    alpha = progress.value
    translationY = slideDistancePx * (1f - progress.value)
    val scale = 0.97f + (0.03f * progress.value)
    scaleX = scale
    scaleY = scale
  }
}

/**
 * Composable wrapper for applying subtle entrance animations to dashboard items
 */
@Composable
fun ScrollEntranceItem(
  index: Int,
  modifier: Modifier = Modifier,
  slideDistance: Dp = 20.dp,
  baseDelayMs: Int = 40,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier.scrollEntrance(
      index = index,
      slideDistance = slideDistance,
      baseDelayMs = baseDelayMs
    )
  ) {
    content()
  }
}
