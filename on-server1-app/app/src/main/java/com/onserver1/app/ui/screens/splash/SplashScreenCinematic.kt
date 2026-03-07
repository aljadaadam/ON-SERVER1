package com.onserver1.app.ui.screens.splash

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Phonelink
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onserver1.app.R
import com.onserver1.app.ui.theme.AccentYellow
import com.onserver1.app.util.AppBridge
import com.onserver1.app.util.RemoteConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ProductCard(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val angle: Float,
    val radius: Float
)

private data class StreamParticle(
    val startAngle: Float,
    val startRadius: Float,
    val speed: Float,
    val size: Float,
    val hue: Int // 0=cyan, 1=blue, 2=white
)

@Composable
fun SplashScreenCinematic(
    onSplashFinished: () -> Unit
) {
    val products = remember {
        listOf(
            ProductCard(Icons.Outlined.Lock, "Unlock Tool", Color(0xFF00D2FF), 240f, 160f),
            ProductCard(Icons.Outlined.Security, "Chimera", Color(0xFFFF6B35), 300f, 160f),
            ProductCard(Icons.Outlined.PhoneAndroid, "Samsung", Color(0xFF4CAF50), 0f, 165f),
            ProductCard(Icons.Outlined.Phonelink, "Honor", Color(0xFF7C4DFF), 60f, 160f),
            ProductCard(Icons.Outlined.Build, "FRP Bypass", Color(0xFFE040FB), 120f, 160f),
            ProductCard(Icons.Outlined.Verified, "iCloud", Color(0xFF00BCD4), 180f, 160f),
        )
    }

    // Stream particles flying toward center
    val particles = remember {
        List(60) {
            StreamParticle(
                startAngle = Random.nextFloat() * 360f,
                startRadius = 0.6f + Random.nextFloat() * 0.4f,
                speed = 0.4f + Random.nextFloat() * 0.6f,
                size = 1.5f + Random.nextFloat() * 3f,
                hue = Random.nextInt(3)
            )
        }
    }

    // ── Phase states ──
    var phase1_gridFade by remember { mutableStateOf(false) }
    var phase2_streams by remember { mutableStateOf(false) }
    var phase3_coreForm by remember { mutableStateOf(false) }
    var phase4_corePulse by remember { mutableStateOf(false) }
    var phase5_burst by remember { mutableStateOf(false) }
    var phase6_cardsShow by remember { mutableStateOf(false) }
    var phase7_cardsHide by remember { mutableStateOf(false) }
    var phase8_logoReveal by remember { mutableStateOf(false) }
    var phase9_textReveal by remember { mutableStateOf(false) }
    var phase10_exit by remember { mutableStateOf(false) }

    // ── Sound ──
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val player = try {
            MediaPlayer.create(context, R.raw.splash_sound)?.apply {
                isLooping = false
                setVolume(0.85f, 0.85f)
                start()
            }
        } catch (_: Exception) { null }
        onDispose {
            try { player?.stop(); player?.release() } catch (_: Exception) { }
        }
    }

    // ── Timeline ──
    LaunchedEffect(Unit) {
        val licenseCheck = async {
            AppBridge.verify() && RemoteConfig.verify()
        }

        delay(150)
        phase1_gridFade = true       // 0.15s: hex grid fades in
        delay(400)
        phase2_streams = true        // 0.55s: particle streams converge
        delay(1200)
        phase3_coreForm = true       // 1.75s: energy core forms at center
        delay(600)
        phase4_corePulse = true      // 2.35s: core pulses bright
        delay(400)
        phase5_burst = true          // 2.75s: burst + shockwave
        delay(350)
        phase6_cardsShow = true      // 3.1s: product cards fly out
        delay(2000)
        phase7_cardsHide = true      // 5.1s: cards converge back
        delay(400)
        phase8_logoReveal = true     // 5.5s: logo appears
        delay(500)
        phase9_textReveal = true     // 6.0s: text reveals
        delay(1000)
        phase10_exit = true          // 7.0s: exit
        delay(700)

        val isLicensed = licenseCheck.await()
        if (!isLicensed) return@LaunchedEffect

        onSplashFinished()
    }

    // ═══════════════════════════════
    //  ANIMATION VALUES
    // ═══════════════════════════════

    // Exit
    val exitAlpha by animateFloatAsState(
        if (phase10_exit) 0f else 1f,
        tween(700, easing = FastOutSlowInEasing), label = "exA"
    )
    val exitScale by animateFloatAsState(
        if (phase10_exit) 1.15f else 1f,
        tween(700, easing = FastOutSlowInEasing), label = "exS"
    )

    // Grid fade
    val gridAlpha by animateFloatAsState(
        if (phase1_gridFade) if (phase5_burst) 0f else 1f else 0f,
        tween(if (phase5_burst) 400 else 800), label = "grA"
    )

    // Streams convergence (0 = at edges, 1 = at center)
    val streamProgress by animateFloatAsState(
        if (phase2_streams) 1f else 0f,
        tween(1200, easing = FastOutSlowInEasing), label = "stP"
    )
    val streamAlpha by animateFloatAsState(
        if (phase2_streams) if (phase5_burst) 0f else 1f else 0f,
        tween(if (phase5_burst) 200 else 600), label = "stA"
    )

    // Core
    val coreScale by animateFloatAsState(
        if (phase5_burst) 3f else if (phase4_corePulse) 1.3f else if (phase3_coreForm) 1f else 0f,
        if (phase5_burst) tween(300, easing = FastOutLinearInEasing)
        else spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "crS"
    )
    val coreAlpha by animateFloatAsState(
        if (phase5_burst) 0f else if (phase3_coreForm) 1f else 0f,
        tween(if (phase5_burst) 300 else 500), label = "crA"
    )
    val coreGlow by animateFloatAsState(
        if (phase4_corePulse) 1f else 0f,
        tween(400, easing = FastOutSlowInEasing), label = "crG"
    )

    // Burst shockwave
    val burstRadius by animateFloatAsState(
        if (phase5_burst) 1f else 0f,
        tween(700, easing = FastOutSlowInEasing), label = "brR"
    )
    val burstAlpha by animateFloatAsState(
        if (phase5_burst) 0f else 0.8f,
        tween(700, easing = FastOutSlowInEasing), label = "brA"
    )

    // Second ring
    var burst2 by remember { mutableStateOf(false) }
    LaunchedEffect(phase5_burst) {
        if (phase5_burst) { delay(120); burst2 = true }
    }
    val burst2Radius by animateFloatAsState(
        if (burst2) 1f else 0f,
        tween(600, easing = FastOutSlowInEasing), label = "br2R"
    )
    val burst2Alpha by animateFloatAsState(
        if (burst2) 0f else 0.5f,
        tween(600, easing = FastOutSlowInEasing), label = "br2A"
    )

    // Flash
    var burstFlash by remember { mutableStateOf(false) }
    val burstFlashAlpha by animateFloatAsState(
        if (burstFlash) 0.8f else 0f,
        tween(if (burstFlash) 40 else 350), label = "bfA"
    )
    LaunchedEffect(phase5_burst) {
        if (phase5_burst) {
            burstFlash = true
            delay(80)
            burstFlash = false
        }
    }

    // Cards
    val cardAnimations = products.indices.map { i ->
        animateFloatAsState(
            if (phase6_cardsShow) 1f else 0f,
            tween(700, delayMillis = i * 110, easing = FastOutSlowInEasing), label = "cd$i"
        )
    }
    val cardsOrbit by animateFloatAsState(
        if (phase6_cardsShow) if (phase7_cardsHide) 160f else 80f else 0f,
        tween(if (phase7_cardsHide) 400 else 2000, easing = FastOutSlowInEasing), label = "cdO"
    )
    val cardHideAnimations = products.indices.map { i ->
        animateFloatAsState(
            if (phase7_cardsHide) 0f else 1f,
            tween(400, delayMillis = i * 35, easing = FastOutLinearInEasing), label = "cdH$i"
        )
    }

    // Logo
    val logoScale by animateFloatAsState(
        if (phase8_logoReveal) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "lgS"
    )
    val logoAlpha by animateFloatAsState(
        if (phase8_logoReveal) 1f else 0f,
        tween(500), label = "lgA"
    )
    val swooshSweep by animateFloatAsState(
        if (phase8_logoReveal) 360f else 0f,
        tween(1000, easing = FastOutSlowInEasing), label = "swp"
    )

    // Text
    val textAlpha by animateFloatAsState(
        if (phase9_textReveal) 1f else 0f,
        tween(600), label = "txA"
    )
    val textSlide by animateFloatAsState(
        if (phase9_textReveal) 0f else 30f,
        tween(600, easing = FastOutSlowInEasing), label = "txS"
    )

    // Continuous
    val inf = rememberInfiniteTransition(label = "inf")
    val pulseVal by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )
    val timeVal by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "time"
    )

    // ═════════════════════
    //  COLORS
    // ═════════════════════
    val bgDark = Color(0xFF030810)
    val bgMid = Color(0xFF081020)
    val cyanPrimary = Color(0xFF00D4FF)
    val bluePrimary = Color(0xFF2962FF)

    // ═════════════════════
    //  UI
    // ═════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = exitAlpha
                scaleX = exitScale
                scaleY = exitScale
            }
            .background(
                Brush.radialGradient(
                    listOf(bgMid, bgDark, Color.Black),
                    center = Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY / 2),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ═══════════════════════════════════
        //  LAYER 1: Hexagonal tech grid
        // ═══════════════════════════════════
        if (gridAlpha > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize().alpha(gridAlpha)) {
                val w = size.width
                val h = size.height
                val hexSize = 28f * density
                val hexH = hexSize * kotlin.math.sqrt(3f)
                val cols = (w / (hexSize * 1.5f)).toInt() + 2
                val rows = (h / hexH).toInt() + 2

                for (row in 0..rows) {
                    for (col in 0..cols) {
                        val cx = col * hexSize * 1.5f
                        val cy = row * hexH + if (col % 2 == 1) hexH / 2f else 0f

                        val dist = kotlin.math.sqrt(
                            (cx - w / 2f) * (cx - w / 2f) + (cy - h / 2f) * (cy - h / 2f)
                        )
                        val maxDist = kotlin.math.sqrt(w * w / 4f + h * h / 4f)
                        val distFrac = (dist / maxDist).coerceIn(0f, 1f)
                        val hexAlpha = (0.08f + 0.06f * (1f - distFrac)) * (0.7f + 0.3f * pulseVal)

                        val hexPath = Path().apply {
                            for (k in 0..5) {
                                val angle = (60.0 * k + 30.0) * PI / 180.0
                                val hx = cx + (hexSize * 0.5f) * cos(angle).toFloat()
                                val hy = cy + (hexSize * 0.5f) * sin(angle).toFloat()
                                if (k == 0) moveTo(hx, hy) else lineTo(hx, hy)
                            }
                            close()
                        }
                        drawPath(
                            hexPath,
                            cyanPrimary.copy(alpha = hexAlpha),
                            style = Stroke(width = 0.5f * density)
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════
        //  LAYER 2: Particle streams converging
        // ═══════════════════════════════════
        if (streamAlpha > 0.01f && streamProgress > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize().alpha(streamAlpha)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val maxR = w.coerceAtLeast(h) * 0.55f

                particles.forEach { p ->
                    val angle = p.startAngle * PI / 180.0
                    val progress = (streamProgress * p.speed).coerceIn(0f, 1f)
                    val currentR = maxR * p.startRadius * (1f - progress)

                    val px = cx + cos(angle).toFloat() * currentR
                    val py = cy + sin(angle).toFloat() * currentR

                    // Trail
                    val trailLen = maxR * 0.08f * (1f - progress)
                    val trailStartX = px + cos(angle).toFloat() * trailLen
                    val trailStartY = py + sin(angle).toFloat() * trailLen

                    val pColor = when (p.hue) {
                        0 -> cyanPrimary
                        1 -> bluePrimary
                        else -> Color.White
                    }
                    val pAlpha = (0.3f + 0.5f * progress).coerceIn(0f, 1f)

                    // Trail line
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, pColor.copy(alpha = pAlpha * 0.4f)),
                            start = Offset(trailStartX, trailStartY),
                            end = Offset(px, py)
                        ),
                        start = Offset(trailStartX, trailStartY),
                        end = Offset(px, py),
                        strokeWidth = p.size * 0.5f * density
                    )

                    // Particle dot
                    drawCircle(
                        color = pColor.copy(alpha = pAlpha),
                        radius = p.size * density * 0.4f,
                        center = Offset(px, py)
                    )

                    // Glow
                    if (p.hue == 0) {
                        drawCircle(
                            color = pColor.copy(alpha = pAlpha * 0.15f),
                            radius = p.size * density * 1.5f,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════
        //  LAYER 3: Energy core
        // ═══════════════════════════════════
        if (coreAlpha > 0.01f) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = coreScale
                        scaleY = coreScale
                        alpha = coreAlpha
                    }
            ) {
                val cx = center.x
                val cy = center.y

                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            cyanPrimary.copy(alpha = 0.15f + coreGlow * 0.2f),
                            bluePrimary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy)
                    ),
                    radius = 55f * density,
                    center = Offset(cx, cy)
                )

                // Mid ring
                drawCircle(
                    color = cyanPrimary.copy(alpha = 0.2f + coreGlow * 0.3f),
                    radius = 30f * density,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f * density)
                )

                // Inner ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f + coreGlow * 0.25f),
                    radius = 18f * density,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f * density)
                )

                // Core bright center
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.6f + coreGlow * 0.4f),
                            cyanPrimary.copy(alpha = 0.3f + coreGlow * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy)
                    ),
                    radius = 12f * density,
                    center = Offset(cx, cy)
                )

                // Rotating energy spokes
                val spokeCount = 6
                for (i in 0 until spokeCount) {
                    val a = (timeVal * 360f + i * 60f) * PI / 180.0
                    val innerR = 18f * density
                    val outerR = 30f * density
                    val spokeAlpha = 0.15f + coreGlow * 0.2f
                    drawLine(
                        color = cyanPrimary.copy(alpha = spokeAlpha),
                        start = Offset(
                            cx + cos(a).toFloat() * innerR,
                            cy + sin(a).toFloat() * innerR
                        ),
                        end = Offset(
                            cx + cos(a).toFloat() * outerR,
                            cy + sin(a).toFloat() * outerR
                        ),
                        strokeWidth = 1f * density,
                        cap = StrokeCap.Round
                    )
                }

                // Orbiting dots
                for (i in 0..2) {
                    val a = (timeVal * 360f * 1.5f + i * 120f) * PI / 180.0
                    val orbR = 25f * density
                    drawCircle(
                        color = cyanPrimary.copy(alpha = 0.5f + coreGlow * 0.3f),
                        radius = 2f * density,
                        center = Offset(
                            cx + cos(a).toFloat() * orbR,
                            cy + sin(a).toFloat() * orbR
                        )
                    )
                }
            }
        }

        // ═══════════════════════════════════
        //  LAYER 4: Burst shockwaves
        // ═══════════════════════════════════
        if (burstRadius > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxR = size.minDimension * 0.55f

                if (burstAlpha > 0.01f) {
                    val r1 = maxR * burstRadius
                    drawCircle(
                        color = cyanPrimary.copy(alpha = burstAlpha * 0.6f),
                        radius = r1,
                        center = center,
                        style = Stroke(width = 4f * density * (1f - burstRadius))
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color.Transparent,
                                cyanPrimary.copy(alpha = burstAlpha * 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r1 * 1.2f
                        ),
                        radius = r1 * 1.2f,
                        center = center
                    )
                }

                if (burst2Alpha > 0.01f) {
                    val r2 = maxR * 0.7f * burst2Radius
                    drawCircle(
                        color = bluePrimary.copy(alpha = burst2Alpha * 0.5f),
                        radius = r2,
                        center = center,
                        style = Stroke(width = 3f * density * (1f - burst2Radius))
                    )
                }

                // Burst debris
                if (burstRadius > 0.1f) {
                    for (i in 0 until 20) {
                        val a = i * 18.0 * PI / 180.0
                        val dist = maxR * burstRadius * (0.4f + (i % 4) * 0.15f)
                        val px = center.x + cos(a).toFloat() * dist
                        val py = center.y + sin(a).toFloat() * dist
                        val pSize = (2f + (i % 3)) * density * (1f - burstRadius)
                        val pColor = if (i % 2 == 0) cyanPrimary else bluePrimary
                        if (pSize > 0.5f) {
                            drawCircle(
                                color = pColor.copy(alpha = (1f - burstRadius) * 0.6f),
                                radius = pSize,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }

        // Flash
        if (burstFlashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(burstFlashAlpha)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, cyanPrimary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }

        // ═══════════════════════════════════
        //  LAYER 5: Product cards (radial orbit)
        // ═══════════════════════════════════
        products.forEachIndexed { i, card ->
            val progress = cardAnimations[i].value
            val hideProgress = cardHideAnimations[i].value
            val effective = progress * hideProgress

            if (effective > 0.01f) {
                val baseAngle = card.angle + cardsOrbit
                val rad = baseAngle * PI / 180.0
                val dist = card.radius * effective
                val ox = cos(rad).toFloat() * dist
                val oy = sin(rad).toFloat() * dist

                // Trail from center
                if (progress < 0.9f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val trailEnd = Offset(
                            center.x + ox * density * 0.28f,
                            center.y + oy * density * 0.28f
                        )
                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(Color.Transparent, card.color.copy(alpha = 0.3f * effective)),
                                start = center,
                                end = trailEnd
                            ),
                            start = center,
                            end = trailEnd,
                            strokeWidth = 2f * density
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = (ox).dp, y = (oy).dp)
                        .graphicsLayer {
                            scaleX = 0.3f + 0.7f * effective
                            scaleY = 0.3f + 0.7f * effective
                            alpha = effective
                            rotationY = cos(rad).toFloat() * 15f
                            rotationX = -sin(rad).toFloat() * 8f
                            cameraDistance = 12f * density
                        }
                        .width(82.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0C1428).copy(alpha = 0.95f),
                                    Color(0xFF080E1E).copy(alpha = 0.95f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            Brush.radialGradient(
                                listOf(card.color.copy(alpha = 0.06f), Color.Transparent)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier
                                .size(38.dp)
                                .alpha(0.3f)
                        ) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(card.color.copy(alpha = 0.5f), Color.Transparent)
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                        Icon(
                            imageVector = card.icon,
                            contentDescription = card.label,
                            tint = card.color,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = card.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // ═══════════════════════════════════
        //  LAYER 6: Logo + text reveal
        // ═══════════════════════════════════
        if (logoAlpha > 0.01f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    // Glow behind
                    Canvas(
                        modifier = Modifier
                            .size(240.dp)
                            .alpha(pulseVal * 0.2f)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(cyanPrimary.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    // Back swoosh
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect(top = 0f, bottom = size.height * 0.46f) {
                            drawOrbitalSwoosh(swooshSweep, cyanPrimary)
                        }
                    }

                    Text(
                        text = "ON",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    // Front swoosh
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect(top = size.height * 0.46f, bottom = size.height) {
                            drawOrbitalSwoosh(swooshSweep, cyanPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .alpha(textAlpha)
                            .graphicsLayer { translationY = textSlide * density }
                    ) {
                        Text("ON", fontSize = 38.sp, fontWeight = FontWeight.Black, color = AccentYellow)
                        Text("-", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f))
                        "SERVER".forEach { c ->
                            Text(c.toString(), fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Text("1", fontSize = 38.sp, fontWeight = FontWeight.Black, color = AccentYellow)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width((160 * textAlpha).dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, cyanPrimary, AccentYellow, cyanPrimary, Color.Transparent)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Integrated Digital Services",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .graphicsLayer { translationY = textSlide * density }
                )
            }
        }
    }
}

private fun DrawScope.drawOrbitalSwoosh(sweep: Float, color: Color) {
    rotate(-22f) {
        val cx = center.x
        val cy = center.y
        val ellipseW = size.width * 0.95f
        val ellipseH = size.height * 0.38f
        val left = cx - ellipseW / 2f
        val top = cy - ellipseH / 2f

        val segments = 30
        val baseStroke = 6.dp.toPx()
        for (i in 0 until segments) {
            val frac = i.toFloat() / segments
            val angle = frac * 360f
            if (angle > sweep) break
            val segLen = minOf(360f / segments + 1f, sweep - angle)
            val thickness = 0.2f + 0.8f * sin(frac.toDouble() * Math.PI).toFloat()
            drawArc(
                color = Color.White,
                startAngle = -90f + angle,
                sweepAngle = segLen,
                useCenter = false,
                style = Stroke(width = baseStroke * thickness, cap = StrokeCap.Butt),
                topLeft = Offset(left, top),
                size = Size(ellipseW, ellipseH)
            )
        }

        val innerW = ellipseW * 0.78f
        val innerH = ellipseH * 0.55f
        val innerLeft = cx - innerW / 2f
        val innerTop = cy - innerH / 2f
        val innerStroke = 2.dp.toPx()
        val innerActualSweep = minOf(220f, maxOf(0f, sweep - 60f))
        if (innerActualSweep > 0f) {
            drawArc(
                color = color.copy(alpha = 0.5f),
                startAngle = -30f,
                sweepAngle = innerActualSweep,
                useCenter = false,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round),
                topLeft = Offset(innerLeft, innerTop),
                size = Size(innerW, innerH)
            )
        }
    }
}
