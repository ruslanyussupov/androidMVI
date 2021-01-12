package com.github.ruslanyussupov.androidmvi.demo

import com.github.ruslanyussupov.androidmvi.core.binder.named
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class MviDemoViewModelBindings(
    coroutineContext: CoroutineContext,
    private val feature: Feature,
    private val eventsListener: MviDemoEventsListener
) : AndroidBinder<MviDemoViewModel>(coroutineContext, Dispatchers.Main.immediate) {

    override fun setup(target: MviDemoViewModel) {
        binder.bind(feature to target named "MviDemoViewModel.In")
        binder.bind(target to feature named "Feature.In")
        binder.bind(feature.events to eventsListener named "Feature.Event")
    }
}