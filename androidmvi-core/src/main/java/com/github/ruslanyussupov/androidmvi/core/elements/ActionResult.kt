package com.github.ruslanyussupov.androidmvi.core.elements

import kotlinx.coroutines.flow.Flow

data class ActionResult<out Effect : Any>(
    val single: Effect?,
    val flow: Flow<Effect>?
) {

    init {
        if (single == null && flow == null) {
            error("Either single or flow should not be null.")
        }
        if (single != null && flow != null) {
            error("Single and flow should not be null at the same time.")
        }
    }
}