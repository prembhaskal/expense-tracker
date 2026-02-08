package com.example.shared_expense_manager.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import com.example.shared_expense_manager.sync.SyncScheduler
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(app: ExpenseTrackerApp) {
    val categories by app.categoryRepository.getAllCategories().collectAsState(initial = emptyList())
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var deleteConfirm by remember { mutableStateOf<CategoryEntity?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            Text("Categories", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Category name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (newName.isBlank()) return@Button
                    scope.launch {
                        val result = app.categoryRepository.addCategory(newName.trim(), null)
                        result.fold(
                            onSuccess = {
                                newName = ""
                                error = null
                                SyncScheduler.enqueueOneTime(context)
                            },
                            onFailure = { error = it.message ?: "Failed to add" },
                        )
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text("Add category")
            }
            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        items(categories) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = c.name, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { deleteConfirm = c }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    deleteConfirm?.let { c ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text("Delete category?") },
            text = { Text("Delete \"${c.name}\"? This won't remove expenses, only the category.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        app.categoryRepository.deleteCategory(c.id)
                        deleteConfirm = null
                        SyncScheduler.enqueueOneTime(context)
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("Cancel") } },
        )
    }
}
