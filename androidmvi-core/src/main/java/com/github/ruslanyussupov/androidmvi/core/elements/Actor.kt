package com.github.ruslanyussupov.androidmvi.core.elements

import kotlinx.coroutines.flow.Flow

interface Actor<Action : Any, in State : Any, out Effect : Any> {

    val results: Flow<Pair<Action, Effect>>

    fun execute(action: Action, state: State)
}