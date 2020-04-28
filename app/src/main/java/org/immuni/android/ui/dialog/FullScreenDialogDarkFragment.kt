package org.immuni.android.ui.dialog

import android.os.Build
import android.os.Bundle
import android.view.View
import org.immuni.android.R

abstract class FullScreenDialogDarkFragment : FullScreenDialogLightFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            dialog?.window?.statusBarColor = resources.getColor(R.color.transparent)
        } else {
            dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            dialog?.window?.statusBarColor = resources.getColor(R.color.transparent)
        }
    }
}