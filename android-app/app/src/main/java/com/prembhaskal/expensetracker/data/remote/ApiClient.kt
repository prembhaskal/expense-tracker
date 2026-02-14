package com.prembhaskal.expensetracker.data.remote

import com.prembhaskal.expensetracker.data.auth.AuthStore
import com.prembhaskal.expensetracker.data.local.entity.CategoryEntity
import com.prembhaskal.expensetracker.data.local.entity.ExpenseEntity
import com.prembhaskal.expensetracker.util.FileLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.util.concurrent.TimeUnit

class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String?,
    private val authStore: AuthStore,
) {
    private val gson: Gson = GsonBuilder().create()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val refreshUrl = "$baseUrl/api/auth/refresh"
    private val refreshLock = Any()

    private val rawClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val token = tokenProvider()
            val requestWithAuth = request.newBuilder()
                .apply { if (token != null) addHeader("Authorization", "Bearer $token") }
                .addHeader("Content-Type", "application/json")
                .build()
            val body = requestWithAuth.body
            val (requestToSend, bufferedBody) = if (body != null) {
                val buffer = Buffer()
                body.writeTo(buffer)
                val byteString = buffer.readByteString()
                val bodyCopy = byteString.toRequestBody(body.contentType())
                Pair(
                    requestWithAuth.newBuilder().method(requestWithAuth.method, bodyCopy).build(),
                    byteString to body.contentType()
                )
            } else {
                Pair(requestWithAuth, null)
            }
            var response = chain.proceed(requestToSend)

            if (response.code == 401 && !request.url.encodedPath.contains("/auth/refresh")) {
                val refreshToken = authStore.getRefreshToken()
                if (refreshToken != null) {
                    synchronized(refreshLock) {
                        val refreshed = tryRefresh(refreshToken)
                        if (refreshed) {
                            response.body?.close()
                            val newToken = tokenProvider()
                            val retryRequest = request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .addHeader("Content-Type", "application/json")
                                .apply {
                                    if (bufferedBody != null) {
                                        method(request.method, bufferedBody.first.toRequestBody(bufferedBody.second))
                                    }
                                }
                                .build()
                            response = chain.proceed(retryRequest)
                        }
                    }
                }
            }
            response
        })
        .build()

    private fun tryRefresh(refreshToken: String): Boolean {
        val json = gson.toJson(mapOf("refresh_token" to refreshToken))
        val request = Request.Builder()
            .url(refreshUrl)
            .post(json.toRequestBody(jsonType))
            .addHeader("Content-Type", "application/json")
            .build()
        rawClient.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                FileLogger.w("ApiClient", "refresh failed: code=${response.code} msg=$body")
                return false
            }
            val dto = gson.fromJson(body, AuthSessionResponse::class.java)
            authStore.setSession(dto.accessToken, dto.refreshToken)
            FileLogger.i("ApiClient", "token refreshed successfully")
            return true
        }
    }

    suspend fun getExpenses(limit: Int = 100, offset: Int = 0): List<ExpenseEntity> {
        val url = "$baseUrl/api/expenses?limit=$limit&offset=$offset"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
            val list = gson.fromJson(body ?: "[]", Array<ExpenseDto>::class.java).toList()
            return list.map { it.toEntity() }
        }
    }

    suspend fun getCategories(): List<CategoryEntity> {
        val request = Request.Builder().url("$baseUrl/api/categories").get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
            val list = gson.fromJson(body ?: "[]", Array<CategoryDto>::class.java).toList()
            return list.map { CategoryEntity(it.id, it.name, it.color, it.createdAt, pendingSync = false) }
        }
    }

    suspend fun createExpense(amount: Double, description: String?, date: String, categoryId: String?): ExpenseEntity {
        val json = gson.toJson(mapOf(
            "amount" to amount,
            "description" to (description ?: ""),
            "date" to date,
            "category_id" to categoryId
        ))
        val request = Request.Builder()
            .url("$baseUrl/api/expenses")
            .post(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
            val dto = gson.fromJson(body, ExpenseDto::class.java)
            return dto.toEntity()
        }
    }

    suspend fun updateExpense(id: String, amount: Double, description: String?, date: String, categoryId: String?) {
        val json = gson.toJson(mapOf(
            "amount" to amount,
            "description" to (description ?: ""),
            "date" to date,
            "category_id" to categoryId
        ))
        val request = Request.Builder()
            .url("$baseUrl/api/expenses/$id")
            .patch(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
        }
    }

    suspend fun deleteExpense(id: String) {
        val request = Request.Builder().url("$baseUrl/api/expenses/$id").delete().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
        }
    }

    suspend fun createCategory(name: String, color: String?): CategoryEntity {
        val json = gson.toJson(mapOf("name" to name, "color" to (color ?: "")))
        val request = Request.Builder()
            .url("$baseUrl/api/categories")
            .post(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
            val dto = gson.fromJson(body, CategoryDto::class.java)
            return CategoryEntity(dto.id, dto.name, dto.color, dto.createdAt, pendingSync = false)
        }
    }

    suspend fun updateCategory(id: String, name: String, color: String?) {
        val json = gson.toJson(mapOf("name" to name, "color" to (color ?: "")))
        val request = Request.Builder()
            .url("$baseUrl/api/categories/$id")
            .patch(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
        }
    }

    suspend fun deleteCategory(id: String) {
        val request = Request.Builder().url("$baseUrl/api/categories/$id").delete().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
        }
    }

    suspend fun loginWithIdToken(idToken: String): AuthSessionResponse {
        val json = gson.toJson(mapOf("id_token" to idToken))
        val request = Request.Builder()
            .url("$baseUrl/api/auth/session")
            .post(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) logAndThrowApiError(response.code, body)
            return gson.fromJson(body, AuthSessionResponse::class.java)
        }
    }

    private fun ExpenseDto.toEntity() = ExpenseEntity(
        id = id,
        amount = amount,
        description = description,
        date = date,
        categoryId = categoryId,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        categoryName = categories?.name,
        addedByName = profiles?.fullName,
        pendingSync = false,
        pendingUpdate = false,
    )
}

class ApiException(val code: Int, override val message: String?) : Exception("API error $code: $message")

private fun logAndThrowApiError(code: Int, body: String?): Nothing {
    FileLogger.e("ApiClient", "API error code=$code msg=$body")
    throw ApiException(code, body)
}
