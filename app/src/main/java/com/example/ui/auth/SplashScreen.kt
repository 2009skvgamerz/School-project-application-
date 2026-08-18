package com.example.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun SplashScreen(
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val scale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            SchoolNavyDark,
            SchoolNavyPrimary,
            Color(0xFF071C40)
          )
        )
      )
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(24.dp)
    ) {
      Box(
        modifier = Modifier
          .size(130.dp)
          .scale(scale)
          .clip(CircleShape)
          .background(Color.White)
          .padding(4.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.school_logo),
          contentDescription = "My School Emblem",
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "ST. JOSEPH'S SCHOOL",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.2.sp
        ),
        color = Color.White
      )

      Surface(
        color = SchoolGold.copy(alpha = 0.25f),
        shape = CircleShape
      ) {
        Text(
          text = "SHINE AND LET SHINE",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          ),
          color = SchoolGoldLight,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      CircularProgressIndicator(
        color = SchoolGoldLight,
        strokeWidth = 3.dp,
        modifier = Modifier.size(28.dp)
      )

      Text(
        text = "School Management System Prototype",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.7f)
      )
    }
  }
}
