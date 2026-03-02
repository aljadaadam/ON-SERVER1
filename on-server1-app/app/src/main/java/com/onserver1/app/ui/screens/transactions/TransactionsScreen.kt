package com.onserver1.app.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.data.model.Transaction
import com.onserver1.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.transactions),
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

            state.transactions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(d.icon48),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(d.space12))
                        Text(
                            text = stringResource(R.string.no_transactions),
                            fontSize = d.font16,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = d.screenPadding,
                        vertical = d.space8
                    ),
                    verticalArrangement = Arrangement.spacedBy(d.space6)
                ) {
                    items(state.transactions) { transaction ->
                        TransactionItem(transaction = transaction, d = d)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, d: Dimens) {
    val isCredit = transaction.type.lowercase() in listOf("deposit", "credit", "refund", "topup", "top_up")
    val amountColor = if (isCredit) SuccessGreen else ErrorRed
    val amountPrefix = if (isCredit) "+" else "-"
    val icon = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconBgColor = if (isCredit) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)

    val typeText = when (transaction.type.lowercase()) {
        "deposit", "credit", "topup", "top_up" -> stringResource(R.string.tx_deposit)
        "purchase", "debit" -> stringResource(R.string.tx_purchase)
        "refund" -> stringResource(R.string.tx_refund)
        "withdrawal" -> stringResource(R.string.tx_withdrawal)
        else -> transaction.type
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(d.corner12),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(d.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(d.icon40),
                shape = CircleShape,
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(d.icon20),
                        tint = amountColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(d.space12))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeText,
                    fontSize = d.font14,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!transaction.description.isNullOrBlank()) {
                    Text(
                        text = transaction.description,
                        fontSize = d.font12,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
                Text(
                    text = formatDate(transaction.createdAt),
                    fontSize = d.font11,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix$${String.format("%.2f", transaction.amount)}",
                    fontSize = d.font15,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = stringResource(R.string.balance_label) + " $${String.format("%.2f", transaction.balance)}",
                    fontSize = d.font11,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = input.parse(dateStr.substringBefore('.'))
        output.format(date!!)
    } catch (e: Exception) {
        dateStr.substringBefore('T')
    }
}
