package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerToAction
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

open class AsyncFeature<Trigger : Any, State : Any, Effect : Any, out Event : Any>(
    initialState: State,
    reducer: Reducer<Effect, State>,
    actor: Actor<Trigger, State, Effect>,
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher,
    eventPublisher: EventPublisher<Trigger, Effect, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Effect, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = actor,
    triggerToAction = BypassTriggerToAction(),
    coroutineContext = coroutineContext,
    dispatcher = dispatcher,
    eventPublisher = eventPublisher
)