package com.onserver1.app.ui.screens.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankakPaymentScreen(
    amount: Double,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: DepositViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current
    val context = LocalContext.current
    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        receiptUri = uri
    }

    // Navigate back on success
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            onDone()
        }
    }

    val gatewayInfo = state.gatewayInfo?.bankak
    val exchangeRate = gatewayInfo?.exchangeRate ?: 600.0
    val localAmount = amount * exchangeRate

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.deposit_bankak) + " ${stringResource(R.string.deposit_payment)}",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = d.screenPadding)
                ) {
                    Spacer(modifier = Modifier.height(d.space8))

                    // Amount Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1565C0).copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(d.space16),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.deposit_amount_to_transfer),
                                fontSize = d.font13,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${String.format("%,.0f", localAmount)} SDG",
                                fontSize = d.font24,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = "= $${"%.2f".format(amount)} USD",
                                fontSize = d.font13,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(d.space4))
                            Text(
                                text = "${stringResource(R.string.deposit_exchange_rate)}: 1 USD = ${exchangeRate.toInt()} SDG",
                                fontSize = d.font11,
                                color = AccentYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))

                    // Transfer Details
                    Text(
                        text = stringResource(R.string.deposit_bank_details),
                        fontSize = d.font15,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(d.space8))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(d.space16)) {
                            BankInfoRow(
                                label = stringResource(R.string.deposit_account_number),
                                value = gatewayInfo?.accountNumber ?: "",
                                context = context,
                                d = d
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = d.space8),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                            BankInfoRow(
                                label = stringResource(R.string.deposit_account_name),
                                value = gatewayInfo?.accountName ?: "",
                                context = context,
                                d = d
                            )
                            if (!gatewayInfo?.transferNote.isNullOrBlank()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = d.space8),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                BankInfoRow(
                                    label = stringResource(R.string.deposit_comment),
                                    value = gatewayInfo?.transferNote ?: "",
                                    context = context,
                                    d = d
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))

                    // Upload Receipt
                    Text(
                        text = stringResource(R.string.deposit_upload_receipt),
                        fontSize = d.font15,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(d.space8))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { imagePicker.launch("image/*") },
                        shape = RoundedCornerShape(d.corner12),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (receiptUri != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = receiptUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(d.corner12)),
                                    contentScale = ContentScale.Crop
                                )
                                // Change button overlay
                                IconButton(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = AccentYellow.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(d.space8))
                                Text(
                                    text = stringResource(R.string.deposit_tap_to_upload),
                                    fontSize = d.font13,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))

                    // Error
                    if (state.error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(d.corner12),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = state.error!!,
                                fontSize = d.font12,
                                color = Color.Red,
                                modifier = Modifier.padding(d.space12)
                            )
                        }
                        Spacer(modifier = Modifier.height(d.space8))
                    }

                    // Success
                    if (state.successMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(d.corner12),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26A17B).copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(d.space12),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF26A17B))
                                Spacer(modifier = Modifier.width(d.space8))
                                Text(
                                    text = state.successMessage!!,
                                    fontSize = d.font13,
                                    color = Color(0xFF26A17B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(d.space8))
                    }

                    Spacer(modifier = Modifier.height(d.space16))

                    // Submit button
                    Button(
                        onClick = {
                            receiptUri?.let { uri ->
                                // Convert URI to File
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                                inputStream?.use { input ->
                                    file.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                viewModel.submitBankakDeposit(amount, file)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = receiptUri != null && !state.isSubmitting && state.successMessage == null,
                        shape = RoundedCornerShape(d.corner12),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1565C0).copy(alpha = 0.3f)
                        )
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(d.space8))
                            Text(
                                text = stringResource(R.string.deposit_submit_request),
                                fontSize = d.font16,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space24))
                }
            }
        }
    }
}

@Composable
private fun BankInfoRow(
    label: String,
    value: String,
    context: Context,
    d: Dimens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("info", value))
                Toast
                    .makeText(context, context.getString(R.string.deposit_copied), Toast.LENGTH_SHORT)
                    .show()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = d.font13,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = d.font13,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(d.space4))
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AccentYellow
            )
        }
    }
}
