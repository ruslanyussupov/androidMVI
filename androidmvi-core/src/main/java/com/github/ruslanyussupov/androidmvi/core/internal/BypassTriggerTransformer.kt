package com.github.ruslanyussupov.androidmvi.core.internal

import com.github.ruslanyussupov.androidmvi.core.elements.TriggerTransformer

internal class BypassTriggerTransformer<Trigger : Any> : TriggerTransformer<Trigger, Trigger> {

    override fun transform(trigger: Trigger): Trigger {
        return trigger
    }
}