package org.immuni.android.ui

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.secretmenu.SecretMenuDialogTouchListener
import org.immuni.android.managers.PermissionsManager
import org.koin.android.ext.android.inject

/**
 * This is the base class of all the activities.
 * Intercepts every user touch event and dispatch it to the [SecretMenu].
 *
 * @see SecretMenu
 * @see ImmuniDialogFragment
 */
open class ImmuniActivity : AppCompatActivity(), SecretMenuDialogTouchListener {

    private val permissionsManager: PermissionsManager by inject()
    private val secretMenu: SecretMenu by inject()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            secretMenu.onTouchEvent(it)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchDialogTouchEvent(ev: MotionEvent?) {
        ev?.let {
            secretMenu.onTouchEvent(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}