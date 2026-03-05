package com.onserver1.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onserver1.app.data.model.Category

@Entity(tableName = "categories")
data class CachedCategory(
    @PrimaryKey val id: String,
    val name: String,
    val nameAr: String?,
    val icon: String?,
    val image: String?,
    val childrenJson: String?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Category {
        val gson = Gson()
        return Category(
            id = id,
            name = name,
            nameAr = nameAr,
            icon = icon,
            image = image,
            children = childrenJson?.let {
                gson.fromJson(it, object : TypeToken<List<Category>>() {}.type)
            }
        )
    }

    companion object {
        fun fromModel(category: Category): CachedCategory {
            val gson = Gson()
            return CachedCategory(
                id = category.id,
                name = category.name,
                nameAr = category.nameAr,
                icon = category.icon,
                image = category.image,
                childrenJson = category.children?.let { gson.toJson(it) }
            )
        }
    }
}
