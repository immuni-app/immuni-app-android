package org.immuni.android.secretmenu.overlay

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * This is the invisible [ViewGroup] overlay that intercepts
 * all the multitouch events.
 */
class InvisibleOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var activity: Activity? = null
    private var listener: TouchListener? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        // handle the motion event
        listener?.onTouch(event)

        // but dispatch it to other sibling view group
        val parentViewGroup = this.parent as? ViewGroup
        parentViewGroup?.let { parent ->
            val childCount = parent.childCount
            for(i in 0..childCount) {
                val view = parent.getChildAt(i)
                if(view != this) {
                    view?.dispatchTouchEvent(event)
                } else {
                    // skip myself
                }
            }
        }

        // important to return true here, otherwise
        // multi touch events will not be intercepted, but only ACTION_DOWN
        return true
    }


    fun setTouchListener(listener: TouchListener) {
        this.listener = listener
    }

    interface TouchListener {
        fun onTouch(event: MotionEvent): Boolean
    }
}