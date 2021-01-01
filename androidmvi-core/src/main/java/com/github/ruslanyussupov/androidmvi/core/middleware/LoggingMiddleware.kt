package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.binder.Connection
import com.github.ruslanyussupov.androidmvi.core.core.Consumer

class LoggingMiddleware<T : Any>(
    wrapped: Consumer<T>,
    private val logger: Logger
) : Middleware<Any, T>(wrapped) {

    private val logTag = "LoggingMiddleware"

    override fun onReceive(connection: Connection<Any, T>, value: T) {
        super.onReceive(connection, value)
        logger.invoke("$connection | onReceive -> $value")
    }

    override fun onBind(connection: Connection<Any, T>) {
        super.onBind(connection)
        logger.invoke("$connection | onBind")
    }

    override fun onComplete(connection: Connection<Any, T>) {
        super.onComplete(connection)
        logger.invoke("$connection | onComplete")
    }
}

typealias Logger = (String) -> Unit