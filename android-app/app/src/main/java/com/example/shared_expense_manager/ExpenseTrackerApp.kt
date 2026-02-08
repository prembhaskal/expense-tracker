package com.example.shared_expense_manager

import android.app.Application
import com.example.shared_expense_manager.data.auth.AuthStore
import com.example.shared_expense_manager.data.auth.GoogleAuthHelper
import com.example.shared_expense_manager.data.local.AppDatabase
import com.example.shared_expense_manager.data.remote.ApiClient
import com.example.shared_expense_manager.data.repository.CategoryRepository
import com.example.shared_expense_manager.data.repository.ExpenseRepository
import com.example.shared_expense_manager.sync.SyncScheduler

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
        authStore = AuthStore(this)
        googleAuthHelper = GoogleAuthHelper(this, BuildConfig.GOOGLE_WEB_CLIENT_ID)
        database = AppDatabase.getInstance(this)
        apiClient = ApiClient(BuildConfig.API_BASE_URL) { authStore.getToken() }
        expenseRepository = ExpenseRepository(database.expenseDao(), apiClient)
        categoryRepository = CategoryRepository(database.categoryDao(), apiClient)
        SyncScheduler.enqueuePeriodic(this)
    }
}
