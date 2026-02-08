package com.prembhaskal.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prembhaskal.expensetracker.data.local.entity.PendingCategoryDeleteEntity

@Dao
interface PendingCategoryDeleteDao {
    @Query("SELECT * FROM pending_category_deletes")
    suspend fun getAll(): List<PendingCategoryDeleteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingCategoryDeleteEntity)

    @Query("DELETE FROM pending_category_deletes WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: String)
}
