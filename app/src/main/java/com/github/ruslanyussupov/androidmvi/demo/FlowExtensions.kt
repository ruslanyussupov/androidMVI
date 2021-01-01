package com.github.ruslanyussupov.androidmvi.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
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