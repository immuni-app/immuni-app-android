package org.immuni.android.ui.forceupdate

import android.os.Bundle
import org.immuni.android.R
import org.immuni.android.extensions.activity.setDarkStatusBar
import org.immuni.android.ui.ImmuniActivity

class ForceUpdateActivity : ImmuniActivity() {

    companion object {
        var isOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOpen = true
        setContentView(R.layout.force_update_activity)
        setDarkStatusBar(resources.getColor(R.color.colorPrimary))
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpen = false
    }
}
