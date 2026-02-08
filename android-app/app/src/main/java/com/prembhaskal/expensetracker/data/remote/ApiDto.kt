package com.prembhaskal.expensetracker.data.remote

import com.google.gson.annotations.SerializedName

data class ExpenseDto(
    val id: String,
    val amount: Double,
    val description: String?,
    val date: String,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("user_id") val userId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val categories: CategoryRefDto?,
    val profiles: ProfileRefDto?,
)

data class CategoryRefDto(val name: String?)
data class ProfileRefDto(@SerializedName("full_name") val fullName: String?)

data class CategoryDto(
    val id: String,
    val name: String,
    val color: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class AuthSessionResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_at") val expiresAt: Int? = null,
    val user: AuthUserDto? = null,
)
data class AuthUserDto(val id: String, val email: String?)
