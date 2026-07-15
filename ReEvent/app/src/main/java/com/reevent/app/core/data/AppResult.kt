package com.reevent.app.core.data

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val reason: FailureReason, val cause: Throwable? = null) : AppResult<Nothing>
}

enum class FailureReason {
    VALIDATION,
    ACCOUNT_ALREADY_EXISTS,
    UNAUTHENTICATED,
    EMAIL_CONFIRMATION_REQUIRED,
    RATE_LIMITED,
    OFFLINE,
    CONFIGURATION,
    CONFLICT,
    SERVER,
    UNKNOWN
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Data<T>(val value: T) : UiState<T>
    data class Error(val reason: FailureReason) : UiState<Nothing>
}
