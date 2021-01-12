package com.github.ruslanyussupov.androidmvi.demo

import com.github.ruslanyussupov.androidmvi.core.binder.Binder
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

abstract class AndroidBinder<T : Any>(
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher
) {

    protected val binder = Binder(coroutineContext, dispatcher)

    abstract fun setup(target: T)
}