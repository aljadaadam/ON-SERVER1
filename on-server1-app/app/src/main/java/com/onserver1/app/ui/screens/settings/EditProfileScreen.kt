package com.onserver1.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    var name by remember(state.user) { mutableStateOf(state.user?.name ?: "") }
    var phone by remember(state.user) { mutableStateOf(state.user?.phone ?: "") }

    LaunchedEffect(state.success) {
        if (state.success != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_edit_profile),
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

            else -> {
                Spacer(modifier = Modifier.height(d.space16))

                // Avatar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(d.avatarSize),
                        shape = CircleShape,
                        color = AccentYellow
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(d.icon40),
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(d.space24))

                // Email (read-only)
                Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
                    Text(
                        text = stringResource(R.string.email),
                        fontSize = d.font13,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = d.space4)
                    )
                    OutlinedTextField(
                        value = state.user?.email ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(d.space12))

                // Name
                Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
                    Text(
                        text = stringResource(R.string.name),
                        fontSize = d.font13,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = d.space4)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            cursorColor = AccentYellow
                        ),
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                        },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(d.space12))

                // Phone
                Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
                    Text(
                        text = stringResource(R.string.phone),
                        fontSize = d.font13,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = d.space4)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(d.corner12),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            cursorColor = AccentYellow
                        ),
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(d.space24))

                // Success/Error messages
                state.success?.let {
                    Text(
                        text = stringResource(R.string.profile_updated),
                        fontSize = d.font14,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = d.screenPadding)
                    )
                    Spacer(modifier = Modifier.height(d.space8))
                }

                state.error?.let {
                    Text(
                        text = it,
                        fontSize = d.font14,
                        color = ErrorRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = d.screenPadding)
                    )
                    Spacer(modifier = Modifier.height(d.space8))
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.updateProfile(name, phone.ifBlank { null })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = d.screenPadding)
                        .height(d.buttonHeight),
                    shape = RoundedCornerShape(d.corner12),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.Black
                    ),
                    enabled = !state.isSaving && name.isNotBlank()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(d.icon20),
                            color = Color.Black,
                            strokeWidth = d.space2
                        )
                    } else {
                        Text(
                            stringResource(R.string.save_changes),
                            fontSize = d.font15,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(d.space24))
            }
        }
    }
}
