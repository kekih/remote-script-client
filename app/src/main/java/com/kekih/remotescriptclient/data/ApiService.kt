package com.kekih.remotescriptclient.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiService(
    private val baseUrl: String,
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun normalizeUrl(url: String): String {
        return url.trimEnd('/')
    }

    suspend fun health(): Result<HealthResponse> = runCatching {
        val request = Request.Builder()
            .url("${normalizeUrl(baseUrl)}/health")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            val body = response.body?.string() ?: throw Exception("Empty body")
            json.decodeFromString<HealthResponse>(body)
        }
    }

    suspend fun execute(
        language: String,
        code: String,
        timeout: Int = 30,
        stdin: String? = null
    ): Result<ExecuteResponse> = runCatching {
        val payload = ExecuteRequest(
            language = language,
            code = code,
            timeout = timeout,
            stdin = stdin
        )
        val bodyJson = json.encodeToString(payload)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = bodyJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${normalizeUrl(baseUrl)}/api/v1/execute")
            .addHeader("X-API-Key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: $body")
            }
            json.decodeFromString<ExecuteResponse>(body)
        }
    }
}
