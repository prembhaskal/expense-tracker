package com.example.shared_expense_manager.data.repository

import com.example.shared_expense_manager.data.local.dao.CategoryDao
import com.example.shared_expense_manager.data.local.dao.PendingCategoryDeleteDao
import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import com.example.shared_expense_manager.data.local.entity.PendingCategoryDeleteEntity
import com.example.shared_expense_manager.data.remote.ApiClient
import com.example.shared_expense_manager.data.remote.ApiException
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
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
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
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            for (c in pending) {
                try {
                    apiClient.createCategory(c.name, c.color)
                    categoryDao.deleteById(c.id)
                } catch (e: Exception) {
                    return@withContext Result.failure(e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushPendingCategoryDeletes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = pendingCategoryDeleteDao.getAll()
            if (pending.isEmpty()) return@withContext Result.success(Unit)
            for (e in pending) {
                try {
                    apiClient.deleteCategory(e.categoryId)
                    pendingCategoryDeleteDao.deleteByCategoryId(e.categoryId)
                } catch (err: Exception) {
                    return@withContext Result.failure(err)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
