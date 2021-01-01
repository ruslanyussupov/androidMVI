package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.core.core.Store

interface Feature<Trigger : Any, State : Any, Event : Any> : Store<Trigger, State> {

    val events: Producer<Event>
}