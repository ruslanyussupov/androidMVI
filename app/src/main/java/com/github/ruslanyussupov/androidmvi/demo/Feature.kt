package com.github.ruslanyussupov.androidmvi.demo

import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.feature.SimpleFeature
import com.github.ruslanyussupov.androidmvi.demo.Feature.Event
import com.github.ruslanyussupov.androidmvi.demo.Feature.State
import com.github.ruslanyussupov.androidmvi.demo.Feature.Trigger
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class Feature(
    coroutineContext: CoroutineContext
) : SimpleFeature<Trigger, State, Event>(
    initialState = State(counter = 0),
    reducer = ReducerImpl(),
    coroutineContext = coroutineContext,
    dispatcher = Dispatchers.Main.immediate,
    eventPublisher = EventPublisherImpl()
) {

    sealed class Trigger {
        object IncrementClicked : Trigger()
    }

    data class State(val counter: Int)

    sealed class Event {
        data class MilestoneReached(val count: Int) : Event()
    }

    class ReducerImpl : Reducer<Trigger, State> {

        override fun reduce(action: Trigger, state: State): State {
            return when (action) {
                Trigger.IncrementClicked -> state.copy(counter = state.counter + 1)
            }
        }
    }

    class EventPublisherImpl : EventPublisher<Trigger, Trigger, State, Event> {

        override fun publish(action: Trigger, effect: Trigger, state: State): Event? {
            return when (action) {
                Trigger.IncrementClicked -> {
                    if (state.counter % 10 == 0) {
                        Event.MilestoneReached(state.counter)
                    } else {
                        null
                    }
                }
            }
        }
    }
}