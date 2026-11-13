package com.kekih.remotescriptclient.data

import kotlinx.serialization.Serializable

@Serializable
data class ExecuteRequest(
    val language: String,
    val code: String,
    val timeout: Int = 30,
    val stdin: String? = null
)

@Serializable
data class ExecuteResponse(
    val language: String,
    val stdout: String,
    val stderr: String,
    val exit_code: Int,
    val timed_out: Boolean,
    val error: String? = null
)

@Serializable
data class HealthResponse(
    val status: String,
    val python: String,
    val nodejs: String? = null,
    val max_timeout: Int
)
