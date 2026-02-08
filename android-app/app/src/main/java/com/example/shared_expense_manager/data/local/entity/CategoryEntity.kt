package com.example.shared_expense_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String?,
    val createdAt: String,
    val pendingSync: Boolean = false,
)
