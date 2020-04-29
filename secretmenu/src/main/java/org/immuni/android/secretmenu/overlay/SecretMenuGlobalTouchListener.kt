package org.immuni.android.secretmenu.overlay

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import org.immuni.android.secretmenu.R
import org.immuni.android.secretmenu.SecretMenuTouchManager

/**
 * This class is register to the [Application] activity lifecycle and
 * add or remove the overlay view to each [Activity] as a content view.
 *
 * @see Activity.addContentView
 */
class SecretMenuGlobalTouchListener(
    application: Application,
    private val touchManager: SecretMenuTouchManager
): InvisibleOverlayView.TouchListener {

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
                .inflate(R.layout.secret_menu_invisible_overlay, null)
                .also { activity.addContentView(it, params) } as InvisibleOverlayView

        view.activity = activity

        view.setTouchListener(this)
    }
}