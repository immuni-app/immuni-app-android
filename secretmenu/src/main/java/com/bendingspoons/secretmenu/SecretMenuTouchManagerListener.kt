package com.bendingspoons.secretmenu

import android.view.MotionEvent

interface SecretMenuTouchManagerListener {
    fun onActivateSecretMenu()
    fun onTouchEvent(e: MotionEvent)
}