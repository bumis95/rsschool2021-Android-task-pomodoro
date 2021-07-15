package com.rsschool.android2021

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsschool.android2021.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        // hide asterisk character
        binding.minutes.transformationMethod = null

        binding.addNewStopwatchButton.setOnClickListener {
            val minutes = binding.minutes.text.toString().toLong() * MINUTE_IN_MILLIS
            stopwatches.add(Stopwatch(nextId++, PERIOD, false, PERIOD))
            stopwatchAdapter.submitList(stopwatches.toList())
        }
    }

    override fun start(id: Int) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.isStarted && it.id != id) {
                newTimers.add(Stopwatch(it.id, it.currentMs, false, PERIOD))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)

        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, isStarted, PERIOD))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        val stopwatch = stopwatches.find { it.isStarted }
        startIntent.putExtra(STARTED_TIMER_TIME_MS, stopwatch?.currentMs)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        val extras = intent!!.extras
//        if (extras != null) {
//            if (extras.containsKey("NotificationMessage")) {
//                setContentView(R.layout.viewmain)
//                // extract the extra-data in the Notification
//                val msg = extras.getString("NotificationMessage")
//                txtView = findViewById<View>(R.id.txtMessage) as TextView
//                txtView.setText(msg)
//            }
//        }
//    }
}