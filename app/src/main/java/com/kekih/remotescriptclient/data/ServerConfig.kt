package com.kekih.remotescriptclient.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val apiKey: String
)

class ServerStore(context: Context) {
    private val prefs = context.getSharedPreferences("vixhub", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getServers(): List<ServerConfig> {
        val raw = prefs.getString("servers", "[]") ?: "[]"
        return try {
            json.decodeFromString<List<ServerConfig>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveServers(list: List<ServerConfig>) {
        prefs.edit().putString("servers", json.encodeToString(list)).apply()
    }

    fun getActiveId(): String? = prefs.getString("active_server_id", null)

    fun setActiveId(id: String?) {
        prefs.edit().putString("active_server_id", id).apply()
    }

    fun getActive(): ServerConfig? {
        val id = getActiveId() ?: return getServers().firstOrNull()
        return getServers().find { it.id == id } ?: getServers().firstOrNull()
    }
}
