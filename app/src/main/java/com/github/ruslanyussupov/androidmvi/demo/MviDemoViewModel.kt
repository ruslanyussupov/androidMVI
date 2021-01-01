package com.github.ruslanyussupov.androidmvi.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.demo.Feature.Event
import com.github.ruslanyussupov.androidmvi.demo.Feature.State
import com.github.ruslanyussupov.androidmvi.demo.Feature.Trigger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MviDemoViewModel : ViewModel(), Consumer<State>, Producer<Trigger> {

    private val _state = MutableLiveData<ViewState>()
    val state: LiveData<ViewState>
        get() = _state

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event>
        get() = _events

    private val feature = Feature(viewModelScope.coroutineContext)
    private val eventsListener = MviDemoEventsListener(_events, viewModelScope)
    private val bindings = MviDemoViewModelBindings(
        viewModelScope.coroutineContext,
        feature,
        eventsListener
    )
    private val triggers = MutableSharedFlow<Trigger>()
    private val viewStateTransformer = ViewStateTransformer()

    init {
        bindings.setup(this)
    }

    override val source: SharedFlow<Trigger>
        get() = triggers

    override fun receive(value: State) {
        _state.value = viewStateTransformer.transform(value)
    }

    fun onViewInteracted(trigger: Trigger) {
        triggers.tryEmitOrBlock(trigger)
    }
}