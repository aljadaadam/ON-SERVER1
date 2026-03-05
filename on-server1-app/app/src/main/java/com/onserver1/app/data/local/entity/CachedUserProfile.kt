package com.onserver1.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.onserver1.app.data.model.User

@Entity(tableName = "user_profile")
data class CachedUserProfile(
    @PrimaryKey val id: String,
    val email: String,
    val phone: String?,
    val name: String,
    val avatar: String?,
    val balance: Double,
    val role: String,
    val isVerified: Boolean,
    val createdAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): User {
        return User(
            id = id,
            email = email,
            phone = phone,
            name = name,
            avatar = avatar,
            balance = balance,
            role = role,
            isVerified = isVerified,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromModel(user: User): CachedUserProfile {
            return CachedUserProfile(
                id = user.id,
                email = user.email,
                phone = user.phone,
                name = user.name,
                avatar = user.avatar,
                balance = user.balance,
                role = user.role,
                isVerified = user.isVerified,
                createdAt = user.createdAt
            )
        }
    }
}
