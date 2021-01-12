package com.github.ruslanyussupov.androidmvi.core.internal

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal fun <T> MutableSharedFlow<T>.tryEmitOrSuspend(scope: CoroutineScope, value: T) {
    if (tryEmit(value)) {
        // emitted successfully without blocking
    } else {
        scope.launch {
            emit(value)
        }
    }
}

internal fun <T> MutableSharedFlow<T>.tryEmitOrBlock(value: T) {
    if (tryEmit(value)) {
        // emitted successfully without blocking
    } else {
        runBlocking {
            emit(value)
        }
    }
}

internal fun <T : Any> SharedFlow<T>.asProducer(): Producer<T> = object : Producer<T> {

    override val source: SharedFlow<T>
        get() = this@asProducer
}

internal fun <T : Any> MutableSharedFlow<T>.asConsumer(): Consumer<T> = object : Consumer<T> {

    override fun receive(value: T) {
        this@asConsumer.tryEmitOrBlock(value)
    }
}