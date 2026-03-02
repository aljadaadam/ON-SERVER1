package com.onserver1.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.components.AppDialog
import com.onserver1.app.ui.components.DialogType
import com.onserver1.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onResetCodeSent: (userId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.forgotPasswordState.collectAsState()
    val userId by viewModel.forgotPasswordUserId.collectAsState()
    var email by remember { mutableStateOf("") }
    val d = LocalDimens.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Success && userId != null) {
            onResetCodeSent(userId!!)
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
                text = stringResource(R.string.forgot_password_title),
                fontSize = d.font28,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )

            Spacer(modifier = Modifier.height(d.space8))

            Text(
                text = stringResource(R.string.forgot_password_desc),
                fontSize = d.font14,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(d.space48))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = {
                    Icon(Icons.Default.Email, null, tint = AccentYellow)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            Spacer(modifier = Modifier.height(d.space24))

            // Send Reset Code Button
            Button(
                onClick = { viewModel.forgotPassword(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.buttonHeight),
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black
                ),
                enabled = email.isNotBlank() && state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(d.icon24),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = stringResource(R.string.send_reset_code),
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
                onDismiss = { viewModel.resetForgotPasswordState() }
            )

            Spacer(modifier = Modifier.height(d.space24))

            // Back to login link
            Text(
                text = stringResource(R.string.back_to_login),
                color = AccentYellow,
                fontSize = d.font15,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}
