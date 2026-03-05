package com.onserver1.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.onserver1.app.data.model.Banner

@Entity(tableName = "banners")
data class CachedBanner(
    @PrimaryKey val id: String,
    val title: String?,
    val image: String,
    val link: String?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Banner {
        return Banner(
            id = id,
            title = title,
            image = image,
            link = link
        )
    }

    companion object {
        fun fromModel(banner: Banner): CachedBanner {
            return CachedBanner(
                id = banner.id,
                title = banner.title,
                image = banner.image,
                link = banner.link
            )
        }
    }
}
