package com.onserver1.app.ui.screens.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.theme.AccentYellow
import com.onserver1.app.ui.theme.ErrorRed
import com.onserver1.app.ui.theme.LocalDimens
import com.onserver1.app.ui.theme.SuccessGreen
import com.onserver1.app.util.FieldTranslator

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
        OrderSuccessDialog(
            orderNumber = state.orderResult?.orderNumber,
            onDismiss = { onOrderSuccess() }
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
            // Product Info Card - Header (Name + Price always visible)
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

                    // Collapsible Details Section
                    val hasDescription = product.description?.isNotBlank() == true
                    if (hasDescription) {
                        var detailsExpanded by remember { mutableStateOf(false) }
                        val arrowRotation by animateFloatAsState(
                            targetValue = if (detailsExpanded) 180f else 0f,
                            animationSpec = tween(300),
                            label = "arrow"
                        )

                        Spacer(Modifier.height(d.space8))

                        // Toggle row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { detailsExpanded = !detailsExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AccentYellow
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.product_details),
                                fontSize = d.font14,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(arrowRotation),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Expandable content
                        AnimatedVisibility(
                            visible = detailsExpanded,
                            enter = expandVertically(animationSpec = tween(300)),
                            exit = shrinkVertically(animationSpec = tween(300))
                        ) {
                            val descText = if (isArabic) (product.descriptionAr ?: product.description!!) else product.description!!
                            SelectionContainer {
                                Text(
                                    text = descText.replace("\\n", "\n"),
                                    fontSize = d.font14,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(12.dp)
                                )
                            }
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
                            val rawLabel = field.name.trim().ifBlank { field.key }
                            val label = if (isArabic) FieldTranslator.translate(rawLabel) else rawLabel
                            val isTextArea = field.type.equals("TEXTAREA", ignoreCase = true)
                            val maxLen = viewModel.getFieldMaxLength(field)
                            val currentValue = state.fieldValues[field.key] ?: ""

                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { newVal ->
                                    viewModel.updateFieldValue(field.key, newVal)
                                },
                                label = { Text("$label${if (field.required) " *" else ""}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(d.corner12),
                                singleLine = !isTextArea,
                                maxLines = if (isTextArea) 4 else 1,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentYellow,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = AccentYellow,
                                    focusedLabelColor = AccentYellow,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                supportingText = if (maxLen > 0) {
                                    { Text("${currentValue.length}/$maxLen", fontSize = d.font11, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                } else null
                            )
                        }
                    }
                }
                Spacer(Modifier.height(d.space16))
            }

            // Quantity selector (if supports quantity)
            if (product.supportsQnt) {
                var qtyText by remember(state.quantity) { mutableStateOf("${state.quantity}") }
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = { viewModel.updateQuantity(state.quantity - 1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, null)
                            }
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = { newVal ->
                                    // Allow only digits
                                    val filtered = newVal.filter { it.isDigit() }
                                    qtyText = filtered
                                    val parsed = filtered.toIntOrNull()
                                    if (parsed != null && parsed > 0) {
                                        viewModel.updateQuantity(parsed)
                                    }
                                },
                                modifier = Modifier.width(90.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = d.font18,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(d.corner12),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentYellow,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                )
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

@Composable
private fun OrderSuccessDialog(
    orderNumber: String?,
    onDismiss: () -> Unit
) {
    // Animations
    val scaleAnim = remember { Animatable(0f) }
    val checkAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f))
        checkAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(scaleAnim.value)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success icon with glow
                Box(contentAlignment = Alignment.Center) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(checkAnim.value)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f))
                    )
                    // Inner circle
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .scale(checkAnim.value)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SuccessGreen, Color(0xFF2E7D32))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Title
                Text(
                    text = stringResource(R.string.order_success_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.order_success_message),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                // Order number card
                if (orderNumber != null) {
                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.order_number),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = orderNumber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentYellow,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // OK Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.dialog_ok),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
