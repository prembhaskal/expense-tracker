package com.example.shared_expense_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.shared_expense_manager.ui.login.LoginScreen
import com.example.shared_expense_manager.ui.nav.MainNav
import com.example.shared_expense_manager.ui.theme.SharedExpenseManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ExpenseTrackerApp
        setContent {
            SharedExpenseManagerTheme {
                var isLoggedIn by remember { mutableStateOf(app.authStore.isLoggedIn()) }
                if (!isLoggedIn) {
                    LoginScreen(
                        app = app,
                        onLoginSuccess = { isLoggedIn = true },
                    )
                } else {
                    MainNav(
                        app = app,
                        onSignOut = {
                            app.authStore.clear()
                            app.googleAuthHelper.signOut()
                            isLoggedIn = false
                        },
                    )
                }
            }
        }
    }
}
