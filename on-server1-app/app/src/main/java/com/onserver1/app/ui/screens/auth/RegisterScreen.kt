package com.onserver1.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.components.AppDialog
import com.onserver1.app.ui.components.DialogType
import com.onserver1.app.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: (userId: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val d = LocalDimens.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            val userId = (state as AuthUiState.Success).userId ?: ""
            onRegisterSuccess(userId)
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
                text = stringResource(R.string.create_new_account),
                fontSize = d.font28,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )

            Spacer(modifier = Modifier.height(d.space32))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.full_name)) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = AccentYellow) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
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

            Spacer(modifier = Modifier.height(d.space12))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = AccentYellow) },
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

            Spacer(modifier = Modifier.height(d.space12))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_optional)) },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = AccentYellow) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            Spacer(modifier = Modifier.height(d.space12))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = AccentYellow) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(d.corner12),
                visualTransformation = PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(d.space24))

            Button(
                onClick = {
                    viewModel.register(name, email, password, phone.ifBlank { null })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d.buttonHeight),
                shape = RoundedCornerShape(d.corner12),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black
                ),
                enabled = state !is AuthUiState.Loading
            ) {
                if (state is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(d.icon24), color = Color.Black)
                } else {
                    Text(stringResource(R.string.register_button), fontSize = d.font18, fontWeight = FontWeight.Bold)
                }
            }

            // Error Dialog
            AppDialog(
                show = state is AuthUiState.Error,
                type = DialogType.ERROR,
                message = (state as? AuthUiState.Error)?.message ?: "",
                onDismiss = { viewModel.resetRegisterState() }
            )

            Spacer(modifier = Modifier.height(d.space24))

            Row {
                Text(stringResource(R.string.have_account), color = Color.White.copy(alpha = 0.6f), fontSize = d.font15)
                Text(
                    stringResource(R.string.login),
                    color = AccentYellow,
                    fontSize = d.font15,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
