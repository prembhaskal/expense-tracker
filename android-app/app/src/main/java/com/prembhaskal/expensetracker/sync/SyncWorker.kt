package com.prembhaskal.expensetracker.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prembhaskal.expensetracker.ExpenseTrackerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "SyncWorker"

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? ExpenseTrackerApp ?: return@withContext Result.failure()
        if (!app.authStore.isLoggedIn()) return@withContext Result.success()
        val pushResult = app.expenseRepository.pushPendingExpenses()
        if (pushResult.isSuccess) {
            Log.d(TAG, "pushPendingExpenses: done")
        } else {
            Log.w(TAG, "pushPendingExpenses: failed, will retry")
        }
        val updateResult = app.expenseRepository.pushPendingUpdates()
        if (updateResult.isSuccess) {
            Log.d(TAG, "pushPendingUpdates: done")
        } else {
            Log.w(TAG, "pushPendingUpdates: failed, will retry")
        }
        val deleteResult = app.expenseRepository.pushPendingDeletes()
        if (deleteResult.isSuccess) {
            Log.d(TAG, "pushPendingDeletes: done")
        } else {
            Log.w(TAG, "pushPendingDeletes: failed, will retry")
        }
        val expenseResult = app.expenseRepository.syncFromApi()
        app.categoryRepository.pushPendingCategories()
        app.categoryRepository.pushPendingCategoryDeletes()
        val categoryResult = app.categoryRepository.syncFromApi()
        if (expenseResult.isFailure || categoryResult.isFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
