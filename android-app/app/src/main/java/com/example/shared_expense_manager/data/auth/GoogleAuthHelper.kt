package com.example.shared_expense_manager.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private const val TAG = "GoogleAuthHelper"

class GoogleAuthHelper(context: Context, webClientId: String) {
    init {
        Log.d(TAG, "init: webClientId is ${if (webClientId.isBlank()) "BLANK (idToken will not be available)" else "set (${webClientId.take(20)}...)" }")
    }

    val googleSignInClient: GoogleSignInClient = if (webClientId.isBlank()) {
        Log.w(TAG, "Using DEFAULT_SIGN_IN without requestIdToken - getIdToken will always be null")
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
    } else {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun getIdTokenFromResult(data: Intent?): String? {
        Log.d(TAG, "getIdTokenFromResult: data=${if (data == null) "null" else "non-null"}")
        if (data == null) return null
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            Log.d(TAG, "getIdTokenFromResult: idToken=${if (token == null) "null" else "present (${token.length} chars)" }, email=${account.email}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "getIdTokenFromResult: getResult failed", e)
            Log.e(TAG, "getIdTokenFromResult: exception message=${e.message}, cause=${e.cause?.message}")
            null
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
    }
}
