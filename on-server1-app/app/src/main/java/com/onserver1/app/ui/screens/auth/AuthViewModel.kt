package com.onserver1.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val message: String = "", val userId: String? = null) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState

    private val _otpState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val otpState: StateFlow<AuthUiState> = _otpState

    private val _forgotPasswordState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val forgotPasswordState: StateFlow<AuthUiState> = _forgotPasswordState

    private val _resetPasswordState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val resetPasswordState: StateFlow<AuthUiState> = _resetPasswordState

    // Stores userId returned from forgot-password for use in reset-password
    private val _forgotPasswordUserId = MutableStateFlow<String?>(null)
    val forgotPasswordUserId: StateFlow<String?> = _forgotPasswordUserId

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result.fold(
                onSuccess = { AuthUiState.Success("Login successful") },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(name: String, email: String, password: String, phone: String?) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            val result = authRepository.register(name, email, password, phone)
            _registerState.value = result.fold(
                onSuccess = { userId -> AuthUiState.Success("Registration successful", userId = userId) },
                onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun verifyOtp(userId: String, code: String) {
        viewModelScope.launch {
            _otpState.value = AuthUiState.Loading
            val result = authRepository.verifyOtp(userId, code)
            _otpState.value = result.fold(
                onSuccess = { AuthUiState.Success("Verified") },
                onFailure = { AuthUiState.Error(it.message ?: "Verification failed") }
            )
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = AuthUiState.Loading
            val result = authRepository.forgotPassword(email)
            _forgotPasswordState.value = result.fold(
                onSuccess = { userId ->
                    _forgotPasswordUserId.value = userId
                    AuthUiState.Success("Reset code sent")
                },
                onFailure = { AuthUiState.Error(it.message ?: "Failed to send reset code") }
            )
        }
    }

    fun resetPassword(userId: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = AuthUiState.Loading
            val result = authRepository.resetPassword(userId, code, newPassword)
            _resetPasswordState.value = result.fold(
                onSuccess = { AuthUiState.Success("Password reset successfully") },
                onFailure = { AuthUiState.Error(it.message ?: "Password reset failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = AuthUiState.Idle
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }

    fun resetOtpState() {
        _otpState.value = AuthUiState.Idle
    }

    fun resendOtp(userId: String) {
        viewModelScope.launch {
            _otpState.value = AuthUiState.Loading
            val result = authRepository.resendOtp(userId)
            _otpState.value = result.fold(
                onSuccess = { AuthUiState.Success("OTP resent") },
                onFailure = { AuthUiState.Error(it.message ?: "Failed to resend OTP") }
            )
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = AuthUiState.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = AuthUiState.Idle
    }
}
