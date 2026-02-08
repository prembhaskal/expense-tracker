package com.example.shared_expense_manager.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import com.example.shared_expense_manager.sync.SyncScheduler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var categoryFilterQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.fillMaxWidth().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showDatePicker = true }) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
            )
        }
        if (categories.isNotEmpty()) {
            CategoryDropdown(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onSelectedCategoryIdChange = { selectedCategoryId = it },
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it; if (!it) categoryFilterQuery = "" },
                filterQuery = categoryFilterQuery,
                onFilterQueryChange = { categoryFilterQuery = it },
                onDismissRequest = { focusManager.clearFocus() },
            )
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
                        onSuccess = {
                            SyncScheduler.enqueueOneTime(context)
                            onBack()
                        },
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

    if (showDatePicker) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val initialMillis = try {
            dateFormat.parse(date)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        date = dateFormat.format(Date(ms))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
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
                        SyncScheduler.enqueueOneTime(context)
                        onBack()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onSelectedCategoryIdChange: (String?) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    filterQuery: String,
    onFilterQueryChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val selectedName = categories.find { it.id == selectedCategoryId }?.name ?: ""
    val filtered = categories.filter { it.name.contains(filterQuery, ignoreCase = true) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
                onDismissRequest()
            },
        ) {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = onFilterQueryChange,
                label = { Text("Filter") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onSelectedCategoryIdChange(null)
                    onExpandedChange(false)
                },
            )
            filtered.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.name) },
                    onClick = {
                        onSelectedCategoryIdChange(c.id)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}
