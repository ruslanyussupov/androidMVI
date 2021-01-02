package com.github.ruslanyussupov.androidmvi.demo

import android.app.Application
import android.util.Log
import com.github.ruslanyussupov.androidmvi.core.middleware.LoggingMiddleware
import com.github.ruslanyussupov.androidmvi.core.middleware.MiddlewareConfiguration
import com.github.ruslanyussupov.androidmvi.core.middleware.Middlewares
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware
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

        Middlewares.configurations += MiddlewareConfiguration(
            condition = WrappingCondition.AllOf(
                WrappingCondition.Conditional {
                    BuildConfig.DEBUG
                },
                WrappingCondition.AnyOf(
                    WrappingCondition.IsNamed,
                    WrappingCondition.IsStandalone
                )
            ),
            factories = listOf { consumer ->
                PlaybackMiddleware(
                    consumer,
                    DebugActivity.recordStore
                ) { message ->
                    Log.d("PlaybackMiddleware", message)
                }
            }
        )
    }
}