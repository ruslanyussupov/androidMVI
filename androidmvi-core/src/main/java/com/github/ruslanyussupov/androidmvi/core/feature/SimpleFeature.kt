package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.ActionResult
import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.internal.BypassTriggerTransformer
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import kotlinx.coroutines.CoroutineScope

open class SimpleFeature<Trigger : Any, State : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Trigger, State>,
    scope: CoroutineScope,
    eventPublisher: EventPublisher<Trigger, Trigger, State, Event>? = null
) : BaseFeature<Trigger, State, Trigger, Trigger, Event>(
    initialState = initialState,
    reducer = reducer,
    actor = BypassActor(),
    triggerTransformer = BypassTriggerTransformer(),
    scope = scope,
    eventPublisher = eventPublisher
) {

    private class BypassActor<Trigger : Any, State : Any> : Actor<Trigger, State, Trigger> {

        override suspend fun execute(action: Trigger, state: State): ActionResult<Trigger> {
            return ActionResult(single = action, flow = null)
        }
    }
}