package com.onserver1.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.onserver1.app.BuildConfig
import com.onserver1.app.R
import com.onserver1.app.ui.components.BalanceCard
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAddBalance: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    // Refresh balance every time screen becomes visible
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadProfile()
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
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.my_account),
                    fontSize = d.font20,
                    fontWeight = FontWeight.Bold
                )
            }
        )

        // Profile Header - compact
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding, vertical = d.space4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            val avatarUrl = remember(state.user?.avatar) {
                state.user?.avatar?.let { path ->
                    val base = BuildConfig.BASE_URL.removeSuffix("/api/").removeSuffix("/api")
                    "$base$path"
                }
            }

            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(d.avatarSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(d.avatarSize),
                    shape = CircleShape,
                    color = AccentYellow
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(d.icon40),
                            tint = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(d.space8))

            Text(
                text = state.user?.name ?: stringResource(R.string.user_default),
                fontSize = d.font20,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = state.user?.email ?: "",
                fontSize = d.font14,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(d.space10))

        // Balance Card
        BalanceCard(
            balance = state.user?.balance ?: 0.0,
            onAddBalance = onNavigateToAddBalance,
            modifier = Modifier.padding(horizontal = d.screenPadding)
        )

        Spacer(modifier = Modifier.height(d.space12))

        // Menu Items
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding),
            shape = RoundedCornerShape(d.corner12),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Outlined.ShoppingBag,
                    title = stringResource(R.string.my_orders),
                    onClick = onNavigateToOrders,
                    d = d
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = d.screenPadding))

                ProfileMenuItem(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = stringResource(R.string.transactions),
                    onClick = onNavigateToTransactions,
                    d = d
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = d.screenPadding))

                ProfileMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = stringResource(R.string.settings),
                    onClick = onNavigateToSettings,
                    d = d
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = d.screenPadding))

                ProfileMenuItem(
                    icon = Icons.Outlined.HelpCenter,
                    title = stringResource(R.string.help_support),
                    onClick = onNavigateToHelp,
                    d = d
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = d.screenPadding))

                ProfileMenuItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_app),
                    onClick = onNavigateToAbout,
                    d = d
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space12))

        // Logout Button
        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding)
                .height(d.buttonHeight),
            shape = RoundedCornerShape(d.corner12),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(d.icon24))
            Spacer(modifier = Modifier.width(d.space8))
            Text(stringResource(R.string.logout), fontSize = d.font15, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(d.space12))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    d: Dimens
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding, vertical = d.space10),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(d.icon24)
            )
            Spacer(modifier = Modifier.width(d.space12))
            Text(
                text = title,
                fontSize = d.font15,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(d.icon20)
            )
        }
    }
}
