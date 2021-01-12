package com.github.ruslanyussupov.androidmvi.core.core

import kotlinx.coroutines.flow.Flow

interface Producer<out T> {
    val source: Flow<T>
}