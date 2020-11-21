package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.PostProcessor
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.internal.SameThreadVerifier
import com.github.ruslanyussupov.androidmvi.core.elements.TriggerTransformer
import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrSuspend
import com.github.ruslanyussupov.androidmvi.core.middleware.wrapWithLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

open class BaseFeature<Trigger : Any, State : Any, Action : Any, Effect : Any, Event : Any>(
    initialState: State,
    reducer: Reducer<Effect, State>,
    actor: Actor<Action, State, Effect>,
    private val triggerTransformer: TriggerTransformer<Trigger, Action>,
    private val scope: CoroutineScope,
    postProcessor: PostProcessor<Action, Effect, State>? = null,
    eventPublisher: EventPublisher<Action, Effect, State, Event>? = null
) : Feature<Trigger, State, Event> {

    private val sameThreadVerifier = SameThreadVerifier()
    private val actions = MutableSharedFlow<Action>(extraBufferCapacity = 100)
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 100)
    private val _state = MutableStateFlow(initialState)

    override val events: SharedFlow<Event>
        get() = _events.asSharedFlow()

    override val state: State
        get() = _state.value

    private val postProcessorWrapper = postProcessor?.let {
        PostProcessorWrapper(it, actions)
    }?.wrapWithLogging()

    private val eventPublisherWrapper = eventPublisher?.let {
        EventPublisherWrapper(it, _events, scope)
    }?.wrapWithLogging()

    private val reducerWrapper = ReducerWrapper(
        reducer,
        _state,
        postProcessorWrapper,
        eventPublisherWrapper
    ).wrapWithLogging()

    private val actorWrapper: Consumer<Pair<Action, State>> = ActorWrapper(
        actor,
        reducerWrapper,
        _state,
        scope,
        sameThreadVerifier
    ).wrapWithLogging()

    init {
        actions.onEach { action ->
            execute(action, state)
        }.launchIn(scope)
    }

    override fun receive(value: Trigger) {
        val action = triggerTransformer.transform(value)
        actions.tryEmitOrSuspend(scope, action)
    }

    override fun flow(): StateFlow<State> = _state

    private fun execute(action: Action, state: State) {
        if (!scope.isActive) return

        if (actorWrapper is ActorWrapper<Action, State, *>) {
            actorWrapper.processAction(action, state)
        } else {
            actorWrapper.receive(Pair(action, state))
        }
    }

    private class ActorWrapper<Action : Any, State : Any, Effect : Any>(
        private val actor: Actor<Action, State, Effect>,
        private val reducerWrapper: Consumer<Triple<Action, Effect, State>>,
        private val stateFlow: MutableStateFlow<State>,
        private val scope: CoroutineScope,
        private val sameThreadVerifier: SameThreadVerifier
    ) : Consumer<Pair<Action, State>> {

        override fun receive(value: Pair<Action, State>) {
            val (action, state) = value
            processAction(action, state)
        }

        fun processAction(action: Action, state: State) {
            if (!scope.isActive) return

            scope.launch {
                val (single, flow) = actor.execute(action, state)
                single?.let {
                    reduce(action, stateFlow.value, it)
                }
                flow?.collect {
                    reduce(action, stateFlow.value, it)
                }
            }
        }

        private fun reduce(action: Action, state: State, effect: Effect) {
            if (!scope.isActive) return

            sameThreadVerifier.verify()
            if (reducerWrapper is ReducerWrapper) {
                reducerWrapper.processEffect(action, effect, state)
            } else {
                reducerWrapper.receive(Triple(action, effect, state))
            }
        }
    }

    private class ReducerWrapper<Action : Any, Effect : Any, State : Any>(
        private val reducer: Reducer<Effect, State>,
        private val stateFlow: MutableStateFlow<State>,
        private val postProcessorWrapper: Consumer<Triple<Action, Effect, State>>?,
        private val eventPublisherWrapper: Consumer<Triple<Action, Effect, State>>?
    ) : Consumer<Triple<Action, Effect, State>> {

        override fun receive(value: Triple<Action, Effect, State>) {
            val (action, effect, state) = value
            processEffect(action, effect, state)
        }

        fun processEffect(action: Action, effect: Effect, state: State) {
            val newState = reducer.reduce(effect, state)
            stateFlow.value = newState
            postProcess(action, effect, newState)
            publishEvent(action, effect, newState)
        }

        private fun postProcess(action: Action, effect: Effect, state: State) {
            if (postProcessorWrapper != null) {
                if (postProcessorWrapper is PostProcessorWrapper) {
                    postProcessorWrapper.process(action, effect, state)
                } else {
                    postProcessorWrapper.receive(Triple(action, effect, state))
                }
            }
        }

        private fun publishEvent(action: Action, effect: Effect, state: State) {
            if (eventPublisherWrapper != null) {
                if (eventPublisherWrapper is EventPublisherWrapper<Action, Effect, State, *>) {
                    eventPublisherWrapper.publish(action, effect, state)
                } else {
                    eventPublisherWrapper.receive(Triple(action, effect, state))
                }
            }
        }
    }

    private class PostProcessorWrapper<Action : Any, Effect : Any, State : Any>(
        private val postProcessor: PostProcessor<Action, Effect, State>,
        private val actions: MutableSharedFlow<Action>
    ) : Consumer<Triple<Action, Effect, State>> {

        override fun receive(value: Triple<Action, Effect, State>) {
            val (action, effect, state) = value
            process(action, effect, state)
        }

        fun process(action: Action, effect: Effect, state: State) {
            postProcessor.process(action, effect, state)?.let {
                actions.tryEmit(it)
            }
        }
    }

    private class EventPublisherWrapper<Action : Any, Effect : Any, State : Any, Event : Any>(
        private val eventPublisher: EventPublisher<Action, Effect, State, Event>,
        private val events: MutableSharedFlow<Event>,
        private val scope: CoroutineScope
    ) : Consumer<Triple<Action, Effect, State>> {

        override fun receive(value: Triple<Action, Effect, State>) {
            val (action, effect, state) = value
            publish(action, effect, state)
        }

        fun publish(action: Action, effect: Effect, state: State) {
            eventPublisher.publish(action, effect, state)?.let {
                events.tryEmitOrSuspend(scope, it)
            }
        }
    }
}