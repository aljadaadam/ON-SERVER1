package com.onserver1.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.*
import kotlin.random.Random

// بنكك brand colors
private val BankakRed = Color(0xFFE52228)
private val BankakRedDark = Color(0xFFC01B20)
private val BankakRedDeep = Color(0xFF8B1117)
private val BankakGreen = Color(0xFFC8D900)
private val BankakGreenLight = Color(0xFFDBED00)
private val BankakCream = Color(0xFFFFF8E7)

private const val BANKAK_LOGO_URL = "https://6990ab01681c79fa0bccfe99.imgix.net/"

/**
 * Cinematic Bankak payment banner — rich animated background with brand identity.
 */
@Composable
fun BankakBanner(
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val infiniteTransition = rememberInfiniteTransition(label = "bankak")

    // ═══ Logo entrance ═══
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "logoS"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 100), label = "logoA"
    )

    // ═══ Text animations ═══
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 350), label = "tA"
    )
    val titleSlide by animateFloatAsState(
        targetValue = if (started) 0f else 40f,
        animationSpec = tween(700, delayMillis = 350, easing = FastOutSlowInEasing), label = "tS"
    )
    val subAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 550), label = "sA"
    )
    val subSlide by animateFloatAsState(
        targetValue = if (started) 0f else 25f,
        animationSpec = tween(600, delayMillis = 550, easing = FastOutSlowInEasing), label = "sS"
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ), label = "bS"
    )

    // ═══ Infinite animations ═══
    // Shimmer sweep
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "shim"
    )

    // Subtle background wave
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(8000, easing = LinearEasing)
        ), label = "wave"
    )

    // Pulse glow around logo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "pulseA"
    )

    // Floating sparks
    val sparkTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(5000, easing = LinearEasing)
        ), label = "sparkT"
    )

    // Energy ring rotation
    val ringAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing)
        ), label = "ring"
    )

    val sparks = remember {
        List(18) {
            BankakSpark(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1.5f,
                speed = Random.nextFloat() * 0.6f + 0.3f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                type = Random.nextInt(3) // 0=green, 1=white, 2=red glow
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ═══ Rich cinematic background ═══
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Dark gradient base
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0508),
                        Color(0xFF2D0A0E),
                        Color(0xFF1A0508)
                    )
                )
            )

            // Animated wave curves
            for (i in 0..2) {
                val path = Path()
                path.moveTo(0f, 0f)
                for (x in 0..w.toInt() step 4) {
                    val xf = x.toFloat()
                    val yBase = h * (0.3f + i * 0.2f)
                    val yOff = sin(xf / (w * 0.15f) + wavePhase + i * 1.2f) * (h * 0.08f)
                    if (x == 0) path.moveTo(xf, yBase + yOff)
                    else path.lineTo(xf, yBase + yOff)
                }
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()

                val waveAlpha = 0.04f - i * 0.01f
                drawPath(
                    path,
                    brush = Brush.verticalGradient(
                        listOf(
                            BankakRed.copy(alpha = waveAlpha),
                            Color.Transparent
                        )
                    )
                )
            }

            // Radial glow from the logo area (left side)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BankakRed.copy(alpha = 0.2f),
                        BankakRedDeep.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.5f),
                    radius = h * 1.2f
                )
            )

            // Secondary green glow from right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BankakGreen.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.3f),
                    radius = h * 0.8f
                )
            )

            // Diagonal light streaks
            for (i in 0..4) {
                val startX = w * (-0.2f + i * 0.35f)
                drawLine(
                    color = BankakRed.copy(alpha = 0.06f),
                    start = Offset(startX, 0f),
                    end = Offset(startX + h * 0.7f, h),
                    strokeWidth = 30f + i * 8f
                )
            }

            // Shimmer sweep
            val shimmerX = shimmer * w * 1.5f
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.06f),
                        BankakGreen.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    startX = shimmerX - 100f,
                    endX = shimmerX + 100f
                )
            )

            // Floating sparks
            sparks.forEach { s ->
                val px = s.x * w
                val py = s.y * h + sin(sparkTime * s.speed + s.phase) * 15f
                val driftX = cos(sparkTime * s.speed * 0.5f + s.phase) * 8f
                val a = (sin(sparkTime * s.speed + s.phase) * 0.35f + 0.45f).coerceIn(0f, 1f)
                val color = when (s.type) {
                    0 -> BankakGreen.copy(alpha = a)
                    1 -> Color.White.copy(alpha = a * 0.5f)
                    else -> BankakRed.copy(alpha = a * 0.4f)
                }
                drawCircle(color, radius = s.size, center = Offset(px + driftX, py))
                // Glow halo for green sparks
                if (s.type == 0) {
                    drawCircle(
                        BankakGreen.copy(alpha = a * 0.15f),
                        radius = s.size * 3f,
                        center = Offset(px + driftX, py)
                    )
                }
            }

            // Energy arc fragments (rotating)
            val arcCx = w * 0.14f
            val arcCy = h * 0.5f
            val arcR = 52f
            for (i in 0..2) {
                val startAngle = ringAngle + i * 120f
                drawArc(
                    color = BankakGreen.copy(alpha = 0.25f),
                    startAngle = startAngle,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(arcCx - arcR, arcCy - arcR),
                    size = Size(arcR * 2, arcR * 2),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }

            // Bottom gradient accent line
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BankakGreen.copy(alpha = 0.5f),
                        BankakGreenLight.copy(alpha = 0.7f),
                        BankakGreen.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(w * 0.05f, h - 3f),
                size = Size(w * 0.9f, 3f)
            )

            // Top subtle red edge
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BankakRed.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, 2f)
            )
        }

        // ═══ Content layer ═══
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo with energy ring + pulse
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(82.dp)
            ) {
                // Outer pulse glow
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    BankakGreen.copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // White circle
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .scale(logoScale)
                        .graphicsLayer { alpha = logoAlpha }
                        .clip(CircleShape)
                        .background(Color.White)
                )

                // Logo from URL
                AsyncImage(
                    model = BANKAK_LOGO_URL,
                    contentDescription = "Bankak",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .scale(logoScale)
                        .graphicsLayer { alpha = logoAlpha }
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "الشحن عبر بنكك",
                    color = Color.White.copy(alpha = titleAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer { translationY = titleSlide }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "اشحن رصيدك مباشرة وبسهولة",
                    color = BankakCream.copy(alpha = subAlpha * 0.9f),
                    fontSize = 13.sp,
                    modifier = Modifier.graphicsLayer { translationY = subSlide }
                )

                Spacer(modifier = Modifier.height(10.dp))

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

private data class BankakSpark(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val type: Int
)
