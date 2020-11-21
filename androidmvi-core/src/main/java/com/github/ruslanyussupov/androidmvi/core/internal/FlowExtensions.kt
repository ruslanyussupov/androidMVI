package com.github.ruslanyussupov.androidmvi.core.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal fun <T> MutableSharedFlow<T>.tryEmitOrSuspend(scope: CoroutineScope, value: T) {
    if (!tryEmit(value)) {
        scope.launch {
            emit(value)
        }
    }
}