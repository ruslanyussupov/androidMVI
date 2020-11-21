package com.github.ruslanyussupov.androidmvi.core.middleware

import android.util.Log
import com.github.ruslanyussupov.androidmvi.core.core.Consumer

class LoggingMiddleware<in T>(
    wrapped: Consumer<T>
) : Middleware<T>(wrapped) {

    override fun onReceived(value: T) {
        Log.d("LoggingMiddleware", "$this -> $value")
    }
}

fun <T> Consumer<T>.wrapWithLogging(): Consumer<T> = LoggingMiddleware(this)