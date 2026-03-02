package com.onserver1.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@Composable
fun BalanceCard(
    balance: Double,
    onAddBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val d = LocalDimens.current

    // --- Animated balance counting ---
    val animatedBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "balanceAnim"
    )

    // --- Shimmer animation ---
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // --- Wallet icon pulse ---
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AccentYellow.copy(alpha = 0.5f),
                        AccentYellow.copy(alpha = 0.2f),
                        AccentYellow.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(d.corner16)
            ),
        shape = RoundedCornerShape(d.corner16),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
        ) {
            // Shimmer overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                AccentYellow.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerOffset, 0f),
                            end = Offset(shimmerOffset + 200f, 0f)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = d.screenPadding, vertical = d.space12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: wallet icon + balance
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing wallet icon
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size((d.icon20.value * pulseScale * 1.8f).dp)
                                .clip(CircleShape)
                                .background(AccentYellow.copy(alpha = 0.12f))
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(d.icon20)
                        )
                    }
                    Spacer(modifier = Modifier.width(d.space10))
                    Column {
                        Text(
                            text = stringResource(R.string.balance),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = d.font11,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", animatedBalance.toDouble())}",
                            color = Color.White,
                            fontSize = d.font22,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right side: add balance button
                FilledTonalButton(
                    onClick = onAddBalance,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(d.corner8),
                    contentPadding = PaddingValues(horizontal = d.space12, vertical = d.space6)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(d.icon16)
                    )
                    Spacer(modifier = Modifier.width(d.space4))
                    Text(
                        text = stringResource(R.string.add_balance),
                        fontSize = d.font12,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
