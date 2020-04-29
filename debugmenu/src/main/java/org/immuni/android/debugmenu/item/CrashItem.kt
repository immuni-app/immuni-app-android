package org.immuni.android.debugmenu.item

import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.debugmenu.ui.ExitActivity

class CrashItem: DebugMenuItem(
    "\uD83C\uDF86 Crash app",
    { context, config ->
        ExitActivity.exitApplication(context)
        val nullValue: String? = null
        val crash = nullValue!!.toLowerCase()
    }
)
