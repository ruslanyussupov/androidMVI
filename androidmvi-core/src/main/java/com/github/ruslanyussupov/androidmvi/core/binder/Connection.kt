package com.github.ruslanyussupov.androidmvi.core.binder

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer

data class Connection<Out, In>(
    val from: Producer<Out>?,
    val to: Consumer<In>,
    val transformer: (Out) -> In,
    val name: String?
) {

    override fun toString(): String {
        return name ?: ANONYMOUS
    }

    companion object {
        private const val ANONYMOUS = "ANONYMOUS"
    }
}

infix fun <Out, In> Pair<Producer<Out>, Consumer<In>>.using(
    transformer: (Out) -> In
): Connection<Out, In> = Connection(
    from = first,
    to = second,
    transformer = transformer,
    name = null
)

infix fun <T> Pair<Producer<T>, Consumer<T>>.named(
    name: String
): Connection<T, T> = Connection(
    from = first,
    to = second,
    transformer = { it },
    name = name
)

infix fun <Out, In> Connection<Out, In>.named(
    name: String
): Connection<Out, In> = copy(
    name = name
)