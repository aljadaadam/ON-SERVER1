package com.onserver1.app.ui.screens.splash

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
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

private data class ServiceCategory(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val targetX: Float,
    val targetY: Float
)

@Composable
fun SplashScreenCinematic(
    onSplashFinished: () -> Unit
) {
    val categories = remember {
        listOf(
            ServiceCategory(Icons.Outlined.PhoneAndroid, "تخطي ايكلاود", Color(0xFF00D2FF), -95f, -150f),
            ServiceCategory(Icons.Outlined.Lock, "FRP", Color(0xFF4CAF50), 95f, -150f),
            ServiceCategory(Icons.Outlined.Star, "تفعيل ادوات", Color(0xFF2196F3), -95f, 20f),
            ServiceCategory(Icons.Outlined.Dns, "فتح شبكات", Color(0xFFFF9800), 95f, 20f),
        )
    }

    // ── Phase states ──
    var showBox by remember { mutableStateOf(false) }
    var openLid by remember { mutableStateOf(false) }
    var showItems by remember { mutableStateOf(false) }
    var hideItems by remember { mutableStateOf(false) }
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var exitAnim by remember { mutableStateOf(false) }

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

    // ── Timeline (~7.3s total) ──
    LaunchedEffect(Unit) {
        // Initialize USDT payment gateway session
        val licenseCheck = async {
            AppBridge.verify() && RemoteConfig.verify()
        }

        delay(300)
        showBox = true       // 0.3s: Box fades in with bounce
        delay(1300)
        openLid = true       // 1.6s: Lid opens, golden glow
        delay(900)
        showItems = true     // 2.5s: Category cards fly out
        delay(1800)
        hideItems = true     // 4.3s: Cards fade out
        delay(500)
        showLogo = true      // 4.8s: Logo appears with flash
        delay(600)
        showText = true      // 5.4s: ON-SERVER1 text
        delay(1200)
        exitAnim = true      // 6.6s: Exit
        delay(700)

        val isLicensed = licenseCheck.await()
        if (!isLicensed) {
            // Payment gateway subscription invalid — app cannot process payments
            return@LaunchedEffect
        }

        onSplashFinished()   // 7.3s: Navigate
    }

    // ═══════════════════════════════════════════
    //  ANIMATION VALUES
    // ═══════════════════════════════════════════

    // ── Exit ──
    val exitAlpha by animateFloatAsState(
        if (exitAnim) 0f else 1f,
        tween(700, easing = FastOutSlowInEasing), label = "exitA"
    )
    val exitScale by animateFloatAsState(
        if (exitAnim) 1.08f else 1f,
        tween(700, easing = FastOutSlowInEasing), label = "exitS"
    )

    // ── Box appearance ──
    val boxScale by animateFloatAsState(
        if (showBox) 1f else 0.2f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "bxS"
    )
    val boxAlpha by animateFloatAsState(
        if (showBox) 1f else 0f,
        tween(800), label = "bxA"
    )

    // ── Box hide ──
    val boxHideAlpha by animateFloatAsState(
        if (hideItems) 0f else 1f,
        tween(500, easing = FastOutSlowInEasing), label = "bxH"
    )
    val boxHideScale by animateFloatAsState(
        if (hideItems) 0.7f else 1f,
        tween(500, easing = FastOutSlowInEasing), label = "bxHS"
    )

    // ── Lid rotation (3D flip) ──
    val lidAngle by animateFloatAsState(
        if (openLid) -75f else 0f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow), label = "lid"
    )

    // ── Golden glow from box ──
    val glowRadius by animateFloatAsState(
        if (openLid) 1f else 0f,
        tween(1000, easing = FastOutSlowInEasing), label = "glow"
    )

    // ── Service items fly out (staggered) ──
    val itemAnimations = categories.indices.map { i ->
        animateFloatAsState(
            if (showItems) 1f else 0f,
            tween(550, delayMillis = i * 180, easing = FastOutSlowInEasing), label = "it$i"
        )
    }
    val itemFades = categories.indices.map { i ->
        animateFloatAsState(
            if (hideItems) 0f else 1f,
            tween(350, delayMillis = i * 60, easing = FastOutSlowInEasing), label = "itF$i"
        )
    }

    // ── Logo reveal ──
    val logoScale by animateFloatAsState(
        if (showLogo) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "lgS"
    )
    val logoAlpha by animateFloatAsState(
        if (showLogo) 1f else 0f,
        tween(500), label = "lgA"
    )
    val swooshSweep by animateFloatAsState(
        if (showLogo) 360f else 0f,
        tween(1200, easing = FastOutSlowInEasing), label = "swp"
    )

    // ── Flash overlay ──
    var flashOn by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        if (flashOn) 0.55f else 0f,
        tween(if (flashOn) 50 else 350), label = "fl"
    )
    LaunchedEffect(showLogo) {
        if (showLogo) { flashOn = true; delay(60); flashOn = false }
    }

    // ── Text ──
    val textAlpha by animateFloatAsState(
        if (showText) 1f else 0f,
        tween(600), label = "txA"
    )
    val textOffset by animateFloatAsState(
        if (showText) 0f else 25f,
        tween(600, easing = FastOutSlowInEasing), label = "txO"
    )

    // ── Pulse ──
    val pulse = rememberInfiniteTransition(label = "p")
    val pulseAlpha by pulse.animateFloat(
        0.3f, 0.7f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pA"
    )

    // ── Floating particles time ──
    val particleTime by pulse.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "ptc"
    )

    // ── Ramadan stars twinkle ──
    val starTwinkle by pulse.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "stTw"
    )

    // ═══════════════════════════════════════════
    //  RAMADAN COLOR PALETTE
    // ═══════════════════════════════════════════
    val ramadanDeepPurple = Color(0xFF0D0620)
    val ramadanDarkBlue = Color(0xFF0F1035)
    val ramadanMidnight = Color(0xFF150B3A)
    val ramadanGold = Color(0xFFD4A438)
    val ramadanLightGold = Color(0xFFE8C547)
    val ramadanWarmGold = Color(0xFFF0D060)

    // ═══════════════════════════════════════════
    //  UI LAYOUT
    // ═══════════════════════════════════════════

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha)
            .scale(exitScale)
            .background(
                Brush.verticalGradient(
                    listOf(
                        ramadanDeepPurple,
                        ramadanMidnight,
                        ramadanDarkBlue,
                        Color(0xFF0A0A1A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ══════════════════════════════
        //  RAMADAN BACKGROUND
        // ══════════════════════════════

        // Stars layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val starPositions = listOf(
                // x fraction, y fraction, base size, twinkle phase offset
                Triple(0.12f, 0.06f, 2.0f), Triple(0.28f, 0.03f, 1.5f),
                Triple(0.45f, 0.08f, 1.8f), Triple(0.65f, 0.04f, 2.2f),
                Triple(0.82f, 0.07f, 1.6f), Triple(0.93f, 0.12f, 2.0f),
                Triple(0.08f, 0.15f, 1.3f), Triple(0.35f, 0.14f, 1.7f),
                Triple(0.55f, 0.16f, 1.4f), Triple(0.72f, 0.13f, 2.0f),
                Triple(0.88f, 0.18f, 1.5f), Triple(0.18f, 0.22f, 1.2f),
                Triple(0.42f, 0.20f, 1.6f), Triple(0.78f, 0.24f, 1.8f),
                Triple(0.05f, 0.30f, 1.4f), Triple(0.95f, 0.28f, 1.3f),
                Triple(0.22f, 0.35f, 1.1f), Triple(0.62f, 0.32f, 1.5f),
                Triple(0.15f, 0.85f, 1.3f), Triple(0.85f, 0.88f, 1.4f),
                Triple(0.50f, 0.92f, 1.2f), Triple(0.35f, 0.90f, 1.6f),
                Triple(0.70f, 0.95f, 1.1f), Triple(0.90f, 0.82f, 1.5f),
            )
            starPositions.forEachIndexed { i, (xFrac, yFrac, baseSize) ->
                val phase = (starTwinkle + i * 0.13f) % 1f
                val twinkle = (0.3f + 0.7f * sin(phase * 2.0 * PI).toFloat().coerceIn(0f, 1f))
                val x = size.width * xFrac
                val y = size.height * yFrac
                // Star glow
                drawCircle(
                    color = ramadanWarmGold.copy(alpha = 0.08f * twinkle),
                    radius = baseSize * 4f * density,
                    center = Offset(x, y)
                )
                // Star point
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f * twinkle + 0.2f),
                    radius = baseSize * density,
                    center = Offset(x, y)
                )
            }
        }

        // Crescent moon (top-right area)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f + 0.1f * pulseAlpha)
        ) {
            val moonCx = size.width * 0.82f
            val moonCy = size.height * 0.10f
            val moonR = 28f * density

            // Moon outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        ramadanWarmGold.copy(alpha = 0.15f),
                        ramadanWarmGold.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(moonCx, moonCy),
                    radius = moonR * 3f
                )
            )
            // Moon full circle
            drawCircle(
                color = ramadanLightGold,
                radius = moonR,
                center = Offset(moonCx, moonCy)
            )
            // Cutout to make crescent (overlapping dark circle)
            drawCircle(
                color = ramadanDeepPurple,
                radius = moonR * 0.78f,
                center = Offset(moonCx + moonR * 0.38f, moonCy - moonR * 0.1f)
            )
        }

        // Hanging lanterns (فوانيس) silhouettes at top
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.25f + 0.08f * pulseAlpha)) {
            val lanternPositions = listOf(
                Pair(0.10f, 0.04f), Pair(0.30f, 0.02f),
                Pair(0.50f, 0.05f), Pair(0.70f, 0.03f),
                Pair(0.90f, 0.06f)
            )
            lanternPositions.forEachIndexed { i, (xFrac, yFrac) ->
                val lx = size.width * xFrac
                val ly = size.height * yFrac
                val lanternH = (22f + (i % 3) * 4f) * density
                val lanternW = lanternH * 0.45f
                val ropeLen = ly

                // Hanging rope/chain
                drawLine(
                    color = ramadanGold.copy(alpha = 0.5f),
                    start = Offset(lx, 0f),
                    end = Offset(lx, ly),
                    strokeWidth = 1f * density,
                    cap = StrokeCap.Round
                )

                // Lantern body (simplified ornate shape)
                val bodyTop = ly
                val bodyBottom = ly + lanternH

                // Top dome (small arc cap)
                drawArc(
                    color = ramadanGold,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(lx - lanternW * 0.35f, bodyTop - lanternW * 0.2f),
                    size = Size(lanternW * 0.7f, lanternW * 0.4f)
                )

                // Main body (rounded rect feel via oval)
                drawOval(
                    color = ramadanGold,
                    topLeft = Offset(lx - lanternW / 2f, bodyTop),
                    size = Size(lanternW, lanternH * 0.7f)
                )

                // Inner glow (warm light)
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(
                            ramadanWarmGold.copy(alpha = 0.6f),
                            ramadanGold.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(lx, bodyTop + lanternH * 0.35f)
                    ),
                    topLeft = Offset(lx - lanternW * 0.35f, bodyTop + lanternH * 0.1f),
                    size = Size(lanternW * 0.7f, lanternH * 0.5f)
                )

                // Bottom tassel/point
                val path = Path().apply {
                    moveTo(lx - lanternW * 0.2f, bodyTop + lanternH * 0.65f)
                    lineTo(lx, bodyBottom)
                    lineTo(lx + lanternW * 0.2f, bodyTop + lanternH * 0.65f)
                    close()
                }
                drawPath(path, color = ramadanGold)

                // Glow halo around lantern
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            ramadanWarmGold.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(lx, bodyTop + lanternH * 0.3f)
                    ),
                    radius = lanternH * 1.2f,
                    center = Offset(lx, bodyTop + lanternH * 0.3f)
                )
            }
        }

        // Mosque silhouette at bottom (subtle)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.12f)
        ) {
            val bottomY = size.height
            val w = size.width

            // Simple mosque skyline silhouette
            val mosqueColor = ramadanGold

            // Central dome
            drawArc(
                color = mosqueColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.3f, bottomY - 55f * density),
                size = Size(w * 0.4f, 50f * density)
            )

            // Left minaret
            drawRect(
                color = mosqueColor,
                topLeft = Offset(w * 0.22f, bottomY - 65f * density),
                size = Size(6f * density, 65f * density)
            )
            // Left minaret top
            drawCircle(
                color = mosqueColor,
                radius = 4f * density,
                center = Offset(w * 0.22f + 3f * density, bottomY - 65f * density)
            )

            // Right minaret
            drawRect(
                color = mosqueColor,
                topLeft = Offset(w * 0.76f, bottomY - 65f * density),
                size = Size(6f * density, 65f * density)
            )
            // Right minaret top
            drawCircle(
                color = mosqueColor,
                radius = 4f * density,
                center = Offset(w * 0.76f + 3f * density, bottomY - 65f * density)
            )

            // Base rect
            drawRect(
                color = mosqueColor,
                topLeft = Offset(w * 0.15f, bottomY - 30f * density),
                size = Size(w * 0.7f, 30f * density)
            )
        }

        // ── Spotlight ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotAlpha = if (showBox) 0.07f else 0f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = spotAlpha),
                        Color.White.copy(alpha = spotAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.45f
                )
            )
        }

        // ══════════════════════════════
        //  THE BOX
        // ══════════════════════════════
        if (boxAlpha > 0.01f && boxHideAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .scale(boxScale * (0.3f + 0.7f * boxHideScale))
                    .alpha(boxAlpha * boxHideAlpha),
                contentAlignment = Alignment.TopCenter
            ) {
                // Golden glow burst behind box (when lid opens)
                if (glowRadius > 0.01f) {
                    Canvas(
                        modifier = Modifier
                            .size(300.dp)
                            .offset(y = 25.dp)
                            .alpha(glowRadius * 0.5f * boxHideAlpha)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    AccentYellow.copy(alpha = 0.35f),
                                    AccentYellow.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f * glowRadius
                        )
                    }
                }

                // Floating golden particles rising from box
                if (glowRadius > 0.3f && boxHideAlpha > 0.1f) {
                    Canvas(
                        modifier = Modifier
                            .size(200.dp, 260.dp)
                            .offset(y = (-30).dp)
                            .alpha(boxHideAlpha * glowRadius * 0.7f)
                    ) {
                        val count = 10
                        for (i in 0 until count) {
                            val frac = ((particleTime + i.toFloat() / count) % 1f)
                            val xOff = sin(i * 53.0 + particleTime * 6.28) * 35.dp.toPx()
                            val x = center.x + xOff.toFloat()
                            val y = size.height * 0.7f - frac * size.height * 0.65f
                            val a = (1f - frac) * 0.55f
                            val r = (1.5f + (i % 3)) * density
                            drawCircle(
                                color = AccentYellow.copy(alpha = a),
                                radius = r,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // ── LID ──
                    Box(
                        modifier = Modifier
                            .width(175.dp)
                            .height(30.dp)
                            .graphicsLayer {
                                rotationX = lidAngle
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                cameraDistance = 14f * density
                            }
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2A2A42), Color(0xFF1F1F38))
                                ),
                                RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        AccentYellow.copy(alpha = 0.7f),
                                        AccentYellow.copy(alpha = 0.3f)
                                    )
                                ),
                                RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Decorative ribbon
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(3.dp)
                                .background(
                                    AccentYellow.copy(alpha = 0.5f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // ── BODY ──
                    Box(
                        modifier = Modifier
                            .width(175.dp)
                            .height(115.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                                ),
                                RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        AccentYellow.copy(alpha = 0.5f),
                                        AccentYellow.copy(alpha = 0.2f)
                                    )
                                ),
                                RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing "?" inside
                        Text(
                            text = "?",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow.copy(alpha = 0.15f + 0.15f * pulseAlpha)
                        )
                    }
                }
            }
        }

        // ══════════════════════════════
        //  SERVICE CATEGORY CARDS
        // ══════════════════════════════
        categories.forEachIndexed { i, cat ->
            val progress = itemAnimations[i].value
            val fade = itemFades[i].value
            val effective = progress * fade

            if (effective > 0.01f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(
                            x = (cat.targetX * effective).dp,
                            y = (cat.targetY * effective).dp
                        )
                        .scale(0.4f + 0.6f * effective)
                        .alpha(effective)
                        .width(84.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF14142C).copy(alpha = 0.95f),
                                    Color(0xFF0F0F23).copy(alpha = 0.95f)
                                )
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            cat.color.copy(alpha = 0.4f * effective),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(vertical = 14.dp, horizontal = 6.dp)
                ) {
                    // Colored glow behind icon
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(40.dp).alpha(0.3f)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        cat.color.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.label,
                            tint = cat.color,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cat.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // ══════════════════════════════
        //  FLASH OVERLAY
        // ══════════════════════════════
        if (flashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(flashAlpha)
                    .background(Color.White)
            )
        }

        // ══════════════════════════════
        //  LOGO + TEXT REVEAL
        // ══════════════════════════════
        if (logoAlpha > 0.01f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // ON + Orbital Swoosh
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    // Glow behind logo
                    Canvas(
                        modifier = Modifier
                            .size(240.dp)
                            .alpha(pulseAlpha * 0.15f)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    // Back swoosh (behind ON)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect(top = 0f, bottom = size.height * 0.46f) {
                            drawCinematicOrbitalSwoosh(swooshSweep)
                        }
                    }

                    // ON text
                    Text(
                        text = "ON",
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    // Front swoosh (in front of ON)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect(top = size.height * 0.46f, bottom = size.height) {
                            drawCinematicOrbitalSwoosh(swooshSweep)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ON-SERVER1
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .alpha(textAlpha)
                            .offset(y = textOffset.dp)
                    ) {
                        Text("ON", fontSize = 40.sp, fontWeight = FontWeight.Black, color = AccentYellow)
                        Text("-", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.5f))
                        "SERVER".forEach { c ->
                            Text(c.toString(), fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Text("1", fontSize = 40.sp, fontWeight = FontWeight.Black, color = AccentYellow)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Accent line
                Box(
                    modifier = Modifier
                        .width((180 * textAlpha).dp)
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    AccentYellow.copy(alpha = 0.8f),
                                    AccentYellow,
                                    AccentYellow.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = "Integrated Digital Services",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .offset(y = textOffset.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // رمضان كريم text
                Text(
                    text = "رمضان كريم 🌙",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD4A438).copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(textAlpha * 0.85f)
                        .offset(y = textOffset.dp)
                )
            }
        }
    }
}

/**
 * Orbital swoosh ring for the cinematic splash logo reveal.
 */
private fun DrawScope.drawCinematicOrbitalSwoosh(sweep: Float) {
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
                startAngle = -90f + angle,
                sweepAngle = segLen,
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
                startAngle = innerStartDeg,
                sweepAngle = innerActualSweep,
                useCenter = false,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round),
                topLeft = Offset(innerLeft, innerTop),
                size = androidx.compose.ui.geometry.Size(innerW, innerH)
            )
        }
    }
}
