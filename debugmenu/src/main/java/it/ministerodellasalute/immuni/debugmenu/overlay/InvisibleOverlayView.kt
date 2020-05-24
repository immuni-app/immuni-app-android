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

package it.ministerodellasalute.immuni.debugmenu.overlay

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
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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
            for (i in 0..childCount) {
                val view = parent.getChildAt(i)
                if (view != this) {
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
