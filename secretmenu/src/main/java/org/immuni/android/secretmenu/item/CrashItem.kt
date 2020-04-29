package org.immuni.android.secretmenu.item

import org.immuni.android.secretmenu.SecretMenuItem
import org.immuni.android.secretmenu.ui.ExitActivity

class CrashItem: SecretMenuItem(
    "\uD83C\uDF86 Crash app",
    { context, config ->
        ExitActivity.exitApplication(context)
        val nullValue: String? = null
        val crash = nullValue!!.toLowerCase()
    }
)
