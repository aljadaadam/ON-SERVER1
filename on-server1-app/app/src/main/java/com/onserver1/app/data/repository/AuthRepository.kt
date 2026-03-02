package com.onserver1.app.data.repository

import com.onserver1.app.data.api.ApiService
import com.onserver1.app.data.api.TokenManager
import com.onserver1.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken)
                tokenManager.saveUserId(authData.user.id)
                Result.success(authData)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String?): Result<String> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name, phone))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                Result.success(data.userId)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(userId: String, code: String): Result<String> {
        return try {
            val response = apiService.verifyOtp(OtpVerifyRequest(userId, code))
            if (response.isSuccessful && response.body()?.success == true) {
                // If tokens are returned (EMAIL_VERIFICATION), save them for auto-login
                val authData = response.body()!!.data
                if (authData != null && authData.accessToken.isNotEmpty()) {
                    tokenManager.saveTokens(authData.accessToken, authData.refreshToken)
                    tokenManager.saveUserId(authData.user.id)
                }
                Result.success("Verified successfully")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(userId: String): Result<String> {
        return try {
            val response = apiService.resendOtp(mapOf("userId" to userId, "type" to "EMAIL_VERIFICATION"))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success("OTP sent")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to resend OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                apiService.logout(RefreshTokenRequest(refreshToken))
            }
        } finally {
            tokenManager.clearTokens()
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun forgotPassword(email: String): Result<String?> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body()?.success == true) {
                val userId = response.body()!!.data?.userId
                Result.success(userId)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send reset code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(userId: String, code: String, newPassword: String): Result<String> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(userId, code, newPassword))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success("Password reset successfully")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Password reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
