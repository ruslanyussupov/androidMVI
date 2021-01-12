package com.github.ruslanyussupov.androidmvi.core.binder

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.core.internal.wrapWithMiddlewares
import com.github.ruslanyussupov.androidmvi.core.middleware.Middleware
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

class Binder(
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher
) {

    private val scope = CoroutineScope(SupervisorJob(coroutineContext.job) + dispatcher)

    fun <T> bind(connection: Pair<Producer<T>, Consumer<T>>) {
        bind(
            Connection(
                from = connection.first,
                to = connection.second,
                transformer = { it },
                name = null
            )
        )
    }

    fun <Out, In> bind(connection: Connection<Out, In>) {
        val consumer = connection.to.wrapWithMiddlewares(standalone = false, name = connection.name)
        @Suppress("UNCHECKED_CAST")
        val middleware = consumer as? Middleware<Out, In>
        connection.from!!.source
            .onStart {
                middleware?.onBind(connection)
            }
            .onCompletion {
                middleware?.onComplete(connection)
            }
            .map { value ->
                connection.transformer.invoke(value)
            }
            .onEach { value ->
                if (middleware == null) {
                    consumer.receive(value)
                } else {
                    middleware.onReceive(connection, value)
                    middleware.receive(value)
                }
            }.launchIn(scope)
    }
}