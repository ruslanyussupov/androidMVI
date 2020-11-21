package com.github.ruslanyussupov.androidmvi.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.demo.Feature.Event
import com.github.ruslanyussupov.androidmvi.demo.Feature.State
import com.github.ruslanyussupov.androidmvi.demo.Feature.Trigger
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class MviDemoViewModel : ViewModel(), Consumer<Trigger>, Producer<State> {

    private val feature = Feature(viewModelScope)
    val events: SharedFlow<Event> = feature.events

    override fun receive(value: Trigger) {
        feature.receive(value)
    }

    override fun flow(): StateFlow<State> = feature.flow()
}