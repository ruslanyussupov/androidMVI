package com.github.ruslanyussupov.androidmvi.core.elements

interface PostProcessor<Action : Any, in Effect : Any, in State : Any> {
    fun process(action: Action, effect: Effect, state: State): Action?
}