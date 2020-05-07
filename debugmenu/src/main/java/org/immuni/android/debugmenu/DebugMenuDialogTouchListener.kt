package org.immuni.android.debugmenu

import android.view.MotionEvent
import androidx.fragment.app.DialogFragment

/**
 * Represent a listener of touch events coming from a [DialogFragment].
 */
interface DebugMenuDialogTouchListener {

    /**
     * Invoked when a dialog fragment detects a touch event.
     */
    fun dispatchDialogTouchEvent(event: MotionEvent?)
}