package com.kekih.remotescriptclient.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kekih.remotescriptclient.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class Section { Home, Files, Bases, Scripts, Sites, Server }

data class UiState(
    val servers: List<ServerConfig> = emptyList(),
    val activeServer: ServerConfig? = null,
    val section: Section = Section.Home,
    val healthOk: Boolean? = null,
    val healthInfo: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filesPath: String = "",
    val files: List<FileItem> = emptyList(),
    val databases: List<String> = emptyList(),
    val dbName: String = "base",
    val dbPath: String = "",
    val dbValue: String = "",
    val scripts: List<ScriptItem> = emptyList(),
    val scriptId: String = "main",
    val scriptLogs: String = "",
    val sites: List<SiteItem> = emptyList(),
    val showServerMenu: Boolean = false,
    val showAddServer: Boolean = false,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = ServerStore(app)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refreshServers()
        checkHealth()
    }

    private fun api(): ApiService? {
        val s = _state.value.activeServer ?: return null
        return ApiService(s.url, s.apiKey)
    }

    fun refreshServers() {
        val list = store.getServers()
        val active = store.getActive()
        _state.update { it.copy(servers = list, activeServer = active) }
    }

    fun openServerMenu() = _state.update { it.copy(showServerMenu = true) }
    fun closeServerMenu() = _state.update { it.copy(showServerMenu = false, showAddServer = false) }
    fun openAddServer() = _state.update { it.copy(showAddServer = true) }

    fun selectServer(id: String) {
        store.setActiveId(id)
        refreshServers()
        closeServerMenu()
        checkHealth()
    }

    fun addServer(name: String, url: String, key: String) {
        val list = store.getServers().toMutableList()
        val cfg = ServerConfig(UUID.randomUUID().toString(), name.ifBlank { url }, url.trim(), key.trim())
        list.add(cfg)
        store.saveServers(list)
        store.setActiveId(cfg.id)
        refreshServers()
        closeServerMenu()
        checkHealth()
    }

    fun deleteServer(id: String) {
        val list = store.getServers().filter { it.id != id }
        store.saveServers(list)
        if (store.getActiveId() == id) store.setActiveId(list.firstOrNull()?.id)
        refreshServers()
    }

    fun go(section: Section) = _state.update { it.copy(section = section, error = null) }

    fun checkHealth() {
        val a = api() ?: run {
            _state.update { it.copy(healthOk = false, healthInfo = "Нет сервера") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val r = withContext(Dispatchers.IO) { a.health() }
            r.fold(
                onSuccess = { h ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            healthOk = true,
                            healthInfo = "v${h.version ?: \"?\"} · Python ${h.python}" +
                                (h.nodejs?.let { n -> " · Node $n" } ?: "")
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isLoading = false, healthOk = false, healthInfo = e.message, error = e.message)
                    }
                }
            )
        }
    }

    fun loadFiles(path: String = _state.value.filesPath) {
        val a = api() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val r = withContext(Dispatchers.IO) { a.listFiles(path) }
            r.fold(
                onSuccess = { res ->
                    _state.update { it.copy(isLoading = false, filesPath = res.path, files = res.items) }
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun mkdir(name: String) {
        val a = api() ?: return
        val path = listOf(_state.value.filesPath, name).filter { it.isNotBlank() }.joinToString("/")
        viewModelScope.launch {
            withContext(Dispatchers.IO) { a.mkdir(path) }
            loadFiles()
        }
    }

    fun deletePath(name: String) {
        val a = api() ?: return
        val path = listOf(_state.value.filesPath, name).filter { it.isNotBlank() }.joinToString("/")
        viewModelScope.launch {
            withContext(Dispatchers.IO) { a.deleteFile(path) }
            loadFiles()
        }
    }

    fun loadDatabases() {
        val a = api() ?: return
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { a.listDb() }
            r.onSuccess { res -> _state.update { it.copy(databases = res.databases) } }
        }
    }

    fun updateDbFields(name: String? = null, path: String? = null, value: String? = null) {
        _state.update {
            it.copy(
                dbName = name ?: it.dbName,
                dbPath = path ?: it.dbPath,
                dbValue = value ?: it.dbValue
            )
        }
    }

    fun readDb() {
        val a = api() ?: return
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val r = withContext(Dispatchers.IO) { a.readDb(s.dbName, s.dbPath) }
            r.fold(
                onSuccess = { res ->
                    val text = (res.value ?: res.data)?.toString() ?: "{}"
                    _state.update { it.copy(isLoading = false, dbValue = text) }
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun writeDb(merge: Boolean, force: Boolean) {
        val a = api() ?: return
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val r = withContext(Dispatchers.IO) {
                a.writeDb(s.dbName, s.dbPath, s.dbValue.ifBlank { "null" }, merge, force)
            }
            r.fold(
                onSuccess = { _state.update { it.copy(isLoading = false) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun loadScripts() {
        val a = api() ?: return
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { a.listScripts() }
            r.onSuccess { res -> _state.update { it.copy(scripts = res.scripts) } }
        }
    }

    fun setScriptId(id: String) = _state.update { it.copy(scriptId = id) }

    fun startScript() {
        val a = api() ?: return
        val id = _state.value.scriptId
        viewModelScope.launch {
            withContext(Dispatchers.IO) { a.startScript(id) }
            loadScripts()
        }
    }

    fun stopScript() {
        val a = api() ?: return
        val id = _state.value.scriptId
        viewModelScope.launch {
            withContext(Dispatchers.IO) { a.stopScript(id) }
            loadScripts()
        }
    }

    fun fetchLogs() {
        val a = api() ?: return
        val id = _state.value.scriptId
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { a.scriptLogs(id) }
            r.onSuccess { res -> _state.update { it.copy(scriptLogs = res.logs) } }
        }
    }

    fun loadSites() {
        val a = api() ?: return
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { a.listSites() }
            r.onSuccess { res -> _state.update { it.copy(sites = res.sites) } }
        }
    }
}
