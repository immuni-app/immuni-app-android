package com.bendingspoons.secretmenu

import android.view.MotionEvent
import kotlinx.coroutines.*

class SecretMenuTouchManager(
    val listener: SecretMenuTouchManagerListener,
    val config: SecretMenuConfiguration) {

    val FINGERS_COUNT = 4
    val FINGERS_COUNT_DEVELOPMENT = 2

    val DELAY = 2000L
    val DELAY_DEVELOPMENT = 1000L

    var job: Job? = null

    fun onTouchEvent(ev: MotionEvent) {

        val fingers = when(config.isDevelopmentDevice()) {
            true -> FINGERS_COUNT_DEVELOPMENT
            false -> FINGERS_COUNT
        }
        if(ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_DOWN && ev.pointerCount == fingers) {
            startTimer()
        } else if(ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP || ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            cancelTimer()
        }
    }

    private fun cancelTimer() {
        job?.cancel()
    }

    private fun startTimer() {

        val delay = when(config.isDevelopmentDevice()) {
            true -> DELAY_DEVELOPMENT
            false -> DELAY
        }

        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Main) {
            delay(delay)
            onTimerEnd()
        }
    }

    private fun onTimerEnd() {
        activateSecretMenu()
    }

    private fun activateSecretMenu() {
        listener.onActivateSecretMenu()
    }
}