package com.example.shared_expense_manager.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.shared_expense_manager.ExpenseTrackerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? ExpenseTrackerApp ?: return@withContext Result.failure()
        if (!app.authStore.isLoggedIn()) return@withContext Result.success()
        val expenseResult = app.expenseRepository.syncFromApi()
        val categoryResult = app.categoryRepository.syncFromApi()
        if (expenseResult.isFailure || categoryResult.isFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
