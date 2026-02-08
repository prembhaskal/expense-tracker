package com.example.shared_expense_manager.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseAddEditScreen(
    app: ExpenseTrackerApp,
    expenseId: String?,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val categories by app.categoryRepository.getAllCategories().collectAsState(initial = emptyList())
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val e = app.expenseRepository.getExpenseById(expenseId)
            if (e != null) {
                amount = e.amount.toString()
                description = e.description ?: ""
                date = e.date
                selectedCategoryId = e.categoryId
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (expenseId == null) "Add expense" else "Edit expense",
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (categories.isNotEmpty()) {
            Text("Category", style = MaterialTheme.typography.labelMedium)
            categories.forEach { c ->
                val selected = selectedCategoryId == c.id
                if (selected) {
                    Button(
                        onClick = { selectedCategoryId = null },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(c.name) }
                } else {
                    OutlinedButton(
                        onClick = { selectedCategoryId = c.id },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(c.name) }
                }
            }
        }
        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0) {
                    error = "Enter a valid amount"
                    return@Button
                }
                error = null
                loading = true
                scope.launch {
                    val result = if (expenseId == null) {
                        app.expenseRepository.addExpense(amt, description.ifBlank { null }, date, selectedCategoryId)
                    } else {
                        app.expenseRepository.updateExpense(expenseId, amt, description.ifBlank { null }, date, selectedCategoryId)
                    }
                    loading = false
                    result.fold(
                        onSuccess = { onBack() },
                        onFailure = { error = it.message ?: "Failed to save" },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Saving…" else "Save")
        }
        if (expenseId != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete expense", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm && expenseId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete expense?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        app.expenseRepository.deleteExpense(expenseId!!)
                        showDeleteConfirm = false
                        onBack()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}
