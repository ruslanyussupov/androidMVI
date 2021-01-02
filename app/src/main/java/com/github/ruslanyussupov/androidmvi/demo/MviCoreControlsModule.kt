package com.github.ruslanyussupov.androidmvi.demo

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore.PlaybackState
import com.github.ruslanyussupov.androidmvi.core.middleware.PlaybackMiddleware.RecordStore.RecordKey
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.base.DebugModuleAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

class MviCoreControlsModule(
    private val recordStore: PlaybackMiddleware.RecordStore
) : DebugModuleAdapter() {

    private var _scope = createScope()
    private val scope: CoroutineScope
        get() {
            if (!_scope.isActive) {
                _scope = createScope()
            }
            return _scope
        }
    private lateinit var startRecording: ImageButton
    private lateinit var stopRecording: ImageButton
    private lateinit var playback: ImageButton
    private lateinit var recordsSpinner: Spinner
    var drawer: DebugDrawer? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        val layout = inflater.inflate(R.layout.playback_controls, parent, false)

        startRecording = layout.findViewById(R.id.startRecording)
        stopRecording = layout.findViewById(R.id.stopRecording)
        playback = layout.findViewById(R.id.playback)
        recordsSpinner = layout.findViewById(R.id.records)

        startRecording.setOnClickListener {
            recordStore.startRecording()
            drawer?.closeDrawer()
        }

        stopRecording.setOnClickListener {
            recordStore.stopRecording()
        }

        playback.setOnClickListener {
            (recordsSpinner.selectedItem as? RecordKey)?.let {
                recordStore.stopRecording()
                recordStore.playback(it)
                drawer?.closeDrawer()
            }
        }

        return layout
    }

    override fun onStart() {
        super.onStart()

        recordStore.records.onEach { records ->
            recordsSpinner.adapter = RecordsAdapter(startRecording.context, records)
        }.launchIn(scope)

        recordStore.state.onEach { playbackState ->
            when (playbackState) {
                PlaybackState.IDLE -> {
                    startRecording.enable()
                    stopRecording.disable()
                    playback.enable()
                }
                PlaybackState.RECORDING -> {
                    startRecording.disable()
                    stopRecording.enable()
                    playback.disable()
                }
                PlaybackState.FINISHED_PLAYBACK -> {
                    Toast.makeText(startRecording.context, R.string.finished_playback, Toast.LENGTH_SHORT).show()
                }
                PlaybackState.PLAYBACK -> {
                    startRecording.disable()
                    stopRecording.disable()
                    playback.disable()
                }
            }
        }.launchIn(scope)
    }

    override fun onStop() {
        super.onStop()
        _scope.cancel()
    }

    private fun createScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private fun ImageButton.enable() {
        isEnabled = true
        isClickable = true
        background?.colorFilter  = null
        drawable?.colorFilter  = null
    }

    private fun ImageButton.disable() {
        isEnabled = false
        isClickable = false
        background?.setColorFilter(resources.getColor(R.color.grey_200), PorterDuff.Mode.SRC_IN)
        drawable?.setColorFilter(resources.getColor(R.color.grey_300), PorterDuff.Mode.SRC_IN)
    }

    class RecordsAdapter(
        context: Context,
        records: List<RecordKey>
    ) : ArrayAdapter<RecordKey>(context, R.layout.list_item_simple_small, records)
}
