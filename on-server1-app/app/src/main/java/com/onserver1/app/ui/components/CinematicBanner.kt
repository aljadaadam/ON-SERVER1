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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onserver1.app.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Cinematic animated banner for the home screen banner area.
 * Features: ON logo with orbital swoosh + particle effects + text reveal
 */
@Composable
fun CinematicBanner(
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val infiniteTransition = rememberInfiniteTransition(label = "banner")

    // === Logo animations ===
    val letterOAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 200, easing = FastOutSlowInEasing), label = "oA"
    )
    val letterOScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.4f,
        animationSpec = tween(600, delayMillis = 200, easing = FastOutSlowInEasing), label = "oS"
    )
    val letterNAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 400, easing = FastOutSlowInEasing), label = "nA"
    )
    val letterNScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.4f,
        animationSpec = tween(600, delayMillis = 400, easing = FastOutSlowInEasing), label = "nS"
    )

    // Swoosh draw
    val swooshProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1400, delayMillis = 300, easing = FastOutSlowInEasing), label = "sw"
    )

    // Text animations
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 800), label = "tA"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 20f,
        animationSpec = tween(600, delayMillis = 800, easing = FastOutSlowInEasing), label = "tO"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 1100), label = "sA"
    )
    val subtitleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 15f,
        animationSpec = tween(600, delayMillis = 1100, easing = FastOutSlowInEasing), label = "sO"
    )

    // Continuous effects
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(80000, easing = LinearEasing), RepeatMode.Restart),
        label = "pt"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    // Particles
    val particles = remember {
        List(20) {
            BannerParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.0003f,
                vy = (Random.nextFloat() - 0.5f) * 0.0003f,
                radius = Random.nextFloat() * 2f + 0.5f,
                opacity = Random.nextFloat() * 0.35f + 0.1f,
                isGold = Random.nextFloat() > 0.5f,
                pulseSpeed = Random.nextFloat() * 0.04f + 0.01f,
                pulseOffset = Random.nextFloat() * PI.toFloat() * 2f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D0B1E),
                        Color(0xFF151030),
                        Color(0xFF1A0F3A)
                    ),
                    start = Offset.Zero,
                    end = Offset(1000f, 500f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background canvas: particles + orbs + grid + shimmer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle grid
            val gridSize = 40f
            val gridColor = Color(0x06FFD700)
            var gx = 0f
            while (gx < w) {
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), 0.5f)
                gx += gridSize
            }
            var gy = 0f
            while (gy < h) {
                drawLine(gridColor, Offset(0f, gy), Offset(w, gy), 0.5f)
                gy += gridSize
            }

            // Ambient orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x20FFD700), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.3f),
                    radius = h * 0.6f
                ),
                center = Offset(w * 0.15f, h * 0.3f),
                radius = h * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x157C3AED), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.7f),
                    radius = h * 0.5f
                ),
                center = Offset(w * 0.85f, h * 0.7f),
                radius = h * 0.5f
            )

            // Particles
            particles.forEach { p ->
                val px = ((p.x + p.vx * particleTime) % 1f).let { if (it < 0) it + 1f else it } * w
                val py = ((p.y + p.vy * particleTime) % 1f).let { if (it < 0) it + 1f else it } * h
                val pAlpha = p.opacity * (0.5f + 0.5f * sin(particleTime * p.pulseSpeed + p.pulseOffset))
                val pColor = if (p.isGold) Color(0xFF00D2FF) else Color.White
                drawCircle(pColor.copy(alpha = pAlpha.coerceIn(0f, 1f)), p.radius, Offset(px, py))
            }

            // Shimmer line
            val shimmerX = shimmerOffset * w
            if (shimmerX > -50f && shimmerX < w + 50f) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0x20FFD700), Color.Transparent),
                        startX = shimmerX - 80f,
                        endX = shimmerX + 80f
                    ),
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX - 30f, h),
                    strokeWidth = 60f
                )
            }
        }

        // Content row: Logo on left, text on right (RTL: logo on right side)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // === ON Logo with orbital swoosh ===
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                // Glow behind logo
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00D2FF).copy(alpha = glowPulse * 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width / 2f
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width / 2f
                    )
                }

                // Logo + Swoosh
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val s = size.width / 200f

                    // Orbital swoosh ellipse
                    if (swooshProgress > 0f) {
                        val rx = 85f * s
                        val ry = 28f * s
                        val rot = -20f * (PI / 180f).toFloat()
                        val segs = (120 * swooshProgress).toInt().coerceAtLeast(1)

                        val path = Path()
                        for (i in 0..segs) {
                            val a = (2f * PI * i / 120f).toFloat()
                            val ex = rx * cos(a)
                            val ey = ry * sin(a)
                            val px = ex * cos(rot) - ey * sin(rot) + cx
                            val py = ex * sin(rot) + ey * cos(rot) + cy
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0x1AFFD700), Color.White, Color(0x1AFFD700))
                            ),
                            style = Stroke(width = 4f * s, cap = StrokeCap.Round)
                        )

                        // Inner ring
                        val inner = Path()
                        val irx = 70f * s; val iry = 20f * s
                        val iSegs = (100 * swooshProgress).toInt().coerceAtLeast(1)
                        for (i in 0..iSegs) {
                            val a = (2f * PI * i / 100f).toFloat()
                            val ex = irx * cos(a); val ey = iry * sin(a)
                            val px = ex * cos(rot) - ey * sin(rot) + cx
                            val py = ex * sin(rot) + ey * cos(rot) + cy
                            if (i == 0) inner.moveTo(px, py) else inner.lineTo(px, py)
                        }
                        drawPath(inner, Color.White.copy(alpha = 0.2f * swooshProgress),
                            style = Stroke(width = 1.2f * s, cap = StrokeCap.Round))
                    }

                    // O letter
                    if (letterOAlpha > 0f) {
                        val oX = cx - 20f * s
                        val oY = cy
                        val sc = letterOScale
                        // Outer
                        drawOval(
                            Color.White.copy(alpha = letterOAlpha),
                            topLeft = Offset(oX - 20f * s * sc, oY - 23f * s * sc),
                            size = androidx.compose.ui.geometry.Size(40f * s * sc, 46f * s * sc)
                        )
                        // Inner cutout
                        drawOval(
                            Color(0xFF0D0B1E).copy(alpha = letterOAlpha),
                            topLeft = Offset(oX - 11f * s * sc, oY - 13f * s * sc),
                            size = androidx.compose.ui.geometry.Size(22f * s * sc, 26f * s * sc)
                        )
                    }

                    // N letter
                    if (letterNAlpha > 0f) {
                        val nX = cx + 16f * s
                        val nY = cy
                        val sc = letterNScale
                        val nW = 26f * s * sc
                        val nH = 44f * s * sc
                        val sw = 7f * s * sc

                        drawRect(Color.White.copy(alpha = letterNAlpha),
                            Offset(nX - nW / 2f, nY - nH / 2f),
                            androidx.compose.ui.geometry.Size(sw, nH))
                        drawRect(Color.White.copy(alpha = letterNAlpha),
                            Offset(nX + nW / 2f - sw, nY - nH / 2f),
                            androidx.compose.ui.geometry.Size(sw, nH))

                        val diag = Path().apply {
                            moveTo(nX - nW / 2f, nY - nH / 2f)
                            lineTo(nX - nW / 2f + sw, nY - nH / 2f)
                            lineTo(nX + nW / 2f, nY + nH / 2f)
                            lineTo(nX + nW / 2f - sw, nY + nH / 2f)
                            close()
                        }
                        drawPath(diag, Color.White.copy(alpha = letterNAlpha))
                    }
                }
            }

            // === Cinematic text ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ON-SERVER1",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow.copy(alpha = titleAlpha),
                    modifier = Modifier.offset(y = titleOffset.dp),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "منصة لخدمات الهواتف الذكية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = titleAlpha),
                    modifier = Modifier.offset(y = titleOffset.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "iCloud • FRP • فك شبكات • أدوات",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF).copy(alpha = subtitleAlpha),
                    modifier = Modifier.offset(y = subtitleOffset.dp),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.offset(y = subtitleOffset.dp)
                ) {
                    MiniStat("525+", "خدمة", subtitleAlpha)
                    MiniStat("136+", "تصنيف", subtitleAlpha)
                    MiniStat("24/7", "دعم", subtitleAlpha)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, alpha: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = AccentYellow.copy(alpha = alpha)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFF9CA3AF).copy(alpha = alpha * 0.8f)
        )
    }
}

private data class BannerParticle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val radius: Float, val opacity: Float,
    val isGold: Boolean,
    val pulseSpeed: Float, val pulseOffset: Float
)
