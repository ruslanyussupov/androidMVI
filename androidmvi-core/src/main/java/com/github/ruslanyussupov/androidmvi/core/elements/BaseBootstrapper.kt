package com.github.ruslanyussupov.androidmvi.core.elements

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

abstract class BaseBootstrapper<Action : Any>(
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher
) : Bootstrapper<Action> {

    protected val scope = CoroutineScope(SupervisorJob(coroutineContext.job) + dispatcher)
    protected val actions = MutableSharedFlow<Action>()

    override val source: Flow<Action>
        get() = actions.asSharedFlow()
}