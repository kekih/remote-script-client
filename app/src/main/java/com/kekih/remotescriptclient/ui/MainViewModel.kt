package com.kekih.remotescriptclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kekih.remotescriptclient.data.ApiService
import com.kekih.remotescriptclient.data.ExecuteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val serverUrl: String = "http://10.0.2.2:8000", // Android emulator -> host localhost
    val apiKey: String = "",
    val language: String = "python",
    val code: String = """print("Hello from remote Python!")""",
    val timeout: Int = 30,
    val isLoading: Boolean = false,
    val lastResult: ExecuteResponse? = null,
    val healthInfo: String? = null,
    val errorMessage: String? = null,
    val connectionOk: Boolean? = null
)

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun updateServerUrl(url: String) {
        _state.update { it.copy(serverUrl = url) }
    }

    fun updateApiKey(key: String) {
        _state.update { it.copy(apiKey = key) }
    }

    fun updateLanguage(lang: String) {
        val sample = when (lang) {
            "python" -> """print("Hello from remote Python!")
print(2 + 2)"""
            "nodejs" -> """console.log("Hello from remote Node.js!");
console.log(2 + 2);"""
            else -> ""
        }
        _state.update {
            it.copy(
                language = lang,
                code = if (it.code.contains("Hello from remote")) sample else it.code
            )
        }
    }

    fun updateCode(code: String) {
        _state.update { it.copy(code = code) }
    }

    fun updateTimeout(t: Int) {
        _state.update { it.copy(timeout = t.coerceIn(1, 60)) }
    }

    fun checkHealth() {
        val current = _state.value
        if (current.serverUrl.isBlank()) {
            _state.update { it.copy(errorMessage = "Укажи URL сервера") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, healthInfo = null) }
            val result = withContext(Dispatchers.IO) {
                ApiService(current.serverUrl, current.apiKey).health()
            }
            result.fold(
                onSuccess = { health ->
                    val info = buildString {
                        append("Статус: ${health.status}\n")
                        append("Python: ${health.python}\n")
                        append("Node.js: ${health.nodejs ?: "не найден"}\n")
                        append("Max timeout: ${health.max_timeout}s")
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            healthInfo = info,
                            connectionOk = true
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Health check failed: ${e.message}",
                            connectionOk = false
                        )
                    }
                }
            )
        }
    }

    fun runScript() {
        val current = _state.value
        if (current.serverUrl.isBlank()) {
            _state.update { it.copy(errorMessage = "Укажи URL сервера") }
            return
        }
        if (current.apiKey.isBlank()) {
            _state.update { it.copy(errorMessage = "Укажи API Key") }
            return
        }
        if (current.code.isBlank()) {
            _state.update { it.copy(errorMessage = "Код пустой") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    lastResult = null
                )
            }
            val result = withContext(Dispatchers.IO) {
                ApiService(current.serverUrl, current.apiKey).execute(
                    language = current.language,
                    code = current.code,
                    timeout = current.timeout
                )
            }
            result.fold(
                onSuccess = { resp ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            lastResult = resp
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка: ${e.message}"
                        )
                    }
                }
            )
        }
    }
}
