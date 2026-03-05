package com.onserver1.app.data.local

import android.content.Context
import androidx.room.Room
import com.onserver1.app.data.local.dao.BannerDao
import com.onserver1.app.data.local.dao.CategoryDao
import com.onserver1.app.data.local.dao.ProductDao
import com.onserver1.app.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "onserver1_cache.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideBannerDao(database: AppDatabase): BannerDao {
        return database.bannerDao()
    }

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()
    }
}
