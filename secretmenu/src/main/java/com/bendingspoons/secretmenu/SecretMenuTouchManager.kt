package com.bendingspoons.secretmenu

import android.view.MotionEvent
import kotlinx.coroutines.*

/**
 * The class responsible to handle the user touch events and decide if
 * the secret menu should be opened or not.
 *
 * @param listener a listener to call when the secret menu should be opened.
 * @param config the secret menu configuration.
 */
class SecretMenuTouchManager(
    private val listener: SecretMenuTouchManagerListener,
    private val config: SecretMenuConfiguration) {

    companion object {
        private const val FINGERS_COUNT = 4
        private const val FINGERS_COUNT_DEVELOPMENT = 2

        private const val DELAY = 2000L
        private const val DELAY_DEVELOPMENT = 1000L
    }

    private var timerJob: Job? = null

    /**
     * When the user hold the fingers down a timer starts
     * and when it finishes the secret menu opens.
     * If the user releases the fingers before the timer ends
     * the timer is cancelled and nothing happens.
     */
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
        timerJob?.cancel()
    }

    private fun startTimer() {

        val delay = when(config.isDevelopmentDevice()) {
            true -> DELAY_DEVELOPMENT
            false -> DELAY
        }

        timerJob?.cancel()
        timerJob = GlobalScope.launch(Dispatchers.Main) {
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