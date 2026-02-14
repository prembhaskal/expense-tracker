package com.prembhaskal.expensetracker.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prembhaskal.expensetracker.util.FileLogger
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODIC_WORK_NAME = "expense_sync_periodic"

    private val syncConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Enqueue a one-time sync (runs when network is available). Call after any local mutation. */
    fun enqueueOneTime(context: Context) {
        FileLogger.i("SyncScheduler", "enqueueOneTime called")
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(syncConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "expense_sync_once",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Enqueue periodic sync (every 15 min when network available). */
    fun enqueuePeriodic(context: Context) {
        FileLogger.i("SyncScheduler", "enqueuePeriodic called")
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(syncConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
