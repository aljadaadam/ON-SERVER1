package com.onserver1.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit
) {
    val d = LocalDimens.current
    val context = LocalContext.current
    val splashPrefs = context.getSharedPreferences("splash_prefs", Context.MODE_PRIVATE)
    var cinematicSplash by remember { mutableStateOf(splashPrefs.getBoolean("cinematic_splash", true)) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
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

        Spacer(modifier = Modifier.height(d.space8))

        // General Section
        SettingsSectionHeader(
            title = stringResource(R.string.settings_general),
            d = d
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
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_value),
                    onClick = { },
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                SettingsToggleItem(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    subtitle = stringResource(R.string.settings_notifications_desc),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                SettingsToggleItem(
                    icon = Icons.Outlined.Movie,
                    title = stringResource(R.string.settings_cinematic_splash),
                    subtitle = stringResource(R.string.settings_cinematic_splash_desc),
                    checked = cinematicSplash,
                    onCheckedChange = {
                        cinematicSplash = it
                        splashPrefs.edit().putBoolean("cinematic_splash", it).apply()
                    },
                    d = d
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space16))

        // Account Section
        SettingsSectionHeader(
            title = stringResource(R.string.settings_account),
            d = d
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
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = stringResource(R.string.settings_edit_profile),
                    onClick = onNavigateToEditProfile,
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.settings_change_password),
                    onClick = onNavigateToChangePassword,
                    d = d
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space16))

        // Privacy Section
        SettingsSectionHeader(
            title = stringResource(R.string.settings_privacy),
            d = d
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
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.privacy_policy),
                    onClick = onNavigateToPrivacyPolicy,
                    d = d
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = d.screenPadding),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
                SettingsItem(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.terms_of_service),
                    onClick = onNavigateToTermsOfService,
                    d = d
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space24))
    }
}

@Composable
fun SettingsSectionHeader(title: String, d: Dimens) {
    Text(
        text = title,
        fontSize = d.font13,
        fontWeight = FontWeight.SemiBold,
        color = AccentYellow,
        modifier = Modifier.padding(horizontal = d.screenPadding, vertical = d.space8)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = d.font15,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = d.font12,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(d.icon20)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    d: Dimens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = d.screenPadding, vertical = d.space6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentYellow,
            modifier = Modifier.size(d.icon24)
        )
        Spacer(modifier = Modifier.width(d.space12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = d.font15,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = d.font12,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentYellow,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        )
    }
}
