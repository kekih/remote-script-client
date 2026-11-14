package com.kekih.remotescriptclient.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiService(private val baseUrl: String, private val apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun url(path: String) = "${baseUrl.trimEnd('/')}$path"

    private fun req(builder: Request.Builder): Request =
        builder.addHeader("X-API-Key", apiKey).build()

    suspend fun health(): Result<HealthResponse> = runCatching {
        val r = client.newCall(Request.Builder().url(url("/health")).get().build()).execute()
        r.use {
            if (!it.isSuccessful) throw Exception("HTTP ${it.code}")
            json.decodeFromString<HealthResponse>(it.body?.string() ?: "")
        }
    }

    suspend fun listFiles(path: String = ""): Result<FilesResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/files?path=$path")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception("HTTP ${it.code}: ${it.body?.string()}")
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }

    suspend fun mkdir(path: String): Result<OkResponse> = runCatching {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("path", path).build()
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/files/mkdir")).post(body))).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{\"ok\":true}")
        }
    }

    suspend fun deleteFile(path: String): Result<OkResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/files?path=$path")).delete())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{\"ok\":true}")
        }
    }

    suspend fun listDb(): Result<DbListResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/db")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }

    suspend fun readDb(name: String, path: String = ""): Result<DbReadResponse> = runCatching {
        val q = if (path.isBlank()) "" else "?path=$path"
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/db/$name$q")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }

    suspend fun writeDb(name: String, path: String, valueJson: String, merge: Boolean, force: Boolean): Result<OkResponse> = runCatching {
        val payload = """{"name":"$name","path":"$path","value":$valueJson,"merge":$merge,"force":$force}"""
        val body = payload.toRequestBody("application/json".toMediaType())
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/db")).post(body))).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{\"ok\":true}")
        }
    }

    suspend fun listScripts(): Result<ScriptsResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/scripts")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }

    suspend fun startScript(id: String): Result<OkResponse> = runCatching {
        val body = """{"id":"$id"}""".toRequestBody("application/json".toMediaType())
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/scripts/start")).post(body))).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{\"ok\":true}")
        }
    }

    suspend fun stopScript(id: String): Result<OkResponse> = runCatching {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("id", id).build()
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/scripts/stop")).post(body))).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{\"ok\":true}")
        }
    }

    suspend fun scriptLogs(id: String): Result<LogsResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/scripts/logs?id=$id")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }

    suspend fun listSites(): Result<SitesResponse> = runCatching {
        val r = client.newCall(req(Request.Builder().url(url("/api/v1/sites")).get())).execute()
        r.use {
            if (!it.isSuccessful) throw Exception(it.body?.string())
            json.decodeFromString(it.body?.string() ?: "{}")
        }
    }
}
