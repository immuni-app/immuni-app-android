package org.immuni.android

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.DialogFragment

open class AscoltoDialogFragment(val ctx: Context) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(ctx, theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                (activity as? AscoltoActivity)?.dispatchDialogTouchEvent(ev)
                return super.dispatchTouchEvent(ev)
            }
        }
    }
}