package com.onserver1.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onserver1.app.ui.theme.AccentYellow
import kotlin.math.*
import kotlin.random.Random

/**
 * Ramadan Kareem banner with crescent moon, stars, lantern and greeting text.
 * رمضان كريم - تصوموا وتفطروا على خير
 */
@Composable
fun RamadanBanner(
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val infiniteTransition = rememberInfiniteTransition(label = "ramadan")

    // Crescent moon entrance
    val moonAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing), label = "mA"
    )
    val moonScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.3f,
        animationSpec = tween(900, delayMillis = 200, easing = FastOutSlowInEasing), label = "mS"
    )

    // Lantern entrance
    val lanternAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 500, easing = FastOutSlowInEasing), label = "lA"
    )

    // Text animations
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400), label = "tA"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 20f,
        animationSpec = tween(600, delayMillis = 400, easing = FastOutSlowInEasing), label = "tO"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 700), label = "sA"
    )
    val subtitleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 15f,
        animationSpec = tween(600, delayMillis = 700, easing = FastOutSlowInEasing), label = "sO"
    )

    // Continuous animations
    val moonGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "moonGlow"
    )
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkle"
    )
    val lanternSwing by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "swing"
    )
    val lanternGlowAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lGlow"
    )

    // Stars data
    val stars = remember {
        List(25) {
            RamadanStar(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                twinkleSpeed = Random.nextFloat() * 0.03f + 0.01f,
                twinkleOffset = Random.nextFloat() * PI.toFloat() * 2f,
                brightness = Random.nextFloat() * 0.5f + 0.3f
            )
        }
    }

    // Ramadan colors
    val ramadanGreen = Color(0xFF1B5E20)
    val ramadanGold = Color(0xFFFFD700)
    val ramadanDeepPurple = Color(0xFF1A0A3E)
    val ramadanNavy = Color(0xFF0D1B3E)
    val warmWhite = Color(0xFFFFF8E1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ramadanDeepPurple,
                        ramadanNavy,
                        Color(0xFF0A2647)
                    ),
                    start = Offset.Zero,
                    end = Offset(1000f, 500f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background canvas: stars + decorative elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle mosque dome silhouette at bottom
            val domeColor = Color(0x15FFFFFF)
            // Central large dome
            drawArc(
                color = domeColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.3f, h * 0.55f),
                size = Size(w * 0.4f, h * 0.6f)
            )
            // Left small dome
            drawArc(
                color = domeColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.05f, h * 0.65f),
                size = Size(w * 0.2f, h * 0.5f)
            )
            // Right small dome
            drawArc(
                color = domeColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.75f, h * 0.65f),
                size = Size(w * 0.2f, h * 0.5f)
            )
            // Left minaret
            drawRoundRect(
                color = domeColor,
                topLeft = Offset(w * 0.12f, h * 0.4f),
                size = Size(w * 0.025f, h * 0.6f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Right minaret
            drawRoundRect(
                color = domeColor,
                topLeft = Offset(w * 0.86f, h * 0.45f),
                size = Size(w * 0.025f, h * 0.55f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Stars twinkling
            stars.forEach { star ->
                val twinkle = (0.3f + 0.7f * sin(starTwinkle * star.twinkleSpeed * 100f + star.twinkleOffset)
                    .coerceIn(0f, 1f))
                val alpha = star.brightness * twinkle * moonAlpha
                val sx = star.x * w
                val sy = star.y * h * 0.7f // Stars mostly in upper portion

                // Star glow
                drawCircle(
                    color = ramadanGold.copy(alpha = (alpha * 0.3f).coerceIn(0f, 1f)),
                    radius = star.size * 3f,
                    center = Offset(sx, sy)
                )
                // Star core
                drawCircle(
                    color = warmWhite.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = star.size,
                    center = Offset(sx, sy)
                )
                // Star cross sparkle
                if (star.size > 2.5f) {
                    val sparkLen = star.size * 2.5f
                    drawLine(
                        warmWhite.copy(alpha = (alpha * 0.6f).coerceIn(0f, 1f)),
                        Offset(sx - sparkLen, sy),
                        Offset(sx + sparkLen, sy),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        warmWhite.copy(alpha = (alpha * 0.6f).coerceIn(0f, 1f)),
                        Offset(sx, sy - sparkLen),
                        Offset(sx, sy + sparkLen),
                        strokeWidth = 0.8f
                    )
                }
            }

            // Ambient golden glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ramadanGold.copy(alpha = moonGlow * 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.18f, h * 0.35f),
                    radius = h * 0.8f
                ),
                center = Offset(w * 0.18f, h * 0.35f),
                radius = h * 0.8f
            )
        }

        // Main content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // === Crescent Moon + Lantern ===
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val s = size.width / 220f

                    // Moon glow aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = moonGlow * 0.25f * moonAlpha),
                                Color(0xFFFFD700).copy(alpha = moonGlow * 0.08f * moonAlpha),
                                Color.Transparent
                            ),
                            center = Offset(cx - 10f * s, cy - 20f * s),
                            radius = 80f * s
                        ),
                        center = Offset(cx - 10f * s, cy - 20f * s),
                        radius = 80f * s
                    )

                    // === Crescent Moon ===
                    val moonCx = cx - 10f * s
                    val moonCy = cy - 20f * s
                    val moonR = 35f * s * moonScale

                    // Main moon circle (golden)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE082),
                                Color(0xFFFFD700),
                                Color(0xFFFFC107)
                            ),
                            center = Offset(moonCx, moonCy),
                            radius = moonR
                        ),
                        center = Offset(moonCx, moonCy),
                        radius = moonR,
                        alpha = moonAlpha
                    )
                    // Cutout circle to make crescent
                    drawCircle(
                        color = Color(0xFF0D1B3E),
                        center = Offset(moonCx + 15f * s * moonScale, moonCy - 8f * s * moonScale),
                        radius = moonR * 0.78f,
                        alpha = moonAlpha
                    )
                    // Small star next to crescent
                    val starX = moonCx + 25f * s
                    val starY = moonCy - 10f * s
                    val starAlpha = (moonAlpha * (0.6f + 0.4f * sin(starTwinkle * 0.05f))).coerceIn(0f, 1f)
                    // Star shape using lines
                    val starR = 6f * s
                    drawCircle(Color(0xFFFFD700).copy(alpha = starAlpha), starR * 0.5f, Offset(starX, starY))
                    drawLine(Color(0xFFFFD700).copy(alpha = starAlpha),
                        Offset(starX - starR, starY), Offset(starX + starR, starY), 1.5f)
                    drawLine(Color(0xFFFFD700).copy(alpha = starAlpha),
                        Offset(starX, starY - starR), Offset(starX, starY + starR), 1.5f)
                    drawLine(Color(0xFFFFD700).copy(alpha = starAlpha),
                        Offset(starX - starR * 0.7f, starY - starR * 0.7f),
                        Offset(starX + starR * 0.7f, starY + starR * 0.7f), 1f)
                    drawLine(Color(0xFFFFD700).copy(alpha = starAlpha),
                        Offset(starX + starR * 0.7f, starY - starR * 0.7f),
                        Offset(starX - starR * 0.7f, starY + starR * 0.7f), 1f)

                    // === Cute Lantern (فانوس) ===
                    val lAlpha = lanternAlpha
                    val lanCx = cx + 20f * s
                    val lanCy = cy + 25f * s
                    val swingRad = lanternSwing * (PI / 180f).toFloat()

                    // Lantern rope / chain
                    drawLine(
                        Color(0xFFFFD700).copy(alpha = lAlpha * 0.7f),
                        Offset(lanCx, lanCy - 35f * s),
                        Offset(lanCx + sin(swingRad) * 5f * s, lanCy - 20f * s),
                        strokeWidth = 2f * s
                    )

                    // Lantern top knob
                    val topX = lanCx + sin(swingRad) * 5f * s
                    val topY = lanCy - 20f * s
                    drawCircle(Color(0xFFFFD700).copy(alpha = lAlpha), 4f * s, Offset(topX, topY))

                    // Lantern body - rounded rectangle shape
                    val lbW = 22f * s
                    val lbH = 30f * s
                    val lbX = topX - lbW / 2f
                    val lbY = topY + 2f * s

                    // Lantern glow behind body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = lanternGlowAnim * 0.3f * lAlpha),
                                Color.Transparent
                            ),
                            center = Offset(topX, topY + lbH / 2f + 2f * s),
                            radius = lbH * 0.8f
                        ),
                        center = Offset(topX, topY + lbH / 2f + 2f * s),
                        radius = lbH * 0.8f
                    )

                    // Top trapezoid
                    val topPath = Path().apply {
                        moveTo(topX - 4f * s, topY + 2f * s)
                        lineTo(topX + 4f * s, topY + 2f * s)
                        lineTo(topX + lbW / 2f, topY + 8f * s)
                        lineTo(topX - lbW / 2f, topY + 8f * s)
                        close()
                    }
                    drawPath(topPath, Color(0xFFFFD700).copy(alpha = lAlpha))

                    // Main body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE65100).copy(alpha = lAlpha),
                                Color(0xFFBF360C).copy(alpha = lAlpha),
                                Color(0xFFE65100).copy(alpha = lAlpha)
                            )
                        ),
                        topLeft = Offset(lbX, lbY + 6f * s),
                        size = Size(lbW, lbH - 6f * s),
                        cornerRadius = CornerRadius(5f * s, 5f * s)
                    )

                    // Decorative golden lines on lantern
                    val lineAlpha = lAlpha * 0.8f
                    drawLine(Color(0xFFFFD700).copy(alpha = lineAlpha),
                        Offset(lbX + lbW * 0.3f, lbY + 8f * s),
                        Offset(lbX + lbW * 0.3f, lbY + lbH - 2f * s), 1.2f * s)
                    drawLine(Color(0xFFFFD700).copy(alpha = lineAlpha),
                        Offset(lbX + lbW * 0.5f, lbY + 8f * s),
                        Offset(lbX + lbW * 0.5f, lbY + lbH - 2f * s), 1.2f * s)
                    drawLine(Color(0xFFFFD700).copy(alpha = lineAlpha),
                        Offset(lbX + lbW * 0.7f, lbY + 8f * s),
                        Offset(lbX + lbW * 0.7f, lbY + lbH - 2f * s), 1.2f * s)

                    // Inner glow / light from lantern
                    drawRoundRect(
                        Color(0xFFFFD700).copy(alpha = lanternGlowAnim * 0.25f * lAlpha),
                        topLeft = Offset(lbX + 3f * s, lbY + 9f * s),
                        size = Size(lbW - 6f * s, lbH - 12f * s),
                        cornerRadius = CornerRadius(3f * s, 3f * s)
                    )

                    // Bottom cap
                    drawRoundRect(
                        Color(0xFFFFD700).copy(alpha = lAlpha),
                        topLeft = Offset(topX - 6f * s, lbY + lbH - 2f * s),
                        size = Size(12f * s, 4f * s),
                        cornerRadius = CornerRadius(2f * s, 2f * s)
                    )
                    // Bottom teardrop
                    drawCircle(Color(0xFFFFD700).copy(alpha = lAlpha), 2.5f * s,
                        Offset(topX, lbY + lbH + 3f * s))

                    // === Cute smiley face on lantern ===
                    val faceAlpha = lAlpha * 0.9f
                    val faceCx = topX
                    val faceCy = lbY + lbH * 0.45f + 4f * s
                    // Left eye
                    drawCircle(Color(0xFF1A0A3E).copy(alpha = faceAlpha), 2f * s,
                        Offset(faceCx - 4f * s, faceCy - 2f * s))
                    // Right eye
                    drawCircle(Color(0xFF1A0A3E).copy(alpha = faceAlpha), 2f * s,
                        Offset(faceCx + 4f * s, faceCy - 2f * s))
                    // Smile arc
                    drawArc(
                        color = Color(0xFF1A0A3E).copy(alpha = faceAlpha),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(faceCx - 5f * s, faceCy),
                        size = Size(10f * s, 6f * s),
                        style = Stroke(width = 1.5f * s, cap = StrokeCap.Round)
                    )
                    // Rosy cheeks
                    drawCircle(Color(0xFFFF8A80).copy(alpha = faceAlpha * 0.5f), 2.2f * s,
                        Offset(faceCx - 7f * s, faceCy + 2f * s))
                    drawCircle(Color(0xFFFF8A80).copy(alpha = faceAlpha * 0.5f), 2.2f * s,
                        Offset(faceCx + 7f * s, faceCy + 2f * s))
                }
            }

            // === Ramadan Text ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "رمضان كريم 🌙",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700).copy(alpha = titleAlpha),
                    modifier = Modifier.offset(y = titleOffset.dp),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تصوموا وتفطروا على خير",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = titleAlpha),
                    modifier = Modifier.offset(y = titleOffset.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "كل عام وأنتم بخير",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFE082).copy(alpha = subtitleAlpha),
                    modifier = Modifier.offset(y = subtitleOffset.dp),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ON-SERVER1",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow.copy(alpha = subtitleAlpha * 0.7f),
                    modifier = Modifier.offset(y = subtitleOffset.dp),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

private data class RamadanStar(
    val x: Float, val y: Float,
    val size: Float,
    val twinkleSpeed: Float,
    val twinkleOffset: Float,
    val brightness: Float
)
