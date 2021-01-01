package com.github.ruslanyussupov.androidmvi.demo

import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

class MviDemoEventsListener(
    private val events: MutableSharedFlow<Feature.Event>,
    private val scope: CoroutineScope
) : Consumer<Feature.Event> {

    override fun receive(value: Feature.Event) {
        events.tryEmitOrSuspend(scope, value)
    }
}