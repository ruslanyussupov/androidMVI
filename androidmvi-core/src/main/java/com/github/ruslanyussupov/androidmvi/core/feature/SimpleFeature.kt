package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.BaseActor
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerTransformer
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrBlock
import com.github.ruslanyussupov.androidmvi.core.middleware.NonWrappable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.CoroutineContext

open class SimpleFeature<Trigger : Any, State : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Trigger, State>,
    coroutineContext: CoroutineContext,
    eventPublisher: EventPublisher<Trigger, Trigger, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Trigger, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = BypassActor(coroutineContext),
    triggerTransformer = BypassTriggerTransformer(),
    coroutineContext = coroutineContext,
    eventPublisher = eventPublisher
) {

    private class BypassActor<Trigger : Any, State : Any>(
        coroutineContext: CoroutineContext
    ) : BaseActor<Trigger, State, Trigger>(coroutineContext), NonWrappable {

        private val _results = MutableSharedFlow<Pair<Trigger, Trigger>>()
        override val results: SharedFlow<Pair<Trigger, Trigger>>
            get() = _results.asSharedFlow()

        override fun execute(action: Trigger, state: State) {
            _results.tryEmitOrBlock(Pair(action, action))
        }
    }
}