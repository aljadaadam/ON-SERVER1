package com.onserver1.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onserver1.app.data.model.Category
import com.onserver1.app.data.model.Product
import com.onserver1.app.data.model.ProductField

@Entity(tableName = "products")
data class CachedProduct(
    @PrimaryKey val id: String,
    val name: String,
    val nameAr: String?,
    val description: String?,
    val descriptionAr: String?,
    val price: Double,
    val originalPrice: Double?,
    val costPrice: Double?,
    val image: String?,
    val imagesJson: String?,
    val type: String,
    val serviceType: String?,
    val categoryId: String,
    val categoryJson: String?,
    val isFeatured: Boolean,
    val isActive: Boolean,
    val externalId: String?,
    val fieldsJson: String?,
    val deliveryTime: String?,
    val supportsQnt: Boolean,
    val minQnt: Int,
    val maxQnt: Int,
    val groupName: String?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Product {
        val gson = Gson()
        return Product(
            id = id,
            name = name,
            nameAr = nameAr,
            description = description,
            descriptionAr = descriptionAr,
            price = price,
            originalPrice = originalPrice,
            costPrice = costPrice,
            image = image,
            images = imagesJson?.let {
                gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
            },
            type = type,
            serviceType = serviceType,
            categoryId = categoryId,
            category = categoryJson?.let {
                gson.fromJson(it, Category::class.java)
            },
            isFeatured = isFeatured,
            isActive = isActive,
            externalId = externalId,
            fields = fieldsJson?.let {
                gson.fromJson(it, object : TypeToken<List<ProductField>>() {}.type)
            },
            deliveryTime = deliveryTime,
            supportsQnt = supportsQnt,
            minQnt = minQnt,
            maxQnt = maxQnt,
            groupName = groupName
        )
    }

    companion object {
        fun fromModel(product: Product): CachedProduct {
            val gson = Gson()
            return CachedProduct(
                id = product.id,
                name = product.name,
                nameAr = product.nameAr,
                description = product.description,
                descriptionAr = product.descriptionAr,
                price = product.price,
                originalPrice = product.originalPrice,
                costPrice = product.costPrice,
                image = product.image,
                imagesJson = product.images?.let { gson.toJson(it) },
                type = product.type,
                serviceType = product.serviceType,
                categoryId = product.categoryId,
                categoryJson = product.category?.let { gson.toJson(it) },
                isFeatured = product.isFeatured,
                isActive = product.isActive,
                externalId = product.externalId,
                fieldsJson = product.fields?.let { gson.toJson(it) },
                deliveryTime = product.deliveryTime,
                supportsQnt = product.supportsQnt,
                minQnt = product.minQnt,
                maxQnt = product.maxQnt,
                groupName = product.groupName
            )
        }
    }
}
