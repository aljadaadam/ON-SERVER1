package com.onserver1.app.data.repository

import com.onserver1.app.data.api.ApiService
import com.onserver1.app.data.local.dao.BannerDao
import com.onserver1.app.data.local.dao.CategoryDao
import com.onserver1.app.data.local.dao.ProductDao
import com.onserver1.app.data.local.dao.UserProfileDao
import com.onserver1.app.data.local.entity.CachedBanner
import com.onserver1.app.data.local.entity.CachedCategory
import com.onserver1.app.data.local.entity.CachedProduct
import com.onserver1.app.data.local.entity.CachedUserProfile
import com.onserver1.app.data.model.*
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val bannerDao: BannerDao,
    private val userProfileDao: UserProfileDao
) {
    suspend fun getFeaturedProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getFeaturedProducts()
            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()!!.data ?: emptyList()
                // Cache products
                try { productDao.insertProducts(products.map { CachedProduct.fromModel(it) }) } catch (_: Exception) {}
                Result.success(products)
            } else {
                // Try cache
                val cached = productDao.getFeaturedProducts()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toModel() })
                } else {
                    Result.failure(Exception("Failed to load featured products"))
                }
            }
        } catch (e: Exception) {
            // Network error — try cache
            val cached = try { productDao.getFeaturedProducts() } catch (_: Exception) { emptyList() }
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toModel() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getProducts(
        page: Int? = null,
        categoryId: String? = null,
        type: String? = null,
        serviceType: String? = null,
        groupName: String? = null,
        search: String? = null
    ): Result<List<Product>> {
        return try {
            val response = apiService.getProducts(page, 20, categoryId, type, serviceType, groupName, search)
            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()!!.data?.products ?: emptyList()
                // Cache products
                try { productDao.insertProducts(products.map { CachedProduct.fromModel(it) }) } catch (_: Exception) {}
                Result.success(products)
            } else {
                // Try cache with filters
                val cached = productDao.getProducts(serviceType, groupName, categoryId, search)
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toModel() })
                } else {
                    Result.failure(Exception("Failed to load products"))
                }
            }
        } catch (e: Exception) {
            val cached = try { productDao.getProducts(serviceType, groupName, categoryId, search) } catch (_: Exception) { emptyList() }
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toModel() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroups(serviceType: String? = null): Result<List<String>> {
        return try {
            val response = apiService.getGroups(serviceType)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                // Try cache
                val cached = productDao.getGroups(serviceType)
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Failed to load groups"))
            }
        } catch (e: Exception) {
            val cached = try { productDao.getGroups(serviceType) } catch (_: Exception) { emptyList() }
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                val categories = response.body()!!.data ?: emptyList()
                try { categoryDao.insertCategories(categories.map { CachedCategory.fromModel(it) }) } catch (_: Exception) {}
                Result.success(categories)
            } else {
                val cached = categoryDao.getAllCategories()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toModel() })
                else Result.failure(Exception("Failed to load categories"))
            }
        } catch (e: Exception) {
            val cached = try { categoryDao.getAllCategories() } catch (_: Exception) { emptyList() }
            if (cached.isNotEmpty()) Result.success(cached.map { it.toModel() })
            else Result.failure(e)
        }
    }

    suspend fun getProduct(id: String): Result<Product> {
        return try {
            val response = apiService.getProduct(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val product = response.body()!!.data!!
                try { productDao.insertProduct(CachedProduct.fromModel(product)) } catch (_: Exception) {}
                Result.success(product)
            } else {
                // Try cache
                val cached = productDao.getProductById(id)
                if (cached != null) Result.success(cached.toModel())
                else Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            val cached = try { productDao.getProductById(id) } catch (_: Exception) { null }
            if (cached != null) Result.success(cached.toModel())
            else Result.failure(e)
        }
    }

    suspend fun getBanners(): Result<List<Banner>> {
        return try {
            val response = apiService.getBanners()
            if (response.isSuccessful && response.body()?.success == true) {
                val banners = response.body()!!.data ?: emptyList()
                try {
                    bannerDao.clearAll()
                    bannerDao.insertBanners(banners.map { CachedBanner.fromModel(it) })
                } catch (_: Exception) {}
                Result.success(banners)
            } else {
                val cached = bannerDao.getAllBanners()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toModel() })
                else Result.failure(Exception("Failed to load banners"))
            }
        } catch (e: Exception) {
            val cached = try { bannerDao.getAllBanners() } catch (_: Exception) { emptyList() }
            if (cached.isNotEmpty()) Result.success(cached.map { it.toModel() })
            else Result.failure(e)
        }
    }

    suspend fun createOrder(items: List<CreateOrderItem>): Result<Order> {
        return try {
            val response = apiService.createOrder(CreateOrderRequest(items))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = try {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        val jsonObj = org.json.JSONObject(errorJson)
                        jsonObj.optString("message", "")
                    } else null
                } catch (_: Exception) { null }
                Result.failure(Exception(errorMsg?.takeIf { it.isNotBlank() } 
                    ?: response.body()?.message 
                    ?: "Order failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                try { userProfileDao.insertProfile(CachedUserProfile.fromModel(user)) } catch (_: Exception) {}
                Result.success(user)
            } else {
                // Try cache
                val cached = userProfileDao.getProfile()
                if (cached != null) Result.success(cached.toModel())
                else Result.failure(Exception("Failed to load profile"))
            }
        } catch (e: Exception) {
            val cached = try { userProfileDao.getProfile() } catch (_: Exception) { null }
            if (cached != null) Result.success(cached.toModel())
            else Result.failure(e)
        }
    }

    suspend fun addBalance(amount: Double): Result<Double> {
        return try {
            val response = apiService.addBalance(mapOf("amount" to amount))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data?.get("balance") ?: 0.0)
            } else {
                Result.failure(Exception("Failed to add balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(page: Int? = null): Result<List<Order>> {
        return try {
            val response = apiService.getOrders(page, 20)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data?.orders ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(page: Int? = null): Result<List<Transaction>> {
        return try {
            val response = apiService.getTransactions(page, 20)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data?.transactions ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppSettings(): Result<Map<String, String>> {
        return try {
            val response = apiService.getAppSettings()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyMap())
            } else {
                Result.failure(Exception("Failed to load settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, phone: String?): Result<User> {
        return try {
            val data = mutableMapOf("name" to name)
            if (phone != null) data["phone"] = phone
            val response = apiService.updateProfile(data)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(avatarFile: java.io.File): Result<User> {
        return try {
            val avatarBody = okhttp3.RequestBody.create(
                "image/*".toMediaType(), avatarFile
            )
            val avatarPart = okhttp3.MultipartBody.Part.createFormData(
                "avatar", avatarFile.name, avatarBody
            )
            val response = apiService.uploadAvatar(avatarPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to upload avatar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        return try {
            val response = apiService.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.message ?: "Password changed")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to change password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // Deposits
    // ============================================
    suspend fun getGatewayInfo(): Result<GatewayInfo> {
        return try {
            val response = apiService.getGatewayInfo()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to load gateway info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUsdtDeposit(amount: Double, txHash: String): Result<DepositResponse> {
        return try {
            val response = apiService.createUsdtDeposit(UsdtDepositRequest(amount, txHash))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "USDT deposit failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBankakDeposit(amount: Double, receiptFile: java.io.File, note: String? = null): Result<Deposit> {
        return try {
            val amountBody = okhttp3.RequestBody.create(
                "text/plain".toMediaType(), amount.toString()
            )
            val noteBody = note?.takeIf { it.isNotBlank() }?.let {
                okhttp3.RequestBody.create("text/plain".toMediaType(), it)
            }
            val receiptBody = okhttp3.RequestBody.create(
                "image/*".toMediaType(), receiptFile
            )
            val receiptPart = okhttp3.MultipartBody.Part.createFormData(
                "receipt", receiptFile.name, receiptBody
            )
            val response = apiService.createBankakDeposit(amountBody, noteBody, receiptPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Bankak deposit failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyDeposits(page: Int? = null): Result<List<Deposit>> {
        return try {
            val response = apiService.getMyDeposits(page, 20)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data?.deposits ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load deposits"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
