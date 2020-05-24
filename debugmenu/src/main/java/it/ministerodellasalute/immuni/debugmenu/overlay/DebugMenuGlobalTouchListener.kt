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
import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import it.ministerodellasalute.immuni.debugmenu.DebugMenuTouchManager
import it.ministerodellasalute.immuni.debugmenu.R

/**
 * This class is register to the [Application] activity lifecycle and
 * add or remove the overlay view to each [Activity] as a content view.
 *
 * @see Activity.addContentView
 */
class DebugMenuGlobalTouchListener(
    application: Application,
    private val touchManager: DebugMenuTouchManager
) : InvisibleOverlayView.TouchListener {

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStarted(activity: Activity) {
                createOverlay(activity)
            }
            override fun onActivityDestroyed(activity: Activity) {
                removeOverlay(activity)
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
        })
    }

    override fun onTouch(event: MotionEvent): Boolean {
        touchManager.onTouchEvent(event)
        return true
    }

    private fun removeOverlay(activity: Activity) {
        activity.window.decorView
            .findViewById<InvisibleOverlayView>(R.id.secret_menu_invisible_overlay)?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }
    }

    private fun createOverlay(activity: Activity) {

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val view = activity.window.decorView.findViewById(R.id.secret_menu_invisible_overlay)
            ?: activity.layoutInflater
                .inflate(R.layout.debug_menu_invisible_overlay, null)
                .also { activity.addContentView(it, params) } as InvisibleOverlayView

        view.activity = activity

        view.setTouchListener(this)
    }
}
