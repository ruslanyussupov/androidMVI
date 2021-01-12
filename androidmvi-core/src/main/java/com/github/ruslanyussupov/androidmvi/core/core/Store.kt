package com.github.ruslanyussupov.androidmvi.core.core

interface Store<in Trigger : Any, out State : Any> : Consumer<Trigger>, Producer<State> {

    val state: State
}