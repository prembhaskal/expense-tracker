package com.example.shared_expense_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllFlow(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 20): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE pendingUpdate = 1 AND pendingSync = 0")
    suspend fun getPendingUpdates(): List<ExpenseEntity>

    @Query("UPDATE expenses SET pendingUpdate = 0 WHERE id = :id")
    suspend fun clearPendingUpdate(id: String)

    @Query(
        """
        SELECT substr(date, 1, 7) AS month, SUM(amount) AS total 
        FROM expenses GROUP BY month ORDER BY month DESC LIMIT 12
        """
    )
    fun getMonthlyTotalsFlow(): Flow<List<MonthTotal>>

    @Query(
        """
        SELECT categoryId, COALESCE(categoryName, 'Uncategorized') AS categoryName, SUM(amount) AS total 
        FROM expenses GROUP BY categoryId
        """
    )
    fun getTotalsByCategoryFlow(): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

data class MonthTotal(val month: String, val total: Double)
data class CategoryTotal(val categoryId: String?, val categoryName: String, val total: Double)
