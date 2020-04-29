package org.immuni.android.debugmenu.item

import android.widget.Toast
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.debugmenu.DebugMenuItem

class AppVersionItem: DebugMenuItem(
    "\uD83D\uDCF1 App Version and Build number",
    { context, config ->
        val value = "${DeviceUtils.appVersionName(context)} (${DeviceUtils.appVersionCode(context)})"
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
