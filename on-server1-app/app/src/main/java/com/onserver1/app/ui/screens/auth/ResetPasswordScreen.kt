package com.onserver1.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.components.AppDialog
import com.onserver1.app.ui.components.DialogType
import com.onserver1.app.ui.theme.*

@Composable
fun ResetPasswordScreen(
    userId: String,
    onPasswordReset: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.resetPasswordState.collectAsState()
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val d = LocalDimens.current

    val passwordsMatch = newPassword == confirmPassword
    val isValid = code.length == 6 && newPassword.length >= 6 && passwordsMatch

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            showSuccess = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BalanceGradientStart,
                        BalanceGradientEnd
                    )
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(d.space8)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(d.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = stringResource(R.string.reset_password_title),
                fontSize = d.font28,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )

            Spacer(modifier = Modifier.height(d.space8))

            Text(
                text = stringResource(R.string.reset_password_desc),
                fontSize = d.font14,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(d.space48))

            // OTP Code Field
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text(stringResource(R.string.otp_code)) },
                leadingIcon = {
                    Icon(Icons.Default.Pin, null, tint = AccentYellow)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = AccentYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = AccentYellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(d.space16))

            // New Password Field
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.new_password)) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null, tint = AccentYellow)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = AccentYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = AccentYellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(d.space16))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null, tint = AccentYellow)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = AccentYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = AccentYellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch
            )

            // Password mismatch warning
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Spacer(modifier = Modifier.height(d.space4))
                Text(
                    text = stringResource(R.string.passwords_dont_match),
                    color = Color(0xFFFF6B6B),
                    fontSize = d.font12
                )
            }

            // Password requirements
            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                Spacer(modifier = Modifier.height(d.space4))
                Text(
                    text = stringResource(R.string.password_requirements),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = d.font12
                )
            }

            Spacer(modifier = Modifier.height(d.space24))

            // Reset Password Button
            Button(
                onClick = { viewModel.resetPassword(userId, code, newPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.buttonHeight),
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black
                ),
                enabled = isValid && state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(d.icon24),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = stringResource(R.string.reset_password_button),
                        fontSize = d.font18,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error Dialog
            AppDialog(
                show = state is AuthUiState.Error,
                type = DialogType.ERROR,
                message = (state as? AuthUiState.Error)?.message ?: "",
                onDismiss = { viewModel.resetResetPasswordState() }
            )

            // Success Dialog
            AppDialog(
                show = showSuccess,
                type = DialogType.SUCCESS,
                message = stringResource(R.string.password_reset_success),
                onDismiss = {
                    showSuccess = false
                    viewModel.resetResetPasswordState()
                    onPasswordReset()
                }
            )
        }
    }
}
