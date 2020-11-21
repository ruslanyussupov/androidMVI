package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.core.Consumer

abstract class Middleware<in T>(
    private val wrapped: Consumer<T>
) : Consumer<T> {

    override fun receive(value: T) {
        onReceived(value)
        wrapped.receive(value)
    }

    protected abstract fun onReceived(value: T)
}