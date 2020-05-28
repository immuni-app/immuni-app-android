/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.debugmenu

import android.view.MotionEvent
import kotlinx.coroutines.*

/**
 * The class responsible to handle the user touch events and decide if
 * the debug menu should be opened or not.
 *
 * @param listener a listener to call when the debug menu should be opened.
 * @param config the debug menu configuration.
 */
class DebugMenuTouchManager(
    private val listener: DebugMenuTouchManagerListener,
    private val config: DebugMenuConfiguration
) {

    companion object {
        private const val FINGERS_COUNT = 2
        private const val DELAY = 1000L
    }

    private var timerJob: Job? = null

    /**
     * When the user hold the fingers down a timer starts
     * and when it finishes the secret menu opens.
     * If the user releases the fingers before the timer ends
     * the timer is cancelled and nothing happens.
     */
    fun onTouchEvent(ev: MotionEvent) {
        if (config.isDevelopmentDevice()) {
            if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_DOWN && ev.pointerCount == FINGERS_COUNT) {
                startTimer()
            } else if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP || ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                cancelTimer()
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = GlobalScope.launch(Dispatchers.Main) {
            delay(DELAY)
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
