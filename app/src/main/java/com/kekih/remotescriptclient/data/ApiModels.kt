package com.kekih.remotescriptclient.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class HealthResponse(
    val status: String = "ok",
    val version: String? = null,
    val python: String = "",
    val nodejs: String? = null,
    val data_dir: String? = null
)

@Serializable
data class FileItem(
    val name: String,
    val is_dir: Boolean = false,
    val size: Long = 0,
    val modified: String? = null
)

@Serializable
data class FilesResponse(val path: String = "", val items: List<FileItem> = emptyList())

@Serializable
data class DbListResponse(val databases: List<String> = emptyList())

@Serializable
data class DbReadResponse(
    val name: String = "",
    val path: String? = null,
    val data: JsonElement? = null,
    val value: JsonElement? = null
)

@Serializable
data class ScriptItem(
    val id: String,
    val running: Boolean = false,
    val size: Long = 0,
    val modified: String? = null
)

@Serializable
data class ScriptsResponse(val scripts: List<ScriptItem> = emptyList())

@Serializable
data class LogsResponse(val id: String = "", val logs: String = "")

@Serializable
data class SiteItem(val id: String, val has_index: Boolean = false)

@Serializable
data class SitesResponse(val sites: List<SiteItem> = emptyList())

@Serializable
data class OkResponse(val ok: Boolean = true, val message: String? = null, val id: String? = null)
