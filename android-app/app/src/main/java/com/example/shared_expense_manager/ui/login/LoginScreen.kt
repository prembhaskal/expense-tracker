package com.example.shared_expense_manager.ui.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shared_expense_manager.ExpenseTrackerApp
import com.example.shared_expense_manager.data.remote.ApiException
import com.example.shared_expense_manager.sync.SyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    app: ExpenseTrackerApp,
    onLoginSuccess: () -> Unit,
) {
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            error = "Sign-in cancelled"
            return@rememberLauncherForActivityResult
        }
        loading = true
        error = null
        val idToken = app.googleAuthHelper.getIdTokenFromResult(result.data)
        if (idToken == null) {
            error = "Failed to get ID token"
            loading = false
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val session = app.apiClient.loginWithIdToken(idToken)
                    app.authStore.setToken(session.accessToken)
                    SyncScheduler.enqueueOneTime(context)
                    true
                } catch (e: Exception) {
                    error = when ((e as? ApiException)?.code) {
                        403 -> "Access restricted"
                        else -> e.message ?: "Sign-in failed"
                    }
                    false
                }
            }
            loading = false
            if (result) onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Expense Tracker",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Sign in with Google to track family expenses.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        } else {
            Button(
                onClick = {
                    val intent = app.googleAuthHelper.getSignInIntent()
                    launcher.launch(intent)
                },
                modifier = Modifier.padding(top = 24.dp).widthIn(min = 200.dp),
            ) {
                Text("Sign in with Google")
            }
        }
    }
}
