package com.onserver1.app.ui.screens.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.data.model.Order
import com.onserver1.app.ui.theme.*
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    // Auto-refresh every 30s if there are active (PENDING/PROCESSING) orders
    val hasActiveOrders = state.orders.any {
        it.status.lowercase() in listOf("pending", "processing")
    }
    LaunchedEffect(hasActiveOrders) {
        while (hasActiveOrders) {
            delay(30_000)
            viewModel.refresh()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.my_orders),
                    fontSize = d.font20,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.size(d.icon24)
                    )
                }
            }
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentYellow)
                }
            }

            state.orders.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(d.icon48),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(d.space12))
                        Text(
                            text = stringResource(R.string.no_orders),
                            fontSize = d.font16,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = d.screenPadding,
                            vertical = d.space8
                        ),
                        verticalArrangement = Arrangement.spacedBy(d.space10)
                    ) {
                        items(state.orders) { order ->
                            OrderCard(order = order, d = d)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, d: Dimens) {
    val statusColor = when (order.status.lowercase()) {
        "completed", "delivered" -> SuccessGreen
        "pending", "processing" -> WarningOrange
        "rejected" -> ErrorRed
        else -> TextGray
    }

    val statusText = when (order.status.lowercase()) {
        "completed" -> stringResource(R.string.status_completed)
        "delivered" -> stringResource(R.string.status_delivered)
        "pending" -> stringResource(R.string.status_pending)
        "processing" -> stringResource(R.string.status_processing)
        "rejected" -> stringResource(R.string.status_rejected)
        else -> order.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(d.corner12),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(d.cardPadding)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(d.icon20),
                        tint = AccentYellow
                    )
                    Spacer(modifier = Modifier.width(d.space8))
                    Text(
                        text = "#${order.orderNumber}",
                        fontSize = d.font15,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(d.corner8),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        fontSize = d.font11,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = d.space8, vertical = d.space4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(d.space10))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(d.space10))

            // Items summary
            order.items?.let { items ->
                items.take(3).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = d.space2),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.product?.name ?: stringResource(R.string.product),
                            fontSize = d.font13,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "×${item.quantity}",
                            fontSize = d.font13,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    // Show submitted field value (IMEI, lock code, etc.)
                    val fieldDisplay = extractFieldDisplay(item.imei, item.metadata)
                    if (fieldDisplay != null) {
                        Text(
                            text = fieldDisplay,
                            fontSize = d.font12,
                            color = AccentYellow.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = d.space4, bottom = d.space4)
                        )
                    }
                }
                if (items.size > 3) {
                    Text(
                        text = stringResource(R.string.more_items, items.size - 3),
                        fontSize = d.font12,
                        color = AccentYellow,
                        modifier = Modifier.padding(top = d.space4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(d.space10))

            // Footer: total + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(order.createdAt),
                    fontSize = d.font12,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    fontSize = d.font16,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow
                )
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = input.parse(dateStr.substringBefore('.'))
        output.format(date!!)
    } catch (e: Exception) {
        dateStr.substringBefore('T')
    }
}

private fun extractFieldDisplay(imei: String?, metadata: String?): String? {
    // Try IMEI first
    if (!imei.isNullOrBlank()) return imei
    // Try to extract from metadata fieldValues
    if (metadata.isNullOrBlank()) return null
    return try {
        val json = JSONObject(metadata)
        val fieldValues = if (json.has("fieldValues")) json.getJSONObject("fieldValues") else json
        val keys = fieldValues.keys()
        if (keys.hasNext()) {
            val key = keys.next()
            val value = fieldValues.getString(key)
            if (value.isNotBlank()) value else null
        } else null
    } catch (_: Exception) { null }
}
