package com.example.shared_expense_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shared_expense_manager.data.local.entity.PendingDeleteEntity

@Dao
interface PendingDeleteDao {
    @Query("SELECT * FROM pending_deletes")
    suspend fun getAll(): List<PendingDeleteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingDeleteEntity)

    @Query("DELETE FROM pending_deletes WHERE expenseId = :expenseId")
    suspend fun deleteByExpenseId(expenseId: String)
}
