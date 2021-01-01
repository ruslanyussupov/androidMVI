package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.binder.Connection
import com.github.ruslanyussupov.androidmvi.core.core.Consumer

abstract class Middleware<Out, In>(
    private val wrapped: Consumer<In>
) : Consumer<In> {

    override fun receive(value: In) {
        wrapped.receive(value)
    }

    open fun onBind(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware {
            onBind(connection)
        }
    }

    open fun onReceive(connection: Connection<Out, In>, value: In) {
        wrapped.applyIfMiddleware {
            onReceive(connection, value)
        }
    }

    open fun onComplete(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware {
            onComplete(connection)
        }
    }

    protected val innerMost by lazy {
        var consumer = wrapped
        while (consumer is Middleware<*, *>) {
            consumer = (consumer as Middleware<Out, In>).wrapped
        }
        consumer
    }

    private inline fun Consumer<In>.applyIfMiddleware(
        block: Middleware<Out, In>.() -> Unit
    ) {
        (this as? Middleware<Out, In>)?.let { middleware ->
            middleware.block()
        }
    }
}