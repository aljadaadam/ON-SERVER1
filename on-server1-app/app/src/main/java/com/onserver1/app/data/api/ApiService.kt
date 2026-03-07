package com.onserver1.app.data.api

import com.onserver1.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============================================
    // Auth
    // ============================================
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<Map<String, String>>>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<ApiResponse<Map<String, String>>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<ForgotPasswordResponse>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Map<String, String>>>

    // ============================================
    // Products
    // ============================================
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("type") type: String? = null,
        @Query("serviceType") serviceType: String? = null,
        @Query("groupName") groupName: String? = null,
        @Query("search") search: String? = null,
        @Query("featured") featured: Boolean? = null
    ): Response<ApiResponse<PaginatedData<Product>>>

    @GET("products/featured")
    suspend fun getFeaturedProducts(): Response<ApiResponse<List<Product>>>

    @GET("products/groups")
    suspend fun getGroups(
        @Query("serviceType") serviceType: String? = null
    ): Response<ApiResponse<List<String>>>

    @GET("products/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<ApiResponse<Product>>

    // ============================================
    // Orders
    // ============================================
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<Order>>

    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<PaginatedData<Order>>>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<ApiResponse<Order>>

    // ============================================
    // User
    // ============================================
    @GET("users/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    @PUT("users/profile")
    suspend fun updateProfile(@Body data: Map<String, String>): Response<ApiResponse<User>>

    @Multipart
    @POST("users/avatar")
    suspend fun uploadAvatar(
        @Part avatar: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<User>>

    @POST("users/balance")
    suspend fun addBalance(@Body data: Map<String, Double>): Response<ApiResponse<Map<String, Double>>>

    @GET("users/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<PaginatedData<Transaction>>>

    // ============================================
    // Banners
    // ============================================
    @GET("banners")
    suspend fun getBanners(): Response<ApiResponse<List<Banner>>>

    // ============================================
    // Server Time
    // ============================================
    @GET("health")
    suspend fun getServerTime(): Response<Map<String, Any>>

    // ============================================
    // App Settings (public)
    // ============================================
    @GET("users/app-settings")
    suspend fun getAppSettings(): Response<ApiResponse<Map<String, String>>>

    // ============================================
    // Change Password
    // ============================================
    @PUT("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Map<String, String>>>

    // ============================================
    // Deposits
    // ============================================
    @GET("deposits/gateway-info")
    suspend fun getGatewayInfo(): Response<ApiResponse<GatewayInfo>>

    @POST("deposits/usdt")
    suspend fun createUsdtDeposit(@Body request: UsdtDepositRequest): Response<ApiResponse<DepositResponse>>

    @Multipart
    @POST("deposits/bankak")
    suspend fun createBankakDeposit(
        @Part("amount") amount: okhttp3.RequestBody,
        @Part("note") note: okhttp3.RequestBody?,
        @Part receipt: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<Deposit>>

    @GET("deposits/my")
    suspend fun getMyDeposits(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<PaginatedData<Deposit>>>
}
