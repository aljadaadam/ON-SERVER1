package com.onserver1.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onserver1.app.R
import kotlin.math.*
import kotlin.random.Random

// بنكك brand colors
private val BankakRed = Color(0xFFE52228)
private val BankakRedDark = Color(0xFFC01B20)
private val BankakRedDeep = Color(0xFF8B1117)
private val BankakGreen = Color(0xFFC8D900)
private val BankakGreenLight = Color(0xFFDBED00)

/**
 * Bankak payment banner — advertises direct charging via بنكك.
 * Animated with brand red/green colors and Bankak logo.
 */
@Composable
fun BankakBanner(
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val infiniteTransition = rememberInfiniteTransition(label = "bankak")

    // Logo entrance animation
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100), label = "logoAlpha"
    )

    // Title slide
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 300), label = "titA"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing), label = "titO"
    )

    // Subtitle slide
    val subAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 500), label = "subA"
    )
    val subOffset by animateFloatAsState(
        targetValue = if (started) 0f else 20f,
        animationSpec = tween(600, delayMillis = 500, easing = FastOutSlowInEasing), label = "subO"
    )

    // Badge pop
    val badgeScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ), label = "badgeS"
    )

    // Glow shimmer
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    // Pulse ring around logo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseA"
    )

    // Floating particles
    val particles = remember {
        List(12) {
            BankakParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.4f + 0.2f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                isGreen = Random.nextBoolean()
            )
        }
    }

    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ), label = "partTime"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BankakRedDeep, BankakRed, BankakRedDark),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Animated background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Diagonal stripe pattern
            for (i in 0..6) {
                val offset = i * (w / 4f)
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(offset - h, 0f),
                    end = Offset(offset, h),
                    strokeWidth = 40f
                )
            }

            // Shimmer wave
            val shimmerX = shimmer * w * 1.3f
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startX = shimmerX - 120f,
                    endX = shimmerX + 120f
                )
            )

            // Floating particles
            particles.forEach { p ->
                val px = p.x * w
                val py = p.y * h + sin(particleTime * p.speed + p.phase) * 12f
                val alpha = (sin(particleTime * p.speed + p.phase) * 0.3f + 0.4f)
                val color = if (p.isGreen) BankakGreen.copy(alpha = alpha)
                else Color.White.copy(alpha = alpha * 0.6f)
                drawCircle(color, radius = p.size, center = Offset(px, py))
            }

            // Green accent line at bottom
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BankakGreen.copy(alpha = 0.6f),
                        BankakGreenLight.copy(alpha = 0.8f),
                        BankakGreen.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                start = Offset(w * 0.1f, h - 3f),
                end = Offset(w * 0.9f, h - 3f),
                strokeWidth = 3f
            )
        }

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo with pulse ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                // Pulse ring
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(BankakGreen.copy(alpha = pulseAlpha))
                )

                // White circle background
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(logoScale)
                        .graphicsLayer { alpha = logoAlpha }
                        .clip(CircleShape)
                        .background(Color.White)
                )

                // Logo
                Image(
                    painter = painterResource(R.drawable.ic_bankak_logo),
                    contentDescription = "Bankak",
                    modifier = Modifier
                        .size(52.dp)
                        .scale(logoScale)
                        .graphicsLayer { alpha = logoAlpha }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "الشحن عبر بنكك",
                    color = Color.White.copy(alpha = titleAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        translationY = titleOffset
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "اشحن رصيدك مباشرة وبسهولة",
                    color = Color.White.copy(alpha = subAlpha * 0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = subOffset
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badge
                Box(
                    modifier = Modifier
                        .scale(badgeScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BankakGreen, BankakGreenLight)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "متوفر الآن ✓",
                        color = BankakRedDeep,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private data class BankakParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val isGreen: Boolean
)
