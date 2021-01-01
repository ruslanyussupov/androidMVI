package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerTransformer
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import kotlin.coroutines.CoroutineContext

open class AsyncFeature<Trigger : Any, State : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Trigger, State>,
    actor: Actor<Trigger, State, Trigger>,
    coroutineContext: CoroutineContext,
    eventPublisher: EventPublisher<Trigger, Trigger, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Trigger, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = actor,
    triggerTransformer = BypassTriggerTransformer(),
    coroutineContext = coroutineContext,
    eventPublisher = eventPublisher
)