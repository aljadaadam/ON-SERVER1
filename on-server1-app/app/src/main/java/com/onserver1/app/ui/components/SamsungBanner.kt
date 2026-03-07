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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onserver1.app.R
import kotlin.math.*
import kotlin.random.Random

// Samsung brand: electric blue + deep navy
private val SBlue = Color(0xFF1428A0)
private val SElectric = Color(0xFF4D8DF7)
private val SLight = Color(0xFF79AFFF)
private val SWhite = Color(0xFFE8F0FE)
private val SDark = Color(0xFF060D2E)
private val SDark2 = Color(0xFF0B1545)

@Composable
fun SamsungBanner(modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val inf = rememberInfiniteTransition(label = "sam")

    // Entry: phone slides up from bottom
    val phoneSlide by animateFloatAsState(
        if (started) 0f else 80f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow), label = "pS"
    )
    val phoneAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(600, 50), label = "pA"
    )
    // Entry: title wipes in from right
    val titleAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(500, 300), label = "tA"
    )
    val titleSlide by animateFloatAsState(
        if (started) 0f else 40f, tween(700, 300, FastOutSlowInEasing), label = "tS"
    )
    // Entry: subtitle fade
    val subAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(500, 500), label = "sA"
    )
    val subSlide by animateFloatAsState(
        if (started) 0f else 20f, tween(600, 500, FastOutSlowInEasing), label = "sS"
    )
    // Entry: badges pop in
    val badgeScale by animateFloatAsState(
        if (started) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "bSc"
    )

    // ─── Infinite ───
    // Data stream particles flowing upward
    val streamPhase by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "str"
    )
    // Pulse ring
    val pulseScale by inf.animateFloat(
        0.4f, 1.6f,
        infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing)), label = "pSc"
    )
    val pulseAlpha by inf.animateFloat(
        0.4f, 0f,
        infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing)), label = "pAl"
    )
    // Shimmer
    val shimmer by inf.animateFloat(
        -0.3f, 1.3f,
        infiniteRepeatable(tween(3500, easing = LinearEasing)), label = "sh"
    )
    // Floating phone
    val floatY by inf.animateFloat(
        -4f, 4f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "fY"
    )
    // Rotating halo
    val haloAngle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "ha"
    )
    // Scanner line
    val scanY by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "sc"
    )

    // Data stream particles
    val dataParticles = remember {
        List(20) {
            Triple(
                Random.nextFloat(),        // x position ratio
                Random.nextFloat(),        // start y offset
                Random.nextFloat() * 2f + 1f // size
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ═══ Background ═══
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height

            // Deep navy gradient
            drawRect(Brush.linearGradient(
                listOf(SDark, SDark2, SDark),
                Offset(0f, 0f), Offset(w, h)
            ))

            // Electric blue glow behind phone area (left)
            drawCircle(
                Brush.radialGradient(
                    listOf(SBlue.copy(0.25f), Color.Transparent),
                    center = Offset(w * 0.22f, h * 0.5f), radius = h * 1.4f
                ),
                center = Offset(w * 0.22f, h * 0.5f), radius = h * 1.4f
            )

            // Secondary glow top-right
            drawCircle(
                Brush.radialGradient(
                    listOf(SElectric.copy(0.08f), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.2f), radius = h * 0.9f
                ),
                center = Offset(w * 0.8f, h * 0.2f), radius = h * 0.9f
            )

            // Data stream particles (flowing upward behind phone)
            dataParticles.forEach { (xr, yOff, sz) ->
                val px = w * 0.05f + xr * w * 0.3f
                val rawY = ((yOff + streamPhase) % 1f)
                val py = h * (1f - rawY)
                val alpha = (sin(rawY * PI).toFloat() * 0.5f).coerceIn(0f, 0.4f)
                drawCircle(SElectric.copy(alpha), sz, Offset(px, py))
                // Trail
                drawLine(
                    SElectric.copy(alpha * 0.3f),
                    Offset(px, py), Offset(px, (py + 12f).coerceAtMost(h)),
                    strokeWidth = sz * 0.6f
                )
            }

            // Hex grid pattern (subtle tech feel)
            val hexSize = 30f
            for (row in 0..(h / hexSize).toInt()) {
                for (col in 0..(w / hexSize).toInt()) {
                    val cx = col * hexSize * 1.5f + if (row % 2 == 0) 0f else hexSize * 0.75f
                    val cy = row * hexSize * 0.866f
                    if (cx < w && cy < h) {
                        drawCircle(SElectric.copy(0.015f), 1.5f, Offset(cx, cy))
                    }
                }
            }

            // Scanner line (horizontal sweep)
            val scanLineY = scanY * h
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, SElectric.copy(0.08f), Color.Transparent),
                    startY = scanLineY - 15f, endY = scanLineY + 15f
                ),
                Offset(0f, scanLineY - 15f), Size(w, 30f)
            )

            // Shimmer sweep
            val sx = shimmer * w * 1.3f
            drawRect(Brush.horizontalGradient(
                listOf(Color.Transparent, Color.White.copy(0.05f), SLight.copy(0.04f), Color.Transparent),
                startX = sx - 120f, endX = sx + 120f
            ))

            // Bottom accent
            drawRect(
                Brush.horizontalGradient(listOf(
                    Color.Transparent, SElectric.copy(0.6f), SLight.copy(0.8f),
                    SElectric.copy(0.6f), Color.Transparent
                )),
                Offset(w * 0.05f, h - 2.5f), Size(w * 0.9f, 2.5f)
            )
            // Top accent
            drawRect(
                Brush.horizontalGradient(listOf(Color.Transparent, SBlue.copy(0.4f), Color.Transparent)),
                Offset(0f, 0f), Size(w, 1.5f)
            )
        }

        // ═══ Content ═══
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phone image (left)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationY = phoneSlide + floatY * density
                        alpha = phoneAlpha
                    }
            ) {
                // Pulse ring behind phone
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f; val cy = size.height / 2f
                    val r = size.minDimension * 0.35f

                    // Pulse
                    drawCircle(
                        SElectric.copy(pulseAlpha),
                        radius = r * pulseScale,
                        center = Offset(cx, cy),
                        style = Stroke(2f)
                    )

                    // Rotating halo dots
                    for (i in 0..5) {
                        val angle = Math.toRadians((haloAngle + i * 60f).toDouble())
                        val dx = cx + (r * 1.1f * cos(angle)).toFloat()
                        val dy = cy + (r * 1.1f * sin(angle)).toFloat()
                        drawCircle(SElectric.copy(0.3f), 2.5f, Offset(dx, dy))
                    }

                    // Glow
                    drawCircle(
                        Brush.radialGradient(listOf(SBlue.copy(0.15f), Color.Transparent)),
                        r * 1.3f, Offset(cx, cy)
                    )
                }

                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.sam_banner),
                    contentDescription = "Samsung",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 135.dp)
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text (right)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SAMSUNG",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = titleSlide * density
                        alpha = titleAlpha
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "خدمات فتح قفل سامسونج",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SLight.copy(alpha = 0.9f),
                    modifier = Modifier.graphicsLayer {
                        translationY = subSlide * density
                        alpha = subAlpha
                    }
                )

                Text(
                    text = "FRP • Network • Screen Lock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SElectric.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = subSlide * density * 0.5f
                        alpha = subAlpha
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.graphicsLayer {
                        scaleX = badgeScale; scaleY = badgeScale
                    }
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(SBlue.copy(0.3f), SElectric.copy(0.2f))))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("Galaxy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SLight) }

                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(SElectric.copy(0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("Note", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SElectric) }

                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("+المزيد", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.8f)) }
                }
            }
        }
    }
}
