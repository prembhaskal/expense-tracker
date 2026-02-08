package com.example.shared_expense_manager.data.repository

import com.example.shared_expense_manager.data.local.dao.ExpenseDao
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity
import com.example.shared_expense_manager.data.local.dao.MonthTotal
import com.example.shared_expense_manager.data.local.dao.CategoryTotal
import com.example.shared_expense_manager.data.remote.ApiClient
import com.example.shared_expense_manager.data.remote.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val apiClient: ApiClient,
) {
    fun getRecentExpenses(limit: Int = 20): Flow<List<ExpenseEntity>> =
        expenseDao.getRecentFlow(limit)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> =
        expenseDao.getAllFlow()

    suspend fun getExpenseById(id: String): ExpenseEntity? =
        expenseDao.getById(id)

    fun getMonthlyTotals(): Flow<List<MonthTotal>> =
        expenseDao.getMonthlyTotalsFlow()

    fun getTotalsByCategory(): Flow<List<CategoryTotal>> =
        expenseDao.getTotalsByCategoryFlow()

    suspend fun syncFromApi(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val expenses = apiClient.getExpenses(500, 0)
            expenseDao.insertAll(expenses)
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(amount: Double, description: String?, date: String, categoryId: String?): Result<ExpenseEntity> =
        withContext(Dispatchers.IO) {
            try {
                val entity = apiClient.createExpense(amount, description, date, categoryId)
                expenseDao.insert(entity)
                Result.success(entity)
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateExpense(id: String, amount: Double, description: String?, date: String, categoryId: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                apiClient.updateExpense(id, amount, description, date, categoryId)
                syncFromApi()
                Result.success(Unit)
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteExpense(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiClient.deleteExpense(id)
            expenseDao.deleteById(id)
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
