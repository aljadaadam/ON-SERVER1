package com.onserver1.app.data.model

import com.google.gson.annotations.SerializedName

// ============================================
// Auth Models
// ============================================
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null
)

data class OtpVerifyRequest(
    val userId: String,
    val code: String,
    val type: String = "EMAIL_VERIFICATION"
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val userId: String,
    val code: String,
    val newPassword: String
)

data class ForgotPasswordResponse(
    val message: String,
    val userId: String?
)

// ============================================
// User Model
// ============================================
data class User(
    val id: String,
    val email: String,
    val phone: String?,
    val name: String,
    val avatar: String?,
    val balance: Double,
    val role: String,
    val isVerified: Boolean,
    val createdAt: String? = null
)

data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

// ============================================
// Product Model
// ============================================
data class Product(
    val id: String,
    val name: String,
    val nameAr: String?,
    val description: String?,
    val descriptionAr: String?,
    val price: Double,
    val originalPrice: Double?,
    val costPrice: Double?,
    val image: String?,
    val images: List<String>?,
    val type: String,
    val serviceType: String?,
    val categoryId: String,
    val category: Category?,
    val isFeatured: Boolean,
    val isActive: Boolean,
    val externalId: String?,
    val fields: String?,
    val deliveryTime: String?,
    val supportsQnt: Boolean = false,
    val minQnt: Int = 0,
    val maxQnt: Int = 0,
    val groupName: String?
)

data class ProductField(
    val name: String,
    val key: String,
    val type: String,
    val required: Boolean
)

data class Category(
    val id: String,
    val name: String,
    val nameAr: String?,
    val icon: String?,
    val image: String?,
    val children: List<Category>?
)

// ============================================
// Order Model
// ============================================
data class Order(
    val id: String,
    val orderNumber: String,
    val status: String,
    val totalAmount: Double,
    val items: List<OrderItem>?,
    val resultCodes: String?,
    val createdAt: String
)

data class OrderItem(
    val id: String,
    val productId: String,
    val product: Product?,
    val quantity: Int,
    val price: Double,
    val imei: String?,
    val metadata: String?
)

data class CreateOrderRequest(
    val items: List<CreateOrderItem>
)

data class CreateOrderItem(
    val productId: String,
    val quantity: Int = 1,
    val metadata: Map<String, String>? = null
)

// ============================================
// Transaction Model
// ============================================
data class Transaction(
    val id: String,
    val type: String,
    val amount: Double,
    val balance: Double,
    val description: String?,
    val createdAt: String
)

// ============================================
// Banner Model
// ============================================
data class Banner(
    val id: String,
    val title: String?,
    val image: String,
    val link: String?
)

// ============================================
// API Response Wrapper
// ============================================
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

data class PaginatedResponse<T>(
    val success: Boolean,
    val data: PaginatedData<T>?
)

data class PaginatedData<T>(
    val products: List<T>?,
    val orders: List<T>?,
    val transactions: List<T>?,
    val deposits: List<T>?,
    val pagination: Pagination?
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

// ============================================
// App Settings (from backend)
// ============================================
data class AppSettings(
    val support_email: String? = null,
    val support_phone: String? = null,
    val whatsapp_number: String? = null,
    val telegram_link: String? = null,
    val privacy_policy_text: String? = null,
    val terms_of_service_text: String? = null,
    val site_name: String? = null,
    val site_description: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// ============================================
// Deposit Models
// ============================================
data class Deposit(
    val id: String,
    val depositNumber: Int,
    val amount: Double,
    val amountLocal: Double?,
    val exchangeRate: Double?,
    val gateway: String,
    val status: String,
    val txHash: String?,
    val receiptImage: String?,
    val adminNote: String?,
    val createdAt: String
)

data class GatewayInfo(
    val usdt: UsdtGateway,
    val bankak: BankakGateway,
    val currency: String
)

data class UsdtGateway(
    val walletAddress: String,
    val network: String,
    val minAmount: Double,
    val maxAmount: Double
)

data class BankakGateway(
    val accountName: String,
    val accountNumber: String,
    val bankName: String,
    val transferNote: String? = null,
    val exchangeRate: Double,
    val minAmount: Double,
    val maxAmount: Double
)

data class UsdtDepositRequest(
    val amount: Double,
    val txHash: String
)

data class DepositResponse(
    val deposit: Deposit?,
    val verification: VerificationResult?
)

data class VerificationResult(
    val verified: Boolean,
    val message: String,
    val actualAmount: Double?
)
