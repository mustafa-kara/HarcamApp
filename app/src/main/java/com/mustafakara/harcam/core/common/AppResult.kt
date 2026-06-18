package com.mustafakara.harcam.core.common

/**
 * Result wrapper for operations that can fail (notably remote calls) — architecture.md §5.
 *
 * Repositories never leak HttpException/IOException to ViewModels; they map failures to
 * [AppError] and return [AppResult.Error]. Local Room reads expose Flow directly and don't
 * use this wrapper.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

/** Catalog of user-meaningful failures. */
sealed interface AppError {
    /** No connectivity / host unreachable. */
    data object Network : AppError

    /** Request timed out. */
    data object Timeout : AppError

    /** Server returned an error status. */
    data class Server(val code: Int) : AppError

    /** Anything else (parse, unexpected). [cause] kept for logging only. */
    data class Unknown(val cause: Throwable? = null) : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}
