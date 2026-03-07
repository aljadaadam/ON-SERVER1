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
import kotlin.random.Random

private val HCyan = Color(0xFF00D4FF)
private val HBlue = Color(0xFF0066FF)
private val HAccent = Color(0xFF00E5CC)
private val HDark1 = Color(0xFF0A1628)
private val HDark2 = Color(0xFF101E38)

@Composable
fun HonorFrpBanner(modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val inf = rememberInfiniteTransition(label = "h")

    // Entry: phone from left
    val phoneSlide by animateFloatAsState(
        if (started) 0f else -60f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "pSl"
    )
    val phoneAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(500, 100), label = "pA"
    )
    // Entry: text
    val titleAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(500, 250), label = "tA"
    )
    val titleSlide by animateFloatAsState(
        if (started) 0f else 30f, tween(600, 250, FastOutSlowInEasing), label = "tS"
    )
    val badgeAlpha by animateFloatAsState(
        if (started) 1f else 0f, tween(500, 500), label = "bA"
    )
    val badgeSlide by animateFloatAsState(
        if (started) 0f else 20f, tween(500, 500, FastOutSlowInEasing), label = "bS"
    )

    // Infinite
    val shimmer by inf.animateFloat(
        -0.3f, 1.3f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "sh"
    )
    val glowPulse by inf.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "gp"
    )
    val floatY by inf.animateFloat(
        -3f, 3f,
        infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "fY"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ═══ Background ═══
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height

            // Base gradient
            drawRect(Brush.linearGradient(
                listOf(HDark1, HDark2, Color(0xFF0D1A30)),
                Offset(0f, 0f), Offset(w, h)
            ))

            // Cyan glow left (behind phone)
            drawCircle(
                Brush.radialGradient(listOf(HCyan.copy(0.2f), Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.5f), radius = h * 1.5f),
                center = Offset(w * 0.12f, h * 0.5f), radius = h * 1.5f
            )

            // Blue glow right
            drawCircle(
                Brush.radialGradient(listOf(HBlue.copy(0.08f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.3f), radius = h * 1f),
                center = Offset(w * 0.85f, h * 0.3f), radius = h * 1f
            )

            // Diagonal streaks
            for (i in 0..5) {
                val sx = w * (-0.1f + i * 0.25f)
                drawLine(HCyan.copy(0.03f), Offset(sx, 0f), Offset(sx + h * 0.5f, h), 25f + i * 8f)
            }

            // Shimmer
            val sx = shimmer * w * 1.3f
            drawRect(Brush.horizontalGradient(
                listOf(Color.Transparent, Color.White.copy(0.06f), HCyan.copy(0.04f), Color.Transparent),
                startX = sx - 100f, endX = sx + 100f
            ))

            // Bottom accent
            drawRect(
                Brush.horizontalGradient(listOf(
                    Color.Transparent, HCyan.copy(0.5f * glowPulse),
                    HAccent.copy(0.7f * glowPulse), HCyan.copy(0.5f * glowPulse), Color.Transparent
                )),
                Offset(w * 0.05f, h - 2.5f), Size(w * 0.9f, 2.5f)
            )

            // Top edge
            drawRect(
                Brush.horizontalGradient(listOf(Color.Transparent, HBlue.copy(0.3f), Color.Transparent)),
                Offset(0f, 0f), Size(w, 1.5f)
            )
        }

        // ═══ Content: Phone LEFT + Text RIGHT (same as UnlockTool) ═══
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phone image (left side, bigger)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = phoneSlide * density
                        translationY = floatY * density
                        alpha = phoneAlpha
                    }
            ) {
                // Glow behind phone
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(HCyan.copy(0.12f * glowPulse), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.7f, center = center
                    )
                }

                AsyncImage(
                    model = "https://www-file.honor.com/content/dam/honor/common/product-list/product-series/honor-400-pro/honor-400-pro-gray-list.png",
                    contentDescription = "Honor",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 135.dp)
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text (right side, fills remaining)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FRP BYPASS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HCyan,
                    letterSpacing = 3.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = titleSlide * density
                        alpha = titleAlpha
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "HONOR",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = titleSlide * density * 0.5f
                        alpha = titleAlpha
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "فتح قفل حساب قوقل",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HAccent.copy(alpha = 0.9f),
                    modifier = Modifier.graphicsLayer {
                        translationY = titleSlide * density * 0.3f
                        alpha = titleAlpha
                    }
                )

                Text(
                    text = "لجميع أجهزة هونر",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.graphicsLayer {
                        translationY = titleSlide * density * 0.2f
                        alpha = titleAlpha
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.graphicsLayer {
                        translationY = badgeSlide * density
                        alpha = badgeAlpha
                    }
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(HCyan.copy(0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("⚡ سريع", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HCyan) }

                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(HAccent.copy(0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("🔓 آمن", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HAccent) }

                    Box(
                        Modifier.clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("✓ مضمون", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.8f)) }
                }
            }
        }
    }
}
