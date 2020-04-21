package org.immuni.android.ui

import android.app.Dialog
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.DialogFragment
import com.bendingspoons.secretmenu.SecretMenuDialogTouchListener

/**
 * This is the base class of all the [DialogFragment].
 * Intercepts every user touch event and dispatch it to the underlying
 * [ImmuniActivity] in order to trigger the opening of the secret menu.
 *
 * @see DialogFragment
 * @see ImmuniApplication
 */
open class ImmuniDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                (activity as? SecretMenuDialogTouchListener)?.dispatchDialogTouchEvent(ev)
                return super.dispatchTouchEvent(ev)
            }
        }
    }
}