package com.onserver1.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onserver1.app.data.local.entity.CachedProduct

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<CachedProduct>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: CachedProduct)

    @Query("SELECT * FROM products WHERE isFeatured = 1 AND isActive = 1 ORDER BY name ASC")
    suspend fun getFeaturedProducts(): List<CachedProduct>

    @Query("""
        SELECT * FROM products WHERE isActive = 1
        AND (:serviceType IS NULL OR serviceType = :serviceType)
        AND (:groupName IS NULL OR groupName = :groupName)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:search IS NULL OR name LIKE '%' || :search || '%' OR nameAr LIKE '%' || :search || '%')
        ORDER BY name ASC
    """)
    suspend fun getProducts(
        serviceType: String? = null,
        groupName: String? = null,
        categoryId: String? = null,
        search: String? = null
    ): List<CachedProduct>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): CachedProduct?

    @Query("SELECT DISTINCT groupName FROM products WHERE groupName IS NOT NULL AND (:serviceType IS NULL OR serviceType = :serviceType)")
    suspend fun getGroups(serviceType: String? = null): List<String>

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
