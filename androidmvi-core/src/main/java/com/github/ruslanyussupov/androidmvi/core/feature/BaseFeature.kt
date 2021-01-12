package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.core.elements.Actor
import com.github.ruslanyussupov.androidmvi.core.elements.Bootstrapper
import com.github.ruslanyussupov.androidmvi.core.elements.EventPublisher
import com.github.ruslanyussupov.androidmvi.core.elements.PostProcessor
import com.github.ruslanyussupov.androidmvi.core.elements.Reducer
import com.github.ruslanyussupov.androidmvi.core.elements.TriggerToAction
import com.github.ruslanyussupov.androidmvi.core.internal.SameThreadVerifier
import com.github.ruslanyussupov.androidmvi.core.internal.asConsumer
import com.github.ruslanyussupov.androidmvi.core.internal.asProducer
import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrBlock
import com.github.ruslanyussupov.androidmvi.core.internal.wrapWithMiddlewares
import com.github.ruslanyussupov.androidmvi.core.middleware.StandaloneMiddleware
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

open class BaseFeature<in Trigger : Any, State : Any, Action : Any, Effect : Any, out Event : Any>(
    initialState: State,
    reducer: Reducer<Effect, State>,
    actor: Actor<Action, State, Effect>,
    private val triggerToAction: TriggerToAction<Trigger, Action>,
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher,
    private val bootstrapper: Bootstrapper<Action>? = null,
    postProcessor: PostProcessor<Action, Effect, State>? = null,
    eventPublisher: EventPublisher<Action, Effect, State, Event>? = null,
    actionsBufferSize: Int = 1024,
    eventsBufferSize: Int = 1024
) : Feature<Trigger, State, Event> {

    private val scope = CoroutineScope(SupervisorJob(coroutineContext.job) + dispatcher)
    private val sameThreadVerifier = SameThreadVerifier()
    private val actions = MutableSharedFlow<Action>(extraBufferCapacity = actionsBufferSize)
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = eventsBufferSize)
    private val _state = MutableStateFlow(initialState)
    private var isAttached = false

    override val events: Producer<Event>
        get() = _events.asSharedFlow().asProducer()

    override val state: State
        get() = _state.value

    override val source: SharedFlow<State>
        get() = _state.asSharedFlow()

    private val postProcessorWrapper = postProcessor?.let {
        PostProcessorWrapper(it, actions)
    }?.wrapWithMiddlewares(standalone = true, wrapperOf = postProcessor)

    private val eventPublisherWrapper = eventPublisher?.let {
        EventPublisherWrapper(it, _events)
    }?.wrapWithMiddlewares(standalone = true, wrapperOf = eventPublisher)

    private val reducerWrapper = ReducerWrapper(
        reducer,
        _state,
        postProcessorWrapper,
        eventPublisherWrapper
    ).wrapWithMiddlewares(standalone = true, wrapperOf = reducer)

    private val actorWrapper: Consumer<Pair<Action, State>> = ActorWrapper(
        actor,
        reducerWrapper,
        _state,
        scope,
        sameThreadVerifier
    ).wrapWithMiddlewares(standalone = true, wrapperOf = actor)

    init {
        val consumers = mutableListOf<Consumer<*>?>(postProcessorWrapper, eventPublisherWrapper, reducerWrapper, actorWrapper)
        if (bootstrapper != null) {
            val actionsConsumer = actions.asConsumer().wrapWithMiddlewares(
                standalone = true,
                wrapperOf = bootstrapper,
                postfix = "Output"
            )
            consumers += actionsConsumer
            bootstrapper.source.onEach { action ->
                actionsConsumer.receive(action)
            }.launchIn(scope)
        }

        scope.coroutineContext.job.invokeOnCompletion {
            consumers.filterIsInstance<StandaloneMiddleware<*>>().forEach { middleware ->
                middleware.complete()
            }
        }

        actions.onEach { action ->
            execute(action, state)
        }.launchIn(scope)

        if (eventPublisher != null) {
            _events.subscriptionCount.filter { count ->
                count > 0
            }.take(1).onEach {
                onAttached()
            }.launchIn(scope)
        } else {
            onAttached()
        }
    }

    private fun onAttached() {
        isAttached = true
        bootstrapper?.initialize()
    }

    override fun receive(value: Trigger) {
        if (!isAttached) {
            error("Must be subscribed to the event publisher before receiving triggers.")
        }
        val action = triggerToAction.transform(value)
        actions.tryEmitOrBlock(action)
    }

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
        private val state: MutableStateFlow<State>,
        private val scope: CoroutineScope,
        private val sameThreadVerifier: SameThreadVerifier
    ) : Consumer<Pair<Action, State>> {

        init {
            actor.results.onEach {
                val (action, effect) = it
                reduce(action, state.value, effect)
            }.launchIn(scope)
        }

        fun processAction(action: Action, state: State) {
            if (!scope.isActive) return
            actor.execute(action, state)
        }

        override fun receive(value: Pair<Action, State>) {
            val (action, state) = value
            processAction(action, state)
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
        private val state: MutableStateFlow<State>,
        private val postProcessorWrapper: Consumer<Triple<Action, Effect, State>>?,
        private val eventPublisherWrapper: Consumer<Triple<Action, Effect, State>>?
    ) : Consumer<Triple<Action, Effect, State>> {

        override fun receive(value: Triple<Action, Effect, State>) {
            val (action, effect, state) = value
            processEffect(action, effect, state)
        }

        fun processEffect(action: Action, effect: Effect, state: State) {
            val newState = reducer.reduce(effect, state)
            this.state.value = newState
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
                actions.tryEmitOrBlock(it)
            }
        }
    }

    private class EventPublisherWrapper<Action : Any, Effect : Any, State : Any, Event : Any>(
        private val eventPublisher: EventPublisher<Action, Effect, State, Event>,
        private val events: MutableSharedFlow<Event>
    ) : Consumer<Triple<Action, Effect, State>> {

        override fun receive(value: Triple<Action, Effect, State>) {
            val (action, effect, state) = value
            publish(action, effect, state)
        }

        fun publish(action: Action, effect: Effect, state: State) {
            eventPublisher.publish(action, effect, state)?.let {
                events.tryEmitOrBlock(it)
            }
        }
    }
}