package com.onserver1.app.ui.screens.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.screens.settings.AppSettingsViewModel
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current
    val context = LocalContext.current

    val email = state.settings["support_email"] ?: ""
    val phone = state.settings["support_phone"] ?: ""
    val whatsapp = state.settings["whatsapp_number"] ?: ""
    val telegram = state.settings["telegram_link"] ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.help_support),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = d.space24),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentYellow)
                }
            }

            else -> {
                Spacer(modifier = Modifier.height(d.space8))

                // Contact Section
                if (email.isNotBlank() || phone.isNotBlank() || whatsapp.isNotBlank() || telegram.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.help_contact_us),
                        fontSize = d.font13,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentYellow,
                        modifier = Modifier.padding(horizontal = d.screenPadding, vertical = d.space8)
                    )

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
                            var itemIndex = 0

                            if (email.isNotBlank()) {
                                if (itemIndex > 0) HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = d.screenPadding),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                ContactItem(
                                    icon = Icons.Outlined.Email,
                                    title = stringResource(R.string.help_email),
                                    value = email,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                                        context.startActivity(intent)
                                    },
                                    d = d
                                )
                                itemIndex++
                            }

                            if (phone.isNotBlank()) {
                                if (itemIndex > 0) HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = d.screenPadding),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                ContactItem(
                                    icon = Icons.Outlined.Phone,
                                    title = stringResource(R.string.help_phone),
                                    value = phone,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    },
                                    d = d
                                )
                                itemIndex++
                            }

                            if (whatsapp.isNotBlank()) {
                                if (itemIndex > 0) HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = d.screenPadding),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                ContactItem(
                                    icon = Icons.Outlined.Chat,
                                    title = stringResource(R.string.help_whatsapp),
                                    value = whatsapp,
                                    onClick = {
                                        val url = "https://wa.me/${whatsapp.replace("+", "").replace(" ", "")}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    d = d
                                )
                                itemIndex++
                            }

                            if (telegram.isNotBlank()) {
                                if (itemIndex > 0) HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = d.screenPadding),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                ContactItem(
                                    icon = Icons.Outlined.Send,
                                    title = stringResource(R.string.help_telegram),
                                    value = telegram,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegram))
                                        context.startActivity(intent)
                                    },
                                    d = d
                                )
                                itemIndex++
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(d.space16))
                }

                // FAQ Section
                Text(
                    text = stringResource(R.string.help_faq),
                    fontSize = d.font13,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentYellow,
                    modifier = Modifier.padding(horizontal = d.screenPadding, vertical = d.space8)
                )

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
                        FaqItem(
                            question = stringResource(R.string.faq_q1),
                            answer = stringResource(R.string.faq_a1),
                            d = d
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = d.screenPadding),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                        FaqItem(
                            question = stringResource(R.string.faq_q2),
                            answer = stringResource(R.string.faq_a2),
                            d = d
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = d.screenPadding),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                        FaqItem(
                            question = stringResource(R.string.faq_q3),
                            answer = stringResource(R.string.faq_a3),
                            d = d
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = d.screenPadding),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                        FaqItem(
                            question = stringResource(R.string.faq_q4),
                            answer = stringResource(R.string.faq_a4),
                            d = d
                        )
                    }
                }

                Spacer(modifier = Modifier.height(d.space24))
            }
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    title: String,
    value: String,
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
            Surface(
                modifier = Modifier.size(d.icon40),
                shape = CircleShape,
                color = AccentYellow.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(d.icon20)
                    )
                }
            }
            Spacer(modifier = Modifier.width(d.space12))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = d.font14,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    fontSize = d.font12,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String,
    d: Dimens
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding, vertical = d.space10)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontSize = d.font14,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(d.icon20),
                    tint = AccentYellow
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = answer,
                    fontSize = d.font13,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = d.space8)
                )
            }
        }
    }
}
