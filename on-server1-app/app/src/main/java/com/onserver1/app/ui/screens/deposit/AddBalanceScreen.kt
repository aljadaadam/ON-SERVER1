package com.onserver1.app.ui.screens.deposit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBalanceScreen(
    onBack: () -> Unit,
    onNavigateToUsdt: (Double) -> Unit,
    onNavigateToBankak: (Double) -> Unit,
    viewModel: DepositViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current
    var amount by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.add_balance),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = d.screenPadding)
        ) {
            Spacer(modifier = Modifier.height(d.space8))

            // Amount Input Section
            Text(
                text = stringResource(R.string.deposit_enter_amount),
                fontSize = d.font15,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(d.space8))

            OutlinedTextField(
                value = amount,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amount = newVal
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = AccentYellow)
                },
                suffix = { Text("USD", fontWeight = FontWeight.Bold, color = AccentYellow) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(d.corner12),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    cursorColor = AccentYellow
                )
            )

            // Quick amount buttons
            Spacer(modifier = Modifier.height(d.space12))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(d.space8)
            ) {
                listOf("10", "25", "50", "100").forEach { quickAmount ->
                    FilterChip(
                        onClick = { amount = quickAmount },
                        label = { Text("$$quickAmount", fontSize = d.font12) },
                        selected = amount == quickAmount,
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentYellow,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(d.space24))

            // Payment Method Section
            Text(
                text = stringResource(R.string.deposit_select_method),
                fontSize = d.font15,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(d.space12))

            // USDT Gateway - show only if active in gateways list
            val activeGateways = state.gatewayInfo?.gateways
            val isUsdtActive = activeGateways == null || activeGateways.any {
                it.nameEn.equals("USDT", ignoreCase = true) || it.name.equals("USDT", ignoreCase = true)
            }
            val isBankakActive = activeGateways == null || activeGateways.any {
                it.nameEn.equals("Bankak", ignoreCase = true) || it.name.contains("بنك")
            }

            if (isUsdtActive) {
                GatewayCard(
                    title = "USDT",
                    subtitle = stringResource(R.string.deposit_usdt_desc),
                    icon = Icons.Default.CurrencyBitcoin,
                    isSelected = selectedGateway == "USDT",
                    gradientColors = listOf(Color(0xFF26A17B), Color(0xFF1A7A5C)),
                    onClick = { selectedGateway = "USDT" },
                    d = d
                )
                Spacer(modifier = Modifier.height(d.space12))
            }

            if (isBankakActive) {
                // Bankak Gateway
                GatewayCard(
                    title = stringResource(R.string.deposit_bankak),
                    subtitle = stringResource(R.string.deposit_bankak_desc),
                    imageUrl = "https://6990ab01681c79fa0bccfe99.imgix.net/bank.png",
                    isSelected = selectedGateway == "BANKAK",
                    gradientColors = listOf(Color(0xFFE52228), Color(0xFFC41E22)),
                    onClick = { selectedGateway = "BANKAK" },
                    d = d
                )
                Spacer(modifier = Modifier.height(d.space12))
            }

            // Show any additional gateways from the API (non USDT/Bankak)
            activeGateways?.filter { gw ->
                !gw.nameEn.equals("USDT", ignoreCase = true) &&
                !gw.name.equals("USDT", ignoreCase = true) &&
                !gw.nameEn.equals("Bankak", ignoreCase = true) &&
                !gw.name.contains("بنك")
            }?.forEach { gw ->
                val gwColor = try {
                    Color(android.graphics.Color.parseColor(gw.color ?: "#6B7280"))
                } catch (_: Exception) { Color(0xFF6B7280) }
                val gwColorDark = gwColor.copy(alpha = 0.8f)

                GatewayCard(
                    title = gw.name,
                    subtitle = gw.nameEn ?: gw.type,
                    icon = Icons.Default.AccountBalance,
                    isSelected = selectedGateway == gw.nameEn?.uppercase(),
                    gradientColors = listOf(gwColor, gwColorDark),
                    onClick = { selectedGateway = gw.nameEn?.uppercase() ?: gw.name },
                    d = d
                )
                Spacer(modifier = Modifier.height(d.space12))
            }

            Spacer(modifier = Modifier.height(d.space24))

            // Exchange rate info for Bankak
            if (selectedGateway == "BANKAK" && state.gatewayInfo != null) {
                val rate = state.gatewayInfo!!.bankak.exchangeRate
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                val localAmount = amountVal * rate

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(d.corner12),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(d.space12)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.deposit_exchange_rate),
                                fontSize = d.font13,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "1 USD = ${rate.toInt()} SDG",
                                fontSize = d.font13,
                                fontWeight = FontWeight.Bold,
                                color = AccentYellow
                            )
                        }
                        if (amountVal > 0) {
                            Spacer(modifier = Modifier.height(d.space4))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.deposit_amount_local),
                                    fontSize = d.font13,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${String.format("%,.0f", localAmount)} SDG",
                                    fontSize = d.font13,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(d.space16))
            }

            // Continue Button
            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            val isValid = parsedAmount > 0 && selectedGateway != null

            Button(
                onClick = {
                    when (selectedGateway) {
                        "USDT" -> onNavigateToUsdt(parsedAmount)
                        "BANKAK" -> onNavigateToBankak(parsedAmount)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isValid,
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = AccentYellow.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = stringResource(R.string.deposit_continue),
                    fontSize = d.font16,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(d.space24))
        }
    }
}

@Composable
fun GatewayCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    imageUrl: String? = null,
    isSelected: Boolean,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    d: Dimens
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    2.dp, AccentYellow, RoundedCornerShape(d.corner12)
                ) else Modifier
            ),
        shape = RoundedCornerShape(d.corner12),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
                .padding(d.space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon ?: Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(d.icon24)
                    )
                }
            }

            Spacer(modifier = Modifier.width(d.space12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = d.font16,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = d.font12,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(d.icon24)
                )
            }
        }
    }
}
