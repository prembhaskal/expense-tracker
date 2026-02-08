package com.prembhaskal.expensetracker.ui.nav

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prembhaskal.expensetracker.ExpenseTrackerApp
import com.prembhaskal.expensetracker.ui.categories.CategoriesScreen
import com.prembhaskal.expensetracker.ui.dashboard.DashboardScreen
import com.prembhaskal.expensetracker.ui.expenses.ExpenseAddEditScreen
import com.prembhaskal.expensetracker.ui.expenses.ExpensesScreen
import com.prembhaskal.expensetracker.ui.reports.ReportsScreen

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_EXPENSES = "expenses"
const val ROUTE_CATEGORIES = "categories"
const val ROUTE_REPORTS = "reports"
const val ROUTE_ADD_EXPENSE = "add_expense"
const val ROUTE_EDIT_EXPENSE = "edit_expense"

@Composable
fun MainNav(app: ExpenseTrackerApp, onSignOut: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavItem(ROUTE_DASHBOARD, "Dashboard"),
        NavItem(ROUTE_EXPENSES, "Expenses"),
        NavItem(ROUTE_CATEGORIES, "Categories"),
        NavItem(ROUTE_REPORTS, "Reports"),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    androidx.compose.material3.TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Text(item.label.take(1), style = MaterialTheme.typography.labelMedium) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(app, onAddExpense = { navController.navigate(ROUTE_ADD_EXPENSE) })
            }
            composable(ROUTE_EXPENSES) {
                ExpensesScreen(
                    app = app,
                    onAddExpense = { navController.navigate(ROUTE_ADD_EXPENSE) },
                    onEditExpense = { id -> navController.navigate("$ROUTE_EDIT_EXPENSE/$id") },
                )
            }
            composable(ROUTE_CATEGORIES) { CategoriesScreen(app) }
            composable(ROUTE_REPORTS) { ReportsScreen(app) }
            composable(ROUTE_ADD_EXPENSE) {
                ExpenseAddEditScreen(app, expenseId = null, onBack = { navController.popBackStack() })
            }
            composable("$ROUTE_EDIT_EXPENSE/{expenseId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("expenseId")
                ExpenseAddEditScreen(app, expenseId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}

private data class NavItem(val route: String, val label: String)
