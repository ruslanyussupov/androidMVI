package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.binder.Connection
import com.github.ruslanyussupov.androidmvi.core.core.Consumer
import kotlinx.coroutines.flow.StateFlow

class PlaybackMiddleware<Out : Any, In : Any>(
    wrapped: Consumer<In>,
    private val recordStore: RecordStore,
    private val logger: Logger? = null
): Middleware<Out, In>(wrapped) {

    var isInPlayback: Boolean = false
        private set

    override fun onBind(connection: Connection<Out, In>) {
        super.onBind(connection)
        logger?.invoke("Creating record store entry for $connection")
        recordStore.register(this, connection)
    }

    override fun onReceive(connection: Connection<Out, In>, value: In) {
        super.onReceive(connection, value)
        logger?.invoke("Sending to record store: [$value] on $connection")
        recordStore.record(this, connection, value)
    }

    override fun onComplete(connection: Connection<Out, In>) {
        super.onComplete(connection)
        logger?.invoke("Removing record store entry for binding $connection")
        recordStore.unregister(this, connection)
    }

    override fun receive(value: In) {
        if (!isInPlayback) {
            super.receive(value)
        }
    }

    fun startPlayback() {
        isInPlayback = true
    }

    fun stopPlayback() {
        isInPlayback = false
    }

    fun replay(obj: Any?) {
        if (isInPlayback) {
            logger?.invoke("PLAYBACK: $obj")
            obj?.let {
                super.receive(obj as In)
            }
        }
    }

    interface RecordStore {

        val records: StateFlow<List<RecordKey>>
        val state: StateFlow<PlaybackState>

        fun startRecording()

        fun stopRecording()

        fun <Out: Any, In: Any> register(
            middleware: PlaybackMiddleware<Out, In>,
            connection: Connection<Out, In>
        )

        fun <Out: Any, In: Any> unregister(
            middleware: PlaybackMiddleware<Out, In>,
            connection: Connection<Out, In>
        )

        fun <Out: Any, In: Any> record(
            middleware: PlaybackMiddleware<Out, In>,
            connection: Connection<Out, In>, value: In
        )

        fun playback(recordKey: RecordKey)

        enum class PlaybackState {
            IDLE, RECORDING, PLAYBACK, FINISHED_PLAYBACK
        }

        data class RecordKey(
            val id: Int,
            val name: String
        ) {

            override fun toString(): String = name
        }

        data class Event(
            val delayMillis: Long,
            val obj: Any
        )
    }
}