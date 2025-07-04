package com.example.dosely

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dosely.ui.theme.DoselyTheme
import kotlin.math.cos
import kotlin.math.sin

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoselyTheme {
                SplashScreen()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3500) // 3.5 seconds for better experience
    }
}

@Composable
fun SplashScreen() {
    // Define colors
    val lightBlue = Color(0xFFDAF4FF)
    val mediumBlue = Color(0xFFB0D3E2)
    val darkBlue = Color(0xFF2E7BB8)
    val accentBlue = Color(0xFF4A90E2)

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")

    // Icon entrance animation
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    // Icon floating animation
    val iconFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_float"
    )

    // Icon subtle rotation
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_rotation"
    )

    // Text animations
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 800),
        label = "title_alpha"
    )

    val titleScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "title_scale"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 1200),
        label = "subtitle_alpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 1600),
        label = "tagline_alpha"
    )

    // Background animations
    val backgroundRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_rotation"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Shimmer effect
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lightBlue,
                        mediumBlue.copy(alpha = 0.8f),
                        darkBlue.copy(alpha = 0.6f)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background elements
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawAnimatedBackground(backgroundRotation, pulseAlpha, mediumBlue, accentBlue)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Icon container with shadow and glow effect
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = (-iconFloat).dp)
                    .rotate(iconRotation)
                    .scale(iconScale)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = accentBlue.copy(alpha = 0.4f),
                        spotColor = accentBlue.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect background
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = shimmerAlpha * 0.3f),
                                    accentBlue.copy(alpha = shimmerAlpha * 0.2f),
                                    Color.Transparent
                                ),
                                radius = 200f
                            )
                        )
                )

                // Your clock icon
                Image(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = "Dosely App Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // App name with shimmer effect
            Text(
                text = "Dosely",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = darkBlue,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .scale(titleScale),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = accentBlue.copy(alpha = 0.3f),
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main tagline
            Text(
                text = "Your Health, On Time",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentBlue,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .shimmer(shimmerAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary tagline
            Text(
                text = "Never miss a dose again",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = mediumBlue,
                modifier = Modifier.alpha(taglineAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading indicator
            LoadingDots(
                modifier = Modifier.alpha(taglineAlpha),
                color = accentBlue
            )
        }
    }
}

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = Color.Blue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}

fun Modifier.shimmer(alpha: Float): Modifier = this.then(
    Modifier.alpha(alpha)
)

private fun DrawScope.drawAnimatedBackground(
    rotation: Float,
    pulseAlpha: Float,
    mediumBlue: Color,
    accentBlue: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    // Draw floating particles
    for (i in 0..8) {
        val angle = rotation + (i * 40f)
        val radius = 150f + (i * 30f)
        val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
        val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

        drawCircle(
            color = if (i % 2 == 0) mediumBlue.copy(alpha = pulseAlpha * 0.3f)
            else accentBlue.copy(alpha = pulseAlpha * 0.2f),
            radius = 15f + (i * 3f),
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }

    // Draw concentric circles
    for (i in 1..3) {
        drawCircle(
            color = accentBlue.copy(alpha = pulseAlpha * 0.1f * i),
            radius = 200f * i,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Draw subtle gradient overlay
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                mediumBlue.copy(alpha = pulseAlpha * 0.1f)
            ),
            radius = 400f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    )
}