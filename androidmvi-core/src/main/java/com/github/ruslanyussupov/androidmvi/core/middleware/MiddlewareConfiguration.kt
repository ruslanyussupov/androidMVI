package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.core.Consumer

data class MiddlewareConfiguration(
    private val condition: WrappingCondition,
    private val factories: List<MiddlewareFactory<*>>
) {

    fun <T> applyOn(
        consumerToWrap: Consumer<T>,
        targetToCheck: Any,
        name: String?,
        standalone: Boolean
    ): Consumer<T> {
        var current = consumerToWrap
        if (condition.shouldWrap(targetToCheck, name, standalone)) {
            factories
        } else {
            emptyList()
        }.forEach { factory ->
            current = factory.invoke(current) as Middleware<Any, T>
        }

        return current
    }
}
