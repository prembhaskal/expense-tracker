package com.prembhaskal.expensetracker

import android.app.Application
import com.prembhaskal.expensetracker.data.auth.AuthStore
import com.prembhaskal.expensetracker.data.auth.GoogleAuthHelper
import com.prembhaskal.expensetracker.data.local.AppDatabase
import com.prembhaskal.expensetracker.data.remote.ApiClient
import com.prembhaskal.expensetracker.data.repository.CategoryRepository
import com.prembhaskal.expensetracker.data.repository.ExpenseRepository
import com.prembhaskal.expensetracker.sync.SyncScheduler
import com.prembhaskal.expensetracker.util.FileLogger

class ExpenseTrackerApp : Application() {
    lateinit var authStore: AuthStore
        private set
    lateinit var googleAuthHelper: GoogleAuthHelper
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var apiClient: ApiClient
        private set
    lateinit var expenseRepository: ExpenseRepository
        private set
    lateinit var categoryRepository: CategoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        FileLogger.init(this)
        authStore = AuthStore(this)
        googleAuthHelper = GoogleAuthHelper(this, BuildConfig.GOOGLE_WEB_CLIENT_ID)
        database = AppDatabase.getInstance(this)
        apiClient = ApiClient(BuildConfig.API_BASE_URL, { authStore.getToken() }, authStore)
        expenseRepository = ExpenseRepository(database.expenseDao(), database.pendingDeleteDao(), apiClient)
        categoryRepository = CategoryRepository(database.categoryDao(), database.pendingCategoryDeleteDao(), apiClient)
        SyncScheduler.enqueuePeriodic(this)
        FileLogger.i("App", "onCreate: enqueuing periodic sync")
    }
}
