package com.onserver1.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onserver1.app.data.local.entity.CachedUserProfile

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(user: CachedUserProfile)

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): CachedUserProfile?

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}
