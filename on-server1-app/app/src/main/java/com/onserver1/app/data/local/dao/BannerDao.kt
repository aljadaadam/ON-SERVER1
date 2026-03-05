package com.onserver1.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onserver1.app.data.local.entity.CachedBanner

@Dao
interface BannerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<CachedBanner>)

    @Query("SELECT * FROM banners")
    suspend fun getAllBanners(): List<CachedBanner>

    @Query("DELETE FROM banners")
    suspend fun clearAll()
}
