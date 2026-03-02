package com.onserver1.app.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.theme.AccentYellow
import com.onserver1.app.ui.theme.ErrorRed
import com.onserver1.app.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    // Show success dialog
    if (state.orderSuccess) {
        AlertDialog(
            onDismissRequest = { onOrderSuccess() },
            confirmButton = {
                TextButton(onClick = { onOrderSuccess() }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text(stringResource(R.string.order_success_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.order_success_message))
                    state.orderResult?.let { order ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${stringResource(R.string.order_number)}: ${order.orderNumber}",
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow
                        )
                    }
                }
            }
        )
    }

    // Show error snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Auto-clear after showing
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_details), fontSize = d.font18, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentYellow)
            }
            return@Scaffold
        }

        val product = state.product
        if (product == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_products), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(d.screenPadding)
        ) {
            // Product Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner16),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(d.space16)) {
                    // Name
                    Text(
                        text = if (isArabic) (product.nameAr ?: product.name) else product.name,
                        fontSize = d.font20,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(d.space8))

                    // Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${product.price}",
                            fontSize = d.font22,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentYellow
                        )
                        if (product.originalPrice != null && product.originalPrice > product.price) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "$${product.originalPrice}",
                                fontSize = d.font14,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(d.space8))

                    // Service info badges
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.serviceType?.let { sType ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (sType) {
                                    "IMEI" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                    "SERVER" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = sType,
                                    fontSize = d.font12,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = when (sType) {
                                        "IMEI" -> Color(0xFF2196F3)
                                        "SERVER" -> Color(0xFF4CAF50)
                                        else -> Color(0xFFFF9800)
                                    }
                                )
                            }
                        }
                        product.deliveryTime?.let { time ->
                            if (time.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(4.dp))
                                        Text(time, fontSize = d.font12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Description
                    product.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(Modifier.height(d.space12))
                            Text(
                                text = if (isArabic) (product.descriptionAr ?: desc) else desc,
                                fontSize = d.font14,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(d.space16))

            // Custom Fields from backend
            if (state.fields.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(d.corner16),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(d.space16)) {
                        Text(
                            text = stringResource(R.string.required_fields),
                            fontSize = d.font16,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(d.space8))
                        state.fields.forEach { field ->
                            // Determine label: trim name, fallback to key if blank
                            val label = field.name.trim().ifBlank { field.key }
                            val isNumberField = field.type.equals("NUMBER", ignoreCase = true)
                            val isTextArea = field.type.equals("TEXTAREA", ignoreCase = true)

                            OutlinedTextField(
                                value = state.fieldValues[field.key] ?: "",
                                onValueChange = { viewModel.updateFieldValue(field.key, it) },
                                label = { Text("$label${if (field.required) " *" else ""}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(d.corner12),
                                singleLine = !isTextArea,
                                maxLines = if (isTextArea) 4 else 1,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (isNumberField) KeyboardType.Number else KeyboardType.Text
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(d.space16))
            }

            // Quantity selector (if supports quantity)
            if (product.supportsQnt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(d.corner16),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(d.space16)) {
                        Text(
                            text = stringResource(R.string.quantity),
                            fontSize = d.font16,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(d.space8))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledIconButton(
                                onClick = { viewModel.updateQuantity(state.quantity - 1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, null)
                            }
                            Text(
                                text = "${state.quantity}",
                                fontSize = d.font20,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FilledIconButton(
                                onClick = { viewModel.updateQuantity(state.quantity + 1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentYellow)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.Black)
                            }
                        }
                        if (product.minQnt > 0 || product.maxQnt > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(R.string.quantity_range)}: ${product.minQnt} - ${product.maxQnt}",
                                fontSize = d.font12,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(d.space16))
            }

            // Total price
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner16),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(d.space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.total_amount),
                        fontSize = d.font16,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format("%.2f", product.price * state.quantity)}",
                        fontSize = d.font22,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(d.space16))

            // Error message
            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(d.corner12),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(error, fontSize = d.font14, color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(d.space12))
            }

            // Place Order Button
            val totalCost = (product.price) * state.quantity
            val hasEnoughBalance = state.userBalance >= totalCost

            Button(
                onClick = { viewModel.placeOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = !state.isOrdering && hasEnoughBalance
            ) {
                if (state.isOrdering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else if (!hasEnoughBalance) {
                    Text(
                        text = stringResource(R.string.no_enough_balance),
                        fontSize = d.font14,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.place_order),
                        fontSize = d.font16,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(d.space16))
        }
    }
}
