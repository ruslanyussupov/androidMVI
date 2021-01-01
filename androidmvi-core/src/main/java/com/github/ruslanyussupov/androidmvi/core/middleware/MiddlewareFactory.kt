package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.core.Consumer

typealias MiddlewareFactory<T> = (Consumer<T>) -> Middleware<Any, T>