package com.meenbeese.chronos.utils

sealed class Option<out T> {
    object None : Option<Nothing>()
    data class Some<out T>(val value: T) : Option<T>()

    fun isEmpty(): Boolean = this is None
    fun isDefined(): Boolean = this is Some
    fun getOrElse(default: @UnsafeVariance T): T = when (this) {
        is Some -> value
        is None -> default
    }

    inline fun <R> map(f: (T) -> R): Option<R> = when (this) {
        is Some -> Some(f(value))
        is None -> None
    }

    inline fun <R> flatMap(f: (T) -> Option<R>): Option<R> = when (this) {
        is Some -> f(value)
        is None -> None
    }

    companion object {
        fun <T> fromNullable(value: T?): Option<T> =
            if (value != null) Some(value) else None
    }
}

fun <T> Option<T>.toNullable(): T? = when(this) {
    is Option.Some -> value
    is Option.None -> null
}
