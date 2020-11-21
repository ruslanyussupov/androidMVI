package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerTransformer
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import kotlinx.coroutines.CoroutineScope

open class AsyncFeature<Trigger : Any, State : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Trigger, State>,
    actor: Actor<Trigger, State, Trigger>,
    scope: CoroutineScope,
    eventPublisher: EventPublisher<Trigger, Trigger, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Trigger, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = actor,
    triggerTransformer = BypassTriggerTransformer(),
    scope = scope,
    eventPublisher = eventPublisher
)