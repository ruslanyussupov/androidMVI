package com.github.ruslanyussupov.androidmvi.core.internal

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.middleware.Middleware
import com.github.ruslanyussupov.androidmvi.core.middleware.Middlewares
import com.github.ruslanyussupov.androidmvi.core.middleware.NonWrappable
import com.github.ruslanyussupov.androidmvi.core.middleware.StandaloneMiddleware

internal fun <In> Consumer<In>.wrapWithMiddlewares(
    standalone: Boolean,
    name: String? = null,
    postfix: String? = null,
    wrapperOf: Any? = null
): Consumer<In> {

    val target = wrapperOf ?: this
    if (target is NonWrappable) {
        return this
    }

    var head = this
    Middlewares.configurations.forEach { configuration ->
        head = configuration.applyOn(head, target, name, standalone)
    }

    if (standalone && head is Middleware<*, *>) {
        head = StandaloneMiddleware(
            head as Middleware<In, In>,
            name ?: wrapperOf?.javaClass?.simpleName,
            postfix
        )
    }

    return head
}