package com.github.ruslanyussupov.androidmvi.core.elements

import kotlinx.coroutines.flow.SharedFlow

interface Actor<Action : Any, in State : Any, out Effect : Any> {

    val results: SharedFlow<Pair<Action, Effect>>

    fun execute(action: Action, state: State)
}