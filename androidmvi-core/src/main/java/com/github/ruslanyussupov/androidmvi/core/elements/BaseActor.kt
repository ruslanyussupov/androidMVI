package com.github.ruslanyussupov.androidmvi.core.elements

import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrBlock
import com.github.ruslanyussupov.androidmvi.core.internal.tryEmitOrSuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

abstract class BaseActor<Action : Any, in State : Any, Effect : Any>(
    coroutineContext: CoroutineContext,
    dispatcher: CoroutineDispatcher,
    extraBufferCapacity: Int = 1024
) : Actor<Action, State, Effect> {

    protected val scope = CoroutineScope(SupervisorJob(coroutineContext.job) + dispatcher)
    private val _results = MutableSharedFlow<Pair<Action, Effect>>(extraBufferCapacity = extraBufferCapacity)

    override val results: Flow<Pair<Action, Effect>>
        get() = _results.asSharedFlow()

    protected fun blockingPostResult(result: Pair<Action, Effect>) {
        _results.tryEmitOrBlock(result)
    }

    protected fun suspendingPostResult(result: Pair<Action, Effect>) {
        _results.tryEmitOrSuspend(scope, result)
    }
}