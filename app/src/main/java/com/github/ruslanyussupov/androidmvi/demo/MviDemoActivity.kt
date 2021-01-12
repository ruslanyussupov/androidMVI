package com.github.ruslanyussupov.androidmvi.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.ruslanyussupov.androidmvi.demo.Feature.Trigger
import kotlinx.android.synthetic.main.activity_mvi_demo.btn_increment
import kotlinx.android.synthetic.main.activity_mvi_demo.tv_counter
import kotlinx.coroutines.flow.collect

class MviDemoActivity : DebugActivity() {

    private val viewModel by viewModels<MviDemoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvi_demo)
        setupDebugDrawer()

        viewModel.state.observe(this) {
            receive(it)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.events.collect { event ->
                when (event) {
                    is Feature.Event.MilestoneReached -> {
                        Toast.makeText(this@MviDemoActivity, "${event.count} reached!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btn_increment.setOnClickListener {
            viewModel.onViewInteracted(Trigger.IncrementClicked)
        }
    }

    private fun receive(value: ViewState) {
        tv_counter.text = "${value.counter}"
    }
}