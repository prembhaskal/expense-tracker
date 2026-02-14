package com.prembhaskal.expensetracker.data.repository

import android.util.Log
import com.prembhaskal.expensetracker.data.local.dao.ExpenseDao
import com.prembhaskal.expensetracker.data.local.dao.PendingDeleteDao
import com.prembhaskal.expensetracker.data.local.entity.ExpenseEntity
import com.prembhaskal.expensetracker.data.local.entity.PendingDeleteEntity
import com.prembhaskal.expensetracker.data.local.dao.MonthTotal
import com.prembhaskal.expensetracker.data.local.dao.CategoryTotal
import com.prembhaskal.expensetracker.data.remote.ApiClient
import com.prembhaskal.expensetracker.data.remote.ApiException
import com.prembhaskal.expensetracker.util.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "ExpenseRepository"

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val pendingDeleteDao: PendingDeleteDao,
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
            FileLogger.i(TAG, "syncFromApi: pulled ${expenses.size} expenses")
            Result.success(Unit)
        } catch (e: ApiException) {
            FileLogger.e(TAG, "syncFromApi: failed code=${e.code} msg=${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            FileLogger.e(TAG, "syncFromApi: failed msg=${e.message}", e)
            Result.failure(e)
        }
    }

    /** Saves to Room immediately (local-first); sync worker will push to API in background. */
    suspend fun addExpense(amount: Double, description: String?, date: String, categoryId: String?): Result<ExpenseEntity> =
        withContext(Dispatchers.IO) {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            val localEntity = ExpenseEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                description = description,
                date = date,
                categoryId = categoryId,
                userId = "pending",
                createdAt = now,
                updatedAt = now,
                pendingSync = true,
            )
            expenseDao.insert(localEntity)
            FileLogger.i(TAG, "addExpense: saved locally id=${localEntity.id} amount=$amount")
            Log.d(TAG, "addExpense: saved locally (pendingSync) id=${localEntity.id} amount=$amount")
            Result.success(localEntity)
        }

    suspend fun pushPendingExpenses(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = expenseDao.getPendingSync()
            FileLogger.i(TAG, "pushPendingExpenses: ${pending.size} pending")
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            Log.d(TAG, "pushPendingExpenses: pushing ${pending.size} pending expense(s)")
            for (e in pending) {
                try {
                    apiClient.createExpense(e.amount, e.description, e.date, e.categoryId)
                    expenseDao.deleteById(e.id)
                    FileLogger.i(TAG, "pushPendingExpenses: synced id=${e.id}")
                    Log.d(TAG, "pushPendingExpenses: synced local id=${e.id}")
                } catch (err: Exception) {
                    FileLogger.e(TAG, "pushPendingExpenses: failed id=${e.id} msg=${err.message}", err)
                    Log.w(TAG, "pushPendingExpenses: failed for id=${e.id}", err)
                    return@withContext Result.failure(err)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "pushPendingExpenses: failed msg=${e.message}", e)
            Log.w(TAG, "pushPendingExpenses: failed", e)
            Result.failure(e)
        }
    }

    /** Updates Room immediately (local-first); sync worker will push to API in background. */
    suspend fun updateExpense(id: String, amount: Double, description: String?, date: String, categoryId: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val existing = expenseDao.getById(id) ?: return@withContext Result.failure(NoSuchElementException("Expense not found"))
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            val updated = existing.copy(
                amount = amount,
                description = description,
                date = date,
                categoryId = categoryId,
                updatedAt = now,
                pendingUpdate = !existing.pendingSync,
            )
            expenseDao.update(updated)
            Log.d(TAG, "updateExpense: saved locally id=$id pendingUpdate=${updated.pendingUpdate}")
            Result.success(Unit)
        }

    /** Deletes from Room immediately (local-first); sync worker will push delete to API in background if expense was synced. */
    suspend fun deleteExpense(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val expense = expenseDao.getById(id)
        expenseDao.deleteById(id)
        if (expense?.pendingSync == true) {
            Log.d(TAG, "deleteExpense: removed local-only expense id=$id (was pendingSync)")
        } else if (expense != null) {
            pendingDeleteDao.insert(PendingDeleteEntity(expenseId = id))
            Log.d(TAG, "deleteExpense: queued delete for sync id=$id")
        }
        Result.success(Unit)
    }

    suspend fun pushPendingUpdates(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = expenseDao.getPendingUpdates()
            FileLogger.i(TAG, "pushPendingUpdates: ${pending.size} pending")
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            Log.d(TAG, "pushPendingUpdates: pushing ${pending.size} pending update(s)")
            for (e in pending) {
                try {
                    apiClient.updateExpense(e.id, e.amount, e.description, e.date, e.categoryId)
                    expenseDao.clearPendingUpdate(e.id)
                    FileLogger.i(TAG, "pushPendingUpdates: synced id=${e.id}")
                    Log.d(TAG, "pushPendingUpdates: synced id=${e.id}")
                } catch (err: Exception) {
                    FileLogger.e(TAG, "pushPendingUpdates: failed id=${e.id} msg=${err.message}", err)
                    Log.w(TAG, "pushPendingUpdates: failed for id=${e.id}", err)
                    return@withContext Result.failure(err)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "pushPendingUpdates: failed msg=${e.message}", e)
            Log.w(TAG, "pushPendingUpdates: failed", e)
            Result.failure(e)
        }
    }

    suspend fun pushPendingDeletes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = pendingDeleteDao.getAll()
            FileLogger.i(TAG, "pushPendingDeletes: ${pending.size} pending")
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            Log.d(TAG, "pushPendingDeletes: pushing ${pending.size} pending delete(s)")
            for (e in pending) {
                try {
                    apiClient.deleteExpense(e.expenseId)
                    pendingDeleteDao.deleteByExpenseId(e.expenseId)
                    FileLogger.i(TAG, "pushPendingDeletes: synced id=${e.expenseId}")
                    Log.d(TAG, "pushPendingDeletes: synced delete id=${e.expenseId}")
                } catch (err: Exception) {
                    FileLogger.e(TAG, "pushPendingDeletes: failed id=${e.expenseId} msg=${err.message}", err)
                    Log.w(TAG, "pushPendingDeletes: failed for id=${e.expenseId}", err)
                    return@withContext Result.failure(err)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "pushPendingDeletes: failed msg=${e.message}", e)
            Log.w(TAG, "pushPendingDeletes: failed", e)
            Result.failure(e)
        }
    }
}
