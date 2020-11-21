package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.core.Store
import kotlinx.coroutines.flow.SharedFlow

interface Feature<Trigger : Any, State : Any, Event : Any> : Store<Trigger, State> {

    val events: SharedFlow<Event>
}