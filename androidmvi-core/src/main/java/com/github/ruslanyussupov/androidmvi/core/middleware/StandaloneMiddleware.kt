package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.binder.Connection

internal class StandaloneMiddleware<In>(
    private val wrappedMiddleware: Middleware<In, In>,
    name: String?,
    postfix: String?
) : Middleware<In, In>(wrappedMiddleware) {

    private var bound = false
    private val connection = Connection<In, In>(
        from = null,
        to = innerMost,
        transformer = { it },
        name = "${name ?: innerMost.javaClass.simpleName}.${postfix ?: "Input"}"
    )

    init {
        onBind(connection)
    }

    override fun onBind(connection: Connection<In, In>) {
        assertSame(connection)
        bound = true
        wrappedMiddleware.onBind(connection)
    }

    override fun receive(value: In) {
        wrappedMiddleware.onReceive(connection, value)
        wrappedMiddleware.receive(value)
    }

    override fun onComplete(connection: Connection<In, In>) {
        wrappedMiddleware.onComplete(connection)
    }

    private fun assertSame(connection: Connection<In, In>) {
        if (bound && connection != this.connection) {
            error("Middleware was initialised in standalone mode, can't accept other connections.")
        }
    }
}