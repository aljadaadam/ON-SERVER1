package com.onserver1.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.onserver1.app.data.local.dao.BannerDao
import com.onserver1.app.data.local.dao.CategoryDao
import com.onserver1.app.data.local.dao.ProductDao
import com.onserver1.app.data.local.dao.UserProfileDao
import com.onserver1.app.data.local.entity.CachedBanner
import com.onserver1.app.data.local.entity.CachedCategory
import com.onserver1.app.data.local.entity.CachedProduct
import com.onserver1.app.data.local.entity.CachedUserProfile

@Database(
    entities = [
        CachedProduct::class,
        CachedCategory::class,
        CachedBanner::class,
        CachedUserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bannerDao(): BannerDao
    abstract fun userProfileDao(): UserProfileDao
}
