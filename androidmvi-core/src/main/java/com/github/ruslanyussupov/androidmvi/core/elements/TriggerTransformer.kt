package com.github.ruslanyussupov.androidmvi.core.elements

interface TriggerTransformer<in Trigger : Any, out Action : Any> {
    fun transform(trigger: Trigger): Action
}