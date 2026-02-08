package com.example.shared_expense_manager.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.data.local.dao.CategoryTotal
import com.example.shared_expense_manager.data.local.dao.MonthTotal

@Composable
fun ReportsScreen(app: ExpenseTrackerApp) {
    val monthly by app.expenseRepository.getMonthlyTotals().collectAsState(initial = emptyList())
    val byCategory by app.expenseRepository.getTotalsByCategory().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            Text("Reports", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Text("Monthly", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        }
        items(monthly) { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = m.month, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${m.total}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Text("By category", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        }
        items(byCategory) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = c.categoryName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${c.total}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
