package com.github.ruslanyussupov.androidmvi.core.elements

interface Reducer<in Trigger : Any, State : Any> {
    fun reduce(action: Trigger, state: State): State
}