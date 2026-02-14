package com.prembhaskal.expensetracker.ui.login

import android.app.Activity
import android.util.Log
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
import com.prembhaskal.expensetracker.ExpenseTrackerApp
import com.prembhaskal.expensetracker.data.remote.ApiException
import com.prembhaskal.expensetracker.sync.SyncScheduler
import com.prembhaskal.expensetracker.util.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LoginScreen"

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
        val resultCodeName = when (result.resultCode) {
            Activity.RESULT_OK -> "RESULT_OK"
            Activity.RESULT_CANCELED -> "RESULT_CANCELED"
            else -> "OTHER(${result.resultCode})"
        }
        Log.d(TAG, "onActivityResult: resultCode=$resultCodeName, data=${if (result.data == null) "null" else "present"}")
        // Even on RESULT_CANCELED, try to read token if data is present (Google sometimes returns CANCELED when SHA-1/config is wrong)
        if (result.resultCode != Activity.RESULT_OK && result.data == null) {
            Log.w(TAG, "onActivityResult: no data, treating as user cancelled")
            error = "Sign-in cancelled"
            return@rememberLauncherForActivityResult
        }
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "onActivityResult: resultCode not OK but data present - attempting to get idToken anyway (check GoogleAuthHelper logs if this fails)")
        }
        loading = true
        error = null
        val idToken = app.googleAuthHelper.getIdTokenFromResult(result.data)
        if (idToken == null) {
            Log.e(TAG, "onActivityResult: getIdTokenFromResult returned null - check GoogleAuthHelper logs")
            error = "Failed to get ID token"
            loading = false
            return@rememberLauncherForActivityResult
        }
        Log.d(TAG, "onActivityResult: got idToken, calling API loginWithIdToken")
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val session = app.apiClient.loginWithIdToken(idToken)
                    Log.d(TAG, "loginWithIdToken: success, got access_token")
                    app.authStore.setSession(session.accessToken, session.refreshToken)
                    FileLogger.i(TAG, "Login: token set, enqueuing one-time sync")
                    SyncScheduler.enqueueOneTime(context)
                    true
                } catch (e: Exception) {
                    val apiCode = (e as? ApiException)?.code
                    Log.e(TAG, "loginWithIdToken: failed", e)
                    Log.e(TAG, "loginWithIdToken: apiCode=$apiCode, message=${e.message}")
                    error = when (apiCode) {
                        403 -> "Access restricted"
                        else -> e.message ?: "Sign-in failed"
                    }
                    false
                }
            }
            loading = false
            if (success) onLoginSuccess()
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
