package com.github.ruslanyussupov.androidmvi.core.core

interface Store<Trigger : Any, State : Any> : Consumer<Trigger>, Producer<State> {

    val state: State
}