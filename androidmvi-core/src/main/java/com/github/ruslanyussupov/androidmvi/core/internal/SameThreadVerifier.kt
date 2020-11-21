package com.github.ruslanyussupov.androidmvi.core.internal

internal class SameThreadVerifier {

    private val originalThreadId = Thread.currentThread().id

    fun verify() {
        val currentThreadId = Thread.currentThread().id
        if (originalThreadId != currentThreadId) {
            error("Should call on the same thread.")
        }
    }

    companion object {
        var isEnabled = true
    }
}