package com.prembhaskal.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_category_deletes")
data class PendingCategoryDeleteEntity(
    @PrimaryKey val categoryId: String,
)
