package com.github.ruslanyussupov.androidmvi.core.internal

import com.github.ruslanyussupov.androidmvi.core.elements.TriggerToAction

internal class BypassTriggerToAction<Trigger : Any> : TriggerToAction<Trigger, Trigger> {

    override fun transform(trigger: Trigger): Trigger {
        return trigger
    }
}