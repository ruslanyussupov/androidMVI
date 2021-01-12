package com.github.ruslanyussupov.androidmvi.core.elements

interface TriggerToAction<in Trigger : Any, out Action : Any> {
    fun transform(trigger: Trigger): Action
}