package com.prembhaskal.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletes")
data class PendingDeleteEntity(
    @PrimaryKey val expenseId: String,
)
