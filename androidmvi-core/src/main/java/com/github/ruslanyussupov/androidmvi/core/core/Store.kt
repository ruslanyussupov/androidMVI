package com.github.ruslanyussupov.androidmvi.core.core

import kotlinx.coroutines.flow.StateFlow

interface Store<Trigger : Any, State : Any> : Consumer<Trigger>, Producer<State> {

    val state: StateFlow<State>
}