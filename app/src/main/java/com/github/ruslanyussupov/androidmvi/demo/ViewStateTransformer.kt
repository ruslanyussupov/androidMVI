package com.github.ruslanyussupov.androidmvi.demo

class ViewStateTransformer {

    fun transform(state: Feature.State): ViewState {
        return ViewState(state.counter)
    }
}