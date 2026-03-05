package com.onserver1.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.components.BalanceCard
import com.onserver1.app.ui.components.CinematicBanner
import com.onserver1.app.ui.components.RamadanBanner
import com.onserver1.app.ui.components.BankakBanner
import com.onserver1.app.ui.components.ProductCard
import com.onserver1.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onViewAll: () -> Unit,
    onAddBalance: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    // Refresh data every time screen becomes visible
    // Full reload if products are empty (e.g. first login), otherwise just balance
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar - compact
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.hello, state.user?.name ?: ""),
                        fontSize = d.font13,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = d.font20,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.notifications),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        // Balance Strip
        BalanceCard(
            balance = state.user?.balance ?: 0.0,
            onAddBalance = onAddBalance,
            modifier = Modifier.padding(horizontal = d.screenPadding)
        )

        Spacer(modifier = Modifier.height(d.space12))

        // Banner Pager with auto-scroll (3 banners)
        val bannerPagerState = rememberPagerState(pageCount = { 3 })

        // Auto-scroll every 5 seconds
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000L)
                val nextPage = (bannerPagerState.currentPage + 1) % 3
                bannerPagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }

        HorizontalPager(
            state = bannerPagerState,
            modifier = Modifier
                .padding(horizontal = d.screenPadding)
                .fillMaxWidth()
                .height(d.bannerHeight)
        ) { page ->
            when (page) {
                0 -> CinematicBanner(
                    modifier = Modifier.fillMaxSize()
                )
                1 -> BankakBanner(
                    modifier = Modifier.fillMaxSize()
                )
                2 -> RamadanBanner(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = d.space8),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val isActive = bannerPagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = d.space2)
                        .height(d.space4)
                        .width(if (isActive) d.space16 else d.space4)
                        .clip(CircleShape)
                        .background(
                            if (isActive) AccentYellow
                            else AccentYellow.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space12))

        // Programs & Software section
        Text(
            text = stringResource(R.string.programs_software),
            fontSize = d.font16,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = d.screenPadding),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(d.space8))

        LazyRow(
            contentPadding = PaddingValues(horizontal = d.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(d.space12)
        ) {
            item { SoftwareItem(icon = Icons.Default.Build, label = stringResource(R.string.tools_activations), d = d, onClick = { onNavigateToScreen("services") }) }
            item { SoftwareItem(icon = Icons.Default.LockOpen, label = stringResource(R.string.network_unlock), d = d, onClick = { onNavigateToScreen("games") }) }
            item { SoftwareItem(icon = Icons.Default.NoEncryption, label = stringResource(R.string.icloud_services), d = d, onClick = { onNavigateToScreen("games") }) }
            item { SoftwareItem(icon = Icons.Default.SupportAgent, label = stringResource(R.string.remote_services_short), d = d, onClick = { onNavigateToScreen("remote_services") }) }
            item { SoftwareItem(icon = Icons.Default.SportsEsports, label = stringResource(R.string.cards_games), d = d, onClick = { onNavigateToScreen("services") }) }
            item { SoftwareItem(icon = Icons.Default.Subscriptions, label = stringResource(R.string.digital_subs), d = d, onClick = { onNavigateToScreen("services") }) }
        }

        Spacer(modifier = Modifier.height(d.space16))

        // Featured Products Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.featured_products),
                fontSize = d.font16,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = onViewAll) {
                Text(stringResource(R.string.view_all), color = AccentYellow, fontSize = d.font12)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = AccentYellow,
                    modifier = Modifier.size(d.icon16)
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space6))

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.productCardHeight),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentYellow)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = d.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(d.space10)
            ) {
                items(state.featuredProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(d.space16))
    }
}

@Composable
fun SoftwareItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    d: com.onserver1.app.ui.theme.Dimens,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(d.quickActionSize + 8.dp)
            .clip(RoundedCornerShape(d.corner12))
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = RoundedCornerShape(d.corner12),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.size(d.quickActionSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BalanceGradientStart,
                                BalanceGradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AccentYellow,
                    modifier = Modifier.size(d.icon28)
                )
            }
        }
        Spacer(modifier = Modifier.height(d.space4))
        Text(
            text = label,
            fontSize = d.font11,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
