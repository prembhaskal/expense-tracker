package com.example.shared_expense_manager.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity

@Composable
fun ExpensesScreen(
    app: ExpenseTrackerApp,
    onAddExpense: () -> Unit = {},
    onEditExpense: (id: String) -> Unit = {},
) {
    val expenses by app.expenseRepository.getAllExpenses().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            Text("Expenses", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onAddExpense, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Add expense")
            }
        }
        items(expenses) { e ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onEditExpense(e.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "${e.amount}", style = MaterialTheme.typography.titleMedium)
                    if (!e.description.isNullOrBlank()) Text(text = e.description, style = MaterialTheme.typography.bodySmall)
                    Text(text = "${e.date} ${e.categoryName?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
