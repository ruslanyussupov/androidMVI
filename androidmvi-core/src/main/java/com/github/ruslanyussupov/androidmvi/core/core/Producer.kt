package com.github.ruslanyussupov.androidmvi.core.core

import kotlinx.coroutines.flow.SharedFlow

interface Producer<out T> {
    val source: SharedFlow<T>
}