package com.github.ruslanyussupov.androidmvi.core.elements

interface EventPublisher<in Action : Any, in Effect : Any, in State : Any, out Event : Any> {
    fun publish(action: Action, effect: Effect, state: State): Event?
}