package com.github.ruslanyussupov.androidmvi.demo

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.ruslanyussupov.androidmvi.core.middleware.InMemoryRecordStore
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.commons.SettingsModule
import io.palaima.debugdrawer.scalpel.ScalpelModule
import io.palaima.debugdrawer.timber.TimberModule

abstract class DebugActivity : AppCompatActivity() {

    private val playbackControlsAction = MviCoreControlsModule(recordStore)

    protected fun setupDebugDrawer() {
        val drawer = DebugDrawer.Builder(this)
            .modules(
                playbackControlsAction,
                ScalpelModule(this),
                TimberModule(""),
                SettingsModule(),
                BuildModule(),
                DeviceModule()
            ).build()

        playbackControlsAction.drawer = drawer
    }

    companion object {
        val recordStore = InMemoryRecordStore { message ->
            Log.d("RecordStore", message)
        }
    }
}