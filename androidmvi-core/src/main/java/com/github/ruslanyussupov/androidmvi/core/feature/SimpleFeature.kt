package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.elements.Bootstrapper
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerToAction
import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrBlock
import com.github.ruslanyussupov.androidmvi.core.middleware.NonWrappable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.CoroutineContext

open class SimpleFeature<Trigger : Any, State : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Trigger, State>,
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher,
    bootstrapper: Bootstrapper<Trigger>? = null,
    eventPublisher: EventPublisher<Trigger, Trigger, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Trigger, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = BypassActor(),
    bootstrapper = bootstrapper,
    triggerToAction = BypassTriggerToAction(),
    coroutineContext = coroutineContext,
    dispatcher = dispatcher,
    eventPublisher = eventPublisher
) {

    private class BypassActor<Trigger : Any, State : Any> : Actor<Trigger, State, Trigger>, NonWrappable {

        private val _results = MutableSharedFlow<Pair<Trigger, Trigger>>()
        override val results: Flow<Pair<Trigger, Trigger>>
            get() = _results.asSharedFlow()

        override fun execute(action: Trigger, state: State) {
            _results.tryEmitOrBlock(Pair(action, action))
        }
    }
}