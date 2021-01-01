package com.github.ruslanyussupov.androidmvi.demo

import android.app.Application
import android.util.Log
import com.github.ruslanyussupov.androidmvi.core.middleware.LoggingMiddleware
import com.github.ruslanyussupov.androidmvi.core.middleware.MiddlewareConfiguration
import com.github.ruslanyussupov.androidmvi.core.middleware.Middlewares
import com.github.ruslanyussupov.androidmvi.core.middleware.WrappingCondition

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Middlewares.configurations += MiddlewareConfiguration(
            condition = WrappingCondition.Always,
            factories = listOf { targetToWrap ->
                LoggingMiddleware(targetToWrap) { message ->
                    Log.d("LoggingMiddleware", message)
                }
            }
        )
    }
}