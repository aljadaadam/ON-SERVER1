package com.onserver1.app.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
import com.onserver1.app.BuildConfig
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*
import com.onserver1.app.util.IntegrityGuard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit
) {
    val d = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.about_app),
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

        Spacer(modifier = Modifier.height(d.space16))

        // App Logo & Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(d.avatarSize),
                shape = CircleShape,
                color = AccentYellow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "ON",
                        fontSize = d.font28,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(d.space12))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = d.font22,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                fontSize = d.font13,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(d.space12))

            Text(
                text = stringResource(R.string.about_description),
                fontSize = d.font14,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = d.lineHeight22
            )
        }

        Spacer(modifier = Modifier.height(d.space24))

        // Links Section
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
                AboutItem(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.privacy_policy),
                    onClick = onNavigateToPrivacyPolicy,
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                AboutItem(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.terms_of_service),
                    onClick = onNavigateToTermsOfService,
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                AboutItem(
                    icon = Icons.Outlined.Star,
                    title = stringResource(R.string.about_rate_app),
                    onClick = { },
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                AboutItem(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.about_share_app),
                    onClick = { },
                    d = d
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space24))

        // Developer info — integrity-protected credit
        val context = LocalContext.current
        val isArabic = Locale.getDefault().language == "ar"
        val creditText = if (isArabic) IntegrityGuard.resolveCreditAr() else IntegrityGuard.resolveCreditEn()
        val creditUrl = IntegrityGuard.resolveUrl()
        Text(
            text = creditText,
            fontSize = d.font12,
            color = AccentYellow.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(creditUrl))
                    context.startActivity(intent)
                }
        )

        Spacer(modifier = Modifier.height(d.space16))
    }
}

@Composable
fun AboutItem(
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(d.icon20)
            )
        }
    }
}
