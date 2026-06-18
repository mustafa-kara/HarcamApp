package com.mustafakara.harcam.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Injectable dispatcher provider so use cases / repositories can be tested with a test
 * dispatcher instead of the real ones — architecture.md §9.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}
