package com.github.ruslanyussupov.androidmvi.core.elements

import com.github.ruslanyussupov.androidmvi.core.core.Producer

interface Bootstrapper<Action : Any> : Producer<Action> {

    fun initialize()
}