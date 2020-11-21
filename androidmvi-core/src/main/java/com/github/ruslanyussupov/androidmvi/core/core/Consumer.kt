package com.github.ruslanyussupov.androidmvi.core.core

interface Consumer<in T> {
    fun receive(value: T)
}