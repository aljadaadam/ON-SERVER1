package com.onserver1.app.ui.screens.splash

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*
import kotlin.math.sin

@Composable
fun SplashScreenClassic(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var logoSettled by remember { mutableStateOf(false) }
    var exitAnimation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val mediaPlayer = try {
            MediaPlayer.create(context, R.raw.splash_sound)?.apply {
                isLooping = false
                setVolume(0.85f, 0.85f)
                start()
            }
        } catch (_: Exception) { null }
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        logoSettled = true
        delay(3500)
        exitAnimation = true
        delay(800)
        onSplashFinished()
    }

    val exitAlpha by animateFloatAsState(
        targetValue = if (exitAnimation) 0f else 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "exitAlpha"
    )
    val exitScale by animateFloatAsState(
        targetValue = if (exitAnimation) 1.08f else 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "exitScale"
    )

    val ringSweep by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing), label = "ringSweep"
    )
    val onLogoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "onLogoScale"
    )
    val onLogoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700, delayMillis = 200, easing = FastOutSlowInEasing), label = "onLogoAlpha"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (logoSettled) -40f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing), label = "logoOffsetY"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "logoScale"
    )

    val onAlpha by animateFloatAsState(
        targetValue = if (logoSettled) 1f else 0f,
        animationSpec = tween(600, delayMillis = 200, easing = FastOutSlowInEasing), label = "onAlpha"
    )
    val onScale by animateFloatAsState(
        targetValue = if (logoSettled) 1f else 0.3f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "onScale"
    )
    val dashAlpha by animateFloatAsState(
        targetValue = if (logoSettled) 1f else 0f,
        animationSpec = tween(400, delayMillis = 500, easing = FastOutSlowInEasing), label = "dashAlpha"
    )

    val server1 = "SERVER1"
    val letterAlphas = server1.indices.map { index ->
        animateFloatAsState(
            targetValue = if (logoSettled) 1f else 0f,
            animationSpec = tween(280, delayMillis = 700 + (index * 120), easing = FastOutSlowInEasing),
            label = "letter$index"
        )
    }
    val letterOffsets = server1.indices.map { index ->
        animateFloatAsState(
            targetValue = if (logoSettled) 0f else 18f,
            animationSpec = tween(350, delayMillis = 700 + (index * 120), easing = FastOutSlowInEasing),
            label = "letterOffset$index"
        )
    }

    val lineWidth by animateFloatAsState(
        targetValue = if (logoSettled) 1f else 0f,
        animationSpec = tween(700, delayMillis = 1500, easing = FastOutSlowInEasing), label = "lineWidth"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (logoSettled) 1f else 0f,
        animationSpec = tween(600, delayMillis = 1700, easing = LinearEasing), label = "subtitleAlpha"
    )
    val subtitleOffset by animateFloatAsState(
        targetValue = if (logoSettled) 0f else 12f,
        animationSpec = tween(600, delayMillis = 1700, easing = FastOutSlowInEasing), label = "subtitleOffset"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha)
            .scale(exitScale)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070714), Color(0xFF0F0F23),
                        BalanceGradientStart, Color(0xFF0D1B2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = logoOffsetY.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp).scale(logoScale)
            ) {
                Canvas(
                    modifier = Modifier.size(260.dp).alpha(glowAlpha * onLogoAlpha * 0.2f)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    clipRect(top = 0f, bottom = size.height * 0.46f) {
                        drawClassicOrbitalSwoosh(ringSweep)
                    }
                }
                Text(
                    text = "ON", fontSize = 82.sp, fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.scale(onLogoScale).alpha(onLogoAlpha)
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    clipRect(top = size.height * 0.46f, bottom = size.height) {
                        drawClassicOrbitalSwoosh(ringSweep)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("ON", fontSize = 46.sp, fontWeight = FontWeight.Black, color = AccentYellow,
                    modifier = Modifier.scale(onScale).alpha(onAlpha))
                Text("-", fontSize = 46.sp, fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f), modifier = Modifier.alpha(dashAlpha))
                server1.forEachIndexed { index, char ->
                    Text(
                        text = char.toString(), fontSize = 46.sp, fontWeight = FontWeight.Black,
                        color = if (char == '1') AccentYellow else Color.White,
                        modifier = Modifier.alpha(letterAlphas[index].value).offset(y = letterOffsets[index].value.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .width((200 * lineWidth).dp)
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent, AccentYellow.copy(alpha = 0.8f),
                                AccentYellow, AccentYellow.copy(alpha = 0.8f), Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Integrated Digital Services",
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center, letterSpacing = 2.5.sp,
                modifier = Modifier.alpha(subtitleAlpha).offset(y = subtitleOffset.dp)
            )
        }
    }
}

private fun DrawScope.drawClassicOrbitalSwoosh(sweep: Float) {
    rotate(-22f) {
        val cx = center.x
        val cy = center.y
        val ellipseW = size.width * 0.95f
        val ellipseH = size.height * 0.38f
        val left = cx - ellipseW / 2f
        val top = cy - ellipseH / 2f

        val segments = 30
        val baseStroke = 7.dp.toPx()
        for (i in 0 until segments) {
            val frac = i.toFloat() / segments
            val angle = frac * 360f
            if (angle > sweep) break
            val segLen = minOf(360f / segments + 1f, sweep - angle)
            val thickness = 0.2f + 0.8f * sin(frac.toDouble() * Math.PI).toFloat()
            drawArc(
                color = Color.White,
                startAngle = -90f + angle, sweepAngle = segLen,
                useCenter = false,
                style = Stroke(width = baseStroke * thickness, cap = StrokeCap.Butt),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(ellipseW, ellipseH)
            )
        }

        val innerW = ellipseW * 0.78f
        val innerH = ellipseH * 0.55f
        val innerLeft = cx - innerW / 2f
        val innerTop = cy - innerH / 2f
        val innerStroke = 2.5.dp.toPx()
        val innerStartDeg = -30f
        val innerFullSweep = 220f
        val innerActualSweep = minOf(innerFullSweep, maxOf(0f, sweep - 60f))
        if (innerActualSweep > 0f) {
            drawArc(
                color = Color.White.copy(alpha = 0.65f),
                startAngle = innerStartDeg, sweepAngle = innerActualSweep,
                useCenter = false,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round),
                topLeft = Offset(innerLeft, innerTop),
                size = androidx.compose.ui.geometry.Size(innerW, innerH)
            )
        }
    }
}
