package com.github.ruslanyussupov.androidmvi.demo

import com.github.ruslanyussupov.androidmvi.core.binder.Binder
import kotlin.coroutines.CoroutineContext

abstract class AndroidBinder<T : Any>(
    coroutineContext: CoroutineContext
) {

    protected val binder = Binder(coroutineContext)

    abstract fun setup(target: T)
}