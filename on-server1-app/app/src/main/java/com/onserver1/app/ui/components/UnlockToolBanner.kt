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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.*

// Colors from UnlockTool logo (orange/golden/brown)
private val UTOrange = Color(0xFFFF8C00)
private val UTOrangeDark = Color(0xFFE67300)
private val UTYellow = Color(0xFFFFAA00)
private val UTBrown = Color(0xFF5C3A1E)
private val UTDarkBg = Color(0xFF4A3520)

@Composable
fun UnlockToolBanner(
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val inf = rememberInfiniteTransition(label = "ut")

    // Entry animations
    val logoSlide by animateFloatAsState(
        targetValue = if (started) 0f else -60f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "lSl"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 100), label = "lA"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 250), label = "tA"
    )
    val titleSlide by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = tween(600, delayMillis = 250, easing = FastOutSlowInEasing), label = "tSl"
    )
    val badgeAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 500), label = "bA"
    )
    val badgeSlide by animateFloatAsState(
        targetValue = if (started) 0f else 20f,
        animationSpec = tween(500, delayMillis = 500, easing = FastOutSlowInEasing), label = "bSl"
    )

    // Infinite
    val shimmer by inf.animateFloat(
        initialValue = -0.3f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "shm"
    )
    val glowPulse by inf.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "gp"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ═══ Background: warm orange/brown from logo ═══
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Main warm gradient
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(UTDarkBg, UTBrown, UTDarkBg),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // Orange glow left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(UTOrange.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(w * 0.1f, h * 0.5f),
                    radius = h * 1.5f
                )
            )

            // Warm glow right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(UTYellow.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w * 0.9f, h * 0.3f),
                    radius = h * 1.2f
                )
            )

            // Diagonal streaks
            for (i in 0..5) {
                val sx = w * (-0.1f + i * 0.25f)
                drawLine(
                    color = UTOrange.copy(alpha = 0.04f),
                    start = Offset(sx, 0f),
                    end = Offset(sx + h * 0.5f, h),
                    strokeWidth = 25f + i * 8f
                )
            }

            // Shimmer
            val shimmerX = shimmer * w * 1.3f
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.07f),
                        UTYellow.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startX = shimmerX - 100f,
                    endX = shimmerX + 100f
                )
            )

            // Bottom accent
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        UTOrange.copy(alpha = 0.6f * glowPulse),
                        UTYellow.copy(alpha = 0.8f * glowPulse),
                        UTOrange.copy(alpha = 0.6f * glowPulse),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(w * 0.05f, h - 2.5f),
                size = Size(w * 0.9f, 2.5f)
            )

            // Top edge
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, UTOrangeDark.copy(alpha = 0.3f), Color.Transparent)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, 1.5f)
            )
        }

        // ═══ Content ═══
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo (rectangle, full display)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = logoSlide * density
                        alpha = logoAlpha
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(UTOrange.copy(alpha = 0.15f * glowPulse), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.6f,
                        center = center
                    )
                }

                AsyncImage(
                    model = "https://file.unlocktool.net/uploads/logo/logo_1766854141_69500dfd06f2b.png",
                    contentDescription = "Unlock Tool",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 105.dp)
                        .padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text fills remaining space
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "UNLOCK TOOL",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = UTYellow,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = titleSlide * density
                            alpha = titleAlpha
                        }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "أداة فتح قفل الهواتف الاحترافية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = titleSlide * density * 0.5f
                            alpha = titleAlpha
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = badgeSlide * density
                            alpha = badgeAlpha
                        },
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UTOrange.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Samsung", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UTYellow)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UTOrange.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Huawei", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UTYellow)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("+المزيد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
