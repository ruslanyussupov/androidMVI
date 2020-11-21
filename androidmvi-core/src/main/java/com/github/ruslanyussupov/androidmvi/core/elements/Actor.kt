package com.github.ruslanyussupov.androidmvi.core.elements

interface Actor<in Action : Any, in State : Any, out Effect : Any> {
    suspend fun execute(action: Action, state: State): ActionResult<Effect>
}