package com.onserver1.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val d = LocalDimens.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success != null) {
            kotlinx.coroutines.delay(2000)
            onBack()
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
                    text = stringResource(R.string.settings_change_password),
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

        // Current Password
        Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
            Text(
                text = stringResource(R.string.current_password),
                fontSize = d.font13,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = d.space4)
            )
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    cursorColor = AccentYellow
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            if (showCurrentPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null,
                            modifier = Modifier.size(d.icon20)
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(d.space12))

        // New Password
        Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
            Text(
                text = stringResource(R.string.new_password),
                fontSize = d.font13,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = d.space4)
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    cursorColor = AccentYellow
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            if (showNewPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null,
                            modifier = Modifier.size(d.icon20)
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(d.space12))

        // Confirm Password
        Column(modifier = Modifier.padding(horizontal = d.screenPadding)) {
            Text(
                text = stringResource(R.string.confirm_password),
                fontSize = d.font13,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = d.space4)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    cursorColor = AccentYellow
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = AccentYellow, modifier = Modifier.size(d.icon20))
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
            )
            if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                Text(
                    text = stringResource(R.string.passwords_dont_match),
                    fontSize = d.font12,
                    color = ErrorRed,
                    modifier = Modifier.padding(top = d.space4)
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space24))

        // Messages
        state.success?.let {
            Text(
                text = stringResource(R.string.password_changed),
                fontSize = d.font14,
                color = SuccessGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = d.screenPadding)
            )
            Spacer(modifier = Modifier.height(d.space8))
        }

        state.error?.let {
            Text(
                text = it,
                fontSize = d.font14,
                color = ErrorRed,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = d.screenPadding)
            )
            Spacer(modifier = Modifier.height(d.space8))
        }

        // Save Button
        Button(
            onClick = {
                viewModel.changePassword(currentPassword, newPassword)
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
            enabled = !state.isSaving
                    && currentPassword.isNotBlank()
                    && newPassword.length >= 6
                    && newPassword == confirmPassword
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(d.icon20),
                    color = Color.Black,
                    strokeWidth = d.space2
                )
            } else {
                Text(
                    stringResource(R.string.change_password_button),
                    fontSize = d.font15,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(d.space8))

        Text(
            text = stringResource(R.string.password_requirements),
            fontSize = d.font12,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = d.screenPadding)
        )

        Spacer(modifier = Modifier.height(d.space24))
    }
}
