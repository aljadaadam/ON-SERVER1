package com.onserver1.app.data.repository

import com.onserver1.app.data.api.ApiService
import com.onserver1.app.data.model.*
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getFeaturedProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getFeaturedProducts()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load featured products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.success(response.body()!!.data?.products ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroups(serviceType: String? = null): Result<List<String>> {
        return try {
            val response = apiService.getGroups(serviceType)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProduct(id: String): Result<Product> {
        return try {
            val response = apiService.getProduct(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBanners(): Result<List<Banner>> {
        return try {
            val response = apiService.getBanners()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load banners"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(items: List<CreateOrderItem>): Result<Order> {
        return try {
            val response = apiService.createOrder(CreateOrderRequest(items))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Order failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
