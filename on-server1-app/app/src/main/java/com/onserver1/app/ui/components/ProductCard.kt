package com.onserver1.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onserver1.app.data.model.Product
import com.onserver1.app.ui.theme.*

/** Pick a fallback icon based on service type / group / product name */
private fun resolveServiceIcon(product: Product): ImageVector {
    val name = product.name.lowercase()
    val group = (product.groupName ?: "").lowercase()
    val svcType = (product.serviceType ?: "").uppercase()

    return when {
        // Games & gift cards
        name.contains("playstation") || name.contains("psn") || name.contains("xbox") ||
        name.contains("nintendo") || name.contains("game") || name.contains("steam") ||
        name.contains("roblox") || name.contains("pubg") || name.contains("fortnite") ||
        group.contains("game") -> Icons.Default.SportsEsports

        // Streaming & entertainment
        name.contains("netflix") || name.contains("spotify") || name.contains("disney") ||
        name.contains("youtube") || name.contains("hulu") || name.contains("apple tv") ||
        name.contains("streaming") -> Icons.Default.OndemandVideo

        // Gift cards & top-up
        name.contains("gift card") || name.contains("itunes") || name.contains("google play") ||
        name.contains("amazon") || name.contains("razer gold") || name.contains("card") ||
        name.contains("voucher") || name.contains("topup") || name.contains("top-up") -> Icons.Default.CardGiftcard

        // Phone / IMEI related
        svcType == "IMEI" || name.contains("unlock") || name.contains("imei") ||
        name.contains("frp") || name.contains("icloud") -> Icons.Default.PhoneAndroid

        // MDM / Bypass
        name.contains("mdm") || name.contains("bypass") || name.contains("activat") -> Icons.Default.LockOpen

        // Server / remote
        svcType == "SERVER" || svcType == "REMOTE" -> Icons.Default.Dns

        // Social media
        name.contains("instagram") || name.contains("tiktok") || name.contains("twitter") ||
        name.contains("facebook") || name.contains("snapchat") -> Icons.Default.Share

        // Crypto / wallet
        name.contains("usdt") || name.contains("crypto") || name.contains("bitcoin") ||
        name.contains("wallet") -> Icons.Default.AccountBalanceWallet

        else -> Icons.Default.ShoppingBag
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val d = LocalDimens.current

    Card(
        onClick = onClick,
        modifier = modifier
            .width(d.productCardWidth)
            .height(d.productCardHeight),
        shape = RoundedCornerShape(d.corner12),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Product Image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.productImageHeight)
            ) {
                val hasImage = !product.image.isNullOrBlank()

                if (hasImage) {
                    SubcomposeAsyncImage(
                        model = product.image,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = d.corner12, topEnd = d.corner12)),
                        error = {
                            // Image failed to load — show icon fallback
                            ServiceIconPlaceholder(product)
                        }
                    )
                } else {
                    // No image URL — show icon fallback
                    ServiceIconPlaceholder(product)
                }

                // Subtle bottom gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(d.space32)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CardDark.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
                // Category badge
                product.category?.let { cat ->
                    Surface(
                        modifier = Modifier
                            .padding(d.space6)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(d.corner8),
                        color = AccentYellow.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = cat.icon ?: "",
                            fontSize = d.font12,
                            modifier = Modifier.padding(horizontal = d.space6, vertical = d.space2)
                        )
                    }
                }
            }

            // Product Info - compact
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = d.space8, vertical = d.space6),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) (product.nameAr ?: product.name) else product.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = d.font12,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    lineHeight = d.lineHeight22
                )

                Spacer(modifier = Modifier.height(d.space4))

                Text(
                    text = "$${product.price}",
                    fontSize = d.font15,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow
                )
            }
        }
    }
}

@Composable
private fun ServiceIconPlaceholder(product: Product) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF243447),
                        CardDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = resolveServiceIcon(product),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AccentYellow.copy(alpha = 0.6f)
        )
    }
}
