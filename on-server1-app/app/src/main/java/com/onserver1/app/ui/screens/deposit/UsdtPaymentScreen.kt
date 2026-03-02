package com.onserver1.app.ui.screens.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsdtPaymentScreen(
    amount: Double,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: DepositViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current
    val context = LocalContext.current
    var txHash by remember { mutableStateOf("") }

    // Navigate back on success after delay
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            onDone()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "USDT ${stringResource(R.string.deposit_payment)}",
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentYellow)
                }
            }

            else -> {
                val walletAddress = state.gatewayInfo?.usdt?.walletAddress ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = d.screenPadding)
                ) {
                    Spacer(modifier = Modifier.height(d.space4))

                    // Amount + Network row (compact)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF26A17B).copy(alpha = 0.12f),
                                RoundedCornerShape(d.corner12)
                            )
                            .padding(horizontal = d.space12, vertical = d.space10),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.deposit_amount_to_send),
                                fontSize = d.font11,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$${String.format("%.2f", amount)} USDT",
                                fontSize = d.font20,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF26A17B)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF26A17B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "BEP20",
                                fontSize = d.font11,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF26A17B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space12))

                    // Wallet Address + QR side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(d.space10)
                    ) {
                        // QR Code (small card)
                        if (walletAddress.isNotEmpty()) {
                            val qrBitmap = remember(walletAddress) {
                                generateQrCode(walletAddress, 512)
                            }
                            Card(
                                shape = RoundedCornerShape(d.corner12),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    qrBitmap?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(90.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }

                        // Address card
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.deposit_wallet_address),
                                fontSize = d.font11,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(d.space4))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("wallet", walletAddress))
                                        Toast.makeText(context, context.getString(R.string.deposit_copied), Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(d.corner12),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = d.space10, vertical = d.space8),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = walletAddress,
                                        fontSize = d.font11,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = d.lineHeight22
                                    )
                                    Spacer(modifier = Modifier.width(d.space4))
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(d.space4))
                            Text(
                                text = stringResource(R.string.deposit_scan_qr),
                                fontSize = d.font11,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space12))

                    // Instructions (compact)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = CardDefaults.cardColors(
                            containerColor = AccentYellow.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(d.space10)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info, contentDescription = null,
                                    tint = AccentYellow, modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(d.space4))
                                Text(
                                    text = stringResource(R.string.deposit_usdt_instructions_title),
                                    fontSize = d.font12,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(d.space4))
                            Text(
                                text = stringResource(R.string.deposit_usdt_instructions),
                                fontSize = d.font11,
                                lineHeight = d.lineHeight22,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))

                    // TX Hash Input
                    Text(
                        text = stringResource(R.string.deposit_tx_hash),
                        fontSize = d.font12,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(d.space4))

                    OutlinedTextField(
                        value = txHash,
                        onValueChange = { txHash = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0x...", fontSize = d.font12) },
                        leadingIcon = {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                        },
                        shape = RoundedCornerShape(d.corner12),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = d.font12),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            cursorColor = AccentYellow
                        )
                    )

                    Spacer(modifier = Modifier.height(d.space8))

                    // Error
                    if (state.error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(d.corner12),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = state.error!!,
                                fontSize = d.font11,
                                color = Color.Red,
                                modifier = Modifier.padding(d.space10)
                            )
                        }
                        Spacer(modifier = Modifier.height(d.space4))
                    }

                    // Success
                    if (state.successMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(d.corner12),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26A17B).copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(d.space10),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF26A17B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(d.space4))
                                Text(
                                    text = state.successMessage!!,
                                    fontSize = d.font12,
                                    color = Color(0xFF26A17B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(d.space4))
                    }

                    Spacer(modifier = Modifier.height(d.space12))

                    // Submit button
                    Button(
                        onClick = {
                            viewModel.submitUsdtDeposit(amount, txHash)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = txHash.length >= 60 && !state.isSubmitting && state.successMessage == null,
                        shape = RoundedCornerShape(d.corner12),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26A17B),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF26A17B).copy(alpha = 0.3f)
                        )
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(d.space4))
                            Text(
                                text = stringResource(R.string.deposit_verify_and_submit),
                                fontSize = d.font13,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))
                }
            }
        }
    }
}

private fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
