package com.example.shared_expense_manager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index("date"), Index("categoryId"), Index("userId")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val description: String?,
    val date: String,
    val categoryId: String?,
    val userId: String,
    val createdAt: String,
    val updatedAt: String,
    val categoryName: String? = null,
    val addedByName: String? = null,
)
