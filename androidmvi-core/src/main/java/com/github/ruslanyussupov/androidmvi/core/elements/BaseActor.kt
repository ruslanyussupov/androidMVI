package com.github.ruslanyussupov.androidmvi.core.elements

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

abstract class BaseActor<Action : Any, in State : Any, out Effect : Any>(
    coroutineContext: CoroutineContext
) : Actor<Action, State, Effect> {

    protected val scope = CoroutineScope(SupervisorJob(coroutineContext.job) + Dispatchers.Main.immediate)
}