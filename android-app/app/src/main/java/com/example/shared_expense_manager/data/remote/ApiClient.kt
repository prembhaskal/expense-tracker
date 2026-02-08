package com.example.shared_expense_manager.data.remote

import com.example.shared_expense_manager.data.local.entity.CategoryEntity
import com.example.shared_expense_manager.data.local.entity.ExpenseEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class ApiClient(private val baseUrl: String, private val tokenProvider: () -> String?) {
    private val gson: Gson = GsonBuilder().create()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val token = tokenProvider()
            val request = chain.request().newBuilder()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            request.addHeader("Content-Type", "application/json")
            chain.proceed(request.build())
        })
        .build()

    suspend fun getExpenses(limit: Int = 100, offset: Int = 0): List<ExpenseEntity> {
        val url = "$baseUrl/api/expenses?limit=$limit&offset=$offset"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
            val body = response.body?.string() ?: "[]"
            val list = gson.fromJson(body, Array<ExpenseDto>::class.java).toList()
            return list.map { it.toEntity() }
        }
    }

    suspend fun getCategories(): List<CategoryEntity> {
        val request = Request.Builder().url("$baseUrl/api/categories").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
            val body = response.body?.string() ?: "[]"
            val list = gson.fromJson(body, Array<CategoryDto>::class.java).toList()
            return list.map { CategoryEntity(it.id, it.name, it.color, it.createdAt) }
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
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
            val dto = gson.fromJson(response.body?.string(), ExpenseDto::class.java)
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
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
        }
    }

    suspend fun deleteExpense(id: String) {
        val request = Request.Builder().url("$baseUrl/api/expenses/$id").delete().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
        }
    }

    suspend fun createCategory(name: String, color: String?): CategoryEntity {
        val json = gson.toJson(mapOf("name" to name, "color" to (color ?: "")))
        val request = Request.Builder()
            .url("$baseUrl/api/categories")
            .post(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
            val dto = gson.fromJson(response.body?.string(), CategoryDto::class.java)
            return CategoryEntity(dto.id, dto.name, dto.color, dto.createdAt)
        }
    }

    suspend fun updateCategory(id: String, name: String, color: String?) {
        val json = gson.toJson(mapOf("name" to name, "color" to (color ?: "")))
        val request = Request.Builder()
            .url("$baseUrl/api/categories/$id")
            .patch(json.toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
        }
    }

    suspend fun deleteCategory(id: String) {
        val request = Request.Builder().url("$baseUrl/api/categories/$id").delete().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
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
            if (!response.isSuccessful) throw ApiException(response.code, body)
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
    )
}

class ApiException(val code: Int, override val message: String?) : Exception("API error $code: $message")
