package com.example.shared_expense_manager.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.sync.SyncScheduler
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity

@Composable
fun DashboardScreen(app: ExpenseTrackerApp, onAddExpense: () -> Unit = {}) {
    val recent by app.expenseRepository.getRecentExpenses(20).collectAsState(initial = emptyList())
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Recent expenses", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { SyncScheduler.enqueueOneTime(context) }) {
                Text("Refresh")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(recent) { e ->
                ExpenseCard(e)
            }
        }
    }
        FloatingActionButton(
            onClick = onAddExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Text("+")
        }
    }
}

@Composable
private fun ExpenseCard(e: ExpenseEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${e.amount}", style = MaterialTheme.typography.titleMedium)
            if (!e.description.isNullOrBlank()) Text(text = e.description, style = MaterialTheme.typography.bodySmall)
            Text(text = "${e.date} ${e.categoryName?.let { " · $it" } ?: ""} ${e.addedByName?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
