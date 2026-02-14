package com.prembhaskal.expensetracker.data.repository

import com.prembhaskal.expensetracker.data.local.dao.CategoryDao
import com.prembhaskal.expensetracker.data.local.dao.PendingCategoryDeleteDao
import com.prembhaskal.expensetracker.data.local.entity.CategoryEntity
import com.prembhaskal.expensetracker.data.local.entity.PendingCategoryDeleteEntity
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

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val pendingCategoryDeleteDao: PendingCategoryDeleteDao,
    private val apiClient: ApiClient,
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllFlow()

    suspend fun getCategoriesList(): List<CategoryEntity> =
        categoryDao.getAll()

    suspend fun syncFromApi(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val categories = apiClient.getCategories()
            categoryDao.insertAll(categories)
            FileLogger.i("CategoryRepository", "syncFromApi: pulled ${categories.size} categories")
            Result.success(Unit)
        } catch (e: ApiException) {
            FileLogger.e("CategoryRepository", "syncFromApi: failed code=${e.code} msg=${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            FileLogger.e("CategoryRepository", "syncFromApi: failed msg=${e.message}", e)
            Result.failure(e)
        }
    }

    /** Saves to Room immediately (local-first); sync worker will push to API in background. */
    suspend fun addCategory(name: String, color: String?): Result<CategoryEntity> =
        withContext(Dispatchers.IO) {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            val local = CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color,
                createdAt = now,
                pendingSync = true,
            )
            categoryDao.insert(local)
            FileLogger.i("CategoryRepository", "addCategory: saved locally id=${local.id}")
            Result.success(local)
        }

    /** Updates Room then syncs (could be made full local-first with pendingUpdate if needed). */
    suspend fun updateCategory(id: String, name: String, color: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                apiClient.updateCategory(id, name, color)
                syncFromApi()
                Result.success(Unit)
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Deletes from Room immediately (local-first); sync worker will push delete to API in background. */
    suspend fun deleteCategory(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        categoryDao.deleteById(id)
        pendingCategoryDeleteDao.insert(PendingCategoryDeleteEntity(categoryId = id))
        Result.success(Unit)
    }

    suspend fun pushPendingCategories(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = categoryDao.getPendingSync()
            FileLogger.i("CategoryRepository", "pushPendingCategories: ${pending.size} pending")
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            for (c in pending) {
                try {
                    apiClient.createCategory(c.name, c.color)
                    categoryDao.deleteById(c.id)
                    FileLogger.i("CategoryRepository", "pushPendingCategories: synced id=${c.id}")
                } catch (e: Exception) {
                    FileLogger.e("CategoryRepository", "pushPendingCategories: failed id=${c.id} msg=${e.message}", e)
                    return@withContext Result.failure(e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e("CategoryRepository", "pushPendingCategories: failed msg=${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun pushPendingCategoryDeletes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = pendingCategoryDeleteDao.getAll()
            FileLogger.i("CategoryRepository", "pushPendingCategoryDeletes: ${pending.size} pending")
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            for (e in pending) {
                try {
                    apiClient.deleteCategory(e.categoryId)
                    pendingCategoryDeleteDao.deleteByCategoryId(e.categoryId)
                    FileLogger.i("CategoryRepository", "pushPendingCategoryDeletes: synced id=${e.categoryId}")
                } catch (err: Exception) {
                    FileLogger.e("CategoryRepository", "pushPendingCategoryDeletes: failed id=${e.categoryId} msg=${err.message}", err)
                    return@withContext Result.failure(err)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e("CategoryRepository", "pushPendingCategoryDeletes: failed msg=${e.message}", e)
            Result.failure(e)
        }
    }
}
