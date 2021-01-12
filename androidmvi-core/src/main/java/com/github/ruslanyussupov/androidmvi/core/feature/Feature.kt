package com.github.ruslanyussupov.androidmvi.core.feature

import com.github.ruslanyussupov.androidmvi.core.core.Producer
import com.github.ruslanyussupov.androidmvi.core.core.Store

interface Feature<in Trigger : Any, out State : Any, out Event : Any> : Store<Trigger, State> {

    val events: Producer<Event>
}