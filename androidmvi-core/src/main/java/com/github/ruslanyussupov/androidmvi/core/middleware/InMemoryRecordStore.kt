package com.github.ruslanyussupov.androidmvi.core.middleware

import com.github.ruslanyussupov.androidmvi.core.binder.Connection
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore.Event
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore.PlaybackState
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore.RecordKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class InMemoryRecordStore(private val logger: Logger? = null) : RecordStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _records = MutableStateFlow(emptyList<RecordKey>())
    private val _state = MutableStateFlow(PlaybackState.IDLE)
    private val recordedEvents = mutableMapOf<Key<*,*>, MutableList<Event>>()
    private var recordBaseTimestampMillis = 0L
    private val isRecording: Boolean
        get() = state.value == PlaybackState.RECORDING

    override val records: StateFlow<List<RecordKey>>
        get() = _records.asStateFlow()

    override val state: StateFlow<PlaybackState>
        get() = _state.asStateFlow()

    override fun startRecording() {
        logger?.invoke("START RECORDING")
        recordedEvents.forEach { entry ->
            entry.value.clear()
        }
        recordBaseTimestampMillis = System.currentTimeMillis()
        _state.value = PlaybackState.RECORDING
    }

    override fun stopRecording() {
        if (isRecording) {
            logger?.invoke("STOP RECORDING")
            recordBaseTimestampMillis = 0L
            _state.value = PlaybackState.IDLE
        }
    }

    override fun <Out : Any, In : Any> register(
        middleware: PlaybackMiddleware<Out, In>,
        connection: Connection<Out, In>
    ) {
        recordedEvents[Key(middleware, connection)] = mutableListOf()
        updateRecords()
    }

    override fun <Out : Any, In : Any> unregister(
        middleware: PlaybackMiddleware<Out, In>,
        connection: Connection<Out, In>
    ) {
        val key = Key(middleware, connection)
        recordedEvents.remove(key)
        updateRecords()
    }

    override fun <Out : Any, In : Any> record(
        middleware: PlaybackMiddleware<Out, In>,
        connection: Connection<Out, In>,
        value: In
    ) {
        val key = Key(middleware, connection)

        if (isRecording) {
            logger?.invoke("RECORD element: [$value] on $connection")
            recordEvent(key, value)
        } else {
            logger?.invoke("SKIP element: [$value] on $connection")
        }
    }

    override fun playback(recordKey: RecordKey) {
        check(!isRecording) {
            "Trying to playback while still recording"
        }

        val key = recordedEvents.keys.first { key ->
            key.id == recordKey.id
        }
        recordedEvents.getValue(key).asFlow().onEach { event ->
            if (event.delayMillis > 0) {
                delay(event.delayMillis)
            }
            logger?.invoke("PLAYBACK: ts: ${event.delayMillis}, event: ${event.obj}")
        }.map { event ->
            event.obj
        }.onStart {
            _state.value = PlaybackState.PLAYBACK
            key.middleWare.startPlayback()
        }.onCompletion {
            logger?.invoke("PLAYBACK FINISHED")
            _state.value = PlaybackState.FINISHED_PLAYBACK
            _state.value = PlaybackState.IDLE
            key.middleWare.stopPlayback()
        }.onEach { obj ->
            key.middleWare.replay(obj)
        }.launchIn(scope)
    }

    private fun recordEvent(key: Key<*, *>, value: Any) {
        check(recordBaseTimestampMillis != 0L) {
            "Don't create events when base timestamp is 0, you'll wait forever for the delay on playback. " +
            "Check if you are in recording state?"
        }

        val events = recordedEvents.getValue(key)
        val delayMillis = System.currentTimeMillis() - recordBaseTimestampMillis - events.sumOf { it.delayMillis }
        events += Event(delayMillis, value)
    }

    private fun updateRecords() {
        _records.value = recordedEvents.keys
            .filter { key ->
                !key.connection.isAnonymous()
            }
            .map { key ->
                key.toRecordKey()
            }
            .sortedBy { recordKey ->
                recordKey.name
            }
    }

    private data class Key<Out: Any, In: Any>(
        val middleWare: PlaybackMiddleware<Out, In>,
        val connection: Connection<Out, In>
    ) {

        val id = hashCode()

        fun toRecordKey() = RecordKey(
            id = id,
            name = connection.name ?: "anonymous"
        )
    }
}