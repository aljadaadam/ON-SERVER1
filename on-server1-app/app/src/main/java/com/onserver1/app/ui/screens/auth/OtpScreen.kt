package com.onserver1.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
fun OtpScreen(
    userId: String,
    onVerified: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.otpState.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    val d = LocalDimens.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onVerified()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BalanceGradientStart, BalanceGradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(d.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.otp_title),
                fontSize = d.font28,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )

            Spacer(modifier = Modifier.height(d.space16))

            Text(
                text = stringResource(R.string.otp_message),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = d.font15,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(d.space32))

            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text(stringResource(R.string.otp_code)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = d.otpFontSize,
                    letterSpacing = d.otpLetterSpacing
                ),
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

            Button(
                onClick = { viewModel.verifyOtp(userId, otpCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.buttonHeight),
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black
                ),
                enabled = otpCode.length == 6 && state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(d.icon24), color = Color.Black)
                } else {
                    Text(stringResource(R.string.verify_button), fontSize = d.font18, fontWeight = FontWeight.Bold)
                }
            }

            // Error Dialog
            AppDialog(
                show = state is AuthUiState.Error,
                type = DialogType.ERROR,
                message = (state as? AuthUiState.Error)?.message ?: "",
                onDismiss = { viewModel.resetOtpState() }
            )

            Spacer(modifier = Modifier.height(d.space24))

            TextButton(onClick = { /* Resend OTP */ }) {
                Text(stringResource(R.string.resend_code), color = AccentYellow, fontSize = d.font15)
            }
        }
    }
}
