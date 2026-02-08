package com.example.shared_expense_manager.data.repository

import com.example.shared_expense_manager.data.local.dao.CategoryDao
import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import com.example.shared_expense_manager.data.remote.ApiClient
import com.example.shared_expense_manager.data.remote.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val categoryDao: CategoryDao,
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

    suspend fun addCategory(name: String, color: String?): Result<CategoryEntity> =
        withContext(Dispatchers.IO) {
            try {
                val entity = apiClient.createCategory(name, color)
                categoryDao.insert(entity)
                Result.success(entity)
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

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

    suspend fun deleteCategory(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiClient.deleteCategory(id)
            categoryDao.deleteById(id)
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
