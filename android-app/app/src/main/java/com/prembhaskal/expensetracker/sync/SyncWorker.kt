package com.prembhaskal.expensetracker.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prembhaskal.expensetracker.ExpenseTrackerApp
import com.prembhaskal.expensetracker.util.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "SyncWorker"

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        FileLogger.i(TAG, "SyncWorker started")
        val app = applicationContext as? ExpenseTrackerApp ?: run {
            FileLogger.e(TAG, "SyncWorker: app context is not ExpenseTrackerApp")
            return@withContext Result.failure()
        }
        val loggedIn = app.authStore.isLoggedIn()
        FileLogger.i(TAG, "isLoggedIn: $loggedIn")
        if (!loggedIn) return@withContext Result.success()
        val pushResult = app.expenseRepository.pushPendingExpenses()
        FileLogger.i(TAG, "pushPendingExpenses: ${if (pushResult.isSuccess) "success" else "fail"}")
        if (pushResult.isSuccess) {
            Log.d(TAG, "pushPendingExpenses: done")
        } else {
            Log.w(TAG, "pushPendingExpenses: failed, will retry")
        }
        val updateResult = app.expenseRepository.pushPendingUpdates()
        FileLogger.i(TAG, "pushPendingUpdates: ${if (updateResult.isSuccess) "success" else "fail"}")
        if (updateResult.isSuccess) {
            Log.d(TAG, "pushPendingUpdates: done")
        } else {
            Log.w(TAG, "pushPendingUpdates: failed, will retry")
        }
        val deleteResult = app.expenseRepository.pushPendingDeletes()
        FileLogger.i(TAG, "pushPendingDeletes: ${if (deleteResult.isSuccess) "success" else "fail"}")
        if (deleteResult.isSuccess) {
            Log.d(TAG, "pushPendingDeletes: done")
        } else {
            Log.w(TAG, "pushPendingDeletes: failed, will retry")
        }
        val expenseResult = app.expenseRepository.syncFromApi()
        FileLogger.i(TAG, "syncFromApi expenses: ${if (expenseResult.isSuccess) "success" else "fail"}")
        app.categoryRepository.pushPendingCategories()
        app.categoryRepository.pushPendingCategoryDeletes()
        val categoryResult = app.categoryRepository.syncFromApi()
        FileLogger.i(TAG, "syncFromApi categories: ${if (categoryResult.isSuccess) "success" else "fail"}")
        val result = if (expenseResult.isFailure || categoryResult.isFailure) {
            FileLogger.w(TAG, "SyncWorker result: retry")
            Result.retry()
        } else {
            FileLogger.i(TAG, "SyncWorker result: success")
            Result.success()
        }
        result
    }
}
