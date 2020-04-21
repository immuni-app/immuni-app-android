package com.bendingspoons.secretmenu

import android.view.MotionEvent

/**
 * Listens to [SecretMenuTouchManager] events.
 */
interface SecretMenuTouchManagerListener {
    /**
     * Invoked when the [SecretMenuTouchManager] decides the secret menu must be opened.
     */
    fun onActivateSecretMenu()

    /**
     * Receives touch events from the app.
     */
    fun onTouchEvent(e: MotionEvent)
}